package g.p.hmachttp;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;


public class SignedHttpPost extends SimpleSignedRequest {
	
	private final String webServiceHost;
	
	private final int webServicePort;
	
	/** portion of the http path that does NOT change between requests, e.g. servlet name */
	private final String httpPathStart;

	/**
	 * 
	 * @param webServiceHost this is the protocol AND host, e.g. http://example.com
	 * @param webServicePort
	 * @param httpPathStart   set to "/api" for protocol version 0
	 * @param protocolVersion
	 * @param serviceName
	 * @param stageName
	 * @param method
	 */
	public SignedHttpPost(
			String webServiceHost,
			int webServicePort,
			String httpPathStart,
			
			String protocolVersion, 
			String serviceName,
			String stageName, 
			String method) {
		super(protocolVersion, serviceName, stageName, method);
		
		if(webServiceHost == null){ throw new IllegalArgumentException("web service host cannot be null"); }
		
		this.webServiceHost = webServiceHost;
		this.webServicePort = webServicePort;
		this.httpPathStart = httpPathStart != null ? httpPathStart : "/";
	}
	
	public SignedHttpPost(StageConfiguration stageConfig, String method){
		this(
				stageConfig.httpProtocolAndHost,
				stageConfig.port,
				stageConfig.httpPathStart,
				stageConfig.protocolVersion,
				stageConfig.serviceName,
				stageConfig.stageName,
				method);
	}
	
	SignedHttpPost(HttpPost httpPost) throws Exception {
		if(httpPost == null) throw new IllegalArgumentException();
		
		URI url = httpPost.getURI();
		this.webServiceHost = url.getHost();
		this.webServicePort = url.getPort();
		this.httpPathStart = url.getPath();
		
		Map<String, String> headers = getHeaders(httpPost);
		super.setHeaders(headers);
		
		if(! SignedRequest.ProtocolVersions.LEGACY.equals(getProtocolVersion())){
			
			setBody(Util.readAndCloseStream(httpPost.getEntity()));
		}
		
	}

	/**
	 * Create an apache HttpPost object to send over the network.
	 * You need to sign the request before calling this method; it will not auto-sign.
	 * @return
	 */
	public HttpPost toHttpPost(){
		return toHttpPost(this, this.protocolHeaders, this.webServiceHost, this.webServicePort, this.httpPathStart);
	}
	
	
	/**
	 * This an effort to get towards a static implementation.
	 */
	private HttpPost toHttpPost(SignedRequest request, Map<String, String> protocolHeaders,
			String webServiceHost, int webServicePort, String httpPathStart){
		
		//if we dont do this, the http header wont be set
		//this should only be a problem if we send without signing
		this.prepareForSigning();
		
		
		
		final String url = webServiceHost + ":" + webServicePort + "/" + request.getStageName() + httpPathStart;
		
		HttpPost post = new HttpPost(url);
		
		
		boolean legacy = SignedRequest.ProtocolVersions.LEGACY.equals(request.getProtocolVersion());
		
		
		for(String name : protocolHeaders.keySet()){
			
			String value = getPH(name);
			
			if(legacy){
				String legacyName = SignedRequest.LegacyHeaders.NEW_TO_LEGACY.get(name);
				name = legacyName != null ? legacyName : name;
			}
			
			post.setHeader(name, value);
		}
		
		for(String name : userHeaders.keySet()){
			//dont let them override protocol headers (if they exist)
			if(! protocolHeaders.keySet().contains(name)){
				post.setHeader(name, userHeaders.get(name));
			}
		}
		
		if(! legacy){
			HttpEntity entity = new StringEntity(getBody(), "UTF-8");
			post.setEntity(entity);
		}
		
		return post;
	}
	
	
	
	private static Map<String, String> getHeaders(HttpPost post){
		Map<String, String> map = new TreeMap<String, String>();
		
		for(Header h : post.getAllHeaders()){
			if(! map.containsKey(h.getName())){
				map.put(h.getName(), h.getValue());
			}
		}
		
		return map;
	}

}

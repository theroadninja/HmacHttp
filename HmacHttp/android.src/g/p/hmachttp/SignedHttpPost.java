package g.p.hmachttp;
import org.apache.http.client.methods.HttpPost;


public class SignedHttpPost extends SimpleSignedRequest {
	
	private final String webServiceHost;
	
	private final int webServicePort;
	
	/** portion of the http path that does NOT change between requets, e.g. servlet name */
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
		
		this.webServiceHost = webServiceHost.endsWith("/") ? webServiceHost : webServiceHost + "/";
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
	
	/**
	 * You need to sign the request before calling this method; it will not auto-sign.
	 * @return
	 */
	public HttpPost toHttpPost(){
		
		final String url = this.webServiceHost + super.getStageName() + httpPathStart;
		
		HttpPost post = new HttpPost(url);
		
		boolean legacy = SignedRequest.PROTOCOL_VERSION_LEGACY.equals(this.getProtocolVersion());
		
		for(String name : protocolHeaders.keySet()){
			
			String value = getPH(name);
			
			if(legacy){
				String legacyName = SignedRequest.LegacyHeaders.MAP.get(name);
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
		
		return post;
	}

}

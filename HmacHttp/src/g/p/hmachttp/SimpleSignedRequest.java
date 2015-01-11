package g.p.hmachttp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Implementation of Signed Request that stores everything in memory.
 *  */
public class SimpleSignedRequest implements SignedRequest {

	
	
	public static final String toRequestDateString(Date d){
		if(d == null){ return null; }
		
		//I believe, one some android versions, one or more
		//of these objects is not reusable
		TimeZone t = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat(SignedRequest.DATE_FORMAT_YYYYMMDDHH);
		df.setTimeZone(t);
		return df.format(d.getTime());
	}
	


	
	/** HTTP header values that are a part of this signed request protocol */
	protected final Map<String, String> protocolHeaders = new TreeMap<String, String>();
	
	/** HTTP headers added by the person using this class */
	protected final Map<String, String> userHeaders = new TreeMap<String, String>();
	
	/** 
	 * A String->String map of 'parameters' that are transported in the http headers
	 * in query string format.
	 * 
	 * Different from queryStringParameters only in the location of transport.
	 */
	private final Map<String, String> methodParameters = new TreeMap<String, String>();
	
	
	/**
	 * A String->String map of query string parameters which obviously prevents the
	 * use of multiple parameters with the same name.
	 * 
	 * Different from methodParameters only in the location of transport.
	 */
	private final Map<String, String> queryStringParameters = new TreeMap<String, String>();
	
	private String httpBody = "";
	
	private String httpPathEndFragment = "";
	
	public SimpleSignedRequest(String protocolVersion, String serviceName, String stageName, String method){
		protocolHeaders.put(SignedRequest.HEADER_SENDER_BASEMAC_VERSION, protocolVersion);
		protocolHeaders.put(SignedRequest.HEADER_REQUEST_ID, UUID.randomUUID().toString().replace("-", ""));
		protocolHeaders.put(SignedRequest.HEADER_AUTH_DATE_YYYYMMDDHH, toRequestDateString(new Date()));
		protocolHeaders.put(SignedRequest.HEADER_SERVICE_NAME, serviceName);
		protocolHeaders.put(SignedRequest.HEADER_STAGE_NAME, stageName);
		protocolHeaders.put(SignedRequest.HEADER_METHOD, method);
		protocolHeaders.put(SignedRequest.HEADER_EXTRA_FIELD, "");
		
	}
	

	
	protected String getPH(String name){
		String s = protocolHeaders.get(name);
		return s != null ? s.trim() : "";
	}
	
	/**
	 * @return the version of this hmac protocol being used, which determines which
	 * fields are part of the signature.
	 */
	public String getProtocolVersion() {
		return getPH(SignedRequest.HEADER_SENDER_BASEMAC_VERSION);
	}

	public String getRequestId() {
		return getPH(SignedRequest.HEADER_REQUEST_ID);
	}

	public String getAuthDate() {
		return getPH(SignedRequest.HEADER_AUTH_DATE_YYYYMMDDHH);
	}

	public String getServiceName() {
		return getPH(SignedRequest.HEADER_SERVICE_NAME);
	}

	public String getStageName() {
		return getPH(SignedRequest.HEADER_STAGE_NAME);
	}

	public String getMethod() {
		return getPH(SignedRequest.HEADER_METHOD);
	}

	public Map<String, String> getMethodParameters() {
		return this.methodParameters;
	}
	
	/**
	 * 
	 * @return the same parameters as in getMethodParameters() in a single string
	 * 	formatted like an http query string but beginning with a '&' instead of a '?'
	 * 	
	 */
	public String getMethodParameterString(){
		StringBuilder sb = new StringBuilder();
		for(String k : methodParameters.keySet()){
			sb.append("&").append(k).append("=").append(methodParameters.get(k));
		}
		
		return sb.toString();
	}

	public Map<String, String> getQueryStringParameters() {
		return this.queryStringParameters;
	}

	public String getBody() {
		return this.httpBody;
	}
	
	public String getHttpPathEndFragment(){
		return this.httpPathEndFragment;
	}

	public String getSignature() {
		return this.protocolHeaders.get(SignedRequest.HEADER_SIGNATURE);
	}

	public void setSignature(String s) {
		this.protocolHeaders.put(SignedRequest.HEADER_SIGNATURE, s);
	}


	public String extraSignedField() {
		return this.protocolHeaders.get(SignedRequest.HEADER_EXTRA_FIELD);
	}


	public void setExtraSignedField(String s) {
		this.protocolHeaders.put(SignedRequest.HEADER_EXTRA_FIELD, s);
		
	}

	public String getKeyName() {
		return getPH(SignedRequest.HEADER_KEY_NAME);
	}

	public void setKeyName(String keyName) {
		this.protocolHeaders.put(SignedRequest.HEADER_KEY_NAME, keyName);
		
	}
	
	public void addHttpHeader(String name, String value){
		this.userHeaders.put(name, value);
	}



	public void prepareForSigning() {
		this.protocolHeaders.put(SignedRequest.HEADER_METHOD_PARAMETERS, getMethodParameterString());
	}

}

package g.p.hmachttp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
	
	/** this is for deserialization */
	protected SimpleSignedRequest(){
		
	}
	

	
	protected String getPH(String name){
		String s = name == null ? null : protocolHeaders.get(name);
		return s != null ? s.trim() : "";
	}
	private void setPH(String name, String value){
		if(name == null) throw new IllegalArgumentException();
		protocolHeaders.put(name, value);
	}
	
	/** TODO:  remove this */
	public Map<String, String> getProtocolHeadersForDebug(){
		return this.protocolHeaders;
	}
	
	/**
	 * Apparently a simple set comparison will not work because
	 * certain languages (ruby) change the capitalization of some
	 * letters in the http headers.
	 * 
	 * @param name
	 * @return
	 */
	private String isProtocolHeader(String name){
		for(String s : SignedRequest.ALL_PROTOCOL_HEADERS){
			if(s.equalsIgnoreCase(name)){
				return s;
			}
		}
		
		for(String s : SignedRequest.LegacyHeaders.LEGACY_TO_NEW.keySet()){
			if(s.equalsIgnoreCase(name)){
				return SignedRequest.LegacyHeaders.LEGACY_TO_NEW.get(s);
			}
		}
		
		return null;
	}

	/** set fields based on http headers from an incoming request */
	protected void setHeaders(Map<String, String> headers) throws Exception {
		for(Map.Entry<String, String> entry : headers.entrySet()){
			String name = entry.getKey();
			
			if(SignedRequest.HEADER_METHOD_PARAMETERS.equalsIgnoreCase(name)
					|| SignedRequest.LegacyHeaders.PARAMETERS.equals(name)){
				
				//we need to reconstruct the parameter map
				this.setMethodParameterString(entry.getValue());
				continue;
			}
			
				
			String pname = isProtocolHeader(name); 
			//}else if(SignedRequest.ALL_PROTOCOL_HEADERS.contains(name)){
			if(pname != null){
				setPH(pname, entry.getValue());
			/*}else if(SignedRequest.LegacyHeaders.LEGACY_TO_NEW.containsKey(name)){
				name = SignedRequest.LegacyHeaders.LEGACY_TO_NEW.get(name);
				setPH(name, entry.getValue());
				*/
			}else{
				userHeaders.put(name, entry.getValue());
			}
		}
	}
	
	/**
	 * @return the version of this hmac protocol being used, which determines which
	 * fields are part of the signature.
	 */
	public String getProtocolVersion() {
		return getPH(SignedRequest.HEADER_SENDER_BASEMAC_VERSION);
	}
	protected void setProtocolVersion(String s){
		setPH(SignedRequest.HEADER_SENDER_BASEMAC_VERSION, s);
	}

	public String getRequestId() {
		return getPH(SignedRequest.HEADER_REQUEST_ID);
	}
	public void setRequestId(String s){
		setPH(SignedRequest.HEADER_REQUEST_ID, s);
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
		List<String> keys = new ArrayList<String>(methodParameters.keySet());
		
		//the sorting is to maintain consistency between object before and after deserialization
		Collections.sort(keys);
		
		
		//TODO:  we are not URL encoding these, which will cause a problem
		//as soon as someone uses a '=' or a '&'
		for(String k : methodParameters.keySet()){
			sb.append("&").append(k).append("=").append(methodParameters.get(k));
		}
		
		return sb.toString();
	}
	void setMethodParameterString(String methodParameterString) throws Exception {
		if(methodParameterString == null){
			return;
		}
		
		try{
			//remove first ampersand
			methodParameterString.replaceFirst("^&", "");
			
			String[] pairs = methodParameterString.split("&");
		    for (String pair : pairs) {
		    	if(pair == null || "".equals(pair.trim())){
		    		continue;
		    	}
		    	String[] nameValue = pair.split("=");
		    	if(nameValue.length == 1){
		    		this.methodParameters.put(nameValue[0], "");
		    	}else if(nameValue.length == 2){
		    		this.methodParameters.put(nameValue[0], nameValue[1]);
		    	}else{
		    		throw new Exception("invalid pair: " + pair);		    		
		    	}
		    	
		    	
		    }
		}catch(Exception ex){
			throw ex;
		}
		
	}

	public Map<String, String> getQueryStringParameters() {
		return this.queryStringParameters;
	}

	public String getBody() {
		return this.httpBody;
	}
	public void setBody(String s){
		this.httpBody = (s == null) ? "" : s;
	}
	
	public String getHttpPathEndFragment(){
		return this.httpPathEndFragment;
	}

	public String getSignature() {
		//return this.protocolHeaders.get(SignedRequest.HEADER_SIGNATURE);
		return getPH(SignedRequest.HEADER_SIGNATURE);
	}

	public void setSignature(String s) {
		//this.protocolHeaders.put(SignedRequest.HEADER_SIGNATURE, s);
		setPH(SignedRequest.HEADER_SIGNATURE, s);
	}


	public String extraSignedField() {
		return this.getPH(SignedRequest.HEADER_EXTRA_FIELD);
		//return this.protocolHeaders.get(SignedRequest.HEADER_EXTRA_FIELD);
	}


	public void setExtraSignedField(String s) {
		//this.protocolHeaders.put(SignedRequest.HEADER_EXTRA_FIELD, s);
		setPH(SignedRequest.HEADER_EXTRA_FIELD, s);
		
	}

	public String getKeyName() {
		return getPH(SignedRequest.HEADER_KEY_NAME);
	}

	public void setKeyName(String keyName) {
		//this.protocolHeaders.put(SignedRequest.HEADER_KEY_NAME, keyName);
		setPH(SignedRequest.HEADER_KEY_NAME, keyName);
		
	}
	
	public void addHttpHeader(String name, String value){
		this.userHeaders.put(name, value);
	}
	
	/**
	 * @return all headers that were set manuall (and which
	 * 	are not part of the hmac protocol)
	 */
	public Map<String, String> getHttpHeaders(){
		return Collections.unmodifiableMap(this.userHeaders);
	}



	public void prepareForSigning() {
		//this.protocolHeaders.put(SignedRequest.HEADER_METHOD_PARAMETERS, getMethodParameterString());
		setPH(SignedRequest.HEADER_METHOD_PARAMETERS, getMethodParameterString());
	}

}

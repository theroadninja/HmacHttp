package g.p.hmachttp;

import java.util.Map;
import java.util.TreeMap;

public interface SignedRequest {
	

	public static final String PROTOCOL_VERSION_LEGACY = "0";
	
	public static final String DATE_FORMAT_YYYYMMDDHH = "yyyyMMddHH";
	
	//TODO:  need a version zero that works with existing stuff...
	
	//TODO:  use json.org lib but change namespace so it wont conflict with android.
	
	//also maybe it should just be a data object?
	// --- could start with memory signed request, and just transform it...
	
	//notes:  object constructors are always for outgoing
	// factory is for incoming.
	
	
	
	
	
	//each of these get their own signing step
	
	public static final String HEADER_SENDER_BASEMAC_VERSION = "X-sender-basemac-version";
	public static final String HEADER_REQUEST_ID = "X-request-id";
	public static final String HEADER_AUTH_DATE_YYYYMMDDHH = "X-auth-date";
	public static final String HEADER_SERVICE_NAME = "X-service-name";
	public static final String HEADER_STAGE_NAME = "X-stage-name";
	public static final String HEADER_EXTRA_FIELD = "X-extra-field";

	//these are combined before signing
	public static final String HEADER_METHOD = "X-method";
	public static final String HEADER_METHOD_PARAMETERS = "X-method-parameters";
	
	
	
	//this is the signature
	public static final String HEADER_SIGNATURE = "X-signature";
	
	/** this field is not part of the signature; it names the key that was used to sign the request */
	public static final String HEADER_KEY_NAME = "X-signature-key-name";
	
	public static final class LegacyHeaders {
		
		/** this one is not an http header */
		public static final String PATH_START = "/api";
		
		public static final String METHOD = "X-method";
		public static final String PARAMETERS = "X-method-params";
		public static final String SIGNATURE = "X-signature";
		public static final String AUTH_DATE_YYYYMMDDHH = "X-auth-date";
		public static final String REQUEST_ID = "X-request_id";
		public static final String SERVICE_NAME = "X-service-name";
		
		@SuppressWarnings("serial")
		public static final Map<String, String> MAP = new TreeMap<String, String>(){{
			put(HEADER_METHOD, METHOD);
			put(HEADER_METHOD_PARAMETERS, PARAMETERS);
			put(HEADER_SIGNATURE, SIGNATURE);
			put(HEADER_AUTH_DATE_YYYYMMDDHH, AUTH_DATE_YYYYMMDDHH);
			put(HEADER_REQUEST_ID, REQUEST_ID);
			put(HEADER_SERVICE_NAME, SERVICE_NAME);
		}};
		
	}
	
	
	
	
	
	public String getProtocolVersion();
	
	public String getRequestId();
	
	/** 
	 * @return the date that is part of the signature to be compared with the current
	 * date in order to prevent old requests from being repeated 
	 */
	public String getAuthDate();
	
	/**
	 * 
	 * @return the "service name" which is part of the signature and can be matched
	 * against what the service calls itself.
	 */
	public String getServiceName();
	
	
	
	public String getStageName();
	
	
	public String getMethod();
	
	/**
	 * 
	 * @return parsed method param values (stored in http header in same format as query string)
	 */
	public Map<String, String> getMethodParameters();
	
	/**
	 * 
	 * @return the same parameters as in getMethodParameters() in a single string
	 * 	formatted like an http query string but beginning with a '&' instead of a '?'
	 * 	
	 */
	public String getMethodParameterString();
	
	/**
	 * 
	 * @return parsed query string values; behavior for duplicate names is undefined
	 */
	public Map<String, String> getQueryStringParameters();
	
	/**
	 * this is a genertic string value that can be used to hold domain-specific data
	 * that is part of the signature.
	 * @return
	 */
	public String extraSignedField();
	
	public void setExtraSignedField(String s);
	
	/**
	 * 
	 * @return the http body of the message
	 */
	public String getBody();
	
	/**
	 * ex:   hostname.com/path/servlet/REST_THINGY/  would be REST_THINGY/
	 * 
	 * Not part of the signature.
	 * 
	 * @return the meaningful part of the path (the part that is not the same
	 * for all requests)
	 */
	public String getHttpPathEndFragment();
	
	/**
	 * @return the HMAC hash which is part of the signature
	 */
	public String getSignature();
	
	/**
	 * manually set the signature hash; possibly only useful for testing.
	 * @param s the signature hash to set.
	 */
	public void setSignature(String s);
	
	public String getKeyName();
	
	public void setKeyName(String s);
	
	/**
	 * Adds an http header, which will replace any http header
	 * with the same name.
	 * @param name
	 * @param value
	 */
	public void addHttpHeader(String name, String value);
	
}

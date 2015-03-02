package g.p.hmachttp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;


public class SignedServletRequest extends SimpleSignedRequest {
	
	public SignedServletRequest(HttpServletRequest request) throws Exception {
		this(getHeaders(request), getInputStream(request));
		
	}
	
	private static InputStream getInputStream(HttpServletRequest request){
		try{
			return request.getInputStream();
		}catch(IOException ex){
			return null;
		}
	}
	
	public SignedServletRequest(Map<String, String> headers, InputStream httpBody) throws Exception{
		
		super.setHeaders(headers);
		
		if(! SignedRequest.ProtocolVersions.LEGACY.equals(getProtocolVersion())){
			if(httpBody != null){
				setBody(Util.readAndCloseStream(httpBody));
			}
		}
		
		//since this is an incoming use case, it is reasonable to calculate
		//the parameter string
		this.prepareForSigning();
	}
	
	
	/**
	 * Pulls the http headers out as a string map.
	 * 
	 * @param request incoming servlet request
	 * @return
	 */
	public static Map<String, String> getHeaders(HttpServletRequest request){
		
		Map<String, String> result = new TreeMap<String, String>();
		
		Enumeration<String> headerNames = request.getHeaderNames();
		
		while(headerNames.hasMoreElements()){
			
			String name = headerNames.nextElement();
			result.put(name, request.getHeader(name));
		}
		
		return result;
	}


}

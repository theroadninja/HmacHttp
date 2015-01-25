package g.p.hmachttp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;


public class SignedServletRequest extends SimpleSignedRequest {
	
	public SignedServletRequest(HttpServletRequest request) throws Exception {
		if(request == null) throw new IllegalArgumentException();
		
		super.setHeaders(getHeaders(request));
		
		if(! SignedRequest.ProtocolVersions.LEGACY.equals(getProtocolVersion())){
			try{
				setBody(Util.readAndCloseStream(request.getInputStream()));
			}catch(IOException ex){
				//leave it blank
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

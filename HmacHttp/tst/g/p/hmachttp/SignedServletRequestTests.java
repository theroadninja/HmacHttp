package g.p.hmachttp;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Test;


public class SignedServletRequestTests {

	/*
	 * Note: Java servlets are fucking retarded and do not support
	 * mocking HttpServletRequest or any convenient way to
	 * transform an HttpPost into an HttpServletRequest without
	 * running an embeded container or using some bullshit mock
	 * framework.
	 */
	
	
	/**
	 * the ruby net library does weird shit with capitals in the headers
	 */
	public static Map<String, String> createMalformedRubyHeaders(){
		
		Map<String, String> headers = new java.util.TreeMap<String, String>();
		headers.put("Accept", "*/*");
		headers.put("Connection", "close");
		headers.put("Host", "snowfish.cloudapp.net:8080");
		headers.put("User-Agent", "Ruby");
		headers.put("X-Auth-Date", "2015030200");
		headers.put("X-Method", "getTime");
		headers.put("X-Method-Parameters", null);
		headers.put("X-Method-Params", null);
		headers.put("X-Request-Id", "0e652956-5b62-42db-84e2-144413660833");
		headers.put("X-Request_id", "0e652956-5b62-42db-84e2-144413660833");
		headers.put("X-Sender-Basemac-Version", "0");
		headers.put("X-Service-Name", "SkinCareSidekickApi");
		headers.put("X-Signature", "n7j4SMzDnXnjM8i5ThGMgVQoXt+24c+51ISoYRQ7yZc");
		headers.put("X-Stage-Name", "test");
		headers.put("", "");
		return headers;
		
	}
	
	@Test
	public void testHeaders() throws Exception{
		
		Map<String, String> headers = createMalformedRubyHeaders();
		
		SignedServletRequest request = new SignedServletRequest(headers, null);
		
		Assert.assertEquals("getTime", request.getMethod());
		
	}

}

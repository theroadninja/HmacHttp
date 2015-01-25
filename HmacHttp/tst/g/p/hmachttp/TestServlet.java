package g.p.hmachttp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

/**
 * There is no convenient way to transform an HttpPost into an
 * HttpServletRequest, so the simplest way to test is to run this
 * servlet somewhere.
 * 
 * @author Dave
 *
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {
	
	/** the test client should use these params to hit the test servlet */
	public static final class TestParams {
		public static final String SERVICE_NAME = "HmacTestService";
		public static final String STAGE_NAME = "alpha";
		
		public static final String PATH_START = "/";
		
		public static final String METHOD_PARAMETER_NAME = "testParam";
		public static final String METHOD_PARAMETER_VALUE = "monkey";
		
		public static final String KEY = "ffffffaaaaaabbbbbbcccccceeeeee333333dddddd";
		
		
		
		public static StageConfiguration getConfig(String host, int port){
			return new StageConfiguration(
					STAGE_NAME,
					SignedRequest.ProtocolVersions.V1,
					host,
					port,
					PATH_START,
					SERVICE_NAME);
		}
	}
	
	public static class MeaningOfLifeRequest {
		
		public static final String METHOD_NAME = "getMeaningOfLife";
		
		public SignedHttpPost toSignedHttpPost(StageConfiguration stageConfig){
			
			SignedHttpPost hp = new SignedHttpPost(stageConfig, METHOD_NAME);
			
			hp.getMethodParameters().put(TestServlet.TestParams.METHOD_PARAMETER_NAME, 
					TestServlet.TestParams.METHOD_PARAMETER_VALUE);
			
			hp.setBody("{ \"question\", \"The Great Question of Life, the Universe and everything\" } ");
			
			return hp;
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}
	
	
	public void doPost(HttpServletRequest httpRequest, HttpServletResponse response) 
			throws ServletException, IOException {
		
		try {
			run(httpRequest, response);
		} catch(ServletException ex){
			throw ex;
		}catch(IOException ex){
			throw ex;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	private void run(HttpServletRequest httpRequest, HttpServletResponse response) throws Exception {
		
		PrintWriter out = response.getWriter();
		
		
		MeaningOfLifeRequest mrequest = new MeaningOfLifeRequest();
		SignedHttpPost outgoing = mrequest.toSignedHttpPost(TestParams.getConfig("localhost", 8080));
		
		
		
		out.println("reading method parameter header: " + httpRequest.getHeader(SignedRequest.HEADER_METHOD_PARAMETERS));
		
		
		SignedServletRequest request = new SignedServletRequest(httpRequest);
		
		
		
		
		
		//assertEquals(outgoing, request);
		
		
		
		
		
		
		
		
		
		
		
		
		
		String body = request.getBody();
		
		String expectedSignature = RequestSigner.calcSignature(request, TestParams.KEY);
		String actualSignature = request.getSignature();
		
		if(expectedSignature.equals(request.getSignature())){
			out.println("request signature ok");
		}else{
			out.println("request signature failed");
			out.println("expected: " + expectedSignature);
			out.println("actual signature: " + actualSignature);
			return;
		}
		
		
		out.println("HmacHttp library test servlet");
		out.println("incoming body was: " + body);
	}
	
	
	
	public static void assertEquals(SignedRequest hp, SignedRequest hp2){
		assertEquals(hp.getProtocolVersion(), hp2.getProtocolVersion());
		//obviously the request id wont match in this test
		//because we dont have the actual object the test client sent: assertEquals(hp.getRequestId(), hp2.getRequestId());
		assertEquals(hp.getAuthDate(), hp2.getAuthDate());
		assertEquals(hp.getServiceName(), hp2.getServiceName());
		assertEquals(hp.getStageName(), hp2.getStageName());
		assertEquals(hp.getMethod(), hp2.getMethod());
		assertEquals(hp.getMethodParameterString(), hp2.getMethodParameterString());
		assertEquals(hp.getBody(), hp2.getBody());
		assertEquals(hp.getHttpPathEndFragment(), hp2.getHttpPathEndFragment());
		assertEquals(hp.getSignature(), hp2.getSignature());
		assertEquals(hp.extraSignedField(), hp2.extraSignedField());
		assertEquals(hp.getKeyName(), hp2.getKeyName());
		
		//Map<String, String> h1 = hp.getHttpHeaders();
		//Map<String, String> h2 = hp2.getHttpHeaders();
		
		//Assert.assertEquals(h1, h2);
		
	}
	
	private static void assertEquals(String expected, String actual){
		if(expected == null){
			if(actual != null){
				throw new RuntimeException("expected: " + expected + " but got: " + actual);
			}
		}else{
			if(! expected.equals(actual)){
				throw new RuntimeException("expected: " + expected + " but got: " + actual);
			}
		}
	}
}

package g.p.hmachttp;

import g.p.hmachttp.TestServlet.MeaningOfLifeRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class TestServletClient {

	public static void main(String[] args) throws Exception {
		new TestServletClient().run();
	}
	
	private final BufferedReader stdin;
	
	public TestServletClient(){
		stdin = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() throws Exception {
		
		String webServiceHost = getInput("hostname", "localhost");
		int port = Integer.valueOf(getInput("port", "8080"));

		StageConfiguration stageConfig = TestServlet.TestParams.getConfig("http://" + webServiceHost, port);
		
		MeaningOfLifeRequest mrequest = new MeaningOfLifeRequest();
		
		SignedHttpPost request = mrequest.toSignedHttpPost(stageConfig);
		
		
		System.out.println("requestId: " + request.getRequestId());
		
		
		RequestSigner.signRequest(request, TestServlet.TestParams.KEY);
		System.out.println("http signature: " + request.getSignature());
		
		
		
		@SuppressWarnings("deprecation") //fuck you apache
		HttpClient httpclient = new DefaultHttpClient();

		HttpPost post = request.toHttpPost();
		System.out.println("URL: " + post.getRequestLine());
		HttpResponse response = httpclient.execute(post);
		
		System.out.println("response status code: " + response.getStatusLine().getStatusCode());
		String responseBody = "";
		if(response.getEntity() != null){
			responseBody = Util.readAndCloseStream(response.getEntity());
		}
		System.out.println("response http body: " + responseBody);
		
	}
	
	
	public String getInput(String prompt, String defaultValue) throws IOException {
		String result = getLn(prompt + "[" + defaultValue + "]: ");
		return "".equals(result) ? defaultValue : result;
	}
	
	public String getLn(String prompt) throws IOException{
		System.out.print(prompt);
		return stdin.readLine().trim();
		
		
	}
	
}

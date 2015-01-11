package g.p.hmachttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class TestClient {

	public static void main(String[] args) throws Exception {
		new TestClient().run();
	}
	
	private final BufferedReader stdin;
	
	public TestClient(){
		stdin = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() throws Exception {
		String webServiceHost = getLn("web service host including protocol>");
		String serviceName = getLn("enter service name>");
		String method = getLn("enter method>");
		
		StageConfiguration stageConfig = new StageConfiguration(
				"prod",
				SignedRequest.PROTOCOL_VERSION_LEGACY,
				webServiceHost,
				80,
				SignedRequest.LegacyHeaders.PATH_START,
				serviceName);
		
		
		
		
		SignedHttpPost request = new SignedHttpPost(stageConfig, method);
		
		request.addHttpHeader("X-verbose-auth_fail", "true");
		
		String key = getLn("enter key>").trim();
		
		String extraField = getLn("enter extra parameter field (leave blank to omit: ").trim();
		if(extraField != null && !"".equals(extraField)){
			request.getMethodParameters().put("test", extraField);
		}
		
		
		System.out.println("date in auth header: " + request.getAuthDate());
		
		RequestSigner.signRequest(request, key);
		System.out.println("http signature: " + request.getSignature());
		
		
		
		@SuppressWarnings("deprecation") //fuck you apache
		HttpClient httpclient = new DefaultHttpClient();

		HttpPost post = request.toHttpPost();
		System.out.println("URL: " + post.getRequestLine());
		HttpResponse response = httpclient.execute(post);
		
		System.out.println("response status code: " + response.getStatusLine().getStatusCode());
		String responseBody = "";
		if(response.getEntity() != null){
			//easy way to do it:
			//responseBody = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = null;
			while(null != (line = reader.readLine())){
				sb.append(line);
			}
			responseBody = sb.toString();
		}
		System.out.println("response http body: " + responseBody);
		
	}
	
	public String getLn(String prompt) throws IOException{
		System.out.print(prompt);
		return stdin.readLine().trim();
		
		
	}
}

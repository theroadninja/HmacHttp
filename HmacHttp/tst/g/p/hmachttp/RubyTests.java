package g.p.hmachttp;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

public class RubyTests {
	
	public static final String RUBY_WORKSPACE = "./ruby.src/";
	
	public static final String HMAC_HTTP_RB = "hmachttp.rb";
	
	
	private static final String ENCRYPT_KEY = "0befd203132e60c6d3fc6341a31f0c980befd203132e60c6d3fc6341a31f0c98";
	
	@SuppressWarnings("serial")
	public static final Map<String, String> METHOD_PARAMETERS = new TreeMap<String, String>(){{
		//&appName=scs&fileUrl=skincaresidekick.com/deployment/scs/test/SkinCareSidekick-2014-12-19.apk&markActive=false&notes=uploaded%202015-03-06%20T%20112418&versionCode=11
		put("appName", "hmac");
		put("fileUrl", "whatever.com/deployment/hmac/test/whatevs-1998-12-50.apk");
		put("markActive", "false");
		put("notes", "uploaded%202015-03-06%20T%20112418");
		put("versionCode", "11");
	}};

	private static String rubyHmac(String keyHex, String... values) throws Exception {
		if(values == null || values.length == 0){
			throw new IllegalArgumentException();
		}
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File(RUBY_WORKSPACE + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$key = [\"" + ENCRYPT_KEY + "\"].pack('H*')");
		//container.runScriptlet("$key = \"" + ENCRYPT_KEY + "\" ");
		
		for(String s : values){
			container.runScriptlet("$key = Hmac.getHmac($key, '" + s + "')");
		}
		
		return (String)container.runScriptlet("Base64.encode64($key).gsub!(/=+$/, '').chomp");
	}
	
	private static String javaHmac(String keyHex, String... values) throws Exception {
		if(values == null || values.length == 0){
			throw new IllegalArgumentException();
		}
		
		byte[] keyBytes = Hex.decodeHex(ENCRYPT_KEY.toCharArray());
		byte[] javaResultBytes = RequestSigner.hmac(keyBytes, values);
		return new String(Base64.encodeBase64(javaResultBytes)).replaceAll("=+$", "");
	}
	
	
	@Test
	public void jrubySanityCheck() throws Exception{
		ScriptingContainer container = new ScriptingContainer();
		
		
		container.runScriptlet("puts $LOAD_PATH");
		
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		//container.runScriptlet(PathType.CLASSPATH, "./ruby.src/" + HMAC_HTTP_RB);
		
		
        //container.runScriptlet("puts 'Hello World!'");
        
		String f = "test.rb";
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + f)), f);
		//Object r1 = container.runScriptlet(PathType.CLASSPATH, "./ruby.src/" + f);
       
		
		
		
		//this works:
		//container.put("x", "1234");
		//throw new Exception(container.get("x").toString());
		
		Object results = container.get("$results");
		Assert.assertNotNull(results);
		
		//String s = container.callMethod(results, "[]", "a", String.class);
		//throw new Exception(s);
		
		

		//private static final String NOT_SO_SECRET_KEY = "3c0042fa1f17397297320cd256a0db778d90b140393f952836605d0b6508d076";
		//key = ["3c0042fa1f17397297320cd256a0db778d90b140393f952836605d0b6508d076"];
		
		//TODO:  create a method that copies the java request to the ruby request
		String s = "$key = [\"" + ENCRYPT_KEY + "\"].pack('H*')";
		//if(1==1) throw new Exception(s);
		container.runScriptlet(s);
		//container.put("$key", "[\"" + ENCRYPT_KEY + "\"]");
		//throw new Exception(container.get("$key").toString());
		
		
		//key = getHmac(key.pack("H*"), yyyymmdd);
		
		//
		//this does work:  String hmacResult = (String)container.runScriptlet("Hmac.getHmac($key, 'test')");
		// Base64.encode64(hmac).gsub!(/=+$/, "").chomp
		String hmacResult = (String)container.runScriptlet("Base64.encode64(Hmac.getHmac($key, 'test')).gsub!(/=+$/, '').chomp");
		
		
		byte[] keyBytes = Hex.decodeHex(ENCRYPT_KEY.toCharArray());
		byte[] javaResultBytes = RequestSigner.hmac(keyBytes, "test");
		String javaResult = new String(Base64.encodeBase64(javaResultBytes)).replaceAll("=+$", "");
		
		Assert.assertEquals(javaResult, hmacResult);
		Assert.assertEquals(javaResult, rubyHmac(ENCRYPT_KEY, "test"));
		
	}
	
	@Test
	public void testHmacFunc() throws Exception{

		Assert.assertEquals(javaHmac(ENCRYPT_KEY, "test"), rubyHmac(ENCRYPT_KEY, "test"));
		
		Assert.assertEquals(javaHmac(ENCRYPT_KEY, "a", "b"), rubyHmac(ENCRYPT_KEY, "a", "b"));
		
		String[] fields = new String[]{
				"2015030616", "addReleasePackage", "&appName=scs&fileUrl=skincaresidekick.com/deployment/scs/test/SkinCareSidekick-2014-12-19.apk&markActive=false&notes=uploaded%202015-03-06%20T%20112418&versionCode=11"
		};
		
		Assert.assertEquals(
				javaHmac(ENCRYPT_KEY, fields), 
				
				rubyHmac(ENCRYPT_KEY, fields));

	}
	
	@Test
	public void testHmacFuncFail() throws Exception{
		String[] fields = new String[]{
				"2015030616", "addReleasePackage", "&appName=scs&fileUrl=skincaresidekick.com/deployment/scs/test/SkinCareSidekick-2014-12-19.apk&markActive=false&notes=uploaded%202015-03-06%20T%20112418&versionCode=11"
		};
		String[] fields2 = new String[]{
				"2015030616", "addReleasePackage", 
				"&appName=scs&fileUrl=skincaresidekick.com/deployment/scs/test/SkinCareSidekick-2014-12-19.apk&markActive=false&notes=uploaded%202015-03-06%20T%20112418&versionCode=11",
				"monkey"
		};
		
		Assert.assertNotSame(
				javaHmac(ENCRYPT_KEY, fields), 
				
				rubyHmac(ENCRYPT_KEY, fields2));
		
	}
	
	@Test
	public void testProtocolVersionT2() throws Exception {
		final String REQUEST_ID = "12345";
		final String METHOD = "getTime";
		final String STAGE = "test";
		
		SimpleSignedRequest testRequest = new SimpleSignedRequest();
		testRequest.setProtocolVersion(SignedRequest.ProtocolVersions.TROUBLESHOOT_2);
		testRequest.setRequestId(REQUEST_ID);
		testRequest.setMethod(METHOD);
		testRequest.setStageName(STAGE);
		
		
		RequestSigner.signRequest(testRequest, ENCRYPT_KEY);
		String javaSignature = testRequest.getSignature();
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$request = SSRequest.new");
		container.runScriptlet("$request.protocolVersion = '" + SignedRequest.ProtocolVersions.TROUBLESHOOT_2 + "'");
		container.runScriptlet("$request.requestId = '" + REQUEST_ID + "'");
		container.runScriptlet("$request.method = '" + METHOD + "'");
		container.runScriptlet("$request.stage = '" + STAGE + "'");
		container.runScriptlet("$signature = Hmac.calcSignatureProtocolT2($request, '" + ENCRYPT_KEY + "')");
		
		String rubySignature = (String)container.get("$signature");
		
		Assert.assertEquals(javaHmac(ENCRYPT_KEY, SignedRequest.ProtocolVersions.TROUBLESHOOT_2), rubyHmac(ENCRYPT_KEY, SignedRequest.ProtocolVersions.TROUBLESHOOT_2));
		Assert.assertEquals(rubyHmac(ENCRYPT_KEY, SignedRequest.ProtocolVersions.TROUBLESHOOT_2), rubySignature);
		
		Assert.assertEquals(javaHmac(ENCRYPT_KEY, SignedRequest.ProtocolVersions.TROUBLESHOOT_2), rubySignature);
		
		Assert.assertEquals(javaSignature, rubySignature);
	}
	
	
	@Test
	public void testProtocolVersionT3() throws Exception {
		final String REQUEST_ID = "12345";
		final String METHOD = "getTime";
		final String STAGE = "test";
		
		SimpleSignedRequest testRequest = new SimpleSignedRequest();
		testRequest.setProtocolVersion(SignedRequest.ProtocolVersions.TROUBLESHOOT_3);
		
		testRequest.setRequestId(REQUEST_ID);
		testRequest.setMethod(METHOD);
		testRequest.setStageName(STAGE);
		
		
		RequestSigner.signRequest(testRequest, ENCRYPT_KEY);
		String javaSignature = testRequest.getSignature();
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$request = SSRequest.new");
		container.runScriptlet("$request.protocolVersion = '" + SignedRequest.ProtocolVersions.TROUBLESHOOT_3 + "'");
		container.runScriptlet("$request.requestId = '" + REQUEST_ID + "'");
		container.runScriptlet("$request.method = '" + METHOD + "'");
		container.runScriptlet("$request.stage = '" + STAGE + "'");
		container.runScriptlet("$signature = Hmac.calcSignatureByProtocol($request, '" + ENCRYPT_KEY + "')");
		
		String rubySignature = (String)container.get("$signature");
		
		
		
		
		container.runScriptlet("$yyyymmdd = Time.new.utc.strftime(\"%Y%m%d%H\");");
		String rubyAuthDate = (String) container.get("$yyyymmdd");
		
		Assert.assertEquals(testRequest.getAuthDate(), rubyAuthDate);
		
		Assert.assertEquals(javaSignature, rubySignature);
	}
	
	
	private static SimpleSignedRequest createTestRequest(){
		final String REQUEST_ID = "12345";
		final String METHOD = "getTime";
		final String STAGE = "test";
		final String SERVICE_NAME = "HMS";
		
		SimpleSignedRequest testRequest = new SimpleSignedRequest();
		
		testRequest.setRequestId(REQUEST_ID);
		testRequest.setMethod(METHOD);
		testRequest.setStageName(STAGE);
		testRequest.setServiceName(SERVICE_NAME);
		
		testRequest.getMethodParameters().putAll(METHOD_PARAMETERS);
		testRequest.getMethodParameters().put("z", "b");
		testRequest.getMethodParameters().put("a", "b");
		
		
		testRequest.prepareForSigning();
		
		return testRequest;
	}
	
	private static void setRubyRequest(ScriptingContainer container, SimpleSignedRequest testRequest){
		container.runScriptlet("$request.protocolVersion = '" + testRequest.getProtocolVersion() + "'");
		container.runScriptlet("$request.requestId = '" + testRequest.getRequestId() + "'");
		container.runScriptlet("$request.method = '" + testRequest.getMethod() + "'");
		container.runScriptlet("$request.stage = '" + testRequest.getStageName() + "'");
		container.runScriptlet("$request.serviceName = '" + testRequest.getServiceName() + "'");
		
		for(Map.Entry<String, String> entry : testRequest.getMethodParameters().entrySet()){
			container.runScriptlet("$request.fields['" + entry.getKey() + "'] = '" + entry.getValue() + "'");
		}
		
		container.runScriptlet("$request.createParamString()");
	}
	
	
	@Test
	public void testProtocolVersionT4() throws Exception {
		
		SimpleSignedRequest testRequest = createTestRequest();
		testRequest.setProtocolVersion(SignedRequest.ProtocolVersions.TROUBLESHOOT_4);
		
		RequestSigner.signRequest(testRequest, ENCRYPT_KEY);
		String javaSignature = testRequest.getSignature();
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$request = SSRequest.new");
		setRubyRequest(container, testRequest);
		
		container.runScriptlet("$signature = Hmac.calcSignatureByProtocol($request, '" + ENCRYPT_KEY + "')");
		
		String rubySignature = (String)container.get("$signature");

		
		Assert.assertEquals(javaSignature, rubySignature);
	}
	
	@Test
	public void testProtocolVersion0() throws Exception {
		SimpleSignedRequest testRequest = createTestRequest();
		testRequest.setProtocolVersion(SignedRequest.ProtocolVersions.LEGACY);
		
		RequestSigner.signRequest(testRequest, ENCRYPT_KEY);
		String javaSignature = testRequest.getSignature();
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$request = SSRequest.new");
		setRubyRequest(container, testRequest);
		
		
		Assert.assertEquals(
				(String)container.runScriptlet("Hmac.calcProtocol0fragment($request)"),
				RequestSigner.calcProtocol0fragment(testRequest));
		
		
		
		
		container.runScriptlet("$signature = Hmac.calcSignatureByProtocol($request, '" + ENCRYPT_KEY + "')");
		
		String rubySignature = (String)container.get("$signature");

		
		Assert.assertEquals(javaSignature, rubySignature);
	}
	
	@Test
	public void testProtocolVersion1() throws Exception {
		SimpleSignedRequest testRequest = createTestRequest();
		testRequest.setProtocolVersion(SignedRequest.ProtocolVersions.V1);
		
		RequestSigner.signRequest(testRequest, ENCRYPT_KEY);
		String javaSignature = testRequest.getSignature();
		
		ScriptingContainer container = new ScriptingContainer();
		container.runScriptlet(new FileInputStream(new File("./ruby.src/" + HMAC_HTTP_RB)), HMAC_HTTP_RB);
		
		container.runScriptlet("$request = SSRequest.new");
		setRubyRequest(container, testRequest);
		
		container.runScriptlet("$signature = Hmac.calcSignatureByProtocol($request, '" + ENCRYPT_KEY + "')");
		
		String rubySignature = (String)container.get("$signature");

		
		Assert.assertEquals(javaSignature, rubySignature);
	}
}

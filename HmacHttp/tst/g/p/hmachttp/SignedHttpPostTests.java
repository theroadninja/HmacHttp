package g.p.hmachttp;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class SignedHttpPostTests {

	public static final StageConfiguration legacyTestStage = new StageConfiguration(
			/*stageName*/"test",
			/*protocolVersion*/SignedRequest.ProtocolVersions.LEGACY,
			/*httpProtocolAndHost*/"http://example.com",
			/*port*/	8080,
			/*httpPathStart*/   "/servlet/path/",
			/*serviceName*/		"HmacTestService");
	
	public static final StageConfiguration testStageV1 = new StageConfiguration(
			/*stageName*/"test",
			/*protocolVersion*/SignedRequest.ProtocolVersions.V1,
			/*httpProtocolAndHost*/"http://example.com",
			/*port*/	8080,
			/*httpPathStart*/   "/servlet/path/",
			/*serviceName*/		"HmacTestService");
	
	
	@Test
	public void thereAndBackAgainKeyTest() throws Exception {
		
		StageConfiguration keyTest = new StageConfiguration(
				"test",
				SignedRequest.ProtocolVersions.KEY_CHECK,
				"http://example.com",
				8080,
				"/servlet/path/",
				"HmacTestService");
		
		SignedHttpPost hp = new SignedHttpPost(keyTest, "testMethod");		
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
	}
	
	@Test
	public void thereAndBackAgainTroubleshoot1Test() throws Exception {
		
		StageConfiguration keyTest = new StageConfiguration(
				"test",
				SignedRequest.ProtocolVersions.TROUBLESHOOT_1,
				"http://example.com",
				8080,
				"/servlet/path/",
				"HmacTestService");
		
		SignedHttpPost hp = new SignedHttpPost(keyTest, "testMethod");		
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
	}
	
	@Test
	public void thereAndBackAgainLegacy() throws Exception {
		
		SignedHttpPost hp = new SignedHttpPost(legacyTestStage, "testMethod");		
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		hp.setExtraSignedField("extra");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		//not used in protocol version 0
		hp.setKeyName("tango");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		//NOTE: path end fragment not part of v0
		//NOTE: body not part of v0

	}
	
	@Test
	public void thereAndBackAgainV1() throws Exception {
		SignedHttpPost hp = new SignedHttpPost(testStageV1, "testMethod");		
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		
		
		hp.setExtraSignedField("extra");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		//not used in protocol version 0
		hp.setKeyName("tango");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		
		hp.setBody("test\n \n \n \t a \n \n c \n b  ");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		
		hp.getMethodParameters().put("a", "b");
		hp.getMethodParameters().put("c", "d");
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
		
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		Assert.assertTrue(! hp.getSignature().equals(""));
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost()));
	}
	
	@Test
	public void testOverrideUrl() throws Exception {
		
		SignedHttpPost hp = new SignedHttpPost(testStageV1, "testMethod");		
		
		hp.setExtraSignedField("extra");
		hp.setKeyName("tango");
		hp.setBody("test\n \n \n \t a \n \n c \n b  ");
		hp.getMethodParameters().put("a", "b");
		hp.getMethodParameters().put("c", "d");
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		Assert.assertTrue(! hp.getSignature().equals(""));
		assertEquals(hp, new SignedHttpPost(hp.toHttpPost(urlFor(testStageV1))));
	}
	
	@Test
	public void testUrlParameter() throws Exception {
		
		// &appName=scs&versionCode=11&markActive=false
		
		SignedHttpPost hp = new SignedHttpPost(legacyTestStage, "testMethod");
		hp.getMethodParameters().put("appName", "scs");
		hp.getMethodParameters().put("versionCode", "11");
		//hp.getMethodParameters().put("fileUrl", "skincaresidekick.com/deployment/scs/test/SkinCareSidekick-2014-12-19.apk");
		hp.getMethodParameters().put("markActive", "false");
		
		String k = "fc5d5078";
		RequestSigner.signRequest(hp, k);
		
		SignedHttpPost hp2 = new SignedHttpPost(hp.toHttpPost());
		
		Assert.assertEquals(hp.getSignature(), hp2.getSignature());
		
	}
	
	public static String urlFor(StageConfiguration stageConfig){
		//default hmac url:
		//final String url = webServiceHost + ":" + webServicePort + "/" + getStageName() + "/" + httpPathStart;
		
		//we dont have stage in the url:
		return stageConfig.httpProtocolAndHost + ":" + stageConfig.port + "/" + stageConfig.httpPathStart;
	}
	
	public static void assertEquals(SignedHttpPost hp, SignedHttpPost hp2){
		Assert.assertEquals(hp.getProtocolVersion(), hp2.getProtocolVersion());
		Assert.assertEquals(hp.getRequestId(), hp2.getRequestId());
		Assert.assertEquals(hp.getAuthDate(), hp2.getAuthDate());
		Assert.assertEquals(hp.getServiceName(), hp2.getServiceName());
		Assert.assertEquals(hp.getStageName(), hp2.getStageName());
		Assert.assertEquals(hp.getMethod(), hp2.getMethod());
		Assert.assertEquals(hp.getMethodParameterString(), hp2.getMethodParameterString());
		Assert.assertEquals(hp.getBody(), hp2.getBody());
		Assert.assertEquals(hp.getHttpPathEndFragment(), hp2.getHttpPathEndFragment());
		Assert.assertEquals(hp.getSignature(), hp2.getSignature());
		Assert.assertEquals(hp.extraSignedField(), hp2.extraSignedField());
		Assert.assertEquals(hp.getKeyName(), hp2.getKeyName());
		
		Map<String, String> h1 = hp.getHttpHeaders();
		Map<String, String> h2 = hp2.getHttpHeaders();
		
		Assert.assertEquals(h1, h2);
		
	}
}

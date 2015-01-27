package g.p.hmachttp;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class RequestSigner {
	

	

	/**
	 * 
	 * @param request
	 * @param recommending using hex string 64 chars long
	 * @throws HmacHttpException
	 */
	public static void signRequest(SignedRequest request, String key) throws HmacHttpException {
		if(request == null || key == null) throw new IllegalArgumentException("parameter cannot be null");
		
		try {
			request.setSignature(calcSignature(request, key));
		} catch (HmacHttpException e) {
			throw e;
		} catch (Exception e) {
			//fuck android style guildelines
			
			throw new HmacHttpException(e);
		}
	}
	
	public static String calcSignature(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, HmacHttpException {
		if(request == null){ return null; }
	
		if(SignedRequest.ProtocolVersions.KEY_CHECK.equals(request.getProtocolVersion())){
	
			return RequestSignerAux.calcKeyProtocol(request, skey);
			
		}else if(SignedRequest.ProtocolVersions.TROUBLESHOOT_1.equals(request.getProtocolVersion())){
			
			return RequestSignerAux.calcTroubleshoot1Protocol(request, skey);
			
		}else if(SignedRequest.ProtocolVersions.LEGACY.equals(request.getProtocolVersion())){
			return calcSignatureProtocol0(request, skey);
		}else if(SignedRequest.ProtocolVersions.V1.equals(request.getProtocolVersion())){
			return RequestSignerV1.get().calcSignature(request, skey);
		}else{
			throw new HmacHttpException("protocol version not recognized: " + request.getProtocolVersion());
		}
		
	}
	
	private static String calcSignatureProtocol0(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException{
		byte[] key = Hex.decodeHex(skey.toCharArray());
		
		request.prepareForSigning();
		String signableRequest = request.getRequestId() + ";" + request.getMethodParameterString() + ";" + request.getMethod();
		
		key = hmac(key, request.getAuthDate());
		key = hmac(key, request.getStageName());
		key = hmac(key, request.getServiceName());
		key = hmac(key, signableRequest);
		
		return new String(Base64.encodeBase64(key)).replaceAll("=+$", "");
	}
	
	public static byte[] hmac(byte[] keyBytes, String message) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
		
		final String HMAC = "HmacSHA256";
		
		SecretKeySpec key = new SecretKeySpec(keyBytes, HMAC);
		Mac mac = Mac.getInstance(HMAC);
		mac.init(key);
		
		byte[] bytes = mac.doFinal(message.getBytes("UTF-8"));
		return bytes;
		
	}
	
}

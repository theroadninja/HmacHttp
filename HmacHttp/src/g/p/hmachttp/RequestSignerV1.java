package g.p.hmachttp;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class RequestSignerV1 {

	private static final RequestSignerV1 instance = new RequestSignerV1();
	
	public static RequestSignerV1 get(){
		return instance;
	}
	
	public String calcSignature(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, HmacHttpException {
		return calcSignature2(request, skey);
	}
	
	static String calcSignature2(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, HmacHttpException {
		byte[] key = Hex.decodeHex(skey.toCharArray());
		
		request.prepareForSigning();
		
		key = RequestSigner.hmac(key, request.getProtocolVersion());
		key = RequestSigner.hmac(key, request.getRequestId());
		key = RequestSigner.hmac(key, request.getAuthDate());
		key = RequestSigner.hmac(key, request.getServiceName());
		key = RequestSigner.hmac(key, request.getStageName());
		key = RequestSigner.hmac(key, request.getMethod());
		key = RequestSigner.hmac(key, request.getMethodParameterString());
		key = RequestSigner.hmac(key, request.extraSignedField());
		//Path end fragment not part of V1 -- getHttpPathEndFragment()
		key = RequestSigner.hmac(key, request.getBody());
		
		return new String(Base64.encodeBase64(key)).replaceAll("=+$", "");
	}
}

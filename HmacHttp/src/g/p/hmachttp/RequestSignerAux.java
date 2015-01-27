package g.p.hmachttp;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * provides request signer for aux protocols that only exist
 * for troubleshooting.
 * 
 * @author Dave
 *
 */
public class RequestSignerAux {

	public static String calcKeyProtocol(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException{
		byte[] key = Hex.decodeHex(skey.toCharArray());
		
		request.prepareForSigning();
		
		key = RequestSigner.hmac(key, request.getProtocolVersion());
		
		return new String(Base64.encodeBase64(key)).replaceAll("=+$", "");
	}
	
	
	public static String calcTroubleshoot1Protocol(SignedRequest request, String skey) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException{
		byte[] key = Hex.decodeHex(skey.toCharArray());
		
		request.prepareForSigning();
		
		key = RequestSigner.hmac(key, request.getProtocolVersion());
		key = RequestSigner.hmac(key, request.getRequestId());
		key = RequestSigner.hmac(key, request.getAuthDate());
		key = RequestSigner.hmac(key, request.getServiceName());
		key = RequestSigner.hmac(key, request.getStageName());
		key = RequestSigner.hmac(key, request.getMethod());
		
		return new String(Base64.encodeBase64(key)).replaceAll("=+$", "");
		
	}
}

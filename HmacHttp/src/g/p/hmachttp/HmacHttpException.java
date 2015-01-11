package g.p.hmachttp;

public class HmacHttpException extends Exception {
	private static final long serialVersionUID = -2175741671477768425L;

	public HmacHttpException(String message){
		super(message);
	}
	
	public HmacHttpException(Exception ex){
		super(ex);
	}

}

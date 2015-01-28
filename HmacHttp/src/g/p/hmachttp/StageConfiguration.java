package g.p.hmachttp;

/**
 * Optional convenience class for organizing endpoints into 'stages'.
 * @author Dave
 *
 */
public class StageConfiguration {

	public final String stageName;
	
	public final String protocolVersion;
	
	public final String httpProtocolAndHost;
	public final int port;
	public final String httpPathStart;
	public final String serviceName;
	
	/** 
	 * When true, adds the stage after the port and before the
	 * 'httpPathStart' fragment
	 */
	private boolean addStageToPath = true;
	
	public StageConfiguration(String stageName,
			String protocolVersion,
			String httpProtocolAndHost,
			int port,
			String httpPathStart,
			String serviceName){
		
		this.stageName = stageName;
		this.protocolVersion = protocolVersion;
		this.httpProtocolAndHost = httpProtocolAndHost;
		this.port = port;
		this.httpPathStart = httpPathStart;
		this.serviceName = serviceName;
	}
	
	public String getStageName(){
		return this.stageName;
	}
	
	public StageConfiguration setAddStageToPath(boolean b){
		this.addStageToPath = b;
		return this;
	}
	
	public boolean getAddStageToPath(){
		return addStageToPath;
	}
	
}

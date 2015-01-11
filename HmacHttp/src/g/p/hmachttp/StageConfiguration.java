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
	
}

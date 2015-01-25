package g.p.hmachttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;

public class Util {

	
	public static String readAndCloseStream(HttpEntity e) {
		if(e == null){
			return null;
		}
		
		try{
			return readAndCloseStream(e.getContent());
		}catch(IOException ex){
			return null;
		}
	}
	
	public static String readAndCloseStream(InputStream is) {
		
		
		if(is == null){
			return null;
		}
		
		String line;
		StringBuilder sb = new StringBuilder();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			//doing it this screwy way to avoid adding extra newline at the end
			line = br.readLine();
			if(line == null){
				return null;
			}
			sb.append(line);
			
			while(null != (line = br.readLine())){
				sb.append("\n" + line);
			}
			return sb.toString();
		}catch(IOException ex){
			return null;
		}finally{
			try{ is.close(); }catch(IOException ex){}
		}
	}

}

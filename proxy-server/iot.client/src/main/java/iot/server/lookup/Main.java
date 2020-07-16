package iot.server.lookup;
import org.eclipse.californium.core.CoapServer;

public class Main{
	static final int port = 5673;
	static final CoapServer server = new CoapServer( port );

	public static void main(String args[]) {
		System.out.println("Starting CoapServer....");
		
		server.add( new MyLookUpResource("lookup"));
		server.start();
	}

}

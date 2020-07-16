package iot.server.lookup;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;

public class Main{
	static final int port = 5673;
	static final CoapServer server = new CoapServer( port );

	static final CaliforniumLogger cl = new CaliforniumLogger();

	public static void main(String args[]) {
		System.out.println("Starting CoapServer....");
		
		cl.disableLogging();

		server.add( new MyLookUpResource("lookup"));
		server.start();
	}

}

package iot.server.register;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Main extends CoapServer{
	static CaliforniumLogger cl = new CaliforniumLogger();

	public static void main(String args[]) {
		System.out.println("Starting CoapServer....");
		cl.disableLogging();
		Main server = new Main();

		MyResourceDiscovery client = new MyResourceDiscovery();
	
		server.add( new MyRegistrationResource("register", client));
		server.start();
	}

}

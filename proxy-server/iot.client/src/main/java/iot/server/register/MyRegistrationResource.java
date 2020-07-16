package iot.server.register;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;


// risorsa sulla quale si registrano i device
public final class MyRegistrationResource extends CoapResource {
	MyResourceDiscovery mc = null;
	
	public MyRegistrationResource(String name, MyResourceDiscovery mc ){
		super( name );
		this.mc = mc;
	}
	
	public void handlePOST(CoapExchange exchange) {	
		String device_uri = exchange.getSourceAddress().toString();
		device_uri = "coap://[" + device_uri.substring(1, 6) + device_uri.substring(11, device_uri.length()) + "]";
		
		System.out.println("Device - " + device_uri +" - sends registration message" );
		
		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload( "Welcome Device!" );
		exchange.respond(response);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// resource discovery 
		mc.resourceDiscovery( device_uri );
				
	}	
}

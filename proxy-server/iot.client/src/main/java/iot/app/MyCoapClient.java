package iot.app;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class MyCoapClient {
	
	CoapClient cc = null;
			
	public void setTemperature(String actuator, String room, String temp) 
	{	
		try {
			cc = new CoapClient( actuator );
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if(!cc.ping()) { // attuatore down o url non corretto
			System.out.println("Device - " + actuator + "- non raggiungibile");
			return;
		}

		System.out.println("The system will set the temperature using the actuator: " + actuator);
	
		CoapResponse cr = cc.post( "room=" + room + "&temperature=" + temp, 1);
		
		if(cr.isSuccess()) {
			System.out.println("Messaggio inviato correttamente all' attuatore.");
			return;
		}
		
		System.out.println("Errore nell'invio del messaggio all'attuatore.");
		System.out.println("Endpoind answer with response code " + cr.getCode().toString() );
	}
	
}

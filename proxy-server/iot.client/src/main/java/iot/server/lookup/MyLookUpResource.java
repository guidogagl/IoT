package iot.server.lookup;

import iot.db.MongoHandler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class MyLookUpResource extends CoapResource {
	MongoHandler mh = null;

	List<String> rooms = new ArrayList<String>();
	List<List<String>> sensors = new ArrayList<List<String>>();

	public MyLookUpResource(String name) {
		super(name);
	}

	public void handleGET(CoapExchange exchange) {

		System.out.println("*+++++++++++++++++++++ GET REQUEST TO LOOKUP INTERFACE +++++++++++++++++++++++*");

		String device_uri = exchange.getSourceAddress().toString();
		device_uri = "coap://[" + device_uri.substring(1, 6) + device_uri.substring(11, device_uri.length()) + "]";
		String roomName = null;
		try {
			roomName = exchange.getQueryParameter("room");
		} catch (Exception e) {
			System.out.println("Mote sends bad query parameter format");
			exchange.respond(ResponseCode.BAD_REQUEST);
			return;
		}
		if (roomName == null || roomName.isEmpty()) {
			System.out.println("Mote sends bad query parameter format");
			exchange.respond(ResponseCode.BAD_REQUEST);
			return;
		}

		System.out.println("Device - " + device_uri + " - sends lookup message for room - " + roomName + " -");

		mh = new MongoHandler();
		List<String> s = mh.getRoomSensors(roomName);
		mh.close();

		if (s.isEmpty()){
			System.out.println("ERR: Actuator ask for an empty room");
			exchange.respond(ResponseCode.BAD_REQUEST);
			return;
		}

		String service = device_uri + "/mysensors";
		CoapClient cc = null;
		try{
			cc = new CoapClient(service);
		} catch( Exception e ){
			System.out.println("Error parsing actuator URI");
			exchange.respond(ResponseCode.BAD_REQUEST);	
			return;
		}

		List<String> temp = new ArrayList<String>();
		for (int i = 0; i < s.size(); i++) {
			CoapClient c = null;
			try{
				c = new CoapClient( s.get(i) );
			} catch( Exception e ){
				continue;
			}

			if( c.ping() )
				temp.add( s.get(i) );
		}
		s = temp;

		if( s.size() <= 0 ){
			System.out.println( "No sensors available for room");
			exchange.respond( ResponseCode.BAD_REQUEST);
		}

		exchange.respond(ResponseCode.VALID);
		
		int exp = 1;
		while( !cc.ping() ){
			try {
				Thread.sleep((int) (exp * 1000 * Math.random()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			exp = exp * 2;		
		} 

		for (int i = 0; i < s.size(); i++) {
			s.set(i, s.get(i).split("]")[0] + "]");
			System.out.println("Sending message to resource /mysensors");
			System.out.println("num=" + s.size() + "&sensor=" + s.get(i));

			cc.post("num=" + s.size() + "&sensor=" + s.get(i), 1);
			try {
				Thread.sleep((int) (2000 * Math.random()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
	}	

}

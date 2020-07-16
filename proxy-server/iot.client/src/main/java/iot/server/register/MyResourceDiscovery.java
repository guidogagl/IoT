package iot.server.register;

import iot.db.MongoHandler;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class MyResourceDiscovery {
	static final String discovery_string = "/.well-known/core";
	static final String sensor_root = "/sensor";
	static final String actuator_root = "/actuator";

	public MyResourceDiscovery(){};

	public synchronized void resourceDiscovery(String uri) {
		System.out.println("*----------- Resource Discover on mote : " + uri + " --------------*");
		URI discovery_uri;

		try {
			discovery_uri = new URI(uri + discovery_string);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		Boolean corrupted_payload = new Boolean(false);
		URI new_uri = null;
		String res_root = null;
		do {
			CoapClient discovery_client = new CoapClient(discovery_uri);
			CoapResponse cr = null;
			int exp = 1;
			while (cr == null || !cr.isSuccess()) {
				try{
					// wait for a random delay to avoid congestion problem
					Thread.sleep((int) ( exp * 100 * Math.random()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cr = discovery_client.get();
				exp = exp * 2;
			}

			res_root = cr.getResponseText();
			try {
				res_root = res_root.split("<")[2].split(">")[0];
				new_uri = new URI(uri + res_root);
			} catch (Exception e) {
				// payload corrupted
				System.out.println("Received corrupted payload while discovering resource on mote: " + uri);
				corrupted_payload = true;
			}
		} while( corrupted_payload );

		System.out.print("Mote: " + new_uri.toString() + "\n");
		if( res_root.equals(sensor_root) ) { 
			// add the sensor to the database and observe the resource
			MongoHandler mh = new MongoHandler();
			mh.insertSensor(new_uri);
			mh.close();

			CoapClient cc = new CoapClient( new_uri );
			SensorHandler sh = new SensorHandler(new_uri.toString());
			cc.observe(sh);
		}
		else if ( res_root.equals( actuator_root ) ){
			MongoHandler mh = new MongoHandler();
			mh.insertActuator(new_uri);
			mh.close();
		}

		System.out.println("*--------------- End -----------------*");
	}
		
}
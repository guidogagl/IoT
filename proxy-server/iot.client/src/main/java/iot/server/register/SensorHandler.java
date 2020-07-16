package iot.server.register;

import iot.db.MongoHandler;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

public class SensorHandler implements CoapHandler{
	String bn = null;	
	CoapObserveRelation cor = null;
	
	SensorHandler( String bn ){
		this.bn = bn;
	}
	
	public void setObserveRelation( CoapObserveRelation COR ) {cor = COR;}
	
	public void onError(){
		System.out.print("Error in observe resource: " + bn + "\n");
	}
	
	public void onLoad(CoapResponse response) {
		System.out.print("*------------Received sensor measuerement-------------*\n");
		String str = null;
		try{
			str = new String (response.getResponseText());
		} catch(Exception e ) {
			System.out.println("Noise measurements, discarding....");
			return;		
		}

		if( str == null || str.isEmpty() || str.length() < 10 ) {
			System.out.println("Noise measurements, discarding....");
			return;
		}
			
		System.out.println( str );
		
		// convert from json to Document
		Document doc = null;
		try{
			doc =  Document.parse ( str );
			List<Document> docs = (ArrayList<Document>)doc.get("e");
			doc = docs.get(0);
		} catch(Exception e ){
			System.out.println("Noise measurements, discarding....");
			return;
		}

		if( doc == null || doc.isEmpty() || !doc.containsKey("n") || !doc.containsKey("u") || !doc.containsKey("v") ) {
			System.out.println("Bad json format response on mote: " + bn);
			return;
		}

		// add current time
		String now = LocalDateTime.now().toString();
		doc.append("bt", now);

		doc = new Document( "e", doc ); 
		String json = doc.toJson();	
		
		System.out.println("Took measurement value from sensor: \n" + json);
		
		MongoHandler mh = new MongoHandler();
		mh.insertSensorMeasurement( json , bn);
		mh.close();
	}
}
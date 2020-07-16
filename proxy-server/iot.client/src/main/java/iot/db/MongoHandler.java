package iot.db;

import java.net.URI;
import java.net.URISyntaxException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.Arrays;
import com.mongodb.Block;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class MongoHandler{
	String coll_str = "sensors";
	
	MongoClient mongoClient = null; 
	MongoDatabase db = null;
	MongoCollection col = null;
	MongoCollection act = null;
	MongoCollection rooms = null;
	
	public MongoHandler(){
		Logger mongoLogger = Logger.getLogger( "com.mongodb" );
		mongoLogger.setLevel(Level.SEVERE);
		
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("db");
		col = db.getCollection( coll_str );
		act = db.getCollection( "actuators" );
		rooms = db.getCollection("rooms");
	}
	
	public void close(){
		mongoClient.close();
	}
	public void insertSensor(URI bn){
		if (this.sensorIsPresent(bn)) return;
		
		Document doc = new Document("bn", bn.toString())
									.append("e", Arrays.asList())
									.append("room", "-1");
		col.insertOne(doc);
		
		doc = (Document)col.find(eq("bn", bn.toString())).first();
		
		System.out.print("Document " + doc.toString() + " inserted on mongo \n");
	}
	
	public void insertActuator(URI bn) {
		if (this.actuatorIsPresent(bn)) 
			return;
				
		Document doc = new Document("bn", bn.toString())
									.append("room", "-1");
		act.insertOne(doc);
		
		doc = (Document)act.find(eq("bn", bn.toString())).first();
		
		System.out.print("Document " + doc.toString() + " inserted on mongo \n");
	
	}
	
	public void insertSensorMeasurement(String json, String bn) {
		if(json == null || json.isEmpty()) {
			System.out.println("BAD JSON measurements received from sensor " + bn);
			return;
		}
		
		Document doc = Document.parse(json);
		doc = new Document("e", doc);
		
		if( doc == null || doc.isEmpty() ) {
			System.out.println("Bad json format response on mote: " + bn);
			return;
		}
		
		// update mongo with new measurements
		col.findOneAndUpdate(
				new Document("bn", bn),
				new Document("$push", doc )
				);
	}
	
	public Boolean sensorIsPresent(URI bn) {
		if ( col.count( new Document("bn", bn.toString())) > 0 )
			return new Boolean(true);
		return new Boolean(false);
	}

	public Boolean actuatorIsPresent(URI bn) {
		if ( act.count( new Document("bn", bn.toString())) > 0 )
			return new Boolean(true);
		return new Boolean(false);
	}
	
	public Boolean sensorIsPresent(URI bn, String room) {
		if ( col.count( new Document("bn", bn.toString() )
								.append("room", room)
				) > 0 )
			return new Boolean(true);
		return new Boolean(false);
	}
	
	public MongoCursor<Document> getRooms(){
		return rooms.find().iterator();
	}
	
	public Document getRoom( String roomName ){
		return (Document) rooms.find( eq("name", roomName) ).first();
	}
	
	public List<String> getRoomActuators( String roomName ){
		List<String> r = new ArrayList<String>();

		MongoCursor<Document> cur = act.find( eq( "room", roomName) ).iterator();
		
		while( cur.hasNext() )
			r.add( cur.next().get("bn").toString() );
		
		return r;
	}

	public List<String> getRoomSensors( String roomName ){
		List<String> r = new ArrayList<String>();

		MongoCursor<Document> cur = col.find( eq( "room", roomName) ).iterator();
		
		while( cur.hasNext() )
			r.add( cur.next().get("bn").toString() );
		
		return r;
	}

 	public boolean insertNewRoom(String name) {
		
		if ( rooms.count( new Document("name", name)  )  > 0 ) 
			return false;
		
		Document doc = new Document("name", name)
							.append("sensors", Arrays.asList() )
							.append("actuators", Arrays.asList() );
		rooms.insertOne( doc );
		
		return true;
	}

	public boolean deleteRoom(String name) {
		
		if ( rooms.count( new Document("name", name)  )  == 0 ) 
			return false;
		
		MongoCursor<Document> cur = rooms.find(eq("name", name)).iterator();
		
		Document doc = cur.next();
		
		List<Document> act_l = ( List<Document> ) doc.get("actuators");
		List<Document> sens_l = ( List<Document> ) doc.get("sensors");
		
		// resetting actuators room
		for(int i = 0; i < act_l.size(); i++)
			act.findOneAndUpdate(eq("bn", act_l.get(i)), 
					new Document("$set", new Document("room", "-1") )
					);
		// resetting sensors room
		for(int i = 0; i < sens_l.size(); i++)
			col.findOneAndUpdate(eq("bn", sens_l.get(i)), 
					new Document("$set", new Document("room", "-1") )
					);
		
		rooms.findOneAndDelete( eq("name", name) );
		
		return true;
	}
	
	public boolean addActuatorToRoom(String room, String actuator) {
		if ( rooms.count( new Document("name", room)  )  == 0 ) 
			return false;
		if ( act.count( new Document("bn", actuator).append("room", "-1")  )  == 0 ) 
			return false;
		
		act.findOneAndUpdate(new Document("bn", actuator).append("room", "-1"), 
				new Document("$set", new Document("room", room)));
		rooms.findOneAndUpdate(new Document("name", room),
				new Document("$push", new Document("actuators", actuator)));
		
		return true;
	}
	
	public boolean addSensorToRoom(String room, String sensor) {
		if ( rooms.count( new Document("name", room)  )  == 0 ) 
			return false;
		if ( col.count( new Document("bn", sensor).append("room", "-1")  )  == 0 ) 
			return false;
		
		col.findOneAndUpdate(new Document("bn", sensor).append("room", "-1"), 
				new Document("$set", new Document("room", room)));
		rooms.findOneAndUpdate(new Document("name", room),
				new Document("$push", new Document("sensors", sensor)));
		return true;
	}
	
	public boolean removeActuatorToRoom(String room, String actuator) {
		if ( rooms.count( new Document("name", room)  )  == 0 ) 
			return false;
		if ( act.count( new Document("bn", actuator).append("room", room)  )  == 0 ) 
			return false;
		
		act.findOneAndUpdate(new Document("bn", actuator).append("room", room), 
				new Document("$set", new Document("room", "-1")));
		rooms.findOneAndUpdate(new Document("name", room),
				new Document("$pull", new Document("actuators", actuator)));
		
		return true;
	}
	
	public boolean removeSensorToRoom(String room, String sensor) {
		if ( rooms.count( new Document("name", room)  )  == 0 ) 
			return false;
		if ( col.count( new Document("bn", sensor).append("room", room)  )  == 0 ) 
			return false;
		
		// rimuovi il vecchio sensore
		col.findOneAndDelete(new Document("bn", sensor).append("room", room) );
		try {
			insertSensor(new URI( sensor));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rooms.findOneAndUpdate(new Document("name", room),
				new Document("$pull", new Document("sensors", sensor)));
		return true;
	}
	
	public MongoCursor<Document> getSensorMeasurements(String sensor){
		return col.find(eq("bn", sensor)).iterator();
	}
	
	public List<String> getDevicesList(String type){
		MongoCollection mycol = col;
		
		if(type.contentEquals("actuator"))
			mycol = act;
	
		List<String> res = new ArrayList<String>();
		
		MongoCursor<Document> cur = mycol.find().iterator();
		while( cur.hasNext())
			res.add( cur.next().get("bn").toString() );
		
		return res;
	}
	
	public String getADevice(String room, String type) {
		MongoCollection mycol = col;
		if( type.contentEquals("actuator") )
			mycol = act;
		
		MongoCursor<Document> cur = mycol.find( eq("room", room)).iterator();
		
		if(cur.hasNext()) 
			return cur.next().get("bn").toString();
		else
			return "";
	}	
}
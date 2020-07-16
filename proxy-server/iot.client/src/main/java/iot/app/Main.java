package iot.app;

import iot.db.MongoHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import com.mongodb.client.MongoCursor;

public class Main {
	BufferedReader myInput = new BufferedReader(new InputStreamReader(System.in));
	MongoHandler mh = new MongoHandler();
	
	Float minTemperature = new Float(-10);
	Float maxTemperature = new Float(35);

	public void help() {
		String h = " ** ------------------- Command List ------------------- **\n"
				+ "!show_rooms - mostra la lista di stanze disponibili e i devices registrati su di esse\n"
				+ "!make_room - costruisce una stanza vuota\n"
				+ "!delete_room -rimuove una stanza e libera i devices registrati su di essa\n "
				+ "!add_actuator_to_room - registra un attuatore libero sulla stanza\n"
				+ "!add_sensor_to_room - registra un sensore libero sulla stanza\n"
				+ "!remove_actuator_to_room - rimuove un attuatore registrato dalla stanza\n"
				+ "!remove_sensor_to_room - rimuove un sensore registrato dalla stanza\n"
				+ "!show_actuators_list - mostra la lista degli attuatori presenti nella rete\n"
				+ "!show_sensors_list -  mostra la lista dei sensori presenti nella rete\n"
				+ "!set_temperature - setta la temperatura in una stanza su cui sono presenti almeno un attuatore e un sensore\n"
				+ "!show_sensor_measurements - mostra le ultime misurazioni effettuate da un sensore\n"
				+ "!help - mostra questo schema\n";
		
		System.out.println( h );
	}
	
	public String getCommand() {
		String str = null;
		try {
			str = new String ( myInput.readLine() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		return new String(str);
	}
	
	public void parseCommands( String command ) {
		if(command == null || command.isEmpty()) {
			help();
		}
		// TODO PRINTARE MEGLIO
		else if( command.contentEquals("!show_rooms") ) {
			MongoCursor<Document>  cur = mh.getRooms();
			
			System.out.println("List of rooms in the database: ");
			try {
				while(cur.hasNext()) {
					System.out.println(cur.next().toJson() );
				}
			} finally {
				cur.close();
			}
		}
		else if( command.contentEquals("!make_room")) {
			System.out.println("Insert room name: ");
			String str = null;
			try {
				str = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			if ( mh.insertNewRoom( str ) )
				System.out.println("New room successfully inserted");
			else
				System.out.println("Room name already taken");
		} 
		else if( command.contentEquals("!delete_room")) {
			System.out.println("Insert room name: ");
			String str = null;
			try {
				str = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			if ( mh.deleteRoom( str ) )
				System.out.println("Room successfully deleted");
			else
				System.out.println("Bad room name");
			
		}
		else if( command.contentEquals("!add_actuator_to_room")) 
		{
			System.out.println("Insert room name: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Insert actuator URI: ");
			String uri = null;
			try {
				uri = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			// TODO TEST
			if( mh.addActuatorToRoom(name, uri) )
				System.out.println("Actuator successfully added");
			else
				System.out.println("Wrong room's name or actuator URI, pleasy retry");
			
		}
		else if( command.contentEquals("!add_sensor_to_room")) 
		{
			System.out.println("Insert room name: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Insert sensor URI: ");
			String uri = null;
			try {
				uri = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			if ( mh.addSensorToRoom(name, uri) )
				System.out.println("Sensor successfully added");
			else
				System.out.println("Wrong room's name or sensor URI, pleasy retry");
		}
		else if( command.contentEquals("!remove_actuator_to_room")) 
		{
			System.out.println("Insert room name: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Insert actuator URI: ");
			String uri = null;
			try {
				uri = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			mh.removeActuatorToRoom(name, uri);
			
		}
		else if( command.contentEquals("!remove_sensor_to_room")) 
		{
			System.out.println("Insert room name: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Insert sensor URI: ");
			String uri = null;
			try {
				uri = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			mh.removeSensorToRoom(name, uri);
			
		}
		// TODO PRINTARE MEGLIO
		else if(  command.contentEquals("!show_sensors_list") )
		{
			List<String> ar = mh.getDevicesList( "" );
			for(String s : ar)
				System.out.println(s);
		}
		else if(  command.contentEquals("!show_actuators_list") )
		{
			List<String> ar = mh.getDevicesList( "actuator" );
			for(String s : ar)
				System.out.println(s);		
		}
		else if (command.contentEquals("!set_temperature"))
		{
			System.out.println("Insert room name: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Insert desired temperature: ");
			String temp = null;
			Boolean goodInput = new Boolean(true);
			do{
				Float t = null;
				try {
					temp = new String ( myInput.readLine() );
					t  = new Float( Float.parseFloat(temp) );
				} catch (Exception e) {
					goodInput = false;	
				}
				
				if( !goodInput || t < minTemperature || t > maxTemperature || temp.length() > 5 ){
					System.out.println("Formato temperatura non ammesso.\nInserire un valore tra " + minTemperature + " e " + maxTemperature);
					System.out.println("Con precisione non maggiore di 0,01");
					goodInput = false;
				}else{
					goodInput = true;
				}
			}while(!goodInput);
			
			String sens_str = mh.getADevice(name, "");
			String act_str = mh.getADevice(name, "actuator");
			
			if(	sens_str == null || act_str == null || 
				sens_str.isEmpty() || act_str.isEmpty()){ 
				System.out.println("The room has not enought devices to control temperature");
				System.out.println("Check that the room has at least 1 actuator and 1 sensor");
			}else 
			{
				MyCoapClient mcc = new MyCoapClient();
				mcc.setTemperature( act_str, name, temp);
			}
		} else if( command.contentEquals("!show_sensor_measurements"))
		{
			System.out.println("Insert sensor: ");
			String name = null;
			try {
				name = new String ( myInput.readLine() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			MongoCursor<Document> cur = mh.getSensorMeasurements( name );
			if( cur != null && cur.hasNext() ) {
				List<Document> docs = (ArrayList<Document>)cur.next().get("e");
				
				System.out.println( "Num: " + docs.size() );
				
				for( int i = docs.size()-1; i >= docs.size() - 20 && i > 0; i-- ) {
					System.out.println(docs.get(i).toJson());
				}
			}else {
				System.out.println("Bad sensor name");
			}
		}
		else
			help();
		
		
		System.out.println("*-------------------------------*");
	}	
	public static void main(String arg[]) {
		System.out.println("++++++++++++++++ MY WINERY CLIENT START +++++++++++++++++++++ \n\n\n\n");
		
		Main mc = new Main();
		mc.help();
		
		while( true ) {
			System.out.println("Insert a command:");
		
			String command = mc.getCommand();			
			mc.parseCommands(command);
		}
		
	}	
}

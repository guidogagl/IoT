#include "coap_server_led.h"

void client_chunk_handler(coap_message_t *response){
	if(response == NULL) {
		puts("Request timed out");
		return;
	}
	const uint8_t *chunk;

	if( coap_get_payload(response, &chunk) > 0 ){
		if(!registered)
			registered = true;
		char * buff = (char*)chunk;
		printf("%s\n", buff);
	}
	
}
void sensor_chunk_handler(coap_message_t *response){
	if(response == NULL) {
		puts("Request timed out");
		return;
	}

	printf("Sensor answer to post request\n");	
}
void lookup_chunk_handler(coap_message_t *response){
	if(response == NULL) {
		puts("Request timed out");
		return;
	}
	printf("LookUp answer to post request\n");	

	if( response->code == BAD_REQUEST ){
		printf("LookUp interface sends BAD_REQUEST\n");
		process_post(&scheduler, PROCESS_EVENT_EXIT, NULL);
	}
}

void free_sensor_list(){
    static int i;
    for(i = 0; i < sensor_number; i++){
		free( address_list[i] );
	}
    sensor_number = 0;
	stream_sensors = -1;
}

PROCESS_THREAD(scheduler, ev, data)
{
	PROCESS_BEGIN();
	while(1)
	{
		PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_INIT );
		LOG_DBG("Starting collect_sensor process\n");
		process_start( &collect_sensors_from_lookup, data );
		
		PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_CONTINUE);
		LOG_DBG("Starting temperature_posting process\n");
		process_start( &temperature_posting, data);
		
		PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_MSG || ev == PROCESS_EVENT_EXIT);

		if( ev == PROCESS_EVENT_EXIT){
			printf( "LookUp Interface bad respond, killing the scheduled processes\n");
			process_post( &temperature_posting, PROCESS_EVENT_EXIT, NULL);
			continue;
		}

		LOG_DBG("Starting closed_loop process\n");
		process_start( &closed_loop_simulation, NULL);
		
		PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_EXIT);
	
		free_sensor_list();
		LOG_DBG("Memory free, process finished\n");
	}
	PROCESS_END();
}

PROCESS_THREAD(contiki_ng_coap_actuator, ev, data)
{	
	PROCESS_BEGIN();

	LOG_INFO("Starting Erbium Actuator Server\n");
	coap_activate_resource(&res_leds, "actuator");
	coap_activate_resource(&res_sensors, "mysensors");

	process_start( &proxy_server_registration, NULL );
	process_start( &scheduler, NULL);

	PROCESS_PAUSE();

	while(1){
		PROCESS_YIELD();
	}

	PROCESS_END();
}

PROCESS_THREAD(proxy_server_registration, ev, data)
{	
    PROCESS_BEGIN();
	static coap_endpoint_t server_ep;
	if ( coap_endpoint_parse( SERVER_ADDRESS, strlen(SERVER_ADDRESS) , &server_ep ) == 0 )
		printf("Bad server endpoint address \n");
	else
	{
		while(!registered){
			printf("Sending registration request to the server \n");
			static coap_message_t request;
			coap_init_message(&request, COAP_TYPE_CON, COAP_POST, 0);
			coap_set_header_uri_path(&request, server_service);
			const uint8_t msg1 = 1;
			coap_set_payload(&request, &msg1, sizeof(msg1) );
			COAP_BLOCKING_REQUEST(&server_ep, &request, client_chunk_handler);
		}

		printf("Registration success!\n"); 
	}		
	PROCESS_END();

}

PROCESS_THREAD(collect_sensors_from_lookup, ev, data)
{	
    PROCESS_BEGIN();

	printf("+++++++++++++++ Start getting sensors from LookUp interface +++++++++++\n");
	static coap_endpoint_t server_ep;
	
	if ( coap_endpoint_parse( LOOKUP_ADDRESS, strlen(LOOKUP_ADDRESS) , &server_ep ) == 0 )
		printf("Bad server endpoint address \n");
	else{
		static char query[100];  
		sprintf(query, "?room=%s", (const char*)data);    

		printf("Sending get query to lookup interface:\n%s\n", query);

		static coap_message_t request;
		coap_init_message(&request, COAP_TYPE_CON, COAP_GET, 0);
		coap_set_header_uri_path(&request, lookup_service);
		coap_set_header_uri_query(&request, query);		
		
		COAP_BLOCKING_REQUEST(&server_ep, &request, lookup_chunk_handler);
	}

	LOG_DBG("Collect sensors process exited\n");
    PROCESS_END();
}

PROCESS_THREAD(temperature_posting, ev, data){
	PROCESS_BEGIN();
	static char query[50];
	{
		char temperature[32];
		memset(temperature, 0, 32); 
		memcpy(temperature, (char*)data, strlen((char*)data)+1);
		sprintf(query, "temperature=%s", (char*)data);
		
	}
	
	printf("Stored post query to sensors:\n%s\n", query);

	// block until the sensors are collected
	PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_INIT || ev == PROCESS_EVENT_EXIT );
	
	if( ev == PROCESS_EVENT_INIT)
	{
		static int i;
		for(i = 0; i < sensor_number; i++){
			printf("Posting new temperature to sensor\n");
			static coap_message_t request1;
			coap_init_message(&request1, COAP_TYPE_CON, COAP_POST, 0);
			coap_set_header_uri_path(&request1, service_url);
			coap_set_payload(&request1, query, strlen(query));	
			
			static coap_endpoint_t ep;
			if( coap_endpoint_parse(address_list[i], strlen(address_list[i]), &ep ) == 0 ){
				printf("Impossibile to parse the endpoint, temperature posting aborted\n");
				continue;
			}

			// could be in competition for the first sensor
			if( i == 0 )
				PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_CONTINUE );

			printf("Sending new temperature to sensor: %s\n", address_list[i]);
			COAP_BLOCKING_REQUEST( &ep, &request1, sensor_chunk_handler);
		}

		printf("Waiting that cloosed loop finish\n");
		// block until the closed loop simulation end
		PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_EXIT );
		process_post(&scheduler, PROCESS_EVENT_EXIT, NULL);
	}
	PROCESS_END();
}

PROCESS_THREAD(closed_loop_simulation, ev, data)
{	
    PROCESS_BEGIN();
	
	// wake up the process that send the new temperature
	process_post(&temperature_posting, PROCESS_EVENT_INIT, NULL);

	printf("++++++++++++++++ Starting actuator mechanism ++++++++++++++++++++++++++\n");

    static struct etimer et;
	printf("Switch led ON\n");
    leds_on(LEDS_NUM_TO_MASK(LED));			
    
    etimer_set(&et, 10 * CLOCK_SECOND );
    static int i;
    for(i=0; i <= 4; i++){
        PROCESS_WAIT_UNTIL( etimer_expired(&et) );
        printf("Sending request to sensor \n");
		static coap_message_t request1;
        coap_init_message( &request1, COAP_TYPE_CON, COAP_GET, 0);
        coap_set_header_uri_path(&request1, service_url);

        static const uint8_t msg = 1;

        coap_set_payload(&request1, &msg, sizeof(msg) );

		static coap_endpoint_t ep;
		if( coap_endpoint_parse(address_list[0], strlen(address_list[0]), &ep ) == 0 ){
			printf("Impossibile to parse the endpoint, closed loop aborted\n");
			break;
		}

        COAP_BLOCKING_REQUEST(&ep, &request1, client_chunk_handler);
		
		if( i == 0 )
			process_post( &temperature_posting, PROCESS_EVENT_CONTINUE, NULL);

        etimer_reset(&et);
    }

	printf("Switch led OFF\n");
    // spengo il led
    leds_off(LEDS_NUM_TO_MASK(LED));	    

	// post finish to temperature posting
	process_post(&temperature_posting, PROCESS_EVENT_EXIT, NULL);
    PROCESS_END();
}
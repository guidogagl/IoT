#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "sys/etimer.h"
#include "coap-blocking-api.h"
/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP
/*
 * Resources to be activated need to be imported through the extern keyword.
 * The build system automatically compiles the resources in the corresponding sub-directory.
 */

#define SERVER_ADDRESS "coap://[fd00::1]"
char * server_service = "/register";

bool registered = false;
char temperature[32];

void client_chunk_handler(coap_message_t *response){
	const uint8_t *chunk;

	if(response == NULL) {
		puts("Request timed out");
		return;
	}
	
	if(!registered)
		registered = true;

	coap_get_payload(response, &chunk);
	
	char * buff = (char*)chunk;

	printf("%s\n", buff);
}

extern coap_resource_t res_obs;
static struct etimer e_timer;

char *buff = NULL;
static bool yet_measured = false;

PROCESS(er_example_server, "Erbium Example Server");
AUTOSTART_PROCESSES(&er_example_server);

PROCESS_THREAD(er_example_server, ev, data)
{
	static coap_endpoint_t server_ep;
  	static coap_message_t request[1];
	PROCESS_BEGIN();
	
	strcpy(temperature, "25");

	LOG_INFO("Starting Registration Process\n");
	if ( coap_endpoint_parse( SERVER_ADDRESS, strlen(SERVER_ADDRESS) , &server_ep ) == 0){
		printf("Bad server endpoint address \n");
		exit(1);
	}
	while(!registered){
		printf("Sending registration request to the server \n");

		coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
		coap_set_header_uri_path(request, server_service);
		const uint8_t msg1 = 1;
		coap_set_payload(request, &msg1, sizeof(msg1) );
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	}

	printf("Registration success!\n");

	LOG_INFO("Starting Erbium Example Server\n");
	coap_activate_resource(&res_obs, "sensor");

	etimer_set(&e_timer, CLOCK_SECOND * 10);

	printf("Loop\n");

	while (1)
	{
		PROCESS_WAIT_EVENT_UNTIL( etimer_expired( &e_timer) );
		
		if( yet_measured ){
			yet_measured = false;
			free(buff);
			buff = NULL;
		}
		printf("Measuring: %s °C\n", temperature);	
		{
			static char b[300];
			sprintf(b, "{\"n\": \"temperature\", \"u\": \"°C\", \"v\": \"%s\"}", temperature);

			buff = malloc(strlen(b) + 1);
			memcpy(buff, b, strlen(b) + 1);				
		}

		// sending temperature to observers
		res_obs.trigger();
			
		yet_measured = true;
		
		etimer_set(&e_timer, CLOCK_SECOND * 10);
	}

	PROCESS_END();
}

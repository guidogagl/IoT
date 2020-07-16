#include "res-leds.h"

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{	// dummy get handler
	printf("Received get request\n");
}


static void
res_post_handler(coap_message_t *request, 
				coap_message_t *response, 
				uint8_t *buffer, 
				uint16_t preferred_size, 
				int32_t *offset){
	
	static size_t len= 0;
	static char room[15];
	static char temp[32];

	memset(room, 0, 15);
	memset(temp, 0, 32);
	
	{
		const char *text = NULL;
		len = coap_get_post_variable(request, "room", &text);
		if( len <= 0 || len > 15 ){
			LOG_DBG("BAD QUERY OPTIONS\n");
			return;
		}
		memcpy(room, text, len);
	}
	{
		const char *text = NULL;
		len = coap_get_post_variable(request, "temperature", &text);
		if( len <= 0 || len > 32 ){
			LOG_DBG("BAD QUERY OPTIONS\n");
			return;
		}
		memcpy(temp, text, len);
	}
	printf("Received request to set temperature to %s in room %s\n", temp, room);


	LOG_DBG("Starting lookup process\n");
	process_post( &scheduler, PROCESS_EVENT_INIT, room );
	process_post( &scheduler, PROCESS_EVENT_CONTINUE, temp);
}



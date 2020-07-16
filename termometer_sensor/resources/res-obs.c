#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

extern char temperature[32];
extern char* buff;
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void
res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/*
 * A handler function named [resource name]_handler must be implemented for each RESOURCE.
 * A buffer for the response payload is provided through the buffer pointer. Simple resources can ignore
 * preferred_size and offset, but must respect the REST_MAX_CHUNK_SIZE limit for the buffer.
 * If a smaller block size is requested for CoAP, the REST framework automatically splits the data.
 */

EVENT_RESOURCE(res_obs,
				 "title=\"Thermometer Sensor\";rt=\"Text\"",
				 res_get_handler,
				 res_post_handler,
				 NULL,
				 NULL, 
		 		res_event_handler);



static void
res_event_handler(void)
{
	coap_notify_observers(&res_obs);
}

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{	
	if( buff == NULL )
		return;
	
	char* b = (char*)buffer;
	
	sprintf(b, "{\"e\": [%s] }", buff);
	int length = strlen(b) + 1;

	coap_set_header_content_format(response, TEXT_PLAIN);
	coap_set_payload(response, buffer, length);

	printf("Sending buffer to server: \n%s\n", (char*)buffer);
}

static void
res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	static size_t len= 0;
	static char temp[32];
	memset(temp, 0, 32);
	{
		const char *text = NULL;
		len = coap_get_post_variable(request, "temperature", &text);
		if( len <= 0 || len > 32 ){
			LOG_DBG("BAD QUERY OPTIONS\n");
			return;
		}
		memcpy(temp, text, len);
	}
	
	printf("Received request to set temperature to %s\n", temp);
	memcpy(temperature, temp, strlen(temp)+1);
}


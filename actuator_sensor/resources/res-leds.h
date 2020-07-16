#include "inclusion.h"

// EXTERN
extern struct process scheduler;


// FUNCTION
static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


// RESOURCE
RESOURCE(res_leds,
		 "title=\"Actuator: ?temperature = float ?sensor = coap_address, POST mode\";rt=\"Control\"",
		 res_get_handler, // GET (dummy)
		 res_post_handler, // POST
		 NULL, // PUT 
		 NULL); // DELETE

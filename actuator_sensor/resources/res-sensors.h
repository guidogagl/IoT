#include "inclusion.h"

// CONSTANT
#define MAX_SENSOR_NUMBER 10

// EXTERN
extern int sensor_number;
extern char * address_list[MAX_SENSOR_NUMBER];
extern int stream_sensors;

extern struct process scheduler;

// FUNCTION
static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

/* A simple actuator example, depending on the color query parameter and post variable mode, corresponding led is activated or deactivated */
RESOURCE(res_sensors,
          "title=\"MySensors ",
          NULL,
          res_post_handler,
          NULL,
          NULL);
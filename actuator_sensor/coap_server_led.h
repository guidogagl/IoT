// inclusion
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include "contiki-net.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

// CONSTANT
#define LOOKUP_ADDRESS "coap://[fd00::1]:5673"
#define SERVER_ADDRESS "coap://[fd00::1]"
#define LED 0
#define MAX_SENSOR_NUMBER 10

#define server_service "/register"
#define lookup_service  "/lookup"
#define service_url  "/sensor"

#define BAD_REQUEST 128
//STATIC
static bool registered = false;

// EXTERN
extern coap_resource_t res_leds;
extern coap_resource_t res_sensors;

// GLOBAL
char * address_list[ MAX_SENSOR_NUMBER];
int sensor_number = 0;
int stream_sensors = -1;

// FUNCTIONS
void client_chunk_handler(coap_message_t *response);
void sensor_chunk_handler(coap_message_t *response);
void client_lookup_handler(coap_message_t *response);
void free_sensor_list();


// PROCESSES

PROCESS(contiki_ng_coap_actuator, "Coap Actuator");
PROCESS(proxy_server_registration, "proxy_server_registration");
PROCESS(collect_sensors_from_lookup, "collect_sensors_from_lookup");
PROCESS(closed_loop_simulation, "closed_loop_simulation");
PROCESS(temperature_posting, "temperature_posting");
PROCESS(scheduler, "scheduler");

AUTOSTART_PROCESSES(&contiki_ng_coap_actuator);


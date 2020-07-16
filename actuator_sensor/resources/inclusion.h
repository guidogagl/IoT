#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki-net.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG
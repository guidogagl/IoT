#include "res-sensors.h"

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
    if( stream_sensors == sensor_number || sensor_number >= MAX_SENSOR_NUMBER) // no other request accepted
        return;

    int temp_num = -1;
    {
        static size_t len= 0;
        const char *text = NULL;
		char temp[10];
        len = coap_get_post_variable(request, "num", &text);
		if( len <= 0 || len > 4 ){
			LOG_DBG("BAD QUERY OPTIONS\n");
			return;
		}
		memcpy(temp, text, len);
        temp_num = atoi( temp );
	}

	if(stream_sensors != -1 && stream_sensors != temp_num)
        return; // incongruent request

    stream_sensors = temp_num;
    
    static char sensor[50];
    {
        static size_t len= 0;
        const char *text = NULL;
		len = coap_get_post_variable(request, "sensor", &text);
		if( len <= 0 || len >= 50 ){
			LOG_DBG("BAD QUERY OPTIONS\n");
			return;
		}
		memcpy(sensor, text, len);
    }

    for(int i = 0; i < sensor_number; i++)
        if( strcmp( sensor, address_list[i] ) == 0 ) 
            return; // sensor already memorized
    coap_endpoint_t sensor_ep;
    if ( coap_endpoint_parse( sensor, strlen(sensor) , &sensor_ep ) == 0){
        printf("Bad sensor endpoint address \n");
        return;
    }
    // allocazione stringa nel vettore
    address_list[sensor_number] = malloc( strlen(sensor) + 1);
    memcpy( address_list[sensor_number], sensor, strlen(sensor) + 1);
    sensor_number++;
    
    printf("Collected sensor %s of the %d from LookUp interface\n", address_list[ sensor_number - 1], stream_sensors);
    if( stream_sensors == sensor_number || sensor_number >= MAX_SENSOR_NUMBER )
        process_post( &scheduler, PROCESS_EVENT_MSG, NULL );

}

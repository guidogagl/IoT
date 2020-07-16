# IoT
Project for the exam of IoT of University of Pisa LM in Artificial Intelligence and Data Engeneering developed by student Guido Gagliardi.

## Installation requirements
--> MongoDb installed

--> Python 2.7 or higher

## Installation instruction

Open a shell and copy this instructions:
```shell
$ cd ~/contiki-ng/
$ mkdir project
$ cd project
$ git clone 
```

## Application filesystem

* border-router-rpl - is the standard border router provided by contiki in the examples
* termometer_sensor - contains the file of the sensor node that provides the observable resources to the cloud
* actuator_sensor - contains the file of the sensor node that emulate a temperature actuator 
* proxy-server/iot.client/src/main/java/iot/ - contains the Java-Californium packages that emulate the cloud application
** ./app - contains the package needed by user to interact with the LLN providing a CLI
** ./db - contains the class needed by the other packages to interact with mongodb
** ./server - contains the packages offering Coap Resources to interact with the LLN


## How to run the application

To run the application open 3 unix shell end the cooja simulation enviroment inside the contikier. 

In cooja open the simulation file stored in ~/contiki-ng/project/simulation.csc and compile all the motes that the system requires.

Now open the first shell and run: 
```shell
$ cd ~/contiki-ng/project
$ sh setup.sh
```

This script will start a tiny python program that will clean your mongoDb from old data and the cooja script for binding the Border Router.

Now in the second and third shell you can run separately this two scripts:
```shell
$ cd ~/contiki-ng/project
$ sh run_server_reg.sh
```
```shell
$ cd ~/contiki-ng/project
$ sh run_server_lookup.sh
```
This two scripts will start 2 different Coap servers offering the resources /register and /lookup.

Now you can start the simulation ( please check the speed limit is set at 100% before ).

To interact with the application follow the instructions shown after running in another shell the script:
```shell
$ cd ~/contiki-ng/project
$ sh run_client.sh
```

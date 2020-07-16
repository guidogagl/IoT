service mongod restart
service mongod status
python ~/contiki-ng/project/proxy-server/iot.client/clear_mongo.py
cd ~/contiki-ng/project/rpl-border-router/
make TARGET=cooja connect-router-cooja

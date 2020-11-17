pid $= rabbitmq-server &

sleep 5 # wait for the server to start
rabbitmqctl start_app
rabbitmqctl add_user guest guest
rabbitmqctl add_vhost /judge-girl
rabbitmqctl set_permissions --vhost "/judge-girl" "judge-girl-service" ".*" ".*" ".*"



#!/bin/bash

# ./gradlew runDrone1

echo "1. Creating a new shipping (ship-1)..."
curl -s -X POST http://localhost:9001/api/shippings \
     -d '{"shippingId":"ship-1","x":10.5,"y":20.5,"timeLeft":5}' \
     -H "Content-Type: application/json"
echo -e "\n"

sleep 1

echo "2. Subscribing to WebSocket..."
(echo '{"shippingId": "ship-1"}'; sleep 10) | websocat ws://localhost:9001/api/events > drone_events.log 2>&1 &

sleep 1

echo "3. Starting the shipping..."
curl -s -X POST http://localhost:9001/api/shippings/ship-1/start
echo -e "\n"

echo "4. Waiting for drone to reach target..."
sleep 10

echo "WebSocket Events Captured:"
cat drone_events.log
rm drone_events.log

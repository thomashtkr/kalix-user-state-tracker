# Kalix demo project
https://www.kalix.io/

## this sample application has 3 modules:

- business-event-model
- business-event-simulation
- user-state-kalix-demo

## business-event-model
This library contains the definition of business-events. These events are about Users and represent status-changes of users. 
A User has a certain 'status', and can move from one status to another. Those changes are encode as `UserStatusMovement`.

## business-event-simulation
This is an application that generates a number of random `UserStatusMovement` events and publishes them onto a topic `user-events`.
The service has an api which triggers the creation of 10 random `UserStatusMovements` for a given number of users:
```
curl -v -X POST http://<servicelocation>/api/simulate/users/10
```
The response contains a list of userIds that has been created.

## user-state-kalix-demo
This service subscribes to the `user-events` topic and creates aggregating overviews about the users and their state.
The following views are implemented:

### users-status per period 
```
curl -v http://<servicelocation>/view/counters/<periodName>/<periodId>
```
(ex. http://localhost:9001/view/counters/peryear/2023)

The following periodNames have been implemented:
- peryear (periodId example: 2023)
- permonth (periodId example: 2023M3)
- perquarter (periodId example: 2023Q3)

# run locally

When running locally, the proxy for the simulation-application is running on port 9000. The views are available on port 9001.

```shell

docker-compose up

cd user-state-kalix-demo
mvn kalix:run

curl -v -X POST  http://localhost:9000/api/simulate/users/10

#single level views
curl -v http://localhost:9001/view/single/counters/peryear
curl -v http://localhost:9001/view/single/counters/peryear/2023
curl -v http://localhost:9001/view/single/counters/permonth
curl -v http://localhost:9001/view/single/counters/perquarter
curl -v http://localhost:9001/view/single/counters/peragegroup

#dual level view
curl -v http://localhost:9001/view/dual/counters/pergender/percountry
curl -v http://localhost:9001/view/dual/counters/pergender/percountry/M_BE


```
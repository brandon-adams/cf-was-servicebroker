# WAS Service Broker

Cloud Foundry service broker for Websphere Application Servers.

`WORK IN PROGRESS NOT READY, FEW BUGS, NEEDS BETTER DOCUMENTATION AND REFACTORING. PULL REQUESTS WELCOME`	

This simple service broker is meant to demonstrate how to create a service broker that manages a service not managed by BOSH with cloudfoundry.

##Setting up

This is a [spring-boot](http://projects.spring.io/spring-boot/) application and it follows many of its principles.


The config file is located at `src/main/resources/config/application.properties`



When the application starts it will create the first ServiceDefinition based on a template found at `src/main/resources/ServiceDescription.json`, you can 
adjust to your preferences there.

## Extensions to CF Service Broker API

Besides the endpoints defined on [Service Broker API](http://docs.cloudfoundry.org/services/api.html) the broker adds a few more to allow creation of services and plans:

`POST /v2/catalog/services` - Creates a new ServiceDefinition

The payload is the same as the one found on the [Service Broker API](http://docs.cloudfoundry.org/services/api.html)

```
{
name: "Websphere"
description: "Websphere Application Server Service"
bindable: true
plans: [0]
tags: [3]
0:  "websphere"
1:  "app server"
2:  "POC"
-
metadata: {
longDescription: "A POC service broker for a Websphere Application Server. Connects to the server and creates deployment managers (services), and managed nodes (bindings)"
}
```

`DELETE /v2/catalog/services/{serviceDefinitionId}` - Removes a service definition. Throws an error if there's plans associated with it

`POST /v2/catalog/services/{serviceDefinitionId}/plans` - Adds a new plan

`DELETE /v2/catalog/services/{serviceDefinitionId}/plans/{planId}` - Removes a new plan. Throws an error if there's instances using this plan

##How does it work
 

### Creating a plan

When you create a plan, the broker will bind the template with the plan JSON object that you used to create the plan. For example:

```

{
"name" : "dev",
"description" : "development plan",
"metadata" : {
"max_size" : "250M",
"connections" : 5,
"bullets" : ["250 megabytes of space","5 simultaneous connections"]
}
}


### Creating an instance


### Binding an instance


## Installing




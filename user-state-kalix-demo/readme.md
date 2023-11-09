# user-state-kalix-demo

To understand the Kalix concepts that are the basis for this example, see [Designing services](https://docs.kalix.io/java/development-process.html) in the documentation.

This project contains the framework to create a Kalix service. To understand more about these components, see [Developing services](https://docs.kalix.io/services/) and check Spring-SDK [official documentation](https://docs.kalix.io/spring/index.html). Examples can be found [here](https://github.com/lightbend/kalix-jvm-sdk/tree/main/samples) in the folders with "spring" in their name.

Use Maven to build your project:

```shell
mvn compile
```

When running a Kalix service locally, we need to have its companion Kalix Proxy running alongside it.

To start your service locally, run:

```shell
mvn kalix:runAll
```

This command will start your Kalix service and a companion Kalix Proxy as configured in [docker-compose.yml](./docker-compose.yml) file.

With both the proxy and your service running, once you have defined endpoints they should be available at `http://localhost:9000`.


To deploy your service, install the `kalix` CLI as documented in
[Setting up a local development environment](https://docs.kalix.io/setting-up/)
and configure a Docker Registry to upload your docker image to.

You will need to update the `dockerImage` property in the `pom.xml` and refer to
[Configuring registries](https://docs.kalix.io/projects/container-registries.html)
for more information on how to make your docker image available to Kalix.

Finally, you can use the [Kalix Console](https://console.kalix.io)
to create a project and then deploy your service into the project either by using `mvn deploy kalix:deploy` which
will conveniently package, publish your docker image, and deploy your service to Kalix, or by first packaging and
publishing the docker image through `mvn deploy` and then deploying the image
through the `kalix` CLI.

# run with podman
To run the application locally with podman, start the docker-compose, and then use `mvn kalix:run`

# run integrationtests with podman

cfr. https://java.testcontainers.org/supported_docker_environment/#podman
```shell
export DOCKER_HOST=unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
```
example:
```shell
DOCKER_HOST=/Users/thomashoutekier/.local/share/containers/podman/machine/qemu/podman.sock
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

To publish the application-image to a container-registry:
```shell
docker login docker.io/thomashtkr
podman push thomashtkr-kalix/user-state-tracker:latest docker://docker.io/thomashtkr/kalix-user-state-tracker:0.0.1-SNAPSHOT
```

To deploy the service:
```shell
#kalix service deploy event-simulation docker.io/thomashtkr/kalix-user-state-tracker:0.0.1-SNAPSHOT
kalix service apply -f deploy/service.yaml
#kalix service expose user-state-tracker
```

To call the endpoint locally:
```shell
 curl -X GET  http://localhost:9001/view/counters/peryear/2023
```



To call the endpoint of the deployed service (port-forwarding):
```shell
kalix service proxy event-simulation --port 8888 --bind-address 0.0.0.0
curl -X POST  http://localhost:8888/view/counters/peryear/2023
```
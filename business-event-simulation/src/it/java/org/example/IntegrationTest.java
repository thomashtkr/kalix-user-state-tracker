package org.example;

import be.htkr.jnj.kalix.demo.event.simulation.Main;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Java SDK.
 *
 * This test will initiate a Kalix Proxy using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {

  @Autowired
  private WebClient webClient;

  @Test
  public void testSimulateUser() throws Exception {
    int numberOfUsers = 5;
    var createdIds = webClient.post().uri("/api/simulate/users/" + numberOfUsers).retrieve().bodyToMono(List.class).block();
    Assertions.assertThat(createdIds).hasSize(numberOfUsers);

    createdIds.forEach(id -> {
      var name = webClient.get()
              .uri(uriBuilder -> uriBuilder.path("/user/{id}/name")
                      .build(id))
              .retrieve().bodyToMono(String.class).block();
      System.out.println(name);
      Assertions.assertThat(name).contains(id.toString());
      Assertions.assertThat(name).contains("_name");
    });

    Thread.sleep(5000);
  }
}
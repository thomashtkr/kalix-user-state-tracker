package be.htkr.jnj.kalix.demo.entity.user;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

@TypeName("user-status-movement")
public record UserEntityStatusMovementEvent(String userId, UserState.Status status, Integer movement, Instant timestamp, UserDemographic demographic) {
}

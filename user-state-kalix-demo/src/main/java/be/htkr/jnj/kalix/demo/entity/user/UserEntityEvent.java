package be.htkr.jnj.kalix.demo.entity.user;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

@TypeName("user-status-movement")
public sealed interface UserEntityEvent {

    String userId();
    UserState.Status status();
    Integer movement();
    Instant timestamp();
    UserDemographic demographic();


     record UserStatusMovementEvent(String userId, UserState.Status status, Integer movement, Instant timestamp, UserDemographic demographic) implements UserEntityEvent {}
}

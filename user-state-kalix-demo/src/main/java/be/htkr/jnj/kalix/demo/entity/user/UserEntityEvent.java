package be.htkr.jnj.kalix.demo.entity.user;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;
import java.util.Optional;

@TypeName("user-status-movement")
public sealed interface UserEntityEvent {

    String userId();
    UserState.Status status();
    Integer movement();
    Instant timestamp();
    UserDemographic demographic();

    Optional<UserDemographic.AgeGroup> getAgeGroup();


     record UserStatusMovementEvent(String userId, UserState.Status status, Integer movement, Instant timestamp, UserDemographic demographic) implements UserEntityEvent {
         public Optional<UserDemographic.AgeGroup> getAgeGroup() {
             return Optional.ofNullable(demographic()).map(UserDemographic::ageGroup);

         }
     }
}

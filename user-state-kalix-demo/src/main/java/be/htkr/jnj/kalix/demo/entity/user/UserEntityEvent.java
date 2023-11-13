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

    UserDemographic.AgeGroup getAgeGroup();


     record UserStatusMovementEvent(String userId, UserState.Status status, Integer movement, Instant timestamp, UserDemographic demographic) implements UserEntityEvent {
         public UserDemographic.AgeGroup getAgeGroup() {
             return demographic() == null ? null : demographic().ageGroup();
         }
     }
}

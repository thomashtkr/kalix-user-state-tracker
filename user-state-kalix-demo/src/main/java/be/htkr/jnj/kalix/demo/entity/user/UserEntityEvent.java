package be.htkr.jnj.kalix.demo.entity.user;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;
import java.util.Optional;

@TypeName("user-status-movement")
public sealed interface UserEntityEvent {

    String userId();
    Integer movement();
    Instant timestamp();
    UserDemographic demographic();

    default Optional<AgeGroup> getAgeGroup() {
        return Optional.ofNullable(demographic()).map(UserDemographic::ageGroup);
    }


     record UserStatusMovementEvent(String userId, UserState.Status status, Integer movement, Instant timestamp, UserDemographic demographic) implements UserEntityEvent {
     }

     record UserAgeGroupMovementEvent(String userId, Integer movement, Instant timestamp, UserDemographic demographic, AgeGroup ageGroup) implements UserEntityEvent {
     }
}

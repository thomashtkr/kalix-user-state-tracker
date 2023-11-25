package be.htkr.jnj.kalix.demo.entity.user.events;

import be.htkr.jnj.kalix.demo.entity.user.AgeGroup;
import be.htkr.jnj.kalix.demo.entity.user.UserDemographic;
import be.htkr.jnj.kalix.demo.entity.user.UserState;

import java.time.Instant;
import java.util.Optional;

public interface UserEntityEvent {

    String userId();
    Integer movement();
    Instant timestamp();

    UserDemographic demographic();
    UserState.Status status();
    default Optional<AgeGroup> getAgeGroup() {
        return Optional.ofNullable(demographic()).map(UserDemographic::ageGroup);
    }

}






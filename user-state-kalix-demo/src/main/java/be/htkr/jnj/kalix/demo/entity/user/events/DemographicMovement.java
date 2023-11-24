package be.htkr.jnj.kalix.demo.entity.user.events;

import be.htkr.jnj.kalix.demo.entity.user.AgeGroup;
import be.htkr.jnj.kalix.demo.entity.user.UserDemographic;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import kalix.javasdk.annotations.TypeName;

import java.time.Instant;
import java.util.Optional;

public interface DemographicMovement extends UserEntityEvent {
    UserDemographic demographic();
    UserState.Status status();

    default Optional<AgeGroup> getAgeGroup() {
        return Optional.ofNullable(demographic()).map(UserDemographic::ageGroup);
    }

    @TypeName("user-demographic-increment")
    record DemographicIncrement(String userId, UserState.Status status, Instant timestamp, UserDemographic demographic) implements DemographicMovement {
        @Override
        public Integer movement() {
            return 1;
        }
    }

    @TypeName("user-demographic-decrement")
    record DemographicDecrement(String userId, UserState.Status status, Instant timestamp, UserDemographic demographic) implements DemographicMovement {
        @Override
        public Integer movement() {
            return -1;
        }
    }
}

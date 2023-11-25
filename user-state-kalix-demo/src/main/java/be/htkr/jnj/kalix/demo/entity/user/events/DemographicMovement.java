package be.htkr.jnj.kalix.demo.entity.user.events;

import be.htkr.jnj.kalix.demo.entity.user.UserDemographic;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

public interface DemographicMovement extends UserEntityEvent {


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

package be.htkr.jnj.kalix.demo.entity.user.events;

import be.htkr.jnj.kalix.demo.entity.user.UserState;
import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

public sealed interface UserStatusMovement extends UserEntityEvent {
    UserState.Status status();

    @TypeName("user-status-increment")
    record UserStatusIncrement(String userId, UserState.Status status, Instant timestamp) implements UserStatusMovement {
        @Override
        public Integer movement() {
            return 1;
        }
    }

    @TypeName("user-status-decrement")
    record UserStatusDecrement(String userId, UserState.Status status, Instant timestamp) implements UserStatusMovement {
        @Override
        public Integer movement() {
            return -1;
        }
    }
}

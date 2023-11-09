package be.htkr.jnj.kalix.demo.entity.statusperperiod;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

public record RegisterStatusMovementCommand(UserState.Status status, int movement) {
}

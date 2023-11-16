package be.htkr.jnj.kalix.demo.entity.singlelevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

public record RegisterStatusMovementCommand(UserState.Status status, int movement) {
}

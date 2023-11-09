package be.htkr.jnj.kalix.demo.events;

import java.time.Instant;

public record UserStatusMovement(String userId, String status, int movement, Instant timestamp) {
}

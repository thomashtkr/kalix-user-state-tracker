package be.htkr.jnj.kalix.demo.events.model;

import java.time.Instant;

public record UserDetails(String favoriteColor, String country, String gender, Instant birthDate) {
}

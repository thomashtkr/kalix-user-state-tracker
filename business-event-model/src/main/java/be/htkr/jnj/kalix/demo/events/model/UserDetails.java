package be.htkr.jnj.kalix.demo.events.model;

import java.time.LocalDate;

public record UserDetails(String favoriteColor, String country, String gender, LocalDate birthDate) {
}

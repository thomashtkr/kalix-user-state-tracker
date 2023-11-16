package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import java.time.LocalDate;
import java.util.UUID;

public record UserState(String id, String name, String email, String favoriteColor, UUID confirmedTandC, LocalDate birthDate) {
    public UserState registerUser(String id, String name) {
        return new UserState(id, name, email(), favoriteColor(), confirmedTandC(), birthDate());
    }

    public UserState completeProfile(String favoriteColor, LocalDate birthDate) {
        return new UserState(id(),name(), email(), favoriteColor, confirmedTandC(), birthDate);
    }

    public UserState confirmGdpr(UUID termsAndConditions) {
        return new UserState(id(), name(), email(), favoriteColor(), termsAndConditions, birthDate());
    }

    public UserState verifyEmail(String email) {
        return new UserState(id(), name(), email, favoriteColor(), confirmedTandC(), birthDate());
    }
}

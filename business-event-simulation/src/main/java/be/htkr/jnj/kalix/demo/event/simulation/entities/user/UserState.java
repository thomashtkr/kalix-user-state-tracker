package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import java.util.UUID;

public record UserState(String id, String name, String email, String favoriteColor, UUID confirmedTandC) {
    public UserState registerUser(String id, String name) {
        return new UserState(id, name, email(), favoriteColor(), confirmedTandC());
    }

    public UserState completeProfile(String favoriteColor) {
        return new UserState(id(),name(), email(), favoriteColor, confirmedTandC());
    }

    public UserState confirmGdpr(UUID termsAndConditions) {
        return new UserState(id(), name(), email(), favoriteColor(), termsAndConditions);
    }

    public UserState verifyEmail(String email) {
        return new UserState(id(), name(), email, favoriteColor(), confirmedTandC());
    }
}

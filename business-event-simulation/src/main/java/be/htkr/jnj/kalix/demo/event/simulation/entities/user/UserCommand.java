package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import java.util.UUID;

public sealed interface UserCommand {
    record RegisterUser(String name) implements UserCommand {}
    record StoreUserProfile(String email, String favoriteColor, String country, String gender) implements UserCommand {}
    record ConfirmGdpr(UUID termsAndConditionsId) implements UserCommand {}
    record VerifyEmail(String email) implements UserCommand {}

}

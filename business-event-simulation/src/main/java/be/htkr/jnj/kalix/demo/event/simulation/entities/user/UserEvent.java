package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;
import java.util.UUID;

public sealed interface UserEvent {

    @TypeName("user-registered")
    record UserRegistered(String userId, String name) implements UserEvent {}
    @TypeName("user-profile-completed")
    record ProfileCompleted(String userId, String favoriteColor, String country, String gender, Instant timestamp) implements UserEvent {}

    @TypeName("user-gdpr-confirmed")
    record GdprConfirmed(String userid, UUID termsAndCondition, Instant timestamp) implements UserEvent {}
    @TypeName("user-email-verified")
    record EmailVerified(String userId, String email, Instant timestamp) implements  UserEvent {}
}

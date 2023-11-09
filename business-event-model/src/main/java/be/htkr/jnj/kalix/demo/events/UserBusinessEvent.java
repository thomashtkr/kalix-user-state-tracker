package be.htkr.jnj.kalix.demo.events;

import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.UUID;


@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserBusinessEvent.UserRegistered.class, name = "registered"),
        @JsonSubTypes.Type(value = UserBusinessEvent.UserProfileCompleted.class, name = "profileCompleted"),
        @JsonSubTypes.Type(value = UserBusinessEvent.UserGdprConsentConfirmed.class, name = "gdprConsent"),
        @JsonSubTypes.Type(value = UserBusinessEvent.UserVerified.class, name = "verified")})
public sealed interface UserBusinessEvent {

    String userId();
    @JsonTypeName("registered")
    record UserRegistered(String userId, User user, Instant registeredAt) implements UserBusinessEvent {
    }
    @JsonTypeName("profileCompleted")
    record UserProfileCompleted(String userId, UserDetails details, Instant completedAt) implements UserBusinessEvent { }
    @JsonTypeName("gdprConsent")
    record UserGdprConsentConfirmed(String userId, Instant confirmedAt, UUID termsAndConditionsId) implements UserBusinessEvent {}
    @JsonTypeName("verified")
    record UserVerified(String userId, String email, Instant verifiedAt) implements UserBusinessEvent {}

}

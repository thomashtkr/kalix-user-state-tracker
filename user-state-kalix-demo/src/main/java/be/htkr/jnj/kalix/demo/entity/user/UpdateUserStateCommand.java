package be.htkr.jnj.kalix.demo.entity.user;

import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;

import java.util.Optional;

public record UpdateUserStateCommand(UserBusinessEvent event) {
    public Optional<UserDemographic> getDemographic() {
        if(event instanceof UserBusinessEvent.UserProfileCompleted completed) {
            return Optional.of(new UserDemographic(completed.details().favoriteColor(),
                            completed.details().country(),
                            completed.details().gender(),
                            completed.details().birthDate()));
        } else {
            return Optional.empty();
        }
    }
}

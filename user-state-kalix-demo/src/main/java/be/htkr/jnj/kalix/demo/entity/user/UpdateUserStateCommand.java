package be.htkr.jnj.kalix.demo.entity.user;

import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public record UpdateUserStateCommand(UserBusinessEvent event) {
    public Optional<UserDemographic> getDemographic() {
        if(event instanceof UserBusinessEvent.UserProfileCompleted completed) {
            var age = ChronoUnit.YEARS.between(completed.details().birthDate(), LocalDate.now());
            var ageGroup = AgeGroup.getAgeGroup(age);
            return Optional.of(new UserDemographic(completed.details().favoriteColor(),
                            completed.details().country(),
                            completed.details().gender(),
                            completed.details().birthDate(),
                            ageGroup));
        } else {
            return Optional.empty();
        }
    }
}

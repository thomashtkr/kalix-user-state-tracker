package be.htkr.jnj.kalix.demo.entity.user;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record UserState(Status previousStatus, Status currentStatus, Instant lastMovement, UserDemographic demographic) {
    public enum Status {
        REGISTERED,
        EMAIL_VERIFIED,
        PROFILE_COMPLETE,
        GDPR_CONFIRMED
    }

    public UserState updateStatus(Status newStatus, Instant timeStamp) {
        return new UserState(currentStatus(), newStatus, timeStamp, demographic());
    }

    public UserState updateDemographic(String favoriteColor, String country, String gender, LocalDate birthDate, UserDemographic.AgeGroup ageGroup) {

        return new UserState(previousStatus(), currentStatus(), lastMovement(), new UserDemographic(favoriteColor, country, gender, birthDate, ageGroup));
    }

}

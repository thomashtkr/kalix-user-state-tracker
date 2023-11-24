package be.htkr.jnj.kalix.demo.entity.user;

import java.time.Instant;

public record UserState(Status previousStatus, Status currentStatus, Instant lastMovement,UserDemographic previousDemographic, UserDemographic currentDemographic) {

    public UserState() {
        this(null, null, null, new UserDemographic(), new UserDemographic());
    }

    public enum Status {
        REGISTERED,
        EMAIL_VERIFIED,
        PROFILE_COMPLETE,
        GDPR_CONFIRMED
    }

    public UserState updateStatus(Status newStatus, Instant timeStamp) {
        return new UserState(currentStatus(), newStatus, timeStamp, previousDemographic(), currentDemographic());
    }

    public UserState updateDemographic(UserDemographic demographic) {
        return new UserState(previousStatus(), currentStatus(), lastMovement(), currentDemographic(), new UserDemographic(demographic.favoriteColor(), demographic.country(), demographic.gender(), demographic.birthDate(), demographic.ageGroup()));
    }
}

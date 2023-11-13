package be.htkr.jnj.kalix.demo.entity.user;

import java.time.LocalDate;

public record UserDemographic(String favoriteColor, String country, String gender, LocalDate birthDate, AgeGroup ageGroup) {

    public UserDemographic updateAgeGroup(AgeGroup ageGroup) {
        return new UserDemographic(favoriteColor(), country(), gender(), birthDate(), ageGroup);
    }
}

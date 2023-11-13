package be.htkr.jnj.kalix.demo.entity.user;

import java.time.LocalDate;

public record UserDemographic(String favoriteColor, String country, String gender, LocalDate birthDate, AgeGroup ageGroup) {

    enum AgeGroup {
        LESS_18,
        _19_25,
        _26_35,
        _36_50,
        _50_60,
        _60_PLUS;

        public static AgeGroup getAgeGroup(long age) {
            if(age < 18 ) {
                return LESS_18;
            }
            if (age < 26 ) {
                return _19_25;
            }
            if (age < 36) {
                return _26_35;
            }
            if (age < 51) {
                return _36_50;
            }
            if (age < 61) {
                return _50_60;
            }
            return _60_PLUS;
        }
    }
}

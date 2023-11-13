package be.htkr.jnj.kalix.demo.entity.user;

import java.time.LocalDate;

public record UserDemographic(String favoriteColor, String country, String gender, LocalDate birthDate, AgeGroup ageGroup) {

    public enum AgeGroup {
        MINUS_18("minus18"),
        _19_25("1925"),
        _26_35("2635"),
        _36_50("3650"),
        _50_60("5060"),
        _60_PLUS("60plus");

        public final String value;

        AgeGroup(String value) {
            this.value = value;
        }


        public static AgeGroup getAgeGroup(long age) {
            if(age < 18 ) {
                return MINUS_18;
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

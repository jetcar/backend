package com.example.oidc.util;

import java.time.LocalDate;

public class PersonalCodeHelper {

    public static LocalDate getDateOfBirth(String code) {
        if (code == null || code.length() < 11) {
            throw new IllegalArgumentException("Invalid code for extracting date of birth");
        }
        try {
            int year = Integer.parseInt(code.substring(1, 3));
            int month = Integer.parseInt(code.substring(3, 5));
            int day = Integer.parseInt(code.substring(5, 7));

            // Adjust year based on the century (assuming 1900s or 2000s)
            int century = (code.charAt(0) == '3' || code.charAt(0) == '4') ? 1900 : 2000;
            year += century;

            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse date of birth from code", e);
        }
    }
}

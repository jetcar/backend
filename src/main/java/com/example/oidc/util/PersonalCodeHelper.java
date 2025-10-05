package com.example.oidc.util;

import java.time.LocalDate;

public class PersonalCodeHelper {

    public static LocalDate getDateOfBirth(String code) {
        if (code == null || code.length() < 7) {
            throw new IllegalArgumentException("Invalid code for extracting date of birth");
        }
        try {
            int year = Integer.parseInt(code.substring(1, 3));
            int month = Integer.parseInt(code.substring(3, 5));
            int day = Integer.parseInt(code.substring(5, 7));

            // Adjust year based on the century (assuming 1900s or 2000s)
            int century;
            char centuryMarker = code.charAt(0);
            if (centuryMarker == '3' || centuryMarker == '4') {
                century = 1900;
            } else if (centuryMarker == '5' || centuryMarker == '6') {
                century = 2000;
            } else if (centuryMarker == '1' || centuryMarker == '2') {
                century = 1800;
            } else {
                throw new IllegalArgumentException("Unknown century marker in code: " + centuryMarker);
            }
            year += century;

            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse date of birth from code", e);
        }
    }
}

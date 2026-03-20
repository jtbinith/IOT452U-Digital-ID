package com.digitalid.util;

import java.util.Random;

// format -> XX-00-01-X — e.g. KR-00-01-T, PL-00-02-M

public class IdGenerator {

    private int counter = 0;
    private final Random random = new Random();

    public String generateId() {
        counter++;
        char prefix1 = randomLetter();
        char prefix2 = randomLetter();
        char suffix = randomLetter();
        String digits = String.format("%04d", counter);
        return String.format("%c%c-%s-%s-%c", prefix1, prefix2,
            digits.substring(0, 2), digits.substring(2, 4), suffix);
    }

    private char randomLetter() {
        return (char) ('A' + random.nextInt(26));
    }
}

package com.digitalid.util;

import java.util.Random;

// format -> XX-000001-X — e.g. KR-000001-T, PL-000002-M

public class IdGenerator {

    private int counter = 0;
    private final Random random = new Random();

    public String generateId() {
        counter++;
        char prefix1 = randomLetter();
        char prefix2 = randomLetter();
        char suffix = randomLetter();
        return String.format("%c%c-%06d-%c", prefix1, prefix2, counter, suffix);
    }

    private char randomLetter() {
        return (char) ('A' + random.nextInt(26));
    }
}

package fr.adcoop.jeudutao.domain.game;

import java.security.SecureRandom;
import java.util.function.Predicate;

public class HandleGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generate(Predicate<String> isAvailable) {
        String handle;
        do {
            handle = generateOne();
        } while (!isAvailable.test(handle));
        return handle;
    }

    private String generateOne() {
        var sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}

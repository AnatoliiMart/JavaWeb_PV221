package itstep.learning.services.generators;

import java.security.SecureRandom;

public class PasswordGenerator implements Generator {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[{]};:'\",<.>/?";

    @Override
    public String generate(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            String charSet = LOWER + UPPER + DIGITS + SPECIAL;
            sb.append(charSet.charAt(random.nextInt(charSet.length())));
        }

        char[] passwordChars = sb.toString().toCharArray();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(length);
            char temp = passwordChars[i];
            passwordChars[i] = passwordChars[randomIndex];
            passwordChars[randomIndex] = temp;
        }

        return new String(passwordChars);
    }
}


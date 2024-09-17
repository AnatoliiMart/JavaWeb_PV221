package itstep.learning.services.generators;
import java.security.SecureRandom;

public class FileNameGenerator implements Generator {
    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    @Override
    public String generate(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}


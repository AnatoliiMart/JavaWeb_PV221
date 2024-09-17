package itstep.learning.services.generators;
import java.security.SecureRandom;

public class OTPGenerator implements Generator {
    private static final String ALLOWED_CHARACTERS = "0123456789";

    @Override
    public String generate(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}


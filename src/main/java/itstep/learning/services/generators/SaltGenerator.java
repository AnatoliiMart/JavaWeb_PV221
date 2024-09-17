package itstep.learning.services.generators;
import java.security.SecureRandom;
import java.util.Base64;

public class SaltGenerator implements Generator {
    @Override
    public String generate(int length) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}

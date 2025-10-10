import org.mindrot.jbcrypt.BCrypt;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "admin123!";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // 검증
        boolean isValid = BCrypt.checkpw(password, hash);
        System.out.println("Verification: " + isValid);
    }
}

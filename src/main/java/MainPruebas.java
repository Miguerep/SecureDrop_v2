import org.mindrot.jbcrypt.BCrypt;

public class MainPruebas {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
        System.out.println("admin:" + hash + ":ADMIN");
    }
}

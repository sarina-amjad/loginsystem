import java.time.LocalDateTime;
import java.util.*;

// Enum for User Status
enum UserStatus {
    ACTIVE,
    LOCKED,
    INACTIVE
}

// User class
class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private UserStatus status;

    public User(String userId, String username, String passwordHash, String email, UserStatus status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    // Simple password check (in real systems, use secure hash + salt)
    public boolean checkPassword(String password) {
        // For demo, assume passwordHash is plain password (never do this in production)
        return passwordHash.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        // Placeholder for hashing logic
        return password; // Replace with real hash function
    }

    public String getEmail() {
        return email;
    }
}

// Session class
class Session {
    private String sessionId;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Session(String sessionId, String userId, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    public void invalidate() {
        expiresAt = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }
}

// Authenticator class
class Authenticator {
    private UserRepository userRepository;

    public Authenticator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            return false;
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }
        return user.checkPassword(password);
    }
}

// UserRepository class (simulated data store)
class UserRepository {
    private Map<String, User> users = new HashMap<>();

    public UserRepository() {
        // Add a sample user: username "john", password "password123"
        users.put("john", new User("1", "john", "password123", "john@example.com", UserStatus.ACTIVE));
    }

    public User findUserByUsername(String username) {
        return users.get(username);
    }
}

// SessionManager class
class SessionManager {
    private Map<String, Session> sessions = new HashMap<>();

    public Session createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1); // Session valid for 1 hour
        Session session = new Session(sessionId, user.getUserId(), now, expiresAt);
        sessions.put(sessionId, session);
        return session;
    }

    public boolean isLoggedIn(String sessionId) {
        Session session = sessions.get(sessionId);
        return session != null && session.isValid();
    }

    public void invalidateSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.invalidate();
        }
    }
}

// PasswordResetService class
class PasswordResetService {
    private UserRepository userRepository;

    public PasswordResetService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void initiateReset(String email) {
        // In real system, generate token and send email
        System.out.println("Password reset initiated for email: " + email);
    }

    public boolean resetPassword(String token, String newPassword) {
        // Validate token and reset password logic here
        // For demo, always return true
        System.out.println("Password has been reset using token: " + token);
        return true;
    }
}

// LoginManager class
class LoginManager {
    private Authenticator authenticator;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private PasswordResetService passwordResetService;

    public LoginManager(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticator = new Authenticator(userRepository);
        this.sessionManager = new SessionManager();
        this.passwordResetService = new PasswordResetService(userRepository);
    }

    public Session login(String username, String password) {
        if (authenticator.authenticate(username, password)) {
            User user = userRepository.findUserByUsername(username);
            return sessionManager.createSession(user);
        }
        return null;
    }

    public void logout(String sessionId) {
        sessionManager.invalidateSession(sessionId);
    }

    public boolean isLoggedIn(String sessionId) {
        return sessionManager.isLoggedIn(sessionId);
    }

    public void forgotPassword(String email) {
        passwordResetService.initiateReset(email);
    }
}

// Demo usage
public class LoginSystemDemo {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        LoginManager loginManager = new LoginManager(userRepository);

        // Attempt login
        Session session = loginManager.login("john", "password123");
        if (session != null) {
            System.out.println("Login successful! Session ID: " + session.getSessionId());
        } else {
            System.out.println("Login failed!");
        }

        // Check if logged in
        boolean loggedIn = loginManager.isLoggedIn(session != null ? session.getSessionId() : "");
        System.out.println("Is logged in? " + loggedIn);

        // Logout
        if (session != null) {
            loginManager.logout(session.getSessionId());
            System.out.println("Logged out.");
        }

        // Forgot password demo
        loginManager.forgotPassword("john@example.com");
    }
}

package service;

import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import dataaccess.UserDAO;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private static final String VALID_USERNAME_PATTERN = "^[A-Za-z0-9_]{1,32}$";
    private static final String VALID_PASSWORD_PATTERN = "^\\p{Print}{1,32}$";       // 4 minimum non-white space chars
    private static final String VALID_EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthService authService) {
        this.userDAO = userDAO;
        this.authService = authService;
    }

    public AuthorizedUserResult register(RegisterRequest registerRequest) throws AlreadyTakenException {
        if (registerRequest == null ||
                isInvalidParameter(registerRequest.username(), VALID_USERNAME_PATTERN) ||
                isInvalidParameter(registerRequest.password(), VALID_PASSWORD_PATTERN) ||
                isInvalidParameter(registerRequest.email(), VALID_EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Error: bad request");
        }

        UserData userData = userDAO.getUserData(registerRequest.username());
        if (userData != null) {
            throw new AlreadyTakenException();
        }
        String passwordHash = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        userData = new UserData(registerRequest.username(), passwordHash, registerRequest.email());
        userDAO.addUserData(userData);
        AuthData authData = authService.createAuth(registerRequest.username());
        return new AuthorizedUserResult(authData.username(), authData.authToken());
    }

    private boolean isInvalidParameter(String param, String regex) {
        // don't allow non-existent parameters
        return param == null || !param.matches(regex);
    }

    public AuthorizedUserResult login(LoginRequest loginRequest) throws UnauthorizedException {
        if (loginRequest == null ||
                isInvalidParameter(loginRequest.username(), VALID_USERNAME_PATTERN) ||
                isInvalidParameter(loginRequest.password(), VALID_PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("Error: bad request");
        }

        UserData userData = userDAO.getUserData(loginRequest.username());
        if (userData == null || !BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new UnauthorizedException();
        }

        AuthData authData = authService.createAuth(loginRequest.username());
        return new AuthorizedUserResult(authData.username(), authData.authToken());
    }

    public void logout(AuthorizationRequest authorizationRequest) throws UnauthorizedException {
        AuthData authData = authService.getAuthData(authorizationRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException();
        }
        authService.deleteAuthData(authData);
    }
}
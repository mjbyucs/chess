package service;

import dataaccess.AlreadyTakenException;
import dataaccess.UserDAO;
import model.*;

public class UserService {
    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthService authService) {
        this.userDAO = userDAO;
        this.authService = authService;
    }

    public AuthorizedUserResult register(RegisterRequest registerRequest) throws AlreadyTakenException {
        UserData userData = userDAO.getUserData(registerRequest.userName());
        if (userData == null) {
            throw new AlreadyTakenException();
        }
        userData = new UserData(registerRequest.userName(), registerRequest.password(), registerRequest.email());
        userDAO.addUserData(userData);
        AuthData authData = authService.createAuth(registerRequest.userName());
        return new AuthorizedUserResult(authData.username(), authData.authToken());
    }

    public AuthorizedUserResult login(LoginRequest loginRequest) throws IllegalArgumentException {
        throw new IllegalArgumentException("Not supported yet");
    }

    public void logout(AuthorizationRequest authorizationRequest) {
        AuthData authData = authService.getAuthData(authorizationRequest.authToken());
    }
}
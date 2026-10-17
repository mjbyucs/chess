package service;

import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
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
        UserData userData = userDAO.getUserData(registerRequest.username());
        if (userData != null) {
            throw new AlreadyTakenException();
        }
        userData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDAO.addUserData(userData);
        AuthData authData = authService.createAuth(registerRequest.username());
        return new AuthorizedUserResult(authData.username(), authData.authToken());
    }

    public AuthorizedUserResult login(LoginRequest loginRequest) throws UnauthorizedException {
        UserData userData = userDAO.getUserData(loginRequest.username());
        if (userData == null || !loginRequest.password().equals(userData.password())) {
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
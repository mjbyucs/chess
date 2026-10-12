package service;

import dataaccess.AlreadyTakenException;
import dataaccess.UserDAO;
import model.*;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public AuthorizedUserResult register(RegisterRequest registerRequest) throws IllegalArgumentException {
        UserData userData = userDAO.getUser(registerRequest.userName());
        if (userData == null) {
            throw new AlreadyTakenException();
        }
    }
    public AuthorizedUserResult login(LoginRequest loginRequest) throws IllegalArgumentException {
        throw new IllegalArgumentException("Not supported yet");
    }
    public void logout(AuthorizationRequest authorizationRequest) {}
}
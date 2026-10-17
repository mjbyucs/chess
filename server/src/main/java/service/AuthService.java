package service;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData createAuth(String userName) {
        AuthData authData = new AuthData(generateAuthToken(), userName);
        authDAO.addAuthData(authData);
        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public AuthData getAuthData(String authToken) {
        return authDAO.getAuthData(authToken);
    }

    public void deleteAuthData(AuthData authData) {
        authDAO.deleteAuthData(authData);
    }
}

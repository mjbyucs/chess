package dataaccess;

import model.AuthData;

public interface AuthDAO {

    void addAuthData(AuthData authData);

    AuthData getAuthData(String authToken);

    void deleteAuthData(AuthData authData);

    void clearDatabase();

}

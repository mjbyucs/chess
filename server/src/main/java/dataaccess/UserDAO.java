package dataaccess;

import model.UserData;

public interface UserDAO {

    void addUserData(UserData userData);

    UserData getUserData(String userName);

    void deleteUserData(UserData userData);

    void clearDatabase();

}

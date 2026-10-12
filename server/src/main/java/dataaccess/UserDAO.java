package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(String userName);
    void insertUser(UserData userdata) throws DataAccessException;

    void deleteDatabase();
}

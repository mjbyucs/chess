package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDAO {

    void createGame(GameData gameData) throws AlreadyTakenException;

    GameData getGameByName(String gameName);

    GameData getGameByID(int gameID);

    void updateGameData(GameData gameData);

    List<GameData> getAllGames();

    void deleteDatabase();
}

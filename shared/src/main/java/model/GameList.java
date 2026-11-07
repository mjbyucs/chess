package model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;

public class GameList extends ArrayList<GameListEntry> {
    public GameList() {}

    public GameList(Collection<GameListEntry> gameEntries) {
        super(gameEntries);
    }

    public String toString() {
        return new Gson().toJson(this.toArray());
    }

}

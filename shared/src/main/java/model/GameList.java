package model;

import com.google.gson.Gson;

import java.util.ArrayList;

public class GameList extends ArrayList<GameListEntry> {
    public GameList() {}

    public String toString() {
        return new Gson().toJson(this.toArray());
    }

}

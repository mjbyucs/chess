package handler;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.DatabaseService;

public class DataStoreHandler {

    private final DatabaseService databaseService;

    public DataStoreHandler(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void handleDeleteDatabase(@NotNull Context context) {
        databaseService.deleteDatabase();
        context.result("{}");
        context.contentType("application/json");
    }
}

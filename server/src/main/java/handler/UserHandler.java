package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import model.AuthorizationRequest;
import model.AuthorizedUserResult;
import model.LoginRequest;
import model.RegisterRequest;
import org.jetbrains.annotations.NotNull;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleRegistration(Context context) throws IllegalArgumentException, AlreadyTakenException {
        try {
            RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);
            if (registerRequest == null) {
                throw new IllegalArgumentException("Error: bad request");
            }

            AuthorizedUserResult authorizedUserResult = userService.register(registerRequest);
            sendJsonResults(context, authorizedUserResult);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Error: bad request", e);
        }
    }

    public void handleLogin(@NotNull Context context) throws UnauthorizedException {
        try {
            LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);
            if (loginRequest == null) {
                throw new IllegalArgumentException("Error: bad request");
            }

            AuthorizedUserResult authorizedUserResult = userService.login(loginRequest);
            sendJsonResults(context, authorizedUserResult);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Error: bad request", e);
        }
    }

    public void handleLogout(@NotNull Context context) throws UnauthorizedException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        userService.logout(authorizationRequest);
        sendJsonResults(context, Map.of());
    }

    private void sendJsonResults(Context context, Object value) {
        context.contentType("application/json");
        context.result(gson.toJson(value));
    }
}

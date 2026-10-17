package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import model.AuthorizationRequest;
import model.AuthorizedUserResult;
import model.LoginRequest;
import model.RegisterRequest;
import org.jetbrains.annotations.NotNull;
import service.UserService;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleRegistration(Context context) throws IllegalArgumentException, AlreadyTakenException {
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);

        if (isInvalidParameter(registerRequest.username()) ||
                isInvalidParameter(registerRequest.password()) ||
                isInvalidParameter(registerRequest.email())) {
            throw new IllegalArgumentException("Error: bad registerRequest");
        }

        AuthorizedUserResult authorizedUserResult = userService.register(registerRequest);
        context.result(translateAuthorization(authorizedUserResult));
    }

    public void handleLogin(@NotNull Context context) throws UnauthorizedException {
        LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);

        if (isInvalidParameter(loginRequest.username()) ||
                isInvalidParameter(loginRequest.password())) {
            throw new IllegalArgumentException("Error: bad loginRequest");
        }

        AuthorizedUserResult authorizedUserResult = userService.login(loginRequest);
        context.result(translateAuthorization(authorizedUserResult));
    }

    public void handleLogout(@NotNull Context context) throws UnauthorizedException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        userService.logout(authorizationRequest);
    }

    private String translateAuthorization(AuthorizedUserResult authorizedUserResult) {
        return "{\"username\": " + authorizedUserResult.username() +
                ", \"authToken\": " + authorizedUserResult.authToken() + "}";
    }

    private boolean isInvalidParameter(String param) {
        // don't allow non-existent parameters
        return param == null || param.isEmpty() || !param.equals(param.trim());
    }
}

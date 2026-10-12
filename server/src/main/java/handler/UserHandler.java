package handler;

import io.javalin.http.Context;
import model.AuthorizedUserResult;
import model.RegisterRequest;
import org.jetbrains.annotations.NotNull;
import service.UserService;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleRegistration(Context ctx) throws IllegalArgumentException {
        String userName = ctx.queryParam("username");
        String password = ctx.queryParam("password");
        String email = ctx.queryParam("email");

        if (isInvalidParameter(userName) || isInvalidParameter(password) || isInvalidParameter(email)) {
            throw new IllegalArgumentException("Error: bad request");
        }

        RegisterRequest registerRequest = new RegisterRequest(userName, password, email);
        AuthorizedUserResult authorizedUserResult = userService.register(registerRequest);
        ctx.result(translateAuthorization(authorizedUserResult));
    }

    public void handleLogin(@NotNull Context context) {
        // TODO: handleLogin
    }

    public void handleLogout(@NotNull Context context) {
        // TODO: handleLogout
    }

    private String translateAuthorization(AuthorizedUserResult authorizedUserResult) {
        return "{\"username\": " + authorizedUserResult.username() +
                ", \"authToken\": " + authorizedUserResult.authToken() + "}";
    }

    private boolean isInvalidParameter(String param) {
        return param == null || param.isEmpty();
    }
}

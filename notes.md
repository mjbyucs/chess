# My Project Notes

- Design idea: after some research on whether to implement the board as a 1-D or 2-D array, I'm planning
  to use a 1-D array of 100 squares representing a 10x10 grid. This has two benefits. First, the outer
  border will be used to detect when a move is off board without needing to calculate wrap-around logic.
  Second, the game wants to reference board squares 1-based and with this representation, the fit is natural.

Example Data Access Methods
Here are some examples of the kinds of methods your DAOs will need to support. This list is not exhaustive.
You should consult your server design in order to determine all of the methods you need to provide.

- clear: A method for clearing all data from the database. This is used during testing.
- createUser: Create a new user.
- getUser: Retrieve a user with the given username.
- createGame: Create a new game.
- getGame: Retrieve a specified game with the given game ID.
- listGames: Retrieve all games.
- updateGame: Updates a chess game. It should replace the chess game string corresponding to a given gameID. This is used when players join a game or when a move is made.
- createAuth: Create a new authorization.
- getAuth: Retrieve an authorization given an authToken.
- deleteAuth: Delete an authorization so that it is no longer valid.

# My Project Notes

- Design idea: after some research on whether to implement the board as a 1-D or 2-D array, I'm planning
  to use a 1-D array of 100 squares representing a 10x10 grid. This has two benefits. First, the outer
  border will be used to detect when a move is off board without needing to calculate wrap-around logic.
  Second, the game wants to reference board squares 1-based and with this representation, the fit is natural.
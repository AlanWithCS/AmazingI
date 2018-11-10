# AmazingI

This is an intelligent mineswepper agent which can automatically solve the minesweeper game. 

Environment
Each difficulty has a different dimension and number of mines:
  Beginner: 8 row x 8 column with 10 mines
  Intermediate: 16x16 with 40 mines
  Expert: 16x30 with 99 mines
The board begins with 1 random tile already uncovered and presumably safe. Mines are randomly placed throughout the board.
The agent dies when it uncovers a mine.

Actuators
The agent has 4 moves:
(1) The action UNCOVER reveals a covered tile.
(2) The action FLAG places a flag on a tile.
(3) The action UNFLAG removes a flag from a tile if that tile has a flag. ○ (4) The action LEAVE ends the game immediately.
The actions UNCOVER, FLAG, and UNFLAG are to be coupled with a pair of coordinates which allows the agent to act on a single tile. 

Sensors
The agent will receive only one percept:
Following an UNCOVER action, the argent will perceive the hint number associated with the previous UNCOVER action. This number
represents how many mines are within that tile’s immediate neighbors. Following a FLAG or UNFLAG action, the agent will perceive -1.

Running the program:
compile the files and use command "java -jar mine.jar" to invoke the program. There are several options:
-m Use the ManualAI instead of MyAI. If both –m and –r specified, ManualAI will be turned off.
-r Use the RandomAI instead of MyAI.
-d Debug mode, which displays the game board after every move.
-v Verbose mode, which displays name of world files as they are loaded.

You can change the number of tiles (length & height & mines) in the "world.java" file and using the command "java -jar mine.jar" to test the performance of the AI.

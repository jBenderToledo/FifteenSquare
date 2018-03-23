/* Name: Jonathan Bender
 * Class: EECS 1510 Intro to Object-Oriented Programming
 * Professor: Dr. ****** TODO: Change to actual name before turning in
 * 
 ***************************************************************************************
 *
 * Task: Use the console to run a sliding block puzzle game situated within
 * 		a 4x4 square of values ranging from 0 (empty) to 15. The winning configuration
 * 		will be such that the numbers, when read left-to-right as if text,
 * 		will read in increasing order from 1 to 15 and then an empty space.
 * 
 * Winning board (4x4):
 * ╔══╤══╤══╤══╗
 * ║ 1│ 2│ 3│ 4║
 * ╟──┼──┼──┼──╢
 * ║ 5│ 6│ 7│ 8║
 * ╟──┼──┼──┼──╢
 * ║ 9│10│11│12║
 * ╟──┼──┼──┼──╢
 * ║13│14│15│  ║
 * ╚══╧══╧══╧══╝
 * 
 * Facilitate the following:
 * 	-- Display the board                                                     [[DONE]]
 * 	-- Tile movement based around the empty (or 0) tile                      [[DONE]]
 * 	-- Board shuffler                                                        [[DONE]]
 * 	-- Check for solved board                                                [[DONE]]
 * 	+++++ AFTER THE GAME WORKS +++++
 * 	-- Ability to undo one player action. Undoing this will redo the action. [[DONE]]
 * 	-- Save function                                                         [[TODO]]
 * 	-- Load function                                                         [[TODO]]
 *
 **************************************************************************************
 *
 *	Getting and loading is facilitated by looking for a text file named "4x4save.txt"
 *    and reading/writing using it. 4 will be replaced with whatever BOARD_SIZE happens
 *    to be.
 */

import java.util.*;
public class fifteenSquare
{
	// Static variables up here because I like being able to toggle settings as desired
	//  or avoid passing a million variables as arguments
	                                                          
	static final char CORNER_UL = '╔';     // Tiles!
	static final char CORNER_UR = '╗';     
	static final char CORNER_LL = '╚';
	static final char CORNER_LR = '╝';
	
	static final char HORI_DOUBLE = '═';
	static final char HORI_SINGLE = '─';
	static final char VERT_DOUBLE = '║';
	static final char VERT_SINGLE = '│';
	
	static final char U_THREE = '╤';       // These are the double borders
	static final char L_THREE = '╟';       //   intersecting singles.
	static final char R_THREE = '╢';       //
	static final char D_THREE = '╧';       //
	
	static final char CROSS = '┼';
   
	static final int SHUFFLE_COUNT = 1000000;                  // Shuffle this many times.
	static final int BOARD_SIZE = 4;                          // What's the side length of the square?
	static int[][] board = new int[BOARD_SIZE][BOARD_SIZE];   // Where do the pieces go?
   
	static final char[] MOVES = new char[] {                  // The command set
		'U', 'L', 'D', 'R'                                     //
	};                                                        // Starts with ULDR for shuffler purposes
	
	static char previousDirection;            // Used for undoing moves. Static because
	                                          //  I don't want to pass it as an argument.
	static boolean quitFlag;                  // Allows quitting the game.
	static boolean exitFlag;                  // Closes the game when outside of the game.
	static boolean isShuffling = false;       // Added this variable to make the command reader less angry.
	static int temp;                          // facilitates the swapping!
	
	static int gamesWon;
	static int gamesPlayed;
	
	static String saveName = String.format("%dx%dBoard.txt", BOARD_SIZE, BOARD_SIZE);
	
	public static void main(String[] args)
	{
		String command;                           // facilitates the inputs!
		Scanner input = new Scanner(System.in);
		gamesWon = 0;
		gamesPlayed = 0;
		
		while (!quitFlag)
		{
			previousDirection = 0;                            // 0 means you can't undo
			gamesPlayed++;
			
			for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)    // Initialize the board with values 0->15.
			{                                                    //
				board[row(i)][col(i)] = i;        // divide for row, mod for row element
			}                                                    //
			                                                     //
			temp = board[0][0];                                  // This is an unsolveable configuration.
			board[0][0] = board[BOARD_SIZE - 1][BOARD_SIZE - 1]; //  Remedy the issue by swapping the
			board[BOARD_SIZE - 1][BOARD_SIZE - 1] = temp;        //  upper-left and lower-right tiles.
			
			shuffle();                                       // Unsolve the board
			
			while (!quitFlag && !isSolved())                     // During the game,
			{
				displayBoard();                                      // Print the board.
				System.out.print("> ");                              // Prompt player input.
				do                                                   //
				{                                                    //
					command = input.nextLine();                       // Read player input.
				} while (command.length() == 0);                     // Buffer any leaking newlines.
				executeCommand(command.toUpperCase().charAt(0));     // Perform command.
			}
			
			if (quitFlag)
			{
				System.out.println("Goodbye!");
			}
			else // if isSolved()
			{
				displayBoard();
				System.out.println("You win!");
				System.out.println("Play again?");
				command = null;
				do
				{
					command = input.nextLine();
				} while (command.length() == 0);
				
				if (command.toUpperCase().charAt(0) == 'N') quitFlag = true;
			} // Closes the current game
		} // Closes the game state
		input.close();
	}
	
	static void displayBoard()
	/*
	 * Multiple steps; this is a complicated pizza.
	 * 
	 * 1. Print the upper border.
	 * 2. Print BOARD_SIZE rows including numbers
	 * 	interspersed with rows including middle borders.
	 * 3. Print the lower border.
	 * 
	 */
	{
		// Step 1
		
		System.out.print(CORNER_UL);                                   // Print the upper border.
		for(int col = 0; col < BOARD_SIZE; col++)
		{
			System.out.printf("%c%c", HORI_DOUBLE, HORI_DOUBLE);
			if (col != BOARD_SIZE - 1)
				System.out.print(U_THREE);
		}
		System.out.println(CORNER_UR);
		
		// Step 2
		
		for (int row = 0; row < BOARD_SIZE; row++)                     // For each row,
		{                                                              //
			System.out.print(VERT_DOUBLE);                              // Print a row of numbers
			for (int col = 0; col < BOARD_SIZE; col++)                  //
			{                                                           //
				if (board[row][col] != 0)                                //
					System.out.printf("%2d", board[row][col]);            //
				else                                                     //
					System.out.print("  ");                               // (or a blank space at tile 0)
				                                                         //
				if (col != BOARD_SIZE - 1)                               //
					System.out.print(VERT_SINGLE);                        //
			}                                                           //
			System.out.println(VERT_DOUBLE);                            //
			                                                            //
			if (row != BOARD_SIZE - 1)                                  // Followed by a row of borders.
			{                                                           //
				System.out.print(L_THREE);                               // 
				for (int col = 0; col < BOARD_SIZE; col++)               //
				{                                                        //
					System.out.printf("%c%c", HORI_SINGLE, HORI_SINGLE);  //
					if (col != BOARD_SIZE - 1)                            //
						System.out.print(CROSS);                           //
				}                                                        //
				System.out.println(R_THREE);                             //
			}                                                           //
		}
		
		// Step 3
		
		System.out.print(CORNER_LL);                                   // Print the lower border.
		for (int col = 0; col < BOARD_SIZE; col++)
		{
			System.out.printf("%c%c", HORI_DOUBLE, HORI_DOUBLE);
			if (col != BOARD_SIZE - 1)
				System.out.print(D_THREE);
		}
		System.out.println(CORNER_LR);
	}
	
	static int row(int n)
	{
		return n / BOARD_SIZE;
	}
	
	static int col(int n)
	{
		return n % BOARD_SIZE;
	}
	
	static int findBlank()
	{
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)  // return the position
		{                                                  //  of the blank tile.
			if (board[i / BOARD_SIZE][i % BOARD_SIZE] == 0) //
				return i;                                    //
		}                                                  //
		
		System.out.println("Something\'s broken."); // You should never execute these lines.
		                                            //
		return -1;                                  //
	}
	
	static void executeCommand(char ch) // TODO implement saving / loading
	/*
	 * If ch is U, D, L, R, and will not crash by following that direction:
	 * 	swap the zero spot with the spot in the appropriate position relative to it.
	 * Else if will crash:
	 * 	if the player is doing the move,
	 * 		Scold the player.
	 * 	if the game is shuffling,
	 *			Don't scold the player.
	 * If ch is H,
	 * 	Print the help prompt.
	 * If ch is Q,
	 * 	Enable quitting.
	 * if ch is Z,
	 * 	if previousMove is not 0,
	 * 		Move in the previous direction and set previousMove to 0.
	 * if ch is S,
	 * 	Make the game state into an array of strings.
	 * 	Convert it into a string, delimiting with newlines.
	 * 	BOARD_SIZE * BOARD_SIZE numbers followed by values that the player can
	 * 		accumulate over time.
	 */
	{
		int zeroPos = findBlank();         // Where on the board is the empty space?
		switch (ch)
		{
		case 'U':                          // Valid state: not at top of board
         if (row(zeroPos) != 0)
         {  // Swap the blank space with the tile above it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos) - 1][col(zeroPos)];
         	board[row(zeroPos) - 1][col(zeroPos)] = temp;
         	
         	temp = -1;              // This means that the board state was moved.
         	                        //  this is relevant to the shuffle() method.
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)     // Scold if wrong and player input.
         {
         	System.out.println("\'" + ch + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'D':                          // Valid state: not at bottom of board
			if (row(zeroPos) != BOARD_SIZE - 1)
         {  // Swap the blank space with the tile below it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos) + 1][col(zeroPos)];
         	board[row(zeroPos) + 1][col(zeroPos)] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'L':                          // Valid state: not at left of board
			if (col(zeroPos) != 0)
         {  // Swap the blank space with the tile to the left of it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos)][col(zeroPos) - 1];
         	board[row(zeroPos)][col(zeroPos) - 1] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'R':                          // Valid state: not at right of board
			if (col(zeroPos) != BOARD_SIZE - 1)
         {  // Swap the blank space with the tile to the right of it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos)][col(zeroPos) + 1];
         	board[row(zeroPos)][col(zeroPos) + 1] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'S':
			System.out.println("Saving is currently unimplemented, sorry!");
			// TODO implement
			break;
		case 'G':
			System.out.println("Getting (Loading) is currently unimplemented, sorry!");
			// TODO implement
			break;
		case 'Q':
			quitFlag = true;            // Player quits.
			break;
		case 'Z':
			switch (previousDirection)  // Move backwards. If it's the first move, tell the player that they can't undo.
			{                           // Undoing again will redo the move.
			case 'U':
				executeCommand('D');
				break;
			case 'D':
				executeCommand('U');
				break;
			case 'L':
				executeCommand('R');
				break;
			case 'R':
				executeCommand('L');
				break;
			default:              // if 0
				System.out.println("\'" + ch + "\' is illegal.");
         	System.out.println("Press H for help.");
				break;
			}
			previousDirection = 0;      // This disallows the user from undoing the undo.
			break;
		case 'H':
			System.out.println("----------------------");
			System.out.println("Commands:");
			System.out.println("U,D,L,R: Move the blank space up, down, left, or right.");
			System.out.println("S:       Save the game state.");
			System.out.println("G:       Get (load) the game state.");
			System.out.println("Z:       Undo your last move.");
			System.out.println("            (doesn't work if it's your first move)");
			System.out.println("H:       Get a list of commands. The H is for \"Hello\"!");
			System.out.println("----------------------");
			break;
		default:   // DNE
			System.out.println("\'" + ch + "\' is illegal.");
      	System.out.println("Press H for help.");
			break;
		}
	}
	
	static void shuffle()
	/*
	 * Because of how I arranged U L D R in MOVES[],
	 * (indexOf(L) + 2) % 4 = indexOf(R), likewise for U and L, vice versa applies.
	 * In other words, I can use that to check for non-undo inputs
	 *    by having all corresponding values be a set number of
	 *    indices away from each other.
	 *    
	 * So, the basic idea is:
	 * 1. Tell the system that I'm shuffling and therefore to not nag me.
	 * 2. Get a correct move SHUFFLE_COUNT times using Random randy;
	 * 	Do so until randy gives us a move that won't undo the previous move.
	 * 3. Execute the move, given that it's a move that won't crash the game.
	 * 4. If it was a move that would crash a game, restart the loop.
	 */
	{
		isShuffling = true;                       // Flag for preventing scolding on
		
		Random randy = new Random();                 // Good ol' Randy's lending a hand!
		int rand;
		for (int i = 0; i < SHUFFLE_COUNT; i++)            // SHUFFLE_COUNT times,
		{                                                          //
			temp = 0;                                               // Allow the check at the bottom
			                                                        //
			do                                                      //
			{                                                       //
				rand = Math.abs(randy.nextInt()) % 4;                // Get a cup of sugar from Randy! Thanks, Randy!
				                                                     //   (Repeat without incrementing if
				                                                     //   effectively undoing previous
			} while (previousDirection == MOVES[(rand + 2) % 4]);   //   action)
			executeCommand(MOVES[rand]);                            // Execute Randy's "sugar". Rude!
			                                                        //
			if (temp != -1) i--;                                    // If nothing happened,
			                                                        //  Repeat without incrementing.
		}
		
		isShuffling = false;                       // Flag off
		previousDirection = 0;                     // Disallow undoing a shuffle move
	}
	
	static boolean isSolved()
	{
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE - 1; i++)    // Check the board configuration
		{                                                        //  to match the ideal setting
			if (board[i / BOARD_SIZE][i % BOARD_SIZE] != (i + 1)) //
				return false;                                      //
		}                                                        //  as described in the header comments
		// I don't need to check for 0 because I've already checked for the other places.
		
		return true;
	}
	
	static void saveGame()
	{
		String[] saveData = new String[BOARD_SIZE * BOARD_SIZE + 2];
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)
		{
			saveData[i] = board[row(i)][col(i)] + "";                 // Casts the elements as Strings
		}
		saveData[saveData.length - 2] = gamesPlayed + "";
		saveData[saveData.length - 1] = gamesWon + "";
		
		try
		/*
		 * Read the file. If it doesn't exist yet, make the file.
		 * Finally, erase the file, write to the file, then close the file.
		 */
		{
			java.io.File saveFile = new java.io.File(saveName);          // 4x4Board.txt
		}
		catch (IOException ex)
		{
			
		}
	}
	
	static void loadGame()
	{
		
	}
}

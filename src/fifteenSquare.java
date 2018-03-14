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
 * Facilitate the following:
 * 	-- Display the board
 * 	-- Tile movement based around the empty (or 0) tile
 * 	-- Board shuffler
 * 	-- Check for solved board
 * 	+++++ AFTER THE GAME WORKS +++++
 * 	-- Ability to undo one player action. Undoing this will redo the action.
 * 	-- Save function
 * 	-- Load function
 * 
 */

import java.util.*;
public class fifteenSquare
{
	// Static variables up here because I like being able to toggle settings as desired.
	                                                          //
	static final int SHUFFLE_COUNT = 1000;                    // Shuffle this many times.
	static final int BOARD_SIZE = 4;                          // What's the side length of the square?
	static int[][] board = new int[BOARD_SIZE][BOARD_SIZE];   // Where do the pieces go?
	                                                          
	static final char[] INPUTS = new char[] {           // The command set
			'U', 'L', 'D', 'R', 'S', 'G', 'Q', 'Z', 'H'   //
	};                                                  // Starts with ULDR for shuffler purposes
	                                                          
	static final char CORNER_UL = '\u2554';     // Tiles!
	static final char CORNER_UR = '\u2557';
	static final char CORNER_LL = '\u255A';
	static final char CORNER_LR = '\u255D';
	
	static final char HORI_DOUBLE = '\u2550';
	static final char HORI_SINGLE = '\u2500';
	static final char VERT_DOUBLE = '\u2551';
	static final char VERT_SINGLE = '\u2502';
	
	static final char U_THREE = '\u2564';     // These are the double borders
	static final char L_THREE = '\u255F';     //   intersecting singles.
	static final char R_THREE = '\u2562';
	static final char D_THREE = '\u2567';
	
	static final char CROSS = '\u253C';
	
	static char previousDirection;            // Used for undoing moves. Static because
	                                          //  I don't want to pass it as an argument.
	static boolean quitFlag;
	static boolean isShuffling = false;       // Added this variable to make the command reader less angry.
	static int temp;                          // facilitates the swapping!
	public static void main(String[] args)
	{
		String command;                           // facilitates the inputs!
		Scanner input = new Scanner(System.in);
		
		while (!quitFlag)
		{
			previousDirection = 0;                            // 0 means you can't undo
			
			for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)    // Initialize the board with values 0->15.
			{                                                    //
				board[i / BOARD_SIZE][i % BOARD_SIZE] = i;        // divide for row, mod for row element
			}                                                    //
			                                                     //
			temp = board[0][0];                                  // This is an unsolveable configuration.
			board[0][0] = board[BOARD_SIZE - 1][BOARD_SIZE - 1]; //  Remedy the issue by swapping the
			board[BOARD_SIZE - 1][BOARD_SIZE - 1] = temp;        //  upper-left and lower-right tiles.
			
			shuffle();                                       // Unsolve the board
			                                                 //
			previousDirection = 0;                           // Don't undo shufflebot.
			
			while (!isSolved() && !quitFlag)                     // During the game,
			{
				displayBoard();                                      // Print the board.
				System.out.println("> ");                            // Read player input.
				do                                                   //
				{                                                    //
					command = input.nextLine().toUpperCase();         // Read player input.
				} while (command.length() == 0);                     // Buffer any leaking newlines.
				executeCommand(command.charAt(0));                   // Perform command.
			}
			
			if (quitFlag)
			{
				System.out.println("Goodbye!");
			}
			else // if isSolved()
			{
				displayBoard();
				System.out.println("Congratulations! Let's go again!");
			}
		}
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
	 * If ch is U, D, L, R, and will not crash:
	 * 	swap the zero spot with the spot in the appropriate position relative to it.
	 * if
	 */
	{
		int zeroPos = findBlank();         // Where on the board is the empty space?
		switch (ch)
		{
		case 'U': // Valid state: not at top of board
         if (findBlank() / BOARD_SIZE != 0)
         {
         // Swap the appropriate positions.
         	temp = board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE];
         	board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE] = board[(zeroPos / BOARD_SIZE) - 1][zeroPos % BOARD_SIZE];
         	board[(zeroPos / BOARD_SIZE) - 1][zeroPos % BOARD_SIZE] = temp;
         	
         	temp = -1;    // This means that the board state was moved.
         	              //  this is relevant to the shuffle() method.
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)    // Scold if wrong and player input.
         {
         	System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
         }
			break;
		case 'D': // Valid state: not at bottom of board
			if (findBlank() / BOARD_SIZE != BOARD_SIZE - 1)
         {
			// Swap the appropriate positions.
         	temp = board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE];
         	board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE] = board[(zeroPos / BOARD_SIZE) + 1][zeroPos % BOARD_SIZE];
         	board[(zeroPos / BOARD_SIZE) + 1][zeroPos % BOARD_SIZE] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
         }
			break;
		case 'L': // Valid state: not at left of board
			if (findBlank() % BOARD_SIZE != 0)
         {
			// Swap the appropriate positions.
         	temp = board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE];
         	board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE] = board[zeroPos / BOARD_SIZE][(zeroPos % BOARD_SIZE) - 1];
         	board[zeroPos / BOARD_SIZE][(zeroPos % BOARD_SIZE) - 1] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
         }
			break;
		case 'R': // Valid state: not at right of board
			if (findBlank() % BOARD_SIZE != BOARD_SIZE - 1)
         {
			// Swap the appropriate positions.
         	temp = board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE];
         	board[zeroPos / BOARD_SIZE][zeroPos % BOARD_SIZE] = board[zeroPos / BOARD_SIZE][(zeroPos % BOARD_SIZE) + 1];
         	board[zeroPos / BOARD_SIZE][(zeroPos % BOARD_SIZE) + 1] = temp;
         	
         	temp = -1;
         	
         	previousDirection = ch;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
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
			quitFlag = true; // Player quits.
			break;
		case 'Z':
			switch (previousDirection)  // move backwards. Otherwise, tell player they can't undo.
			{
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
				System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
				break;
			}
			break;
		case 'H':
			System.out.println("Commands:");
			System.out.println("----------------------");
			System.out.println("U,D,L,R: Move the blank space up, down, left, or right.");
			System.out.println("S:       Save the game state.");
			System.out.println("G:       Get (load) the game state.");
			System.out.println("Z:       Undo your last move. Execute again to redo.");
			System.out.println("H:       Get a list of commands. The H is for \"Hello\"!");
			System.out.println("----------------------");
			break;
		default:   // DNE
			System.out.println("\'" + ch + "\' is illegal in this board state. Press H for help.");
			break;
		}
	}
	
	static void shuffle()
	/*
	 * Because of how I arranged U L D R in INPUTS[],
	 * (indexOf(L) + 2) % 4 = indexOf(R) and likewise for U and L.
	 * In other words, I can use that to check for non-undo inputs.
	 */
	{
		isShuffling = true;
		
		Random randy = new Random();  // Good ol' Randy's lending a hand!
		int rand;
		for (int i = 0; i < SHUFFLE_COUNT; i++)            // SHUFFLE_COUNT times,
		{                                                      //
			temp = 0;                                           // Allow the check at the bottom
			do                                                  //
			{                                                   //
				rand = Math.abs(randy.nextInt()) % 4;            // Get a cup of sugar from Randy! Thanks, Randy!
				                                                 //   (Repeat without incrementing if
				                                                 //   effectively undoing previous
			} while (previousDirection == INPUTS[(rand + 2) % 4]); //   action)
			executeCommand(INPUTS[rand]);                       // Execute Randy's "sugar". Rude!
			
			if (temp != -1) i--;                            // If nothing happened,
			                                                //  Repeat without incrementing.
		}
		
		isShuffling = false;
	}
	
	static boolean isSolved()
	{
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)        // Check the board configuration
		{                                                        //  to match the ideal setting
			if (board[i / BOARD_SIZE][i % BOARD_SIZE] != (i + 1)) //
				return false;                                      //
		}                                                        //  as described in the header comments
		
		return true;
	}
}

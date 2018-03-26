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
 * 	-- Save function                                                         [[DONE]]
 * 	-- Load function                                                         [[DONE]]
 *
 **************************************************************************************
 *
 *	Getting and loading is facilitated by looking for a text file named "4x4save.txt"
 *    and reading/writing using it. 4 will be replaced with whatever BOARD_SIZE happens
 *    to be.
 */

import java.util.*;
import java.io.*;
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
	static final int BOARD_SIZE = 4;                           // The board is a BOARD_SIZE x BOARD_SIZE grid.
	
	static final char[] MOVES = new char[] {                  // The command set
		'U', 'L', 'D', 'R'                                     //
	};                                                        // Starts with ULDR for shuffler purposes.
	
	static String saveName = String.format("%dx%dBoard.txt", BOARD_SIZE, BOARD_SIZE); // What do we name the save file?
	
	public static void main(String[] args)
	{
		String command;                                    // facilitates the inputs!
		Scanner input = new Scanner(System.in);            //
		int temp;                                          // If I need to swap anything, I can.
		char previousDirection = 0;                        // What was the last move?
		int gamesWon = 0;                                  // How many games did the player win?
		int gamesPlayed = 0;                               // How many games did the player play?
		int movesMade = 0;                                 // How many moves did the player make?
		int oldZeroPos;                                    // Where was the zero position before?
		                                                   //
		int[][] board = new int[BOARD_SIZE][BOARD_SIZE];   // Where do the pieces go?
		
		do
		{
			previousDirection = 0;                            // 0 means you can't undo
			gamesPlayed++;
			
			for (int index = 0; index < BOARD_SIZE * BOARD_SIZE; index++) // Initialize the board with values 0->15.
			{                                                             //
				board[row(index)][col(index)] = index;                     // 
			}                                                    ///////////
			                                                     //
			temp = board[0][0];                                  // This is an unsolveable configuration.
			board[0][0] = board[BOARD_SIZE - 1][BOARD_SIZE - 1]; //  Remedy the issue by swapping the
			board[BOARD_SIZE - 1][BOARD_SIZE - 1] = temp;        //  upper-left and lower-right tiles.
			
			shuffle(board);                                       // Unsolve the board.
			
			while (previousDirection != 'Q' && !isSolved(board)) // During the game,
			{                                                    /////
				oldZeroPos = findBlank(board);                       //
				displayBoard(board);                                 // Print the board.
				System.out.print("> ");                              // Prompt player input.
				                                                     //
				command = null;                                      // Empty the input String.
				do                                                   //
				{                                                    //
					command = input.nextLine();                       // Read player input.
				} while (command != null && command.length() == 0);  // Buffer any leaking newlines to prevent crashing.
				command = command.toUpperCase();                     // Convert to uppercase to make subsequent code less ugly.
				                                                     //
				previousDirection = executeCommand(board,            // Store the previous valid direction from passing the board,
						command.charAt(0),                             //  passing a command,
						previousDirection,                             //  passing the previous direction,
						false);                                        //  and verifying the lack of shuffling.
				
				if (isADirectionInput(command.charAt(0))               //
						&& board[row(oldZeroPos)][col(oldZeroPos)] != 0) // If I perform a move that changes the board state directly,
				{                                                      //  then I should increment the number of moves made.
					movesMade++;                                        //
				}                                                      //
				else if (command.charAt(0) == 'Z'                      //
						&& board[row(oldZeroPos)][col(oldZeroPos)] != 0) // If I undo a move, then the number
				{                                                      //  of moves made should go down.
					movesMade--;                                        //
				}                                                      //
				else if (command.charAt(0) == 'S')                     //// SAVING:
				{                                                        //
					try                                                   // 
					{                                                     //
						saveGame(board, gamesWon, gamesPlayed, movesMade); // Pass all relevant variables to save.
						System.out.println("Done!");                       // Confirm that nothing went wrong.
					}                                                     //
					catch (IOException ex)                                // Forced exception catch.
					{                                                     //
						System.out.println(ex);                            // No idea what went wrong. Pass to user.
					}                                                     //
				}                                                        //
				else if (command.charAt(0) == 'G')                       ////////////// LOADING:
				{                                                                    //
					String[] saveData = loadGame();                                   // Get all relevant variables.
					if (saveData[0].equals("FAILED"))                                 //  If you can't,
					{                                                                    //
						System.out.println("No save file exists. Try saving your game."); // then it probably doesn't exist.
					}                                                                    //
					else                                                              // If successful load,
					{                                                                 ////////////
						for (int itemNo = 0; itemNo < saveData.length - 3; itemNo++)             // Set the board.
						{                                                                        //
							board[row(itemNo)][col(itemNo)] = Integer.parseInt(saveData[itemNo]); //
						}                                                                        //
						                                                                         //
						gamesWon =    Integer.parseInt(saveData[saveData.length - 3]);           // Restore all metadata.
						gamesPlayed = Integer.parseInt(saveData[saveData.length - 2]);           //
						movesMade =   Integer.parseInt(saveData[saveData.length - 1]);           //
					}                                                                 ////////////
					                                                                  //
					previousDirection = 0;                                            // Prevent undoing after loading.
				}
				else if (command.charAt(0) == 'M')                                   // Print player stats.
				{
					System.out.println("STATS:");
					System.out.printf("Games won:\t%d\n", gamesWon);
					System.out.printf("Games played:\t%d\n", gamesPlayed);
					System.out.printf("Moves made:\t%d\n", movesMade);
				} // Ending main-relevant command tree
				
			} // Ending the current game state
			
			if (previousDirection != 'Q') // Increment gamesWon and congratulate the player if it wasn't a game exit.
			{
				gamesWon++;
				displayBoard(board);
				System.out.println("You win!");
			}
			previousDirection = 0;
			
			System.out.println("Play again?");
			
			command = null;
			do
			{
				command = input.nextLine();
			} while (command != null && command.length() == 0);
			
			if (command.toUpperCase().charAt(0) == 'N') previousDirection = 'Q'; // Prioritizes no over yes
		} while (previousDirection != 'Q'); // Closes the program
		input.close();
	}
	
	static void displayBoard(int[][] board)
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
	
	static int findBlank(int[][] board)
	{
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)      // return the position
		{                                                      //  of the blank tile.
			if (board[row(i)][col(i)] == 0)                     //
				return i;                                        //
		}                                                      //
		
		System.out.println("Something\'s broken."); // You should never execute these lines.
		                                            //
		return -1;                                  //
	}
	
	static char executeCommand(int[][] board, char command, char previous, boolean isShuffling)
	/*
	 * This receives and passes previousDirection out.
	 * 
	 * If command is U, D, L, or R, and will not crash by following that direction:
	 * 	swap the zero spot with the spot in the appropriate position relative to it.
	 * 	set the previous direction to command.
	 * Else if will crash:
	 * 	if the player is doing the move,
	 * 		Scold the player.
	 * 	if the game is shuffling,
	 *			Don't scold the player.
	 * If command is H,
	 * 	Print the help prompt.
	 * If command is Q,
	 * 	Enable quitting.
	 * if command is Z,
	 * 	if previousMove is not 0,
	 * 		Move in the previous direction and set previousMove to 0.
	 * if command is M,
	 * 	Print user's metadata after going to main.
	 * if command is S,
	 * 	Return to main and save data.
	 * if command is G,
	 * 	Return to main and load data.
	 */
	{
		int zeroPos = findBlank(board);         // Where on the board is the empty space?
		int temp;
		
		switch (command)
		{
		case 'U':                          // Valid state: not at top of board
         if (row(zeroPos) != 0)
         {  // Swap the blank space with the tile above it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos) - 1][col(zeroPos)];
         	board[row(zeroPos) - 1][col(zeroPos)] = temp;
         	
         	previous = command;
         }
         else if (!isShuffling)     // Scold if wrong and player input.
         {
         	System.out.println("\'" + command + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'D':                          // Valid state: not at bottom of board
			if (row(zeroPos) != BOARD_SIZE - 1)
         {  // Swap the blank space with the tile below it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos) + 1][col(zeroPos)];
         	board[row(zeroPos) + 1][col(zeroPos)] = temp;
         	
         	previous = command;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + command + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'L':                          // Valid state: not at left of board
			if (col(zeroPos) != 0)
         {  // Swap the blank space with the tile to the left of it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos)][col(zeroPos) - 1];
         	board[row(zeroPos)][col(zeroPos) - 1] = temp;
         	
         	previous = command;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + command + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'R':                          // Valid state: not at right of board
			if (col(zeroPos) != BOARD_SIZE - 1)
         {  // Swap the blank space with the tile to the right of it.
         	temp = board[row(zeroPos)][col(zeroPos)];
         	board[row(zeroPos)][col(zeroPos)] = board[row(zeroPos)][col(zeroPos) + 1];
         	board[row(zeroPos)][col(zeroPos) + 1] = temp;
         	
         	previous = command;
         }
         else if (!isShuffling)
         {
         	System.out.println("\'" + command + "\' is illegal.");
         	System.out.println("Press H for help.");
         }
			break;
		case 'Q':
			previous = command;
			break;
		case 'Z':
			switch (previous)  // Move backwards. If it's the first move or an undo, tell the player that they can't undo.
			{                           
			case 'U':
				previous = executeCommand(board, 'D', '\0', false);  // Due to how executeCommand works, I have to store the result somewhere.
				previous = 0;                                    // <-- This disallows the user from undoing the undo.
				break;
			case 'D':
				previous = executeCommand(board, 'U', '\0', false);
				previous = 0; 
				break;
			case 'L':
				previous = executeCommand(board, 'R', '\0', false);
				previous = 0; 
				break;
			case 'R':
				previous = executeCommand(board, 'L', '\0', false);
				previous = 0; 
				break;
			default:              // if 0
				System.out.println("\'" + command + "\' is illegal.");
         	System.out.println("Press H for help.");
				break;
			}
			break;
		case 'H':
			System.out.println("----------------------");
			System.out.println("Commands:");
			System.out.println("U,D,L,R: Move the blank space up, down, left, or right.");
			System.out.println("S:       Save the game state.");
			System.out.println("G:       Get (load) the game state.");
			System.out.println("Z:       Undo your last move.");
			System.out.println("            (doesn't work if it's your first move or you just did this)");
			System.out.println("M:       Look at what you've done across your play sessions.");
			System.out.println("H:       Get a list of commands.");
			System.out.println("----------------------");
			break;
		case 'S':
			System.out.println("Saving...");
			break;
		case 'G':
			System.out.println("Loading...");
			break;
		case 'M': // Needs to be facilitated by main()
			break;
		default:   // DNE
			System.out.println("\'" + command + "\' is illegal.");
      	System.out.println("Press H for help.");
			break;
		}
		
		return previous;
	}
	
	static void shuffle(int[][] board)
	/*
	 * Because of how I arranged U L D R in MOVES[],
	 * (indexOf(L) + 2) % 4 = indexOf(R), likewise for U and L, vice versa applies.
	 * In other words, I can use that to check for non-undo inputs
	 *    by having all corresponding values be a set number of
	 *    indices away from each other.
	 *    
	 * So, the basic idea is:
	 * 1. Get the original zero position in each iteration.
	 * 2. Get a random integer
	 * 	Do so until randy gives us a move that won't undo the previous move.
	 * 3. Execute the move, given that it's a move that won't crash the game.
	 * 4. If it was a valid move that changes the board state without crashing, increment the counter.
	 * 5. If it hasn't shuffled enough times, go to step 1.
	 * 6. Exit shuffle().
	 */
	{
		int zeroPlace;      // If the board at this position contains a nonzero term, the board state was changed.
		char previousMove = 0;
		
		Random random = new Random();
		int randomInt;
		int shuffleIndex = 0;
		do                                                            // SHUFFLE_COUNT times,
		{                                                             ///////
			zeroPlace = findBlank(board);                                   // Get where zero is.
			                                                                //
			do                                                              //
			{                                                               /////
				randomInt = Math.abs(random.nextInt()) % 4;                     // Get a random integer in range [0,4)
				                                                                //   (Repeat without incrementing if MOVES[randomInt]
				                                                                //    will effectively undo the previous action.
			} while (previousMove == MOVES[(randomInt + 2) % 4]);           /////   )
			executeCommand(board,                                           // Attempt to place a piece,
					MOVES[randomInt],                                         //
			      previousMove,                                             //
			      true);                                                    //  telling the program not to nag.
			                                                                //
			if (board[row(zeroPlace)][col(zeroPlace)] != 0) shuffleIndex++; // If the board state changed,
			                                                           ///////  the counter increases.
		} while (shuffleIndex < SHUFFLE_COUNT);                       //
	}
	
	static boolean isSolved(int[][] board)
	{
		final int BOARD_SIZE = board.length;
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE - 1; i++)    // Check the board configuration
		{                                                        //  to match the ideal setting
			if (board[row(i)][col(i)] != (i + 1))                 //
				return false;                                      //
		}                                                        //  as described in the header comments
		
		// I don't need to check for 0 because I've already checked for the other places.
		
		return true;
	}
	
	static boolean isADirectionInput(char ch)
	{
		for (int i = 0; i < MOVES.length; i++)
		{
			if (ch == MOVES[i]) return true;      // If ch is U, D, L, or R, return true.
		}
		
		return false;
	}
	
	static void saveGame(int[][] board, int gamesWon, int gamesPlayed, int movesMade) throws IOException
	/*
	 * 1. Read board into the first BOARD_SIZE * BOARD_SIZE elements of the saving array.
	 * 2. Read gamesWon, gamesPlayed, movesMade into the last three elements.
	 * 3. Erase the saved file.
	 * 4. Make a new PrintWriter with the fileName at the top of the class.
	 * 5. Use PrintWriter to write the elements of saveData to the file.
	 * 6. Cleanup, exit method.
	 */
	{
		String[] saveData = new String[BOARD_SIZE * BOARD_SIZE + 3];                     // 1.
		for (int boardPos = 0; boardPos < saveData.length - 3; boardPos++)               //
		{                                                                                //
			saveData[boardPos] = Integer.toString(board[row(boardPos)][col(boardPos)]);   //
		}                                                                                //
		saveData[saveData.length - 3] = Integer.toString(gamesWon);                      // 2.
		saveData[saveData.length - 2] = Integer.toString(gamesPlayed);      ///////////////
		saveData[saveData.length - 1] = Integer.toString(movesMade);        // Making these into Strings to avoid subsequent misery
		
		File saveFile = new File(saveName);                                 // 3. 
		saveFile.delete();                                                  //
		
		
		PrintWriter saveWriter = new PrintWriter(saveFile);           // 4.
		                                                              // 
		for (int itemNo = 0; itemNo < saveData.length; itemNo++)      /////// 5.
		{                                                                  //
			saveWriter.print(saveData[itemNo]);                             // Print item.
			if (itemNo + 1 != saveData.length)                              //
			{                                                               //
				saveWriter.print(" ");                                       // Print delimiters in between items
			}                                                               //  but not after the final item.
		}                                                                  //
		                                                              ///////
		saveWriter.close();                                           // 6.
	}
	
	static String[] loadGame()
	/*
	 * The first BOARD_SIZE * BOARD_SIZE elements are the numbers on the board.
	 * The last three elements are the number of games won, the number of games played, and the number of moves made.
	 * 
	 * 1. Load the appropriate file.
	 * 2. Read BOARD_SIZE * BOARD_SIZE + 3 elements from the file, save as Strings.
	 * 3. Check for each element on the board to be a legitimate element.
	 * 4. Reader cleanup.
	 * 5. Check the validity of the board state. Every element should be unique.
	 * 6. Return the loaded data.
	 * 
	 * ex A: If savedData doesn't exist, return a single-element array {"FAILED"}.
	 */
	{
		String[] loadData = new String[BOARD_SIZE * BOARD_SIZE + 3];
		
		File savedData = new File(saveName);           // 1.
		
		try
		{
			Scanner fileInput = new Scanner(savedData); // 2.
			for (int itemNo = 0; itemNo < loadData.length; itemNo++) 
			{                                           //
				if (fileInput.hasNext())                 //
				{                                        //
					loadData[itemNo] = fileInput.next();  //
				}                                        //
				else                                     // If there aren't an appropriate number of elements,
				{                                        //
					fileInput.close();                    //
					return new String[] {"FAILED"};       //  then this is likely not a usable file.
				}                                        //
				                                         /////////////
				if (itemNo < BOARD_SIZE * BOARD_SIZE                // 3. Validity checking by checking to be a possible element.
						&& Integer.parseInt(loadData[itemNo]) >= BOARD_SIZE * BOARD_SIZE)
				{                                                   //
					fileInput.close();                               //
					return new String[] {"FAILED"};                  // Return failure if it couldn't be on the board.
				}                                                   //
			}                                                      //
			
			fileInput.close();                          // 4.
		}
		catch (FileNotFoundException ex)                  // A:
		{                                                 //
			return new String[] {"FAILED"};                // this tells the calling method that the loading method failed.
		}                                                 //
		
		for (int firstIndex = 0; firstIndex < loadData.length - 4; firstIndex++) // 5.
		{                                                                        //
			for (int secondIndex = firstIndex + 1;                                // Check every board element
					secondIndex < loadData.length - 3;                              //  against every subsequent board element.
					secondIndex++)                                                  //
			{                                                                     //
				if (loadData[firstIndex].equals(loadData[secondIndex]))            // If two are equal,
				{                                                                  //  Your data's bad.
					return new String[] {"FAILED"};
				}
			}                                                                     
		}
		return loadData;                               // 6.
	}
}

package connect_four;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Dondzila_AI
{
	int player;
	int opponent;
	int timeLimit;
	long startTime;
	int lookAhead = 6;
	MinMaxTree tree = new MinMaxTree();
	ArrayList<MoveNode> possibleMoves = new ArrayList<MoveNode>();
	Random r = new Random();
	
	
	public Dondzila_AI(int[][] board, int player, int timeLimit, long startTime)
	{
		this.player = player;
		this.opponent = player == 1 ? 2 : 1;
		this.timeLimit = timeLimit;
		this.startTime = startTime;
		
		int open = 0;
		
		// Get initial moves
		for (int col = 0; col < 7; col++)
		{
			if (board[0][col] == 0)
			{
				for (int row = 5; row >= 0; row--)
				{
					if (board[row][col] == 0)
					{
						open += row + 1;
						possibleMoves.add( new MoveNode(row, col, 1, player) );
						break;
					}
					
				}
			}
		}
		
		//Adjust lookahead based on open spaces remaining
		//if (open <= 24) lookAhead = 10;
		//else if (open <= 18) lookAhead = 42;
		
		tree.getRoot().addMoves( possibleMoves );
	}
	
	public MoveNode computeMove(Node current, int[][] board)
	{
		int best = Integer.MIN_VALUE;
		ArrayList<MoveNode> bestMoves = new ArrayList<MoveNode>();
		
		if (current == tree.getRoot())
		{
			// On root use threads
			ArrayList<AIRunnable> threads = new ArrayList<AIRunnable>();
			
			for (MoveNode p : current.getPossibleMoves())
			{
				// Get initial values and moves
				computeRank(p, p.getPlayer(), board);
				p.addMoves( current.getPossibleMoves() );
				AIRunnable a = new AIRunnable( p, this, board );
				threads.add( a );
			}
			
			try
			{
				for (AIRunnable r : threads)
				{
					r.runner.join();
				}
			}
			catch (InterruptedException e)
			{
				System.out.println( "Main interrupted" );
			}
			
			// Find best move out of threads
			for (AIRunnable r : threads)
			{
				MoveNode p = r.getMove();
				int rank = p.getRank();
				
    			if (rank > best)
    			{
    				best = rank;
    				bestMoves.clear();
    				bestMoves.add( p );
    			}
    			else if (rank == best)
    			{
    				// Choose best move that has shallow depth
    				if (bestMoves.isEmpty())
    				{
    					bestMoves.add( p );
    				}
    				else if (p.getDepth() < bestMoves.get( 0 ).getDepth())
    				{
    					bestMoves.clear();
        				bestMoves.add( p );
    				}
    				else if (p.getDepth() == bestMoves.get( 0 ).getDepth())
    				{
    					bestMoves.add( p );
    				}
    			}
			}
		}
		else
		{
    		for (MoveNode p : current.getPossibleMoves())
    		{
    			// get rank for each move
    			int row = p.getRow(), col = p.getCol();
    			computeRank(p, p.getPlayer(), board);
    			
    			// move down the tree if needed
    			if (Integer.MAX_VALUE != p.getRank() && p.getHeight() < lookAhead)
    			{
    				p.addMoves( current.getPossibleMoves() );
    				
    				if (!p.getPossibleMoves().isEmpty())
    				{
    					board[row][col] = p.getPlayer();
    					MoveNode m = computeMove(p, board);
    					
    					// set new rank and depth
    					p.setRank( m.getRank() );
    					p.setDepth( m.getDepth() );
    					board[row][col] = 0;
    				}
    				else
    				{
    					p.setDepth( p.getHeight() );
    				}
    			}
    			else
    			{
    				p.setDepth( p.getHeight() );
    			}
    			
    			int rank = p.getRank();
    			
    			// Take best move possible
    			if (Integer.MAX_VALUE != rank)
    			{
        			if (rank > best)
        			{
        				best = rank;
        				bestMoves.clear();
        				bestMoves.add( p );
        			}
        			else if (rank == best)
        			{
        				if (bestMoves.isEmpty())
        				{
        					bestMoves.add( p );
        				}
        				else if (p.getDepth() < bestMoves.get( 0 ).getDepth())
        				{
        					bestMoves.clear();
            				bestMoves.add( p );
        				}
        				else if (p.getDepth() == bestMoves.get( 0 ).getDepth())
        				{
        					bestMoves.add( p );
        				}
        			}
    			}
    			else
    			{
    				best = rank;
    				bestMoves.clear();
    				bestMoves.add( p );
    				break;
    			}
    		}
		}
		
		// In case of equal moves, choose one
		MoveNode ret = bestMoves.get( r.nextInt(bestMoves.size()) );
		
		// Subtract best from current except on root
		if (current != tree.getRoot())
		{
    		ret.setRank( ((MoveNode)current).getRank() - ret.getRank() );
		}
		
		return ret;
	}
	
	void computeRank(MoveNode c, int id, int[][] board)
	{
		String[] dirs = {"ul", "l", "dl", "d", "dr", "r", "ur"};
		Map<String, Integer[]> pRanks = new HashMap<String, Integer[]>();
		
		int oid = c.getOpponent();
		
		// Get rank in each direction
		for (String dir : dirs)
		{
			pRanks.put( dir, dirRank(id, dir, c.getRow(), c.getCol(), 0, -1, board) );
		}
		
		// Get the maximum rank of all directions
		int leftDiag = split( "ul", "dr", id, oid, pRanks );
		int rightDiag = split( "dl", "ur", id, oid, pRanks );
		int horiz = split( "l", "r", id, oid, pRanks );
		int down = downRank( c.getRow(), id, oid, pRanks.get("d") ); //nonSplit( id, oid, pRanks.get("d") );
		int pRank = Math.max( leftDiag, Math.max( rightDiag, Math.max( horiz, down ) ) );
		
		c.setRank(pRank);
	}
	
	// Makes adjustments on diagonals and horizonal ranks
	int split(String dir1, String dir2, int id, int oid, Map<String,Integer[]> pRanks)
	{
		Integer[] s1 = pRanks.get(dir1), s2 = pRanks.get(dir2);
		
		// First adjust single direction
		s1[1] = nonSplit( id, oid, s1 );
		s2[1] = nonSplit( id, oid, s2 );
		
		if (s1[1] == Integer.MAX_VALUE || s2[1] == Integer.MAX_VALUE)
		{
			return Integer.MAX_VALUE;
		}
		
		int rank = 0;
		
		// If pieces are same on both sides accumulate and adjust accordingly
		if (s1[0] == s2[0])
		{
			rank = s1[1] + s2[1];
			
			if (id == s1[0] && rank >= 6)
			{
				rank = Integer.MAX_VALUE;
			}
			else if (oid == s1[0] && rank >= 3)
			{
				rank = Integer.MAX_VALUE / 2;
			}
			// Prevent bad moves
			else if (id == s1[0] && rank <= 4)
			{
				if (s1[2] != 0 && s2[2] != 0)
				{
					rank -= rank >=2 ? 2 : rank;
				}
			}
			else if (oid == s1[0] && rank <= 2)
			{
				if (s1[2] != 0 && s2[2] != 0)
				{
					rank -= rank >=1 ? 1 : 0;
				}
			}
			else
			{
				rank *= 2;
			}
		}
		else // Otherwise adjust indivually and then accumulate
		{
			// Different amounts if pieces are different or
			// there is board edge or open space
			// prevents bad moves
			if (id == s1[0] && oid == s2[0])
			{
				if (s1[1] <= 4 && s1[2] != 0)
				{
					s1[1] -= s1[1] >= 2 ? 2 : s1[1];
				}
				
				if (s2[1] <= 2 && s2[2] != 0)
				{
					s2[1] -= s2[2] >= 1 ? 1 : 0;
				}
			}
			else if (oid == s1[0] && id == s2[0])
			{
				if (s1[1] <= 2 && s1[2] != 0)
				{
					s1[1] -= s1[1] >= 1 ? 1 : 0;
				}
				
				if (s2[1] <= 4 && s2[2] != 0)
				{
					s2[1] -= s2[1] >= 2 ? 2 : s2[1];
				}
			}
			else
			{
				if (id == s1[0])
				{
					if (-1 == s2[0] && s1[1] <= 4)
					{
						s1[1] -= 2;
					}
					else if (0 == s2[0] && s1[1] <= 2)
					{
						s1[1] -= 1;
					}
				}
				else if (oid == s1[0])
				{
					if (-1 == s2[0] && s1[1] <= 2)
					{
						s1[1] -= 1;
					}
				}
				
				if (id == s2[0])
				{
					if (-1 == s1[0] && s2[1] <= 4)
					{
						s2[1] -= 2;
					}
					else if (0 == s1[0] && s2[1] <= 2)
					{
						s2[1] -= 1;
					}
				}
				else if (oid == s2[0])
				{
					if (-1 == s1[0] && s2[1] <= 2)
					{
						s2[1] -= 1;
					}
				}
			}
			
			rank = s1[1] + s2[1];
		}
		
		return rank;
	}
	
	// Adjust the rank when near top, prevents bad moves
	int downRank(int row, int id, int oid, Integer[] s)
	{
		// Different amounts if pieces below are current players or opponents
		if (id == s[0])
		{
			if (s[1] <= 4 && row == 0)
			{
				return s[1] - 2;
			}
			else if (s[1] <=2 && row == 1)
			{
				return s[1] - 1;
			}
		}
		else if (oid == s[0])
		{
			if (s[1] <= 2 && row == 0)
			{
				return s[1] - 1;
			}
			else if (s[1] <=1 && row == 1)
			{
				return s[1];
			}
		}
		
		return nonSplit( id, oid, s );
	}
	
	// Adjusts score for individual directions, forcing wins or block wins
	int nonSplit(int id, int oid, Integer[] s)
	{
		int rank = s[1];
		
		if (id == s[0] && rank >= 6)
		{
			rank = Integer.MAX_VALUE;
		}
		else if (oid == s[0] && rank >= 3)
		{
			rank = Integer.MAX_VALUE / 2;
		}
		
		return rank;
	}
	
	// Gets the rank in a single direction
	Integer[] dirRank(int id, String dir, int row, int col, int rank, int initial, int[][] board)
	{
		boolean move = false;
		int last = -1;
		
		// Move in direction if possible
		switch (dir)
		{
			case "ul":
				if (row != 0 && col != 0)
				{
					move = true;
					row--;
					col--;
				}
				break;
			
			case "l":
				if (col != 0)
				{
					move = true;
					col--;
				}
				break;
				
			case "dl":
				if (row != 5 && col != 0)
				{
					move = true;
					row++;
					col--;
				}
				break;
				
			case "d":
				if (row != 5)
				{
					move = true;
					row++;
				}
				break;
				
			case "dr":
				if (row != 5 && col != 6)
				{
					move = true;
					row++;
					col++;
				}
				break;
				
			case "r":
				if (col != 6)
				{
					move = true;
					col++;
				}
				break;
				
			case "ur":
				if (row != 0 && col != 6)
				{
					move = true;
					row--;
					col++;
				}
				break;
		}
		
		// determine initial piece
		if (initial == -1 && move)
		{
			initial = board[row][col];
		}
		
		// hit edge, no last piece
		if (!move)
		{
			last = 0;
		}
		else
		{
			int oid = id == 1 ? 2 : 1;
			
			// Recursively add to rank based on pieces seen
    		if (id == initial && board[row][col] == id)
    		{
    			return dirRank(id, dir, row, col, rank + 2, initial, board);
    		}
    		else if (oid == initial && board[row][col] == oid)
    		{
    			return dirRank(id, dir, row, col, rank + 1, initial, board);
    		}
    		else
    		{
    			last = board[row][col];
    		}
		}
		
		return new Integer[]{initial, rank, last};
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		long startTime = System.currentTimeMillis();
		int[][] board = new int[6][7];
		String[] tempB = args[1].split( "\\],\\[" );
		String player = args[3];
		int playerNum = player.equals( "player-one" ) ? 1 : 2;
		int timeLimit = Integer.parseInt( args[5] );
		
		for (int i = 0; i < tempB.length; i++)
		{
			board[i] = Arrays.stream( tempB[i].replaceAll( "[\\[\\]]", "" ).split( "," ) )
					.mapToInt(Integer::parseInt).toArray();
		}
		
		Dondzila_AI ai = new Dondzila_AI( board, playerNum, timeLimit, startTime );
		
		MoveNode move = ai.computeMove(ai.tree.getRoot(), board);
		long total = System.currentTimeMillis() - startTime;
		System.out.println( "\nTIME TAKEN: " + (total / 1000.f) + "s\n");
		
		System.exit(move.getCol());
	}
}

// Used to start threads, each thread takes a starting move
class AIRunnable implements Runnable
{
	private volatile MoveNode startNode;
	private Dondzila_AI ai;
	int[][] board;
	Thread runner;
	
	public AIRunnable(MoveNode startNode, Dondzila_AI ai, int[][] board)
	{
		this.startNode = startNode;
		this.ai = ai;
		this.board = new int[6][7];
		
		// Needs a copy of the board
		for (int i = 0; i < 6; i++)
		{
			System.arraycopy( board[i], 0, this.board[i], 0, 7 );
		}
		
		runner = new Thread(this);
		runner.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		// compute the move from starting move
		MoveNode move = ai.computeMove( startNode, board );
		startNode.setRank( move.getRank() );
		startNode.setDepth( move.getDepth() );
	}

	/**
	 * @return the move
	 */
	public MoveNode getMove()
	{
		return this.startNode;
	}
}

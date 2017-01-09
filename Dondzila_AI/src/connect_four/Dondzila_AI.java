/**
 * File:       Dondzila_AI.java
 * Package:    connect_four
 * Project:    connect_four
 * Date:       Jan 6, 2017, 12:13:34 PM
 * Purpose:    
 * @author     Kory Dondzila
 * @version    "%I%, %G%"
 * Copyright:  2017
 */

package connect_four;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * 
 */
public class Dondzila_AI
{
	int[][] board;
	int player;
	int opponent;
	int timeLimit;
	long startTime;
	MinMaxTree tree = new MinMaxTree();
	ArrayList<MoveNode> possibleMoves = new ArrayList<MoveNode>();
	Random r = new Random();
	
	/**
	 * 
	 */
	public Dondzila_AI(int[][] board, int player, int timeLimit, long startTime)
	{
		this.board = board;
		this.player = player;
		this.opponent = player == 1 ? 2 : 1;
		this.timeLimit = timeLimit;
		this.startTime = startTime;
		
		for (int i = 0; i < 7; i++)
		{
			if (board[0][i] == 0)
			{
				for (int j = 5; j >= 0; j--)
				{
					if (board[j][i] == 0)
					{
						possibleMoves.add( new MoveNode(j, i, 1, player) );
						break;
					}
					
				}
			}
		}
		
		tree.getRoot().addMoves( possibleMoves );
	}
	
	public MoveNode computeMove()
	{	
		Node current = tree.getCurrent();
		int best = Integer.MIN_VALUE;
		ArrayList<MoveNode> bestMoves = new ArrayList<MoveNode>();
		
		for (MoveNode p : current.getPossibleMoves())
		{
			tree.setCurrent( p );
			int row = p.getRow(), col = p.getCol();
			String space = "";
			for (int s = 1; s < p.getHeight(); s++)
			{
				space += " ";
			}
			System.out.println( space + "CELL: " + row + ", " + col );
			computeRank(p, p.getPlayer());
			
			if (Integer.MAX_VALUE != p.getRank() && p.getHeight() < 6)
			{
				System.out.println( space + "RANK before: " + p.getRank() );
				p.addMoves( current.getPossibleMoves() );
				
				if (!p.getPossibleMoves().isEmpty())
				{
					board[row][col] = p.getPlayer();
					MoveNode m = computeMove();
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
			System.out.println( space + "RANK: " + rank );
			
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
    				else if (p.getDepth() > bestMoves.get( 0 ).getDepth())
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
		
		MoveNode ret = bestMoves.get( r.nextInt(bestMoves.size()) );
		
		if (current != tree.getRoot())
		{
    		ret.setRank( ((MoveNode)current).getRank() - ret.getRank() );
		}
		
		return ret;
	}
	
	void computeRank(MoveNode c, int id)
	{
		String[] dirs = {"ul", "l", "dl", "d", "dr", "r", "ur"};
		Map<String, Integer[]> pRanks = new HashMap<String, Integer[]>();
		
		int oid = c.getOpponent();
		
		for (String dir : dirs)
		{
			pRanks.put( dir, dirRank(id, dir, c.getRow(), c.getCol(), 0, -1) );
		}
		
		int leftDiag = split( "ul", "dr", id, oid, pRanks );
		int rightDiag = split( "dl", "ur", id, oid, pRanks );
		int horiz = split( "l", "r", id, oid, pRanks );
		int down = downRank( c.getRow(), id, oid, pRanks.get("d") ); //nonSplit( id, oid, pRanks.get("d") );
		int pRank = Math.max( leftDiag, Math.max( rightDiag, Math.max( horiz, down ) ) );
		
		c.setRank(pRank);
	}
	
	int split(String dir1, String dir2, int id, int oid, Map<String,Integer[]> pRanks)
	{
		Integer[] s1 = pRanks.get(dir1), s2 = pRanks.get(dir2);
		s1[1] = nonSplit( id, oid, s1 );
		s2[1] = nonSplit( id, oid, s2 );
		
		if (s1[1] == Integer.MAX_VALUE || s2[1] == Integer.MAX_VALUE)
		{
			return Integer.MAX_VALUE;
		}
		
		int rank = 0;
		
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
		else
		{
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
	
	int downRank(int row, int id, int oid, Integer[] s)
	{
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
	
	Integer[] dirRank(int id, String dir, int row, int col, int rank, int initial)
	{
		boolean move = false;
		int last = -1;
		
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
		
		if (initial == -1 && move)
		{
			initial = board[row][col];
		}
		
		if (!move)
		{
			last = 0;
		}
		
		int oid = id == 1 ? 2 : 1;
		
		if (move)
		{
    		if (id == initial && board[row][col] == id)
    		{
    			return dirRank(id, dir, row, col, rank + 2, initial);
    		}
    		else if (oid == initial && board[row][col] == oid)
    		{
    			return dirRank(id, dir, row, col, rank + 1, initial);
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
		
		MoveNode move = ai.computeMove();
		long total = System.currentTimeMillis() - startTime;
		System.out.println( "\nTIME TAKEN: " + (total / 1000.f) + "s\n");
		
		System.exit(move.getCol());
	}

}

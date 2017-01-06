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
	ArrayList<Cell> possibleMoves = new ArrayList<Cell>();
	
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
	}
	
	public int computeMove()
	{
		for (int i = 0; i < 7; i++)
		{
			if (board[0][i] == 0)
			{
				for (int j = 5; j >= 0; j--)
				{
					if (board[j][i] == 0)
					{
						possibleMoves.add( new Cell(j, i) );
						break;
					}
					
				}
			}
		}
		
		int best = -1;
		ArrayList<Integer> bestCols = new ArrayList<Integer>();
		
		for (Cell p : possibleMoves)
		{
			System.out.println( "CELL: " + p.getRow() + ", " + p.getCol() );
			computeRank(p);
			System.out.println( "RANK: " + p.getRank() );
			if (p.getRank() > best)
			{
				best = p.getRank();
				bestCols.clear();
				bestCols.add( p.getCol() );
			}
			else if (p.getRank() == best)
			{
				bestCols.add( p.getCol() );
			}
		}
		
		Random r = new Random();
		
		return bestCols.get( r.nextInt(bestCols.size()) );
	}
	
	void computeRank(Cell p)
	{
		String[] dirs = {"ul", "l", "dl", "d", "dr", "r", "ur"};
		Map<String, Integer> pRanks = new HashMap<String, Integer>();
		Map<String, Integer> oRanks = new HashMap<String, Integer>();
		int row = p.getRow();
		int col = p.getCol();
		
		board[row][col] = player;
		
		for (String dir : dirs)
		{
			pRanks.put( dir, dirRank(dir, row, col, 0, -1) );
			if (row - 1 >= 0)
			{
				int tempP = player;
    			player = opponent;
    			opponent = tempP;
    			oRanks.put( dir, dirRank(dir, row - 1, col, 0, -1) );
    			tempP = player;
    			player = opponent;
    			opponent = tempP;
			}
		}
		
		board[row][col] = 0;
		int pRank = 0, oRank = 0;
		
		pRank = Math.max( pRanks.get( "ul" ) + pRanks.get( "dr" ), 
		            Math.max( pRanks.get( "l" ) + pRanks.get( "r" ), 
		            Math.max( pRanks.get( "dl" ) + pRanks.get( "ur" ), pRanks.get( "d" ) ) ) );
		
		if (row - 1 >= 0)
		{
			oRank = Math.max( oRanks.get( "ul" ) + oRanks.get( "dr" ), 
		  		        Math.max( oRanks.get( "l" ) + oRanks.get( "r" ), 
		  			    Math.max( oRanks.get( "dl" ) + oRanks.get( "ur" ), oRanks.get( "d" ) ) ) );
		}
		
		System.out.println( "pRank: " + pRank + " oRank: " + oRank );
		
		p.setRank( pRank - oRank );
	}
	
	int dirRank(String dir, int row, int col, int rank, int initial)
	{
		boolean move = false;
		
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
		
		if (initial == -1)
		{
			initial = board[row][col];
		}
		
		if (move && player == initial && board[row][col] == player)
		{
			rank = dirRank( dir, row, col, rank + 2, initial);
		}
		else if (move && opponent == initial && board[row][col] == opponent)
		{
			rank = dirRank( dir, row, col, rank + 1, initial);
		}
		
		return rank;
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
		
		int col = ai.computeMove();
		long total = System.currentTimeMillis() - startTime;
		System.out.println( "\nTIME TAKEN: " + (total / 1000.f) + "s\n");
		
		System.exit(col);
	}

}

class Cell
{
	private int row = -1;
	private int col = -1;
	private int rank = 0;
	
	public Cell(int row, int col)
	{
		this.col = col;
		this.row = row;
	}

	/**
	 * @return the col
	 */
	public int getCol()
	{
		return this.col;
	}

	/**
	 * @return the row
	 */
	public int getRow()
	{
		return this.row;
	}

	/**
	 * @return the rank
	 */
	public int getRank()
	{
		return this.rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank( int rank )
	{
		this.rank = rank;
	}
}

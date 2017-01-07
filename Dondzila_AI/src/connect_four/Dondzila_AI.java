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
		
		int best = Integer.MIN_VALUE;
		ArrayList<Integer> bestCols = new ArrayList<Integer>();
		Random r = new Random();
		
		for (Cell p : possibleMoves)
		{
			int row = p.getRow(), col = p.getCol();
			System.out.println( "CELL: " + row + ", " + col );
			computeRank(p, player);
			board[row][col] = player;
			int oBest = Integer.MIN_VALUE;
			ArrayList<Cell> oBbestCells = new ArrayList<Cell>();
			
			for (Cell o: possibleMoves)
			{
				if (p.equals( o ))
				{
					if (row > 0)
					{
						Cell temp = new Cell(row - 1, col);
						System.out.println( "O CELL: " + temp.getRow() + ", " + temp.getCol() );
						computeRank( temp, opponent );
						o.setRank( temp.getRank( opponent ), opponent );
					}
				}
				else
				{
					System.out.println( "O CELL: " + o.getRow() + ", " + o.getCol() );
					computeRank( o, opponent );
				}
				
				if (o.getRank( opponent ) > oBest)
				{
					oBest = o.getRank( opponent );
					oBbestCells.clear();
					oBbestCells.add( o );
				}
				else if (o.getRank( opponent ) == oBest)
				{
					oBbestCells.add( o );
				}
			}
			
			board[row][col] = 0;
			Cell oBestCell = oBbestCells.get( r.nextInt( oBbestCells.size() ) );
			System.out.println( "pRank: " + p.getRank(player) + " oRank: " + oBestCell.getRank(opponent) );
			int rank = p.getRank(player) - oBestCell.getRank(opponent);
			System.out.println( "RANK: " + rank );
			
			if (rank > best)
			{
				best = rank;
				bestCols.clear();
				bestCols.add( p.getCol() );
			}
			else if (rank == best)
			{
				bestCols.add( p.getCol() );
			}
		}
		
		
		return bestCols.get( r.nextInt(bestCols.size()) );
	}
	
	void computeRank(Cell c, int id)
	{
		String[] dirs = {"ul", "l", "dl", "d", "dr", "r", "ur"};
		Map<String, Integer[]> pRanks = new HashMap<String, Integer[]>();
		
		int oid = id == 1 ? 2 : 1;
		
		for (String dir : dirs)
		{
			Integer[] dRank = dirRank(id, dir, c.getRow(), c.getCol(), 0, -1);
			if (id == dRank[0] && dRank[1] >= 6)
			{
				dRank[1] += 10;
			}
			else if (oid == dRank[0] && dRank[1] >= 3)
			{
				dRank[1] += 5;
			}
			
			pRanks.put( dir, dRank );
		}
		
		int leftDiag = split( "ul", "dr", id, oid, pRanks );
		int rightDiag = split( "dl", "ur", id, oid, pRanks );
		int horiz = split( "l", "r", id, oid, pRanks );
		
		Integer[] d = pRanks.get( "d" );
		int down = d[1];
		if (player == id && id == d[0] && down >= 6)
		{
			down = Integer.MAX_VALUE;
		}
		else if (player == id && oid == d[0] && down >= 3)
		{
			down = Integer.MAX_VALUE / 2;
		}
		
		int pRank = Math.max( leftDiag, Math.max( rightDiag, Math.max( horiz, down ) ) );
		
		c.setRank(pRank, id);
	}
	
	int split(String dir1, String dir2, int id, int oid, Map<String,Integer[]> pRanks)
	{
		Integer[] s1 = pRanks.get(dir1), s2 = pRanks.get(dir2);
		int rank = s1[1] + s2[1];
		
		if (s1[0] == s2[0])
		{
			if (player == id && id == s1[0] && rank >= 6)
			{
				rank = Integer.MAX_VALUE;
			}
			else if (player == id && oid == s1[0] && rank >= 3)
			{
				rank = Integer.MAX_VALUE / 2;
			}
			else
			{
				rank *= 2;
			}
		}
		
		return rank;
	}
	
	Integer[] dirRank(int id, String dir, int row, int col, int rank, int initial)
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
		
		int oid = id == 1 ? 2 : 1;
		
		if (move && id == initial && board[row][col] == id)
		{
			rank = dirRank(id, dir, row, col, rank + 2, initial)[1];
		}
		else if (move && oid == initial && board[row][col] == oid)
		{
			rank = dirRank(id, dir, row, col, rank + 1, initial)[1];
		}
		
		return new Integer[]{initial, rank};
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
	private int rankP1 = 0;
	private int rankP2 = 0;
	
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
	public int getRank(int id)
	{
		return id == 1 ? this.rankP1 : this.rankP2;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank( int rank, int id )
	{
		if (id == 1)
			this.rankP1 = rank;
		else
			this.rankP2 = rank;
	}
}

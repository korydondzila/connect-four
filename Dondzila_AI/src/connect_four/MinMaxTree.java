/**
 * File:       MinMaxTree.java
 * Package:    connect_four
 * Project:    Dondzila_AI
 * Date:       Jan 8, 2017, 2:39:17 PM
 * Purpose:    
 * @author     Kory Dondzila
 * @version    "%I%, %G%"
 * Copyright:  2017
 */

package connect_four;

import java.util.ArrayList;

/**
 * 
 */
public class MinMaxTree
{
	private Node root;
	private Node current;
	
	/**
	 * 
	 */
	public MinMaxTree()
	{
		root = new Node();
		current = root;
	}

	/**
	 * @return the root
	 */
	public Node getRoot()
	{
		return this.root;
	}

	/**
	 * @return the current
	 */
	public Node getCurrent()
	{
		return this.current;
	}

	/**
	 * @param current the current to set
	 */
	public void setCurrent( MoveNode current )
	{
		this.current = current;
	}
}

class Node
{
	protected ArrayList<MoveNode> possibleMoves;
	
	public Node()
	{
		possibleMoves = new ArrayList<MoveNode>();
	}
	
	public ArrayList<MoveNode> getPossibleMoves()
	{
		return possibleMoves;
	}
	
	public void addMoves(ArrayList<MoveNode> moves)
	{
		for (MoveNode move : moves)
		{
			possibleMoves.add( new MoveNode( move.getRow(), move.getCol(), move.getHeight(), move.getPlayer() ) );
		}
	}
}

class MoveNode extends Node
{
	private int row;
	private int col;
	private int rank = 0;
	private int height;
	private int depth = 0;
	private int player;
	private int opponent;
	
	public MoveNode(int row, int col, int height, int player)
	{
		this.col = col;
		this.row = row;
		this.height = height;
		this.player = player;
		this.opponent = player == 1 ? 2 : 1;
	}
	
	public void addMoves(ArrayList<MoveNode> moves)
	{
		for (MoveNode move : moves)
		{
			if (row == move.getRow() && col == move.getCol())
			{
				if (0 < row)
				{
					possibleMoves.add( new MoveNode( move.getRow() - 1, move.getCol(), move.getHeight() + 1, opponent ) );
				}
			}
			else
			{
				possibleMoves.add( new MoveNode( move.getRow(), move.getCol(), move.getHeight() + 1, opponent ) );
			}
		}
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
	 * @return the height
	 */
	public int getHeight()
	{
		return this.height;
	}

	/**
	 * @return the player
	 */
	public int getPlayer()
	{
		return this.player;
	}

	/**
	 * @return the opponent
	 */
	public int getOpponent()
	{
		return this.opponent;
	}

	/**
	 * @return the depth
	 */
	public int getDepth()
	{
		return this.depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth( int depth )
	{
		this.depth = depth;
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

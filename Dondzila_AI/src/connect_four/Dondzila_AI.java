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

import java.util.Random;

/**
 * 
 */
public class Dondzila_AI
{

	/**
	 * 
	 */
	public Dondzila_AI()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		
		Random rand = new Random();
		
		int col = rand.nextInt( 7 );
		
		System.exit(col);
	}

}

package uk.me.thega.graph;

/**
 * Class that will generate DOT compliant colours in a loop.
 * 
 * @author pwhittlesea
 *
 */
public class DotColours {
	
	/** The colours we want to loop through. */
	static String[] colours = {
		"red", 
		"blue", 
		"green", 
		"yellow", 
		"orange", 
		"purple", 
		"maroon", 
		"brown", 
		"greenyellow"
	};

	/** The current colour. */
	private int current = 0;

	/**
	 * Get the next colour in the list.
	 * 
	 * @return the string representation of the colour.
	 */
	public String next() {
		if (current++ >= colours.length - 1)  {
			current = 0;
		}
		return current();
	}
	
	/**
	 * Get the current colour in the list.
	 * 
	 * @return the string representation of the colour.
	 */
	public String current() {
		return colours[current];
	}
}

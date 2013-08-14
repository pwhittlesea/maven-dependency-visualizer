package uk.me.thega.graph;

import org.junit.Assert;

import org.junit.Test;

import uk.me.thega.graph.DotColours;

/**
 * Test that the looping and functionality of the {@link DotColours} class
 * is correct.
 * 
 * @author pwhittlesea
 *
 */
public class DotColoursTest {

	/**
	 * Test that the first colour we get is the second colour in the list
	 * if we call next.
	 */
	@Test
	public void testFirstColour() {
		final DotColours colours = new DotColours();
		Assert.assertEquals(DotColours.colours[1], colours.next());
	}

	/**
	 * Test that the current will return the first colour in the list.
	 */
	@Test
	public void testCurrentBeforeNext() {
		final DotColours colours = new DotColours();
		Assert.assertEquals(DotColours.colours[0], colours.current());
	}

	/**
	 * Test that we can loop happily over the list again and again.
	 */
	@Test
	public void testLooping() {
		final DotColours colours = new DotColours();
		for (int i = 0; i < DotColours.colours.length + 10; i++) {
			colours.next();
		}
	}
}

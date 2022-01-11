/**
 * 
 */
/**
 * @author atri
 *
 */

package mathtest;

import math.Arithmetic;
import static org.junit.Assert.*;

import org.junit.Test;

public class DemoTest {

	@Test
	public void testMultiply() {
		Arithmetic tester = new Arithmetic();
	    assertEquals("20 x 5 must be 100", 100, tester.multiply(20, 5));
	}

	@Test
	public void testAdd() {
		Arithmetic tester = new Arithmetic();
	    assertEquals("20 + 5 must be 25", 25, tester.add(20, 5));
	}

	@Test
	public void testSubtract() {
		Arithmetic tester = new Arithmetic();
	    assertEquals("20 - 5 must be 15", 15, tester.subtract(20, 5));
	}

	@Test
	public void testDivide() {
		Arithmetic tester = new Arithmetic();
		double result = tester.divide(20, 5);
		System.out.println(result);
	    assertEquals("20 / 5 must be 4", 4, (int)tester.divide(20, 5));
	}

}

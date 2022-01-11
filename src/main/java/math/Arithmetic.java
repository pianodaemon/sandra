package math;

public class Arithmetic {
	public static int multiply(int x, int y) { return x*y; }
	public static int add(int x, int y) { return x+y; }
	public static int subtract(int x, int y) { return x-y; }
	public static double divide(int x, int y) { return (x*1.0)/y; }
	
	public static void main(String[] args) {
		int operand1 = Integer.valueOf(args[0]);
		int operand2 = Integer.valueOf(args[1]);
		
		System.out.println("Result := " + divide(operand1, operand2));
		
	}
}

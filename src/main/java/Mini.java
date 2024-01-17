/*
 * Copyright (c) 2023. Alexander Voglsperger
 * https://wtf-my-code.works
 */

public class Mini {
	public static void main(String[] args) { // Assumption the path to the input file is correct

		// Create a new scanner and parser object with the input file
		final Scanner scanner = new Scanner(args[0]);
		final Parser parser = new Parser(scanner);
		// Parse the input file
		parser.Parse();
		// Print number of errors detected
		if(parser.errors.count > 0) {
			System.out.println("Number of errors detected: " + parser.errors.count);
		} else {
			System.out.println("No errors detected");
		}
	}
}
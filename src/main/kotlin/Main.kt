fun main(args: Array<String>) { // Assumption the path to the input file is correct
	if (args.isEmpty()) {
		throw Error("One argument for the input file expected")
	} else if (args.size > 1) {
		System.err.println("More than one argument received, only considering the first one!")
	}

	// Create a new scanner and parser object with the input file
	val scanner = Scanner(args[0])
	val parser = Parser(scanner)

	// Parse the input file
	parser.Parse()
	// Print number of errors detected
	if (parser.errors.count > 0) {
		println("Number of errors detected: " + parser.errors.count)
	} else {
		println("No errors detected")
	}
}
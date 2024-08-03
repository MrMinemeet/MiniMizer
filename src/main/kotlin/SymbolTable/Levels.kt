package SymbolTable

enum class Levels(val value: Int) {
	UNKNOWN(-2),
	UNIVERSE(-1),
	PROGRAM(0);

	operator fun inc(): Levels {
		return when (this) {
			UNKNOWN -> throw IllegalStateException("Unknown level")
			UNIVERSE -> PROGRAM
			PROGRAM -> throw IllegalStateException("No more levels")
		}
	}

	operator fun dec(): Levels {
		return when (this) {
			UNKNOWN -> throw IllegalStateException("Unknown level")
			UNIVERSE -> throw IllegalStateException("Already in universe scope")
			PROGRAM -> UNIVERSE
		}
	}
}
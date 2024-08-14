package controlFlowGraph

/**
 * Represents an operation in the control flow graph.
 * Each operation has a specific meaning and can be used to perform a specific operation on the operands.
 * A "-" denotes that the operand is not used.
 */
enum class Operation(val symbol: String) {
	/**
	 * Negate
	 *
	 * `x neg -`
 	 */
	NEG ("-"),

	/**
	 * Add
	 *
	 * `x plus y`
	 */
	PLUS("+"),

	/**
	 * Subtract
	 *
	 * `x minus y`
	 */
	MINUS("-"),

	/**
	 * Multiply
	 *
	 * `x times y`
	 */
	TIMES("*"),

	/**
	 * Divide
	 *
	 * `x div y`
	 */
	DIV("/"),

	/**
	 * Remainder (aka. modulo)
	 *
	 * `x rem y`
	 */
	REM("%"),

	/**
	 * Compare
	 *
	 * `x cmp y`
	 */
	CMP("CMP"),

	/**
	 * Phi instruction
	 *
	 * `x phi opds`
	 */
	PHI("PHI"),

	/**
	 * Load
	 *
	 * `x ld y`
	 */
	LD("LD"),

	/**
	 * Load register y (only used after removal of phi instructions)
	 *
	 * `- lr y`
	 */
	LR("LR"),

	/**
	 * Load constant y (only used after removal of phi instructions)
	 *
	 *
	 */
	LC("LC"),

	/**
	 * Store y to the memory address denoted by x
	 *
	 * `x store y`
	 */
	ST("ST"),

	/**
	 * Assign y to the variable x
	 *
	 * `x ass y`
	 */
	ASS(":="),

	/**
	 * Read integer value
	 *
	 * `- read -`
	 */
	READ("RD"),

	/**
	 * Write integer value y
	 *
	 * `- write y`
	 */
	WRITE("WRT"),

	/**
	 * Return (used at end of the program)
	 *
	 * `- ret -`
	 */
	RET("RET"),

	/**
	 * Branch to block y
	 *
	 * `- br y`
	 */
	BR("BR"),

	/**
	 * Branch to block y if x references `a cmp b` and `a < b`
	 *
	 * `x blt y`
	 */
	BLT("BLT"),

	/**
	 * Branches to block y if x references `a cmp b` and `a == b`
	 *
	 * `x beq y`
	 */
	BEQ("BEQ"),

	/**
	 * Branches to block y if x references `a cmp b` and `a > b`
	 *
	 * `x bgt y`
	 */
	BGT("BGT"),

	/**
	 * Branches to block y if x references `a cmp b` and `a >= b`
	 *
	 * `x bge y`
	 */
	BGE("BGE"),

	/**
	 * Branches to block y if x references `a cmp b` and `a != b`
	 */
	BNE("BNE"),

	/**
	 * Branches to block y if x references `a cmp b` and `a <= b`
	 */
	BLE("BLE")
}
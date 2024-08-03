package controlFlowGraph

/**
 * Represents an operation in the control flow graph.
 * Each operation has a specific meaning and can be used to perform a specific operation on the operands.
 * A "-" denotes that the operand is not used.
 */
enum class Operation {
	/**
	 * Negate
	 *
	 * `x neg -`
 	 */
	NEG,

	/**
	 * Add
	 *
	 * `x plus y`
	 */
	PLUS,

	/**
	 * Subtract
	 *
	 * `x minus y`
	 */
	MINUS,

	/**
	 * Multiply
	 *
	 * `x times y`
	 */
	TIMES,

	/**
	 * Divide
	 *
	 * `x div y`
	 */
	DIV,

	/**
	 * Remainder (aka. modulo)
	 *
	 * `x rem y`
	 */
	REM,

	/**
	 * Compare
	 *
	 * `x cmp y`
	 */
	CMP,

	/**
	 * Phi instruction
	 *
	 * `x phi opds`
	 */
	PHI,

	/**
	 * Load
	 *
	 * `x ld y`
	 */
	LD,

	/**
	 * Load register y (only used after removal of phi instructions)
	 *
	 * `- lr y`
	 */
	LR,

	/**
	 * Load constant y (only used after removal of phi instructions)
	 *
	 *
	 */
	LC,

	/**
	 * Store y to the memory address denoted by x
	 *
	 * `x store y`
	 */
	ST,

	/**
	 * Assign y to the variable x
	 *
	 * `x ass y`
	 */
	ASS,

	/**
	 * Read integer value
	 *
	 * `- read -`
	 */
	READ,

	/**
	 * Write integer value y
	 *
	 * `- write y`
	 */
	WRITE,

	/**
	 * Return (used at end of the program)
	 *
	 * `- ret -`
	 */
	RET,

	/**
	 * Branch to block y
	 *
	 * `- br y`
	 */
	BR,

	/**
	 * Branch to block y if x references `a cmp b` and `a < b`
	 *
	 * `x blt y`
	 */
	BLT,

	/**
	 * Branches to block y if x references `a cmp b` and `a == b`
	 *
	 * `x beq y`
	 */
	BEQ,

	/**
	 * Branches to block y if x references `a cmp b` and `a > b`
	 *
	 * `x bgt y`
	 */
	BGT,

	/**
	 * Branches to block y if x references `a cmp b` and `a >= b`
	 *
	 * `x bge y`
	 */
	BGE,

	/**
	 * Branches to block y if x references `a cmp b` and `a != b`
	 */
	BNE,

	/**
	 * Branches to block y if x references `a cmp b` and `a <= b`
	 */
	BLE
}
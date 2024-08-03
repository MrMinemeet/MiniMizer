package controlFlowGraph

/**
 * Represents an instruction in the control flow graph.
 *
 * Each instruction has two operants that can reference a variable, a constant or some other instruction.
 * Some instructions have operants that are not used and are represented as `null`.
 */
class Instruction: Node() {
	/**
	 * The instruction left of the operator
	 */
	var first: Node? = null

	/**
	 * The instruction right of the operator
	 */
	var second: Node? = null

	/**
	 * The operator
	 */
	var operator: Operation? = null

	/**
	 * The previous instruction
	 */
	var prev: Instruction? = null

	/**
	 * The next instruction
	 */
	var next: Instruction? = null

	/**
	 * The block the instruction belongs to
	 */
	var block: Block? = null
}
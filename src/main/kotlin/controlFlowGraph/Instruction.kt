package controlFlowGraph

/**
 * Represents an instruction in the control flow graph.
 *
 * Each instruction has two operants that can reference a variable, a constant or some other instruction.
 * Some instructions have operants that are not used and are represented as `null`.
 */
// I want to keep the comments
class Instruction: Node {
	companion object {
		private var curId = 0
		fun getNewInstrId(): Int {
			return curId++
		}
	}

	val instrId = getNewInstrId()

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

	constructor(first: Node?, operator: Operation, second: Node?) {
		this.first = first
		this.operator = operator
		this.second = second
	}

	constructor(first: Node, operator: Operation): this(first, operator, null)

	constructor(operator: Operation, second: Node): this(null, operator, second)

	init {
		// FIXME: This is somehow always true
		/*
		if (first == null && second == null) {
			System.err.println("At least one of the operands must be non-null")
		}*/
	}

	override fun toString() = toIrPrintString()

	override fun toIrPrintString(): String {
		return "${first?.toIrPrintString() ?: ""} $operator ${second?.toIrPrintString() ?: ""}"
	}
}
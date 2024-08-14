package controlFlowGraph

/**
 * Represents a constant in the control flow graph
 */
@Suppress("ConvertSecondaryConstructorToPrimary") // I want to keep the comments
class Constant: Node {
	/**
	 * The value of the constant
	 */
	val value: Int

	constructor (value: Int) {
		this.value = value
	}

	override fun toString() = value.toString()
}
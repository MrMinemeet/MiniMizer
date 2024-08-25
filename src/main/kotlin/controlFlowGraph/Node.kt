package controlFlowGraph
/**
 * Represents a node in the control flow graph
 */
abstract class Node {
	private companion object {
		private var id = 0
		private fun getNewId(): Int {
			return id++
		}
	}

	/**
	 * The unique identifier of the block
	 */
	val id = getNewId()

	/**
	 * Type of the node
	 */
	val type = NodeType.NORMAL

	/**
	 * Provides the representation of the node for debugging purposes.
	 * By default, it returns [toString]
	 * @return The representation of the node
	 */
	open fun toIrPrintString() = toString()
}
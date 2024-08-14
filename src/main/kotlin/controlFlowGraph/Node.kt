package controlFlowGraph

import java.util.*

/**
 * Represents a node in the control flow graph
 */
open class Node {
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
}
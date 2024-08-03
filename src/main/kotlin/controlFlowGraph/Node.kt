package controlFlowGraph

import java.util.*

/**
 * Represents a node in the control flow graph
 */
open class Node {
	/**
	 * The unique identifier of the block
	 */
	val id = UUID.randomUUID() ?: throw Error("UUID generation failed")

	/**
	 * Type of the node
	 */
	val type = NodeType.NORMAL
}
package controlFlowGraph

import java.util.*

/**
 * Represents the longest possible instruction sequence without any branches
 * * no jumps into this sequence (except to the first instruction)
 * * no jumps out of this sequence (except from the last instruction)
 * * no inner jumps
 */
class Block {
	enum class Kind {
		NORMAL, LOOP_HEADER, JOIN_BLOCKS
	}

	/**
	 * The unique identifier of the block
	 */
	val id = UUID.randomUUID() ?: throw Error("UUID generation failed")

	/**
	 * Block kind
	 */
	val kind: Kind = Kind.NORMAL

	/**
	 * Sequential successor
	 */
	var seqSuc: Block? = null

	/**
	 * Successor when following a jump
	 */
	var jumpSuc: Block? = null


	/**
	 * List of predecessor blocks
	 */
	val preds = mutableListOf<Block>()

	/**
	 * First instruction in the block
	 */
	var first: Instruction? = null

	/**
	 * Last instruction in the block
	 */
	var last: Instruction? = null
}
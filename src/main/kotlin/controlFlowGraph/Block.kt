package controlFlowGraph

import java.util.*

/**
 * Represents the longest possible instruction sequence without any branches
 * * no jumps into this sequence (except to the first instruction)
 * * no jumps out of this sequence (except from the last instruction)
 * * no inner jumps
 */
// I want to keep the comments
class Block: Iterable<Instruction> {
	companion object {
		private var id = 0
		private fun getNewId(): Int {
			return id++
		}

		/**
		 * Iterates over all blocks reachable from the given block.
		 * @param block The block to start the iteration from
		 * @param visited Set of visited blocks. Empty set by default when called from outside
		 * @param action Action to perform on each block
		 */
		fun iterateBlocks(block: Block, visited: MutableSet<Block> = mutableSetOf(), action: (Block) -> Unit) {
			if (visited.contains(block)) {
				return
			}
			visited.add(block)
			action(block)
			block.seqSuc?.let { iterateBlocks(it, visited, action) }
			block.jumpSuc?.let { iterateBlocks(it, visited, action) }
		}
	}

	enum class Kind {
		NORMAL, LOOP_HEADER, JOIN_BLOCKS
	}

	/**
	 * The unique identifier of the block
	 */
	val id = getNewId()

	/**
	 * Block kind
	 */
	val kind: Kind = Kind.NORMAL

	/**
	 * Sequential successor
	 *
	 * Called *left* in the examples on the slides.
	 */
	var seqSuc: Block? = null

	/**
	 * Successor when following a jump
	 *
	 * Called *right* in the examples on the slides.
	 */
	var jumpSuc: Block? = null

	/**
	 * Dominator, i.e., the block that occurs before this block.
	 *
	 * `null` if this block is the entry block.
	 */
	var dom: Block? = null

	/**
	 * List of predecessor blocks
	 */
	val preds = mutableListOf<Block>()

	/**
	 * First instruction in the block
	 */
	var first: Instruction? = null;

	/**
	 * Last instruction in the block
	 */
	var last: Instruction? = null

	constructor(dominator: Block? = null) {
		this.dom = dominator
	}

	constructor(dominator: Block, first: Instruction): this(dominator) {
		this.first = first
		this.last = first
	}

	fun appendInstr(instr: Instruction) {
		if (first == null) {
			first = instr
			last = instr
		} else {
			last?.next = instr
			instr.prev = last
			last = instr
		}
	}

	/**
	 * Prints the block in the style suggested by the example in the assignment description.
	 */
	override fun toString(): String {
		val sb = StringBuilder()

		// Head of block
		sb.append("$id --- seqSuc: ${seqSuc?.id} -- jumpSuc: ${jumpSuc?.id} -- dom: ${dom?.id} -- pred: ${preds.map { it.id }}\n")

		// Add instructions
		for (instr in this) {
			sb.append("\t$instr\n")
		}

		return sb.toString()
	}

	/**
	 * Iterator for the instructions in the block.
	 */
	override fun iterator(): Iterator<Instruction> {
		return object : Iterator<Instruction> {
			var current: Instruction? = first

			override fun hasNext(): Boolean {
				return current != null
			}

			override fun next(): Instruction {
				val result = current ?: throw NoSuchElementException("No more instructions in block!")
				current = result.next
				return result
			}
		}
	}
}
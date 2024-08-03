package symbolTable

/**
 * Represents a struct in the symbol table.
 *
 * @param kind The kind of the struct.
 * @param elemType Type of array elements (if kind is ARRAY).
 */
class Struct(val kind: Kind, val elemType: Struct? = null) {
	enum class Kind {
		NONE, INTEGER, ARRAY;

		companion object {
			fun fromString(kind: String): Kind = when (kind) {
				"none" -> NONE
				"ARRAY" -> ARRAY
				"INTEGER" -> INTEGER
				else -> throw IllegalArgumentException("Invalid kind: '$kind'")
			}
		}
	}

	constructor(elemType: Struct) : this(Kind.ARRAY, elemType)


	override fun toString(): String {
		return when (kind) {
			Kind.INTEGER, Kind.NONE -> kind.toString()
			Kind.ARRAY -> "$elemType[]"
		}
	}

	/**
	 * Checks if this struct is equal to another struct.
	 * Two structs are equal if they have the same kind and the same element type (if they are arrays).
	 */
	fun isEqual(other: Struct): Boolean =
		if (kind == Kind.ARRAY)
			other.kind == Kind.ARRAY && elemType!!.isEqual(other.elemType!!)
		else
			kind == other.kind

	fun isRefType() = kind == Kind.ARRAY
}
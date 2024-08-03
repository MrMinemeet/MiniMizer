package SymbolTable

class Obj(val kind: Kind, val name: String, val type: Struct? = null, val value: Int = 0) {
	enum class Kind {
		VARIABLE, TYPE, PROGRAM, CONSTANT
	}

	var level: Levels = Levels.UNKNOWN
	val locals: MutableMap<String, Obj> = mutableMapOf()

	override fun toString() = "Obj '$name' of kind $kind with type $type ${if (value != 0) "and value $value" else ""}"
}
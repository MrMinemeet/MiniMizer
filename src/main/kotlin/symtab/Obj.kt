package symtab

class Obj(val kind: Kind, val name: String, val type: Struct? = null) {
	enum class Kind {
		VARIABLE, TYPE, PROGRAM
	}

	var level: Levels = Levels.UNKNOWN
	val locals: MutableMap<String, Obj> = mutableMapOf()

	override fun toString() = "Obj '$name' of kind $kind with type $type"
}
package symtab

class Scope(val outer: Scope?, val level: Levels? = Levels.UNKNOWN) {
	// Maps declaration of locals from name to object
	private val locals: MutableMap<String, Obj> = mutableMapOf()
	/// Maps declaration of local from name to object
	val immutableLocals = locals.toMap()
	/// Number of local variables in the scope
	private var nVars: Int = 0

	/**
	 * Try to find object by name in the current scope and outer scopes.
	 * Starts at the current scope and goes up to the outermost scope incrementally.
	 * @param name The name of the object
	 * @return The object
	 */
	fun findGlobal(name: String): Obj? {
		var res = findLocal(name)
		if (res == null && outer != null) {
			res = outer.findGlobal(name)
		}
		return res
	}

	/**
	 * Find a local object by name
	 * @param name The name of the object
	 * @return The object
	 */
	fun findLocal(name: String) = locals[name]

	/**
	 * Insert a new object into the current scope.
	 * @param obj The object to insert
	 */
	fun insert(obj: Obj) {
		locals[obj.name] = obj
		if (obj.kind == Obj.Kind.VARIABLE) {
			// Increment number of local variables
			nVars++
		}
	}

	override fun toString() = "Scope '$level' with $nVars local variable(s) and ${locals.size} entries"
}
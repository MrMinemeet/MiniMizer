package symbolTable

import Parser

class SymTab(private val parser: Parser) {
	companion object {
		// Universe types
		val INT_TYPE = Struct(Struct.Kind.INTEGER)
		val NO_TYPE = Struct(Struct.Kind.NONE)
	}

	// Define universe objects
	val intObj: Obj by lazy {
		insert(Obj.Kind.CONSTANT, "int", INT_TYPE, -1)
	}

	val arrObj: Obj by lazy {
		insert(Obj.Kind.CONSTANT, "arr", Struct(Struct.Kind.ARRAY))
	}

	// Init scope for universe
	private var curLevel = Levels.UNIVERSE
	var curScope = Scope(null, curLevel)

	/**
	 * Open a new scope and moves to the inner scope
	 * @throws IllegalStateException If the current level is already in the innermost scope
	 */
	fun openScope() {
		this.curLevel++
		curScope = Scope(curScope, curLevel)
	}

	/**
	 * Close the current scope and moves to the outer scope
	 * @throws IllegalStateException If the current level is already in the outermost scope
	 */
	fun closeScope() {
		this.curLevel--
		curScope = curScope.outer!!
	}

	/**
	 * Insert a new object into the current scope.
	 * @param kind The kind of object
	 * @param name The name of the object
	 * @param type The type of the object (if applicable)
	 * @throws Error If the name already exists in the current scope
	 */
	fun insert(kind: Obj.Kind, name: String, type: Struct? = null, value: Int = 0): Obj {
		// Check if name already exists in current scope
		if (curScope.findLocal(name) != null) {
			throw Error("(Line ${parser.scanner.lineNr}) Name $name already exists in current scope")
		}

		// Does not exist -> Create
		val newObj = Obj(kind, name, type, value)
		newObj.level = curLevel

		// Add to current scope
		curScope.insert(newObj)

		// Return the new object
		return newObj
	}

	/**
	 * Find an object by name
	 * @param name The name of the object
	 * @return The object
	 * @throws Error If the name is not found in any scope
	 */
	fun find(name: String): Obj {
		// Search from current scope to outer scopes
		var scope: Scope? = curScope
		while (scope != null) {
			val obj = scope.findLocal(name)
			if (obj != null) {
				return obj
			}
			scope = scope.outer
		}

		if (name == "arrayAccess") {
			return arrObj
		}

		// Not found
		throw Error("(Line ${parser.scanner.lineNr}) Name '$name' not found in any scope")
	}

	/**
	 * Updates the SSA and adds the object as a new SSA object
	 * @param obj The object to add as a new SSA object
	 */
	fun addAsNewSSA(obj: Obj): Obj {
		val increasedSSA = insert(obj.kind, obj.getNewSsaName(), obj.objType, obj.value)
		return increasedSSA
	}
}
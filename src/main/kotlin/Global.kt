object Global {
	const val DEBUG = true

	/**
	 * Prints a debug message if [DEBUG] is true.
	 * The message will be lazily evaluated.
	 */
	@JvmStatic
	fun debug(fn: () -> String) {
		if (DEBUG) {
			println("[DEBUG] ${fn()}")
		}
	}

	/**
	 * Prints a debug message if [DEBUG] is true.
	 * The message will be lazily evaluated.
	 */
	@JvmStatic
	fun debug(obj: Any) = debug { obj.toString() }
}
object GlobalVariables {
	const val DEBUG = true

	@JvmStatic
	fun debug(msg: String) {
		if (DEBUG) {
			println("[DEBUG] $msg")
		}
	}

	@JvmStatic
	fun debug(obj: Any) = debug(obj.toString())
}
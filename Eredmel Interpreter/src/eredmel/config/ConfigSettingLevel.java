package eredmel.config;

/**
 * Encodes information about where a level should apply
 */
public enum ConfigSettingLevel {
	/**
	 * The configuration should apply to the entire session. It can be set
	 * once in a linker command and apply to all files
	 */
	SESSION,
	/**
	 * The configuration should only apply to the current file. It can be
	 * set individually in each original source file
	 */
	FILE;
}

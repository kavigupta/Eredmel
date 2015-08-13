package eredmel.config;

import java.util.function.Predicate;

/**
 * An enumeration representing a setting, along with validation code, and
 * it's internal name in the code.
 * 
 * @author Kavi Gupta
 */
public enum ConfigSetting {
	/**
	 * Represents the number of spaces in a tab to be used by the
	 * normalizer and denormalizer in representing a file
	 */
	TABWIDTH("4", x -> x.matches("\\d+"), "tabwidth", ConfigSettingLevel.FILE),
	/**
	 * Represents the prefix that must preceed every line if it is to be
	 * interpreted as an Eredmel command; this can be any valid regex that
	 * works in both enhanced and non-enhanced modes.
	 */
	PREFIX("", x -> !x.matches(".+\\s.+"), "prefix",
			ConfigSettingLevel.SESSION);
	/**
	 * The default value of this setting.
	 */
	public String defaultValue;
	/**
	 * Returns whether the given String would work for the given setting.
	 */
	Predicate<String> validator;
	/**
	 * The key used by the application to represent this configuration
	 * setting.
	 */
	public String internalKey;
	/**
	 * The level at which this setting should apply
	 */
	public ConfigSettingLevel level;
	private ConfigSetting(String defaultValue, Predicate<String> validator,
			String internalKey, ConfigSettingLevel level) {
		this.defaultValue = defaultValue;
		this.validator = validator;
		this.internalKey = internalKey;
		this.level = level;
	}
	/**
	 * Loads a setting based on an internal configuration setting string.
	 * 
	 * @param configSetting
	 *        the setting string to use
	 * @return the configuration setting tied to this setting string
	 */
	public static ConfigSetting fromConfigString(String configSetting) {
		for (ConfigSetting set : values())
			if (set.internalKey.equals(configSetting)) return set;
		return null;
	}
}
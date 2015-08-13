package eredmel.config;

import java.util.HashMap;
import java.util.function.Predicate;

import eredmel.regex.EnregexType;
import eredmel.regex.Pattern;

/**
 * A class for storing information about configuration settings regarding the
 * processing of Eredmel Files.
 * 
 * @author Kavi Gupta
 */
public class EredmelConfiguration {
	/**
	 * An enumeration representing a setting, along with validation code, and
	 * it's internal name in the code.
	 */
	public static enum ConfigSetting {
		/**
		 * Represents the number of spaces in a tab to be used by the
		 * normalizer and denormalizer in representing a file
		 */
		TABWIDTH("4", x -> x.matches("\\d+"), "tabwidth"),
		/**
		 * Represents the prefix that must preceed every line if it is to be
		 * interpreted as an Eredmel command.
		 */
		PREFIX("", x -> !x.matches(".+\\s.+"), "prefix");
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
		private ConfigSetting(String defaultValue,
				Predicate<String> validation, String internalKey) {
			this.defaultValue = defaultValue;
			this.validator = validation;
			this.internalKey = internalKey;
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
	private HashMap<ConfigSetting, String> config;
	/**
	 * Creates an empty configuration settings file
	 */
	public EredmelConfiguration() {
		config = new HashMap<>();
	}
	/**
	 * Adds a configuration setting to this file
	 * 
	 * @param setting
	 *        the setting to set
	 * @param value
	 *        the value to associate it with
	 * @return whether the value associated with the given setting is a valid
	 *         match
	 */
	public boolean add(ConfigSetting setting, String value) {
		if (!setting.validator.test(value)) return false;
		config.put(setting, value);
		return true;
	}
	/**
	 * Return whether a setting has been defined by a call to
	 * {@link #add(ConfigSetting, String)}
	 * 
	 * @param setting
	 *        the setting to check
	 * @return whether it has been defined
	 */
	public boolean isDefined(ConfigSetting setting) {
		return config.containsKey(setting);
	}
	/**
	 * Gets the current value of the given setting
	 * 
	 * @param setting
	 *        the setting to get the value of
	 * @return the associated value of the setting, or the default value if
	 *         none has been set
	 */
	private String get(ConfigSetting setting) {
		return isDefined(setting) ? config.get(setting)
				: setting.defaultValue;
	}
	/**
	 * The value of tabwidth that has currently been set, or the default
	 * 
	 * @return the number of spaces per tab
	 */
	public int tabwidth() {
		return Integer.parseInt(get(ConfigSetting.TABWIDTH));
	}
	/**
	 * Constructs a Regex Pattern from the line start anchor, the set line
	 * prefix, and the given regex.
	 * 
	 * @param regex
	 *        the regex to use
	 * @param flags
	 *        the flags to use, apart from {@code Pattern#MULTILINE}, which is
	 *        enabled by default.
	 * @return a Pattern adjusted for the line prefix
	 */
	public Pattern patternMatch(String regex, int flags) {
		return Pattern.compile("^" + get(ConfigSetting.PREFIX) + regex, flags
				| Pattern.MULTILINE, EnregexType.EREDMEL_STANDARD);
	}
	@Override
	public EredmelConfiguration clone() {
		EredmelConfiguration copy = new EredmelConfiguration();
		@SuppressWarnings("unchecked")
		HashMap<ConfigSetting, String> mapClone = (HashMap<ConfigSetting, String>) this.config
				.clone();
		copy.config = mapClone;
		return copy;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EredmelConfiguration other = (EredmelConfiguration) obj;
		if (config == null) {
			if (other.config != null) return false;
		} else if (!config.equals(other.config)) return false;
		return true;
	}
	@Override
	public String toString() {
		return config.toString();
	}
}

package eredmel.config;

import java.util.HashMap;

import eredmel.regex.EnregexType;
import eredmel.regex.Pattern;

/**
 * A class for storing information about configuration settings regarding the
 * processing of Eredmel Files.
 * 
 * @author Kavi Gupta
 */
public class EredmelConfiguration {
	private HashMap<ConfigSetting, String> config;
	public static EredmelConfiguration getDefault() {
		return new EredmelConfiguration(new HashMap<>());
	}
	/**
	 * Creates a configuration settings file with the given backing map
	 */
	private EredmelConfiguration(HashMap<ConfigSetting, String> config) {
		this.config = config;
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
	public boolean set(ConfigSetting setting, String value) {
		if (!setting.validator.test(value)) return false;
		config.put(setting, value);
		return true;
	}
	/**
	 * Return whether a setting has been defined by a call to
	 * {@link #set(ConfigSetting, String)}
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
	/**
	 * Unsets all the configuration values except for those that are
	 * system-level
	 * 
	 * @return
	 */
	public EredmelConfiguration preserveOnlySession() {
		HashMap<ConfigSetting, String> globals = new HashMap<>();
		this.config
				.entrySet()
				.stream()
				.filter(x -> x.getKey().level == ConfigSettingLevel.SESSION)
				.forEach(x -> globals.put(x.getKey(), x.getValue()));
		return new EredmelConfiguration(globals);
	}
	@Override
	public EredmelConfiguration clone() {
		@SuppressWarnings("unchecked")
		HashMap<ConfigSetting, String> mapClone = (HashMap<ConfigSetting, String>) this.config
				.clone();
		return new EredmelConfiguration(mapClone);
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

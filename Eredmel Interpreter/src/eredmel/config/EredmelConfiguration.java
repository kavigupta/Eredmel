package eredmel.config;

import java.util.HashMap;
import java.util.function.Predicate;

import eredmel.regex.EnregexType;
import eredmel.regex.Pattern;

public class EredmelConfiguration {
	public static enum ConfigSetting {
		TABWIDTH("4", x -> x.matches("\\d+"), "tabwidth", "tabwidth"),
		PREFIX("", x -> !x.matches(".+\\s.+"), "prefix", "prefix");
		public String defaultValue;
		Predicate<String> validation;
		public String internalKey;
		public String commandLineKey;
		private ConfigSetting(String defaultValue,
				Predicate<String> validation, String internalKey,
				String commandLineKey) {
			this.defaultValue = defaultValue;
			this.validation = validation;
			this.internalKey = internalKey;
			this.commandLineKey = commandLineKey;
		}
		public static ConfigSetting fromConfigString(String configSetting) {
			for (ConfigSetting set : values())
				if (set.internalKey.equals(configSetting)) return set;
			return null;
		}
	}
	private HashMap<ConfigSetting, String> config;
	public EredmelConfiguration() {
		config = new HashMap<>();
	}
	public void put(ConfigSetting setting, String value) {
		config.put(setting, value);
	}
	public boolean isDefined(ConfigSetting setting) {
		return config.containsKey(setting);
	}
	private String get(ConfigSetting setting) {
		return isDefined(setting) ? config.get(setting)
				: setting.defaultValue;
	}
	public int tabwidth() {
		return Integer.parseInt(get(ConfigSetting.TABWIDTH));
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
	public Pattern patternMatch(String enregex, boolean enhanced) {
		return Pattern
				.compile("^" + get(ConfigSetting.PREFIX) + enregex,
						(enhanced ? Pattern.ENHANCED_REGEX : 0)
								| Pattern.MULTILINE,
						EnregexType.EREDMEL_STANDARD);
	}
}

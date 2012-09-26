package co.freeside.betamax.util

@Category(Properties)
class PropertiesCategory {

	boolean getBoolean(String key, boolean defaultValue = false) {
		def value = this.getProperty(key)
		value ? value.toBoolean() : defaultValue
	}

	int getInteger(String key, int defaultValue = 0) {
		def value = this.getProperty(key)
		value ? value.toInteger() : defaultValue
	}

	public <T> T getEnum(String key, T defaultValue) {
		def value = this.getProperty(key)
		value ? Enum.valueOf(defaultValue.getClass(), value) : defaultValue
	}

}

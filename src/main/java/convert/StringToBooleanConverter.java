package convert;

/**
 * Converts a {@link String} to a {@link Boolean}.
 */
final class StringToBooleanConverter implements Converter<String, Boolean> {

	public Boolean convert(String source) {
		String value = source.trim().toLowerCase();
		if ("".equals(value)) {
			return null;
		} else if (Boolean.TRUE.toString().equals(value)) {
			return true;
		} else if (Boolean.FALSE.toString().equals(value)) {
			return false;
		}
		throw new IllegalArgumentException("Invalid boolean value '" + source + "'");
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(String.class, Boolean.class);
	}

}


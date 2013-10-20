package convert;

/**
 * Converts from any {@link Object} to a {@link String}.
 */
final class ObjectToStringConverter implements Converter<Object, String> {

	public String convert(Object source) {
		return source.toString();
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(Object.class, String.class);
	}
}

package convert;

/**
 * Converts a {@link String} to a {@link char[]}.
 */
final class StringToCharacterArrayConverter implements Converter<String, char[]> {

	@Override
	public char[] convert(String source) {
		return source.toCharArray();
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(String.class, char[].class);
	}
}

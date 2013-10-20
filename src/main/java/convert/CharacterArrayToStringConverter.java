package convert;

/**
 * Converts from any {@code char[]} to a {@link String}.
 */
final class CharacterArrayToStringConverter implements Converter<char[], String> {
	@Override
	public String convert(char[] source) {
		return String.valueOf(source);
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(char[].class, String.class);
	}
}

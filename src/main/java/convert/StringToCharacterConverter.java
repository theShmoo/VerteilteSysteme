package convert;

/**
 * Converts a {@link String} to a {@link Character}.
 */
final class StringToCharacterConverter implements Converter<String, Character> {

	public Character convert(String source) {
		if (source.length() == 0) {
			return null;
		}
		if (source.length() > 1) {
			throw new IllegalArgumentException(
					"Can only convert a [String] with length of 1 to a [Character]; string value '" + source + "'  has length of " + source.length());
		}
		return source.charAt(0);
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(String.class, Character.class);
	}

}


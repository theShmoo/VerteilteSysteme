package convert;

/**
 * Converts from any {@code byte[]} to a {@link String}.
 */
final class ByteArrayToStringConverter implements Converter<byte[], String> {
	@Override
	public String convert(byte[] source) {
		return new String(source);
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(byte[].class, String.class);
	}
}

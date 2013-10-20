package convert;

/**
 * Converts a {@link String} to a {@code byte[]}.
 */
final class StringToByteArrayConverter implements Converter<String, byte[]> {

	@Override
	public byte[] convert(String source) {
		return source.getBytes();
	}

	@Override
	public ConvertiblePair getConvertibleType() {
		return new ConvertiblePair(String.class, byte[].class);
	}
}

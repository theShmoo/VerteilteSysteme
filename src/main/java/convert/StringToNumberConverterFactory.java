package convert;

import java.util.Collections;
import java.util.Set;

/**
 * Converts from a {@link String} some JDK-standard {@link Number} implementation.
 * <p/>
 * The supported types are:
 * <ul>
 * <li>{@link Byte}</li>
 * <li>{@link Short}</li>
 * <li>{@link Integer}</li>
 * <li>{@link Long}</li>
 * <li>{@link Float}</li>
 * <li>{@link Double}</li>
 * </ul>
 */
final class StringToNumberConverterFactory implements GenericConverter {
	public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToNumber<T>(targetType);
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Number.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
		return source == null ? null : this.getConverter((Class<Number>) targetType).convert((String) source);
	}

	private static final class StringToNumber<T extends Number> implements Converter<String, T> {
		private final Class<T> targetType;

		public StringToNumber(Class<T> targetType) {
			this.targetType = targetType;
		}

		public T convert(String source) {
			if (source.length() == 0) {
				return null;
			}
			return parseNumber(source, this.targetType);
		}

		@Override
		public ConvertiblePair getConvertibleType() {
			return new ConvertiblePair(String.class, targetType);
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class, using the corresponding {@code valueOf()}
	 * methods.<br/>
	 * Trims the input {@code String} before attempting to parse the number.
	 *
	 * @param text        the text to convert
	 * @param targetClass the target class to parse into
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 * @see Byte#decode
	 * @see Short#decode
	 * @see Integer#decode
	 * @see Long#decode
	 * @see Float#valueOf
	 * @see Double#valueOf
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Number> T parseNumber(String text, Class<T> targetClass) {
		if (text == null) {
			throw new IllegalArgumentException("Text must not be null");
		} else if (targetClass == null) {
			throw new IllegalArgumentException("Target class must not be null");
		}
		String trimmed = text.replaceAll("\\p{javaWhitespace}+", "");

		if (targetClass.equals(Byte.class)) {
			return (T) (Byte.valueOf(trimmed));
		} else if (targetClass.equals(Short.class)) {
			return (T) (Short.valueOf(trimmed));
		} else if (targetClass.equals(Integer.class)) {
			return (T) (Integer.valueOf(trimmed));
		} else if (targetClass.equals(Long.class)) {
			return (T) (Long.valueOf(trimmed));
		} else if (targetClass.equals(Float.class)) {
			return (T) Float.valueOf(trimmed);
		} else if (targetClass.equals(Double.class)) {
			return (T) Double.valueOf(trimmed);
		} else {
			throw new IllegalArgumentException(
					"Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
		}
	}
}

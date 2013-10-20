package convert;

/**
 * Converts a source object of type {@code S} to a target of type {@code T}.<br/>
 * Implementations of this interface are thread-safe and can be shared.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface Converter<S, T> {

	/**
	 * Converts the {@code source} of type {@code S} to target type {@code T}.
	 *
	 * @param source the source object to convert, which must be an instance of {@code S}
	 * @return the converted object, which must be an instance of {@code T}
	 * @throws IllegalArgumentException if the source could not be converted to the desired target type
	 */
	T convert(S source);

	/**
	 * Return the source and target types which this {@code Converter} can convert between.<br/>
	 *
	 * @return the convertible pair
	 */
	ConvertiblePair getConvertibleType();
}

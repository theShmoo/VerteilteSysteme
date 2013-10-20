package convert;

import java.util.Set;

/**
 * Generic converter interface for converting between two or more types.
 * <p/>
 * A GenericConverter may support converting between multiple source/target type pairs
 * (see {@link #getConvertibleTypes()}.
 * In addition, {@code GenericConverter} implementations have access to source/target types during the type conversion
 * process. This allows for resolving source and target field metadata such as annotations and generics information,
 * which can be used influence the conversion logic.
 *
 * @see Converter
 */
public interface GenericConverter {

	/**
	 * Return the source and target types which this converter can convert between.<br/>
	 * Each entry is a convertible source-to-target type pair.
	 *
	 * @return the convertible pair
	 */
	Set<ConvertiblePair> getConvertibleTypes();

	/**
	 * Convert the {@code source} to the {@code targetType}.
	 *
	 * @param source     the source object to convert (may be {@code null})
	 * @param sourceType the type descriptor we are converting from
	 * @param targetType the type descriptor we are converting to
	 * @return the converted object
	 */
	Object convert(Object source, Class<?> sourceType, Class<?> targetType);

}

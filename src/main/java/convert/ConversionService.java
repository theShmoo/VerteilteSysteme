package convert;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Base {@link ConversionService} implementation suitable for use in most environments.<br/>
 * By default it is configured with {@link Converter}s appropriate for most environments.
 * <p/>
 * Designed for direct instantiation but also exposes the static {@link #addDefaultConverters(ConversionService)}
 * utility method for ad hoc use against any {@code ConversionService} instance.
 */
public class ConversionService {
	/**
	 * General NO-OP converter used when conversion is not required.
	 */
	private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter();

	/**
	 * Used as a cache entry when no converter is available.<br/>
	 * This converter is never returned.
	 */
	private static final GenericConverter NO_MATCH = new NoOpConverter();

	private final LinkedHashMap<ConvertiblePair, GenericConverter> converterCache= new LinkedHashMap<ConvertiblePair, GenericConverter>();

	private final Converters converters = new Converters();

	public ConversionService() {
		addDefaultConverters(this);
	}

	/**
	 * Add {@link Converter}s appropriate for most environments.
	 *
	 * @param conversionService the registry of converters to add to
	 */
	public static void addDefaultConverters(ConversionService conversionService) {
		addScalarConverters(conversionService);
	}

	/**
	 * Registers some {@link Converter}s for converting basic types to {@link String} and vice-versa.
	 *
	 * @param conversionService the registry of converters to add to
	 */
	private static void addScalarConverters(ConversionService conversionService) {
		conversionService.addConverter(Boolean.class, String.class, new ObjectToStringConverter());
		conversionService.addConverter(new StringToBooleanConverter());

		conversionService.addConverter(Character.class, String.class, new ObjectToStringConverter());
		conversionService.addConverter(new StringToCharacterConverter());

		conversionService.addConverter(new ByteArrayToStringConverter());
		conversionService.addConverter(new StringToByteArrayConverter());
		conversionService.addConverter(new CharacterArrayToStringConverter());
		conversionService.addConverter(new StringToCharacterArrayConverter());

		conversionService.addConverter(Number.class, String.class, new ObjectToStringConverter());
		conversionService.addConverter(new StringToNumberConverterFactory());

		conversionService.addConverter(Object.class, String.class, new ObjectToStringConverter());
	}

	/**
	 * Add a plain {@link Converter} to this registry.
	 *
	 * @throws IllegalArgumentException if the types could not be resolved
	 */
	public void addConverter(Converter<?, ?> converter) {
		ConvertiblePair typeInfo = getRequiredTypeInfo(converter);
		if (typeInfo == null) {
			throw new IllegalArgumentException("Unable to the determine sourceType and targetType");
		}
		addConverter(new ConverterAdapter(typeInfo, converter));
	}

	public void addConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
		ConvertiblePair typeInfo = new ConvertiblePair(sourceType, targetType);
		addConverter(new ConverterAdapter(typeInfo, converter));
	}

	public void addConverter(GenericConverter converter) {
		this.converters.add(converter);
	}

	/**
	 * Convert the {@code source} to {@code targetType}.
	 *
	 * @param source     the source object to convert (may be {@code null})
	 * @param targetType the target type to convert to (required)
	 * @return the converted object, an instance of {@code targetType}
	 * @throws IllegalArgumentException if {@code targetType} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("The targetType to convert to cannot be null");
		}
		return (T) convert(source, source.getClass(), targetType);
	}

	/**
	 * Convert the {@code source} to the {@code targetType}.
	 *
	 * @param source     the source object to convert (may be {@code null})
	 * @param sourceType the type descriptor we are converting from
	 * @param targetType the type descriptor we are converting to
	 * @return the converted object
	 */
	public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("The targetType to convert to cannot be null");
		}
		if (sourceType == null) {
			if (source != null) {
				throw new IllegalArgumentException("The source must be [null] if sourceType == [null]");
			}
			return handleResult(sourceType, targetType, convertNullSource(sourceType, targetType));
		}
		if (source != null && !sourceType.isInstance(source)) {
			throw new IllegalArgumentException("The source to convert from must be an instance of " +
					sourceType + "; instead it was a " + source.getClass().getName());
		} else if (source == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
		}
		sourceType = sourceType.isPrimitive() ? Converters.primitiveTypeToWrapperMap.get(sourceType) : sourceType;
		targetType = targetType.isPrimitive() ? Converters.primitiveTypeToWrapperMap.get(targetType) : targetType;

		GenericConverter converter = getConverter(sourceType, targetType);
		if (converter != null) {
			Object result = invokeConverter(converter, source, sourceType, targetType);
			return handleResult(sourceType, targetType, result);
		}
		return handleConverterNotFound(source, sourceType, targetType);
	}

	protected Object convertNullSource(Class<?> sourceType, Class<?> targetType) {
		return null;
	}

	protected GenericConverter getConverter(Class<?> sourceType, Class<?> targetType) {
		ConvertiblePair key = new ConvertiblePair(sourceType, targetType);
		GenericConverter converter = this.converterCache.get(key);
		if (converter != null) {
			return (converter != NO_MATCH ? converter : null);
		}

		converter = this.converters.find(sourceType, targetType);
		if (converter == null) {
			converter = getDefaultConverter(sourceType, targetType);
		}

		if (converter != null) {
			this.converterCache.put(key, converter);
			return converter;
		}

		this.converterCache.put(key, NO_MATCH);
		return null;
	}

	protected GenericConverter getDefaultConverter(Class<?> sourceType, Class<?> targetType) {
		return (targetType.isAssignableFrom(sourceType) ? NO_OP_CONVERTER : null);
	}

	private ConvertiblePair getRequiredTypeInfo(Object converter) {
		return ((Converter<?, ?>) converter).getConvertibleType();
	}

	public static Object invokeConverter(GenericConverter converter, Object source, Class<?> sourceType, Class<?> targetType) {
		return converter.convert(source, sourceType, targetType);
	}

	private static class Converters {
		private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>();

		private static final Set<Class<?>> IGNORED_CLASSES;

		private final Map<ConvertiblePair, ConvertersForPair> converters = new LinkedHashMap<ConvertiblePair, ConvertersForPair>(36);

		static {
			primitiveTypeToWrapperMap.put(Boolean.TYPE, Boolean.class);
			primitiveTypeToWrapperMap.put(Byte.TYPE, Byte.class);
			primitiveTypeToWrapperMap.put(Character.TYPE, Character.class);
			primitiveTypeToWrapperMap.put(Short.TYPE, Short.class);
			primitiveTypeToWrapperMap.put(Integer.TYPE, Integer.class);
			primitiveTypeToWrapperMap.put(Long.TYPE, Long.class);
			primitiveTypeToWrapperMap.put(Float.TYPE, Float.class);
			primitiveTypeToWrapperMap.put(Double.TYPE, Double.class);

			Set<Class<?>> ignored = new HashSet<Class<?>>();
			ignored.add(Object.class);
			ignored.add(Object[].class);
			IGNORED_CLASSES = Collections.unmodifiableSet(ignored);
		}

		public void add(GenericConverter converter) {
			Set<ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
			if (convertibleTypes != null) {
				for (ConvertiblePair convertiblePair : convertibleTypes) {
					getMatchableConverters(convertiblePair).add(converter);
				}
			}
		}

		private ConvertersForPair getMatchableConverters(ConvertiblePair convertiblePair) {
			ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
			if (convertersForPair == null) {
				convertersForPair = new ConvertersForPair();
				this.converters.put(convertiblePair, convertersForPair);
			}
			return convertersForPair;
		}

		public GenericConverter find(Class<?> sourceType, Class<?> targetType) {
			List<Class<?>> sourceCandidates = getTypeHierarchy(sourceType);
			List<Class<?>> targetCandidates = getTypeHierarchy(targetType);
			for (Class<?> sourceCandidate : sourceCandidates) {
				for (Class<?> targetCandidate : targetCandidates) {
					GenericConverter converter = getRegisteredConverter(sourceType, targetType, sourceCandidate, targetCandidate);
					if (converter != null) {
						return converter;
					}
				}
			}
			return null;
		}

		private GenericConverter getRegisteredConverter(Class<?> sourceType, Class<?> targetType,
														Class<?> sourceCandidate, Class<?> targetCandidate) {
			ConvertersForPair convertersForPair = converters.get(new ConvertiblePair(sourceCandidate, targetCandidate));
			return convertersForPair == null ? null : convertersForPair.getConverter(sourceType, targetType);
		}

		/**
		 * Returns an ordered class hierarchy for the given type.
		 *
		 * @param type the type
		 * @return an ordered list of all classes that the given type extends or implements.
		 */
		private List<Class<?>> getTypeHierarchy(Class<?> type) {
			if (type.isPrimitive()) {
				type = primitiveTypeToWrapperMap.get(type);
			}
			Set<Class<?>> typeHierarchy = new LinkedHashSet<Class<?>>();
			collectTypeHierarchy(typeHierarchy, type);
			if (type.isArray()) {
				typeHierarchy.add(Object[].class);
			}
			typeHierarchy.add(Object.class);
			return new ArrayList<Class<?>>(typeHierarchy);
		}

		private void collectTypeHierarchy(Set<Class<?>> typeHierarchy, Class<?> type) {
			if (type != null && !IGNORED_CLASSES.contains(type)) {
				if (typeHierarchy.add(type)) {
					Class<?> superclass = type.getSuperclass();
					if (type.isArray()) {
						superclass = primitiveTypeToWrapperMap.get(superclass);
					}
					collectTypeHierarchy(typeHierarchy, createRelated(type, superclass));

					for (Class<?> implementsInterface : type.getInterfaces()) {
						collectTypeHierarchy(typeHierarchy, createRelated(type, implementsInterface));
					}
				}
			}
		}

		private Class<?> createRelated(Class<?> type, Class<?> relatedType) {
			if (relatedType == null && type.isArray()) {
				relatedType = Array.newInstance(type, 0).getClass();
			}
			if (!type.equals(relatedType)) {
				return type.getSuperclass();
			}
			return null;
		}
	}

	private Object handleConverterNotFound(Object source, Class<?> sourceType, Class<?> targetType) {
		if (source == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
			return null;
		}
		if (targetType.isAssignableFrom(sourceType) && targetType.isInstance(source)) {
			return source;
		}
		throw new IllegalStateException(String.format("No converter found capable of converting from type %s to type %s", sourceType, targetType));
	}

	private Object handleResult(Class<?> sourceType, Class<?> targetType, Object result) {
		if (result == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
		}
		return result;
	}

	private void assertNotPrimitiveTargetType(Class<?> sourceType, Class<?> targetType) {
		if (targetType.isPrimitive()) {
			throw new IllegalArgumentException("A null value cannot be assigned to primitive type " + targetType.getName());
		}
	}

	/**
	 * Adapts a {@link Converter} to a {@link GenericConverter}.
	 */
	@SuppressWarnings("unchecked")
	private final class ConverterAdapter implements GenericConverter {

		private final ConvertiblePair typeInfo;
		private final Converter<Object, Object> converter;

		public ConverterAdapter(ConvertiblePair typeInfo, Converter<?, ?> converter) {
			this.converter = (Converter<Object, Object>) converter;
			this.typeInfo = typeInfo;
		}

		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(this.typeInfo);
		}

		public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
			if (source == null) {
				return convertNullSource(sourceType, targetType);
			}
			return this.converter.convert(source);
		}
	}

	/**
	 * Manages converters registered with a specific {@link ConvertiblePair}.
	 */
	private static class ConvertersForPair {

		private final LinkedList<GenericConverter> converters = new LinkedList<GenericConverter>();

		public void add(GenericConverter converter) {
			this.converters.addFirst(converter);
		}

		public GenericConverter getConverter(Class<?> sourceType, Class<?> targetType) {
			for (GenericConverter converter : this.converters) {
				for (ConvertiblePair convertiblePair : converter.getConvertibleTypes()) {
					if (convertiblePair.getSourceType() == sourceType && convertiblePair.getTargetType() == targetType) {
						return converter;
					}
				}
			}
			for (GenericConverter converter : this.converters) {
				for (ConvertiblePair convertiblePair : converter.getConvertibleTypes()) {
					if (sourceType.isAssignableFrom(convertiblePair.getSourceType())
							&& convertiblePair.getTargetType().isAssignableFrom(targetType)) {
						return converter;
					}
				}
			}
			return null;
		}
	}

	/**
	 * Internal converter that performs no operation.
	 */
	private static class NoOpConverter implements GenericConverter {
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
			return source;
		}
	}
}

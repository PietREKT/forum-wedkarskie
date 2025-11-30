package org.piet.forumbackend.globals.utils;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {
    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T>{

        private final Class<T> enumType;

        private StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public @Nullable T convert(String source) {
            if (source == null) return null;

            String normalized = source.trim().replace(' ', '_');

            for (T constant : enumType.getEnumConstants()){
                if (constant.name().equalsIgnoreCase(normalized)){
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid value '" + source + "' for enum " + enumType.getSimpleName());
        }
    }
}

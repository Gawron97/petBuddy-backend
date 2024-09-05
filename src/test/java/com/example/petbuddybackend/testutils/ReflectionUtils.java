package com.example.petbuddybackend.testutils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static List<String> getPrimitiveNames(Class<?> clazz) {
        return getPrimitiveNames(clazz, "");
    }

    public static List<String> getPrimitiveNames(Class<?> clazz, String prefix) {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(isPrimitiveOrBoxed(field)) {
                fieldNames.add(prefix + field.getName());
            }
        }
        return fieldNames;
    }

    public static boolean isPrimitiveOrBoxed(Field field) {
        Class<?> fieldType = field.getType();

        return fieldType.isPrimitive() ||
                fieldType.equals(String.class) ||
                fieldType.equals(Long.class) ||
                fieldType.equals(Integer.class) ||
                fieldType.equals(Boolean.class) ||
                fieldType.equals(Double.class) ||
                fieldType.equals(Float.class) ||
                fieldType.equals(BigDecimal.class);
    }

    public static boolean isClass(Field field) {
        Class<?> fieldType = field.getType();

        return fieldType.isRecord() ||
                fieldType.isAnonymousClass() ||
                fieldType.isMemberClass() ||
                fieldType.isLocalClass();
    }
}

package com.example.petbuddybackend.testutils;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Set;

public final class ValidationUtils {

    private ValidationUtils() {}

    @SneakyThrows
    public static boolean fieldsNotNullRecursive(Object obj, Set<String> fieldsToSkip) {
        boolean allNotNull = true;
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            if (field.get(obj) == null && !fieldsToSkip.contains(field.getName())) {
                allNotNull = false;
                System.out.println("Field '" + field.getName() + "' is null in object: " + obj.getClass().getSimpleName());
            } else if (ReflectionUtils.isClass(field)) {
                allNotNull = fieldsNotNullRecursive(field.get(obj));
            }
        }
        return allNotNull;
    }

    public static boolean fieldsNotNullRecursive(Object obj) {
        return fieldsNotNullRecursive(obj, Set.of());
    }
}

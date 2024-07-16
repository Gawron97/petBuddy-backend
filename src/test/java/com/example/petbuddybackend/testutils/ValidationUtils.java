package com.example.petbuddybackend.testutils;

import java.lang.reflect.Field;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean fieldsNotNull(Object obj) throws IllegalAccessException {
        boolean allNotNull = true;
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(obj) == null) {
                allNotNull = false;
                System.out.println("Field '" + field.getName() + "' is null in object: " + obj.getClass().getSimpleName());
            }
        }
        return allNotNull;
    }
}

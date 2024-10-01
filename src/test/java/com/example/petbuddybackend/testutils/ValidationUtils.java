package com.example.petbuddybackend.testutils;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ValidationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

    @SneakyThrows
    public static boolean fieldsNotNullRecursive(Object obj, Set<String> fieldsToSkip) {
        boolean allNotNull = true;
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            if (field.get(obj) == null && !fieldsToSkip.contains(field.getName())) {
                allNotNull = false;
                logger.error("Field '{}' is null in object: {}", field.getName(), obj.getClass().getSimpleName());
            } else if (ReflectionUtils.isClass(field)) {
                logger.info("Descending into '{}' field that is a class", field.getName());
                allNotNull = fieldsNotNullRecursive(field.get(obj));
            }
        }
        return allNotNull;
    }

    public static boolean fieldsNotNullRecursive(Object obj) {
        return fieldsNotNullRecursive(obj, Set.of());
    }
}

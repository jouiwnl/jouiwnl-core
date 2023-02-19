package com.jouiwnl.core.dto;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Objects;

public class DynamicDto extends LinkedHashMap<String, Object> {

    private DynamicDto() {
    }

    public Object get(Object... keys) {
        return Arrays.stream(keys).map(this::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static DynamicDto of() {
        return new DynamicDto();
    }

    public static DynamicDto of(DatabaseEntity<?> entity) {
        return of(entity, true);
    }

    public static DynamicDto of(DatabaseEntity<?> entity, boolean autoGenerate) {
        if (entity == null) {
            return null;
        }
        final DynamicDto dto = new DynamicDto().with("id", entity.getId());
        if (Boolean.TRUE.equals(autoGenerate)) {
            Arrays.stream(entity.getClass().getDeclaredMethods()).sorted(Comparator.comparing(Method::getName)).forEach(method -> {
                Character firstLetterLowerCase = Character.toLowerCase(method.getName().charAt(3));
                String restPropertyName = method.getName().substring(4, method.getName().getBytes(StandardCharsets.UTF_8).length);
                String propertyName = firstLetterLowerCase + restPropertyName;

                try {
                    if (method.getName().startsWith("get") && !method.getName().startsWith("getId")) {
                        dto.with(propertyName, method.invoke(entity));
                    }
                } catch(IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
                    //
                }
            });
        }
        return dto;
    }

    public DynamicDto with(String key, Object value) {
        put(key, value);
        return this;
    }
}
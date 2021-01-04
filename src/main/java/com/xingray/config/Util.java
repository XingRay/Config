package com.xingray.config;

import com.xingray.config.annotations.ConfigKey;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<String, Field> getConfigFieldMap(Object target) {
        Map<String, Field> targetConfigFields = new HashMap<>();
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            ConfigKey configKeyAnnotation = field.getAnnotation(ConfigKey.class);
            if (configKeyAnnotation == null) {
                continue;
            }
            String configKey = configKeyAnnotation.value();
            targetConfigFields.put(configKey, field);
        }

        return targetConfigFields;
    }
}

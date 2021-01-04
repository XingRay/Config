package com.xingray.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapConfig {

    private final Object target;

    private final Map<String, Field> targetConfigFields;

    @SuppressWarnings("rawtypes")
    private final ClassMap<FieldConverter<?, String>> fieldConverterMap;

    public MapConfig(Object controller) {
        this.target = controller;
        this.fieldConverterMap = new ClassMap<>();

        targetConfigFields = Util.getConfigFieldMap(target);
    }

    public <V> void addFieldConverter(Class<V> viewCls, FieldConverter<V, String> fieldConverter) {
        fieldConverterMap.put(viewCls, fieldConverter);
    }

    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        getConfig(config);
        return config;
    }

    public void getConfig(Map<String, String> config) {
        if (config == null) {
            return;
        }

        for (Map.Entry<String, Field> entry : targetConfigFields.entrySet()) {
            String name = entry.getKey();
            Field field = entry.getValue();

            Object targetField = null;
            try {
                targetField = field.get(target);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (targetField == null) {
                return;
            }

            //noinspection rawtypes
            FieldConverter fieldConverter = fieldConverterMap.getByClassObject(targetField);
            Object value = null;
            if (fieldConverter != null) {
                //noinspection unchecked
                value = fieldConverter.getConfig(targetField);
            } else {
                value = targetField;
            }
            config.put(name, value.toString());
        }
    }

    public void updateTarget(Map<String, String> config) {
        if (config == null) {
            return;
        }

        Set<String> configPropertyNames = config.keySet();
        configPropertyNames.retainAll(targetConfigFields.keySet());
        for (String name : configPropertyNames) {
            Field field = targetConfigFields.get(name);
            try {
                Object viewField = field.get(target);
                String value = config.get(name);
                //noinspection rawtypes
                FieldConverter fieldConverter = fieldConverterMap.getByClassObject(viewField);
                if (fieldConverter != null) {
                    //noinspection unchecked
                    fieldConverter.restoreConfig(viewField, value);
                } else {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

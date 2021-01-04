package com.xingray.config;


import com.xingray.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectConfig<T> {

    private final Object target;
    private final Class<T> configClass;
    private final Map<String, Field> controllerViewFields;
    @SuppressWarnings("rawtypes")
    private final ClassMap<FieldConverter> fieldConverterMap;

    public ObjectConfig(Object target, Class<T> configClass) {
        this.target = target;
        this.configClass = configClass;
        this.fieldConverterMap = new ClassMap<>();

        controllerViewFields = Util.getConfigFieldMap(target);
    }

    public <V, S> void addViewState(Class<V> viewCls, FieldConverter<V, S> fieldConverter) {
        fieldConverterMap.put(viewCls, fieldConverter);
    }

    public T getConfig() {
        T config = null;
        try {
            config = configClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        populateConfig(config);
        return config;
    }

    public void populateConfig(T config) {
        if (config == null) {
            return;
        }

        Field[] declaredFields = config.getClass().getDeclaredFields();
        Set<String> configPropertyNames = new HashSet<>();
        for (Field field : declaredFields) {
            configPropertyNames.add(field.getName());
        }

        configPropertyNames.retainAll(controllerViewFields.keySet());
        for (String name : configPropertyNames) {
            Field field = controllerViewFields.get(name);
            try {
                Object viewField = field.get(target);
                //noinspection rawtypes
                FieldConverter fieldConverter = fieldConverterMap.getByClassObject(viewField);
                Object value = null;
                if (fieldConverter != null) {
                    //noinspection unchecked
                    value = fieldConverter.getConfig(viewField);
                } else {
                    value = viewField;
                }
                ReflectUtil.set(config, name, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void populateView(T config) {
        if (config == null) {
            return;
        }

        Field[] declaredFields = config.getClass().getDeclaredFields();
        Set<String> configPropertyNames = new HashSet<>();
        for (Field field : declaredFields) {
            configPropertyNames.add(field.getName());
        }

        configPropertyNames.retainAll(controllerViewFields.keySet());
        for (String name : configPropertyNames) {
            Field field = controllerViewFields.get(name);
            try {
                Object viewField = field.get(target);
                Object value = ReflectUtil.get(config, name);
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

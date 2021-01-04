package com.xingray.config;

import java.util.HashMap;
import java.util.Map;

public class ClassMap<T> {

    private final HashMap<Class<?>, T> map;

    public ClassMap() {
        map = new HashMap<>();
    }

    public T put(Class<?> cls, T t) {
        return map.put(cls, t);
    }

    @SuppressWarnings("rawtypes")
    public T getByClassObject(Object viewField) {
        T t = map.get(viewField.getClass());
        if (t == null) {
            for (Map.Entry<Class<?>, T> entry : map.entrySet()) {
                Class cls = entry.getKey();
                if (cls.isInstance(viewField)) {
                    t = entry.getValue();
                    if (t != null) {
                        map.put(cls, t);
                    }
                    break;
                }
            }
        }
        return t;
    }
}

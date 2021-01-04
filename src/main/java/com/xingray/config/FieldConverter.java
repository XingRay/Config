package com.xingray.config;

public interface FieldConverter<T, C> {
    C getConfig(T targetField);

    void restoreConfig(T targetField, C state);
}

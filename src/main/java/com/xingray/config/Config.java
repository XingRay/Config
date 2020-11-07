package com.xingray.config;

import com.xingray.config.annotations.ConfigType;
import com.xingray.config.annotations.Path;
import com.xingray.config.annotations.RealPath;
import com.xingray.config.annotations.UsePackageName;
import com.xingray.util.ConfigUtil;
import com.xingray.util.FileUtil;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private final String rootPath;
    private Map<Class<?>, Object> configs;
    private Map<Class<?>, String> pathCache;
    private JsonEngine jsonEngine;

    public Config() {
        this(null);
    }

    public Config(String rootPath) {
        this.rootPath = rootPath;
        configs = new HashMap<>();
        pathCache = new HashMap<>();
    }

    public void setJsonEngine(JsonEngine jsonEngine) {
        this.jsonEngine = jsonEngine;
    }

    public void initForClasses(Class<?>... classes) {
        for (Class<?> c : classes) {
            initForClass(c);
        }
    }

    public void initForClasses(Iterable<Class<?>> classes) {
        for (Class<?> c : classes) {
            initForClass(c);
        }
    }

    public <T> void initForClass(Class<T> cls) {
        try {
            T o = cls.getDeclaredConstructor().newInstance();
            load(o, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Object... objects) {
        for (Object o : objects) {
            Class cls = o.getClass();
            load(o, cls);
        }
    }

    public void init(Iterable<Object> objects) {
        for (Object o : objects) {
            Class cls = o.getClass();
            load(o, cls);
        }
    }

    public <T> void init(T config) {
        Class<T> cls = (Class<T>) config.getClass();
        load(config, cls);
    }

    private <T> void load(T config, Class<T> cls) {
        int configType = getConfigType(cls);
        if (configType == ConfigType.PROPERTIES) {
            loadProperties(config, cls);
        } else {
            loadJson(cls);
        }
    }

    private <T> void loadProperties(T config, Class<T> cls) {
        String path = pathCache.get(cls);
        if (path == null) {
            path = makePath(cls);
            pathCache.put(cls, path);
        }
        config = ConfigUtil.populateFromProperties(path, config);
        configs.put(cls, config);
    }

    private <T> void loadJson(Class<T> cls) {
        String path = pathCache.get(cls);
        if (path == null) {
            path = makePath(cls);
            pathCache.put(cls, path);
        }
        String s = FileUtil.readFile(path);
        T config = jsonEngine.fromJson(s, cls);
        configs.put(cls, config);
    }


    public <T> T get(Class<T> cls) {
        return (T) configs.get(cls);
    }

    public <T> void save(T config) {
        Class cls = config.getClass();
        String path = pathCache.get(cls);
        if (path == null) {
            path = makePath(cls);
            pathCache.put(cls, path);
        }

        int configType = getConfigType(cls);
        if (configType == ConfigType.JSON) {
            FileUtil.writeFile(path, jsonEngine.toJson(config));
        } else if (configType == ConfigType.PROPERTIES) {
            ConfigUtil.writeToProperties(path, config);
        }
    }

    private <T> String makePath(Class<?> cls) {
        RealPath realPathAnnotation = cls.getAnnotation(RealPath.class);
        if (realPathAnnotation != null) {
            return realPathAnnotation.value();
        }

        Path pathAnnotation = cls.getAnnotation(Path.class);
        if (pathAnnotation != null) {
            return pathAnnotation.value();
        }

        String path;
        UsePackageName usePackageNameAnnotation = cls.getAnnotation(UsePackageName.class);
        if (usePackageNameAnnotation == null) {
            path = rootPath + cls.getName();
        } else {
            path = rootPath + cls.getCanonicalName().replace(".", "_");
        }

        int value = getConfigType(cls);
        if (value == ConfigType.PROPERTIES) {
            path += ".properties";
        } else if (value == ConfigType.JSON) {
            path += ".json";
        }

        return path;
    }

    private int getConfigType(Class<?> cls) {
        ConfigType configTypeAnnotation = cls.getAnnotation(ConfigType.class);
        if (configTypeAnnotation != null) {
            return configTypeAnnotation.value();
        }
        return ConfigType.PROPERTIES;
    }
}

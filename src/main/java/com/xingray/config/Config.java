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
    private final Map<Class<?>, Object> configs;
    private final Map<Class<?>, String> pathCache;
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
            //noinspection rawtypes
            Class cls = o.getClass();
            //noinspection unchecked
            load(o, cls);
        }
    }

    public void init(Iterable<Object> objects) {
        for (Object o : objects) {
            //noinspection rawtypes
            Class cls = o.getClass();
            //noinspection unchecked
            load(o, cls);
        }
    }

    public <T> void init(T config) {
        //noinspection unchecked
        Class<T> cls = (Class<T>) config.getClass();
        load(config, cls);
    }

    private <T> T load(T config, Class<T> cls) {
        int configType = getConfigType(cls);
        if (configType == ConfigType.PROPERTIES) {
            return loadProperties(config, cls);
        } else if (configType == ConfigType.JSON) {
            return loadJson(cls);
        } else {
            return null;
        }
    }

    private <T> T loadProperties(T config, Class<T> cls) {
        String path = pathCache.get(cls);
        if (path == null) {
            path = makePath(cls);
            pathCache.put(cls, path);
        }
        if (config == null) {
            try {
                config = cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        config = ConfigUtil.populateFromProperties(path, config);
        configs.put(cls, config);
        return config;
    }

    private <T> T loadJson(Class<T> cls) {
        String path = pathCache.get(cls);
        if (path == null) {
            path = makePath(cls);
            pathCache.put(cls, path);
        }
        String s = FileUtil.readFile(path);
        T config = jsonEngine.fromJson(s, cls);
        configs.put(cls, config);

        return config;
    }


    public <T> T get(Class<T> cls) {
        //noinspection unchecked
        T t = (T) configs.get(cls);
        if (t == null) {
            t = load(null, cls);
        }
        return t;
    }

    public <T> void save(T config) {
        //noinspection rawtypes
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
            return rootPath + pathAnnotation.value();
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

package com.chatalytics.compute.util;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Contains utilities for reading in YAML objects.
 *
 * @author giannis
 *
 */
public class YamlUtils {

    /**
     * Reads in a YAML from a string and returns a constructed object of type <code>clazz</code>
     */
    public static <T> T readYamlFromString(String yamlStr, Class<T> clazz) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, clazz);
    }

    public static <T> String writeYaml(T value) {
        Yaml yaml = new Yaml();
        return yaml.dump(value);
    }

    /**
     * Reads in a YAML from a resource and returns a constructed object of type <code>clazz</code>
     */
    public static <T> T readYamlFromResource(String resource, Class<T> clazz) {
        InputStream is = null;
        try {
            is = ClassLoader.getSystemResourceAsStream(resource);
            Yaml yaml = new Yaml();
            return yaml.loadAs(is, clazz);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}

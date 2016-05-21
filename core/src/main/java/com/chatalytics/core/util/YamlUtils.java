package com.chatalytics.core.util;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.exception.MissingConfigException;

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
     *
     * @param yamlStr
     *            The YAML string to parse
     * @return A newly constructed {@link ChatAlyticsConfig}
     */
    public static <T> T readYamlFromString(String yamlStr, Class<T> clazz) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, clazz);
    }

    /**
     * Reads in a YAML from a string and returns a newly constructed {@link ChatAlyticsConfig}
     *
     * @param yamlStr
     *            The YAML string to parse
     * @return A newly constructed {@link ChatAlyticsConfig}
     */
    public static ChatAlyticsConfig readChatAlyticsConfigFromString(String yamlStr) {
        return readYamlFromString(yamlStr, ChatAlyticsConfig.class);
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
            if (is == null) {
                String msg = String.format("Can't find %s to construct %s", resource, clazz);
                throw new MissingConfigException(msg);
            }
            Yaml yaml = new Yaml();
            return yaml.loadAs(is, clazz);
        } finally {
            try {
                is.close();
            } catch (Exception e) { }
        }
    }

    /**
     * Reads a {@link ChatAlyticsConfig} from a resource in the classpath
     *
     * @param resource
     *            The resource to read
     * @return A newly constructed {@link ChatAlyticsConfig}
     */
    public static ChatAlyticsConfig readChatAlyticsConfig(String resource) {
        return readYamlFromResource(resource, ChatAlyticsConfig.class);
    }
}

package com.chatalytics.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link CommonCLIBuilder}
 *
 * @author giannis
 */
public class CommonCLIBuilderTest {

    @Test
    public void testParseOptions() {
        String[] args = new String[] { "argument" };
        CommandLine cli = CommonCLIBuilder.parseOptions(this.getClass(), args, new Options());
        assertNotNull(cli);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOptions_withMissingOptions() {
        String[] args = new String[] { "fail-on-random-option" };
        Options options = new Options();
        options.addOption(Option.builder("o").required().build());
        CommonCLIBuilder.parseOptions(this.getClass(), args, options);
    }

    @Test
    public void testGetCommonOptions() {
        Options options = CommonCLIBuilder.getCommonOptions();
        assertNotNull(options);
        assertEquals(1, options.getOptions().size());
    }

    @Test
    public void testGetConfigOption() {
        Options options = CommonCLIBuilder.getCommonOptions();
        String configValue = "test-config";
        String[] args = new String[] { "-c", configValue };
        CommandLine cli = CommonCLIBuilder.parseOptions(this.getClass(), args, options);
        String configResult = CommonCLIBuilder.getConfigOption(cli);
        assertEquals(configValue, configResult);
    }

}

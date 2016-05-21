package com.chatalytics.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommonCLIBuilder {

    public static final String CONFIG_OPT = "c";

    /**
     * Parses the command line arguments
     * @param clazz
     *            The main class invoked
     * @param args
     *            The args passed in
     * @param options
     *            The options to parse against
     * @return All the cli args
     * @throws IllegalArgumentException if parsing fails
     */
    public static CommandLine parseOptions(Class<?> clazz, String[] args, Options options) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            printUsageAndExit(clazz, options, e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static void printUsageAndExit(Class<?> clazz, Options options, String msg) {
        HelpFormatter formatter = new HelpFormatter();
        String footer = "\n" + msg;
        formatter.printHelp(clazz.getName(), null, options, footer);
    }

    /**
     * @return The common options
     */
    public static Options getCommonOptions() {

        Option opt = Option.builder(CONFIG_OPT)
                           .required()
                           .argName("chatalytics config file")
                           .hasArg()
                           .desc("Type the name of the config in the classpath")
                           .build();

        Options options = new Options();
        options.addOption(opt);

        return options;
    }

    /**
     * @param cli
     *            A parsed {@link CommandLine}
     * @return The name of the chatalytics config to load
     */
    public static String getConfigOption(CommandLine cli) {
        return cli.getOptionValue(CommonCLIBuilder.CONFIG_OPT);
    }
}

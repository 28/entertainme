package org.theparanoidtimes.entertainme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

/**
 * Main entry class, loads configurations and delays command execution to
 * {@code {@link CommandExecutor}}.
 *
 * @author 28
 */
public class EntertainMeCli {

    /**
     * Name of the default properties file.
     */
    private static final String defaultPropertiesFileName = "/defaults.properties";

    /**
     * Name of the entertainers configuration file.
     */
    private static final String propertiesFileName = "/entertainers.properties";

    /**
     * Main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        try {
            Properties properties = loadProperties();
            Options options = getOptions(properties);
            CommandExecutor executor = getExecutor(properties);
            CommandLine commandLine = new DefaultParser().parse(options, args);
            int status = executor.executeCommand(commandLine);
            System.exit(status);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Loads all properties needed for execution. Default properties located
     * within the jar and entertainers properties that is located in the file
     * system. Default properties are loaded first, then entertainers which is
     * merged over the default properties, overriding same keys if any.
     *
     * @return combined properties.
     * @throws Exception if either default or entertainers properties cannot be
     *                   loaded.
     */
    private static Properties loadProperties() throws Exception {
        Properties properties = new Properties();

        // load default properties
        properties.putAll(getDefaultProperties());

        // load user properties
        getUserProperties().ifPresent(properties::putAll);
        return properties;
    }

    /**
     * Loads default properties from the jar file.
     *
     * @return default properties.
     * @throws Exception if default properties cannot be loaded.
     */
    private static Properties getDefaultProperties() throws Exception {
        try (InputStream resourceAsStream = EntertainMeCli.class.getResourceAsStream(defaultPropertiesFileName)) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            return properties;
        }
    }

    /**
     * Loads the entertainers properties. This file must be located next to the
     * jar file in the filesystem, since this is the location that will be
     * searched.
     *
     * @return {@code {@link Optional}} of entertainers properties.
     * @throws Exception if entertainers properties cannot be loaded.
     */
    private static Optional<Properties> getUserProperties() throws Exception {
        String jarFolder = new File(EntertainMeCli.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()).getParent();
        File userProperties = new File(jarFolder + propertiesFileName);
        if (userProperties.exists() && !userProperties.isDirectory()) {
            try (FileInputStream fileInputStream = new FileInputStream(userProperties)) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                return Optional.of(properties);
            }
        }
        return Optional.empty();
    }

    /**
     * Converts the combined properties to {@code {@link Options}} object which
     * is used for command line argument parsing. During conversion 'baseUrl'
     * key is removed from the properties since that key is not needed as a
     * command line argument.
     *
     * @param properties properties to convert.
     * @return {@code {@link Options}} created from the passed properties.
     */
    private static Options getOptions(Properties properties) {
        Options options = new Options();
        properties.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals("baseUrl"))
                .forEach(e -> options.addOption(Option.builder((String) e.getKey())
                        .numberOfArgs(Option.UNLIMITED_VALUES)
                        .required(false)
                        .build()));
        return options;
    }

    /**
     * Returns a new {@code {@link CommandExecutor}} instance for the passed
     * properties. Properties are translated to a {@code {@link Map}} and passed
     * to the instance along with the 'baseUrl' value which is removed from the
     * map.
     *
     * @param properties properties to build {@code {@link CommandExecutor}}
     *                   with.
     * @return a new {@code {@link CommandExecutor}} instance.
     */
    private static CommandExecutor getExecutor(Properties properties) {
        Map<String, String> map = properties.entrySet()
                .stream()
                .collect(
                        toMap(e -> (String) e.getKey(),
                                e -> (String) e.getValue()));
        String baseUrl = map.remove("baseUrl");
        return new CommandExecutor(baseUrl, map);
    }
}

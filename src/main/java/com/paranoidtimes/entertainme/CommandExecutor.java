package com.paranoidtimes.entertainme;

import org.apache.commons.cli.CommandLine;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * This class constructs the browsing address based on input parameters and
 * opens a browser with that address.
 *
 * @author 28
 */
class CommandExecutor {

    /**
     * Used encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Base URL string for building the browsing address.
     */
    private final String baseUrl;

    /**
     * A map of nickname - YouTube user name mappings.
     */
    private final Map<String, String> entertainersMap;

    /**
     * Default constructor. Constructs a new instance of
     * {@code {@link CommandExecutor}}.
     *
     * @param baseUrl         base url for browsing addresses.
     * @param entertainersMap nickname-YouTube user name mapping.
     */
    CommandExecutor(String baseUrl, Map<String, String> entertainersMap) {
        this.baseUrl = baseUrl;
        this.entertainersMap = entertainersMap;
    }

    /**
     * Parses the passed {@code {@link CommandLine}} object and determines which
     * entertainer is wanted. Delegates command execution to
     * {@code {{@link #doExecuteCommand(String, String[])}}}.
     *
     * @param commandLine {@code {@link CommandLine}} object to parse.
     * @return status code to return upon application exit.
     * @throws Exception if {@code {@link CommandLine}} parsing fails or
     * @see CommandExecutor#doExecuteCommand(String, String[])
     */
    int executeCommand(CommandLine commandLine) throws Exception {
        for (Map.Entry<String, String> entry : entertainersMap.entrySet()) {
            if (commandLine.hasOption(entry.getKey())) {
                return doExecuteCommand(entry.getValue(), commandLine.getOptionValues(entry.getKey()));
            }
        }
        System.err.println("Entertainer not found!");
        return -1;
    }

    /**
     * Constructs the browsing address and initiates browser opening. Delegates
     * browser opening to {@code {@link #openBrowser(URI)}}.
     *
     * @param username     Youtube username.
     * @param optionValues search phrases.
     * @return status code to return upon application exit.
     * @throws Exception if browser address cannot be constructed or the browser
     *                   cannot be opened.
     * @see CommandExecutor#openBrowser(URI)
     */
    private int doExecuteCommand(String username, String[] optionValues) throws Exception {
        Optional<String> searchString = Arrays.stream(optionValues)
                .reduce((s, s2) -> s.concat(" ").concat(s2).trim());
        if (searchString.isPresent()) {
            String searchQuery = URLEncoder.encode(searchString.get(), ENCODING);
            String urlString = String.format(baseUrl, username, searchQuery);
            return openBrowser(new URL(urlString).toURI());
        }
        return -1;
    }

    /**
     * Opens a default system browser with the passed URL. The process will fail
     * if desktop is not supported by the system or if browsing desktop action
     * is not supported.
     *
     * @param uri to pass to the browser.
     * @return status code to return upon application exit.
     * @throws Exception if the browser cannot be opened.
     */
    private int openBrowser(URI uri) throws Exception {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
                return 0;
            } else {
                System.err.println("Browsing is not supported!");
                return -1;
            }
        } else {
            System.err.println("Desktop is not supported!");
            return -1;
        }
    }
}

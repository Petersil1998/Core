package net.petersil98.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main Class, which holds global instances of a {@link Logger} and {@link ObjectMapper}
 */
public class Core {
    public static final Logger LOGGER = LogManager.getLogger(Core.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();
}

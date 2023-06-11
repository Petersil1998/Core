package net.petersil98.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Core {
    public static final Logger LOGGER = LogManager.getLogger("CORE");
    public static final ObjectMapper MAPPER = new ObjectMapper();
}

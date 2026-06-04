package com.sellerpro.parser;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that returns the correct parser based on platform name.
 * All parsers are Spring-managed beans injected via constructor.
 */
@Component
public class ParserFactory {

    private final Map<String, BaseParser> parsers = new HashMap<>();

    public ParserFactory(List<BaseParser> parserList) {
        for (BaseParser parser : parserList) {
            parsers.put(parser.getPlatform().toUpperCase(), parser);
        }
    }

    /**
     * Returns the parser for a given platform.
     * @param platform e.g. "AMAZON", "FLIPKART", "MEESHO", "NYKAA", "WEBSITE"
     */
    public BaseParser getParser(String platform) {
        if (platform == null) throw new IllegalArgumentException("Platform cannot be null");
        BaseParser parser = parsers.get(platform.toUpperCase());
        if (parser == null) {
            throw new IllegalArgumentException("No parser found for platform: " + platform
                + ". Supported: " + parsers.keySet());
        }
        return parser;
    }

    public boolean isSupported(String platform) {
        return platform != null && parsers.containsKey(platform.toUpperCase());
    }

    public java.util.Set<String> getSupportedPlatforms() {
        return parsers.keySet();
    }
}

package com.box.utils;

import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import java.time.ZoneId;
import java.time.DateTimeException;

// Custom ArgumentType to validate timezone
public class ZoneIdArgumentType implements ArgumentType<ZoneId> {
    @Override
    public ZoneId convert(ArgumentParser parser, net.sourceforge.argparse4j.inf.Argument arg,
                          String value) throws ArgumentParserException {
        try {
            return ZoneId.of(value);
        } catch (DateTimeException ex) {
            throw new ArgumentParserException("Invalid timezone ID: " + value, parser);
        }
    }
}

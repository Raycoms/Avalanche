package com.constantine.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Logging utility class.
 */
public final class Log
{
    /**
     * Mod logger.
     */
    private static Logger logger = null;

    /**
     * Private constructor to hide the public one.
     */
    private Log()
    {
        /*
         * Intentionally left empty.
         */
    }

    static
    {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        //Only create logger if current logger is empty.
        if (logger == null)
        {
            Log.logger = LogManager.getLogger(Constants.APP_NAME);
        }
        return logger;
    }
}

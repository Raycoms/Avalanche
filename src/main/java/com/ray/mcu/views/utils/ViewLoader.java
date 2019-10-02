package com.ray.mcu.views.utils;

import com.ray.mcu.views.GlobalView;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import java.io.File;

/**
 * A view of the global network where all correct servers ought to have an identical one.
 */
public final class ViewLoader
{
    /**
     * ObjectMapper to handle jsons.
     */
    private static final ObjectMapper MAPPER = JsonFactory.create();

    /**
     * Load the View from file.
     *
     * @param configLocation the location the file is at.
     * @param subfix the file name.
     * @return the filled view class.
     */
    public static GlobalView loadView(final String configLocation, final String subfix)
    {
        return MAPPER.readValue(new File(configLocation + subfix), GlobalView.class).setConfigLocation(configLocation);
    }

    /**
     * Store the view to json.
     *
     * @param configLocation the location to store it to.
     * @param view           the view.
     */
    public static void storeView(final String configLocation, final GlobalView view)
    {
        MAPPER.writeValue(new File(configLocation), view);
    }

    /**
     * Private constructor to hide implicit public one.
     */
    private ViewLoader()
    {
        /*
         * Intentionally left empty.
         */
    }
}

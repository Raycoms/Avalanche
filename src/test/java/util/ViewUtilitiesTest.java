package util;

import com.ray.mcu.server.ServerData;
import com.ray.mcu.views.GlobalView;
import com.ray.mcu.views.utils.ViewLoader;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test to run the key utilities.
 */
public class ViewUtilitiesTest
{
    /**
     * The test resource location.
     */
    private static final String TEST_LOCATION = "./src/test/resources/";

    @Test
    public void storeAndRetrieveView()
    {
        final ServerData data = new ServerData(0, "127.0.0.1", 7000);
        final Map<Integer, ServerData> map = new HashMap<>();
        map.put(0, data);
        final GlobalView view = new GlobalView(0, 0, map);
        ViewLoader.storeView(TEST_LOCATION + "view.json", view);

        final GlobalView view2 = ViewLoader.loadView(TEST_LOCATION + "view.json");

        assertTrue(view.equals(view2));
    }
}

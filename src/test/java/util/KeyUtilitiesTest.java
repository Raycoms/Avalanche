package util;

import com.constantine.server.ServerData;
import com.constantine.utils.KeyUtilities;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Test to run the key utilities.
 */
public class KeyUtilitiesTest
{
    /**
     * The test resource location.
     */
    private static final String TEST_LOCATION = "./src/test/resources/test";

    @Test
    public void generateOrLoadKey()
    {
        final ServerData data = new ServerData(0, "127.0.0.1", 7000);
        assertNull(KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data));

        KeyUtilities.generateOrLoadKey(data, TEST_LOCATION);
        assertNotNull(data.getPublicKey());

        assertNotNull(KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data));

        try
        {
            Files.delete(Paths.get(TEST_LOCATION + 0));
            Files.delete(Paths.get(TEST_LOCATION + 0 + ".pub"));
            assertNull(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void generateOrLoadKeys()
    {
        for (int i = 0; i < 10; i++)
        {
            final ServerData data = new ServerData(i, "127.0.0.1", 7000);
            assertNull(KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data));

            KeyUtilities.generateOrLoadKey(data, TEST_LOCATION);
            assertNotNull(data.getPublicKey());

            assertNotNull(KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data));
        }

        for (int i = 0; i < 10; i++)
        {
            try
            {
                Files.delete(Paths.get(TEST_LOCATION + i));
                Files.delete(Paths.get(TEST_LOCATION + i + ".pub"));
                assertNull(null);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void signAndVerify()
    {
        final ServerData data = new ServerData(0, "127.0.0.1", 7000);
        KeyUtilities.generateOrLoadKey(data, TEST_LOCATION);
        assertNotNull(data.getPublicKey());

        final PublicKey publicKey = KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data);
        assertNotNull(publicKey);

        final PrivateKey privateKey = KeyUtilities.loadPrivateKeyFromFile(TEST_LOCATION, data);
        assertNotNull(privateKey);

        final byte[] message = new byte[20];
        final Random random = new Random();
        for (int i = 0; i < message.length; i++)
        {
            message[i] = (byte) (random.nextBoolean() ? 0 : 1);
        }

        final byte[] signedMessage = KeyUtilities.signMessage(message, privateKey);
        assertNotNull(signedMessage);

        assertTrue(KeyUtilities.verifyKey(message, signedMessage, publicKey));

        try
        {
            Files.delete(Paths.get(TEST_LOCATION + 0));
            Files.delete(Paths.get(TEST_LOCATION + 0 + ".pub"));
            assertNull(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void encryptDecrypt()
    {
        final ServerData data = new ServerData(0, "127.0.0.1", 7000);
        KeyUtilities.generateOrLoadKey(data, TEST_LOCATION);
        assertNotNull(data.getPublicKey());

        final PublicKey publicKey = KeyUtilities.loadPublicKeyFromFile(TEST_LOCATION, data);
        assertNotNull(publicKey);

        final PrivateKey privateKey = KeyUtilities.loadPrivateKeyFromFile(TEST_LOCATION, data);
        assertNotNull(privateKey);

        final byte[] message = new byte[20];
        final Random random = new Random();
        for (int i = 0; i < message.length; i++)
        {
            message[i] = (byte) (random.nextBoolean() ? 0 : 1);
        }

        final byte[] encryptedMessage = KeyUtilities.encryptMessage(message, publicKey);
        assertNotNull(encryptedMessage);

        assertArrayEquals(message, KeyUtilities.decryptMessage(encryptedMessage, privateKey));

        try
        {
            Files.delete(Paths.get(TEST_LOCATION + 0));
            Files.delete(Paths.get(TEST_LOCATION + 0 + ".pub"));
            assertNull(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

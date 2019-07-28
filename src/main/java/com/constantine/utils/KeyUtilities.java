package com.constantine.utils;

import com.constantine.server.ServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.security.rsa.RSAKeyPairGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

/**
 * Utility class for key generation.
 */
public final class KeyUtilities
{
    /**
     * Key generator object to generate the keys.
     */
    private static final RSAKeyPairGenerator gen = new RSAKeyPairGenerator();

    /*
     * Initiate the key generator with a specific random seed.
     * To guarantee that we generate the same keys on all servers.
     */
    static
    {
        final SecureRandom random = new SecureRandom();
        random.setSeed(1337);
        gen.initialize(1024, random);
    }

    /**
     * Generates or loads keys if existent for all servers in a list.
     *
     * @param list     the list of servers.
     * @param location the location to save or load it from
     */
    public static void generateOrLoadKeys(final List<ServerData> list, final String location)
    {
        for (final ServerData server : list)
        {
            generateOrLoadKey(server, location);
        }
    }

    /**
     * Generates or loads key if existent for a specific server.
     *
     * @param data     the server data.
     * @param location the location to save or load it from
     */
    public static void generateOrLoadKey(@NotNull final ServerData data, @NotNull final String location)
    {
        final Path publicPath = Paths.get(location + data.getId() + ".pub");
        final Path privatePath = Paths.get(location + data.getId());

        if (Files.exists(publicPath) && Files.exists(privatePath))
        {
            data.setPublicKey(loadPublicKeyFromFile(location, data));
        }
        else
        {
            try
            {
                final KeyPair keyPair = gen.generateKeyPair();

                Files.write(privatePath, keyPair.getPrivate().getEncoded());
                Files.write(publicPath, keyPair.getPublic().getEncoded());

                data.setPublicKey(keyPair.getPublic());
            }
            catch (final IOException e)
            {
                Log.getLogger().error("Error during key saving process!", e);
            }
        }
    }

    /**
     * Load the public key from file.
     * @param location the location it is at.
     * @param data the server data to load it for.
     * @return the public key or null on failure.
     */
    @Nullable
    public static PublicKey loadPublicKeyFromFile(@NotNull final String location, @NotNull final ServerData data)
    {
        try
        {
            final byte[] pub = Files.readAllBytes(Paths.get(location + data.getId() + ".pub"));

            final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pub);
            final KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");
            return publicKeyFactory.generatePublic(publicKeySpec);
        }
        catch (final NoSuchAlgorithmException | IOException | InvalidKeySpecException e)
        {
            Log.getLogger().error("Error during key loading process!", e);
        }
        return null;
    }

    /**
     * Load the private key from file.
     * @param location the location it is at.
     * @param data the server data to load it for.
     * @return the private key or null on failure.
     */
    @Nullable
    public static PrivateKey loadPrivateKeyFromFile(@NotNull final String location, @NotNull final ServerData data)
    {
        try
        {
            final byte[] priv = Files.readAllBytes(Paths.get(location + data.getId()));

            final PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(priv);
            final KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
            return privateKeyFactory.generatePrivate(ks);
        }
        catch (final NoSuchAlgorithmException | IOException | InvalidKeySpecException e)
        {
            Log.getLogger().error("Error during key loading process!", e);
        }
        return null;
    }

    /**
     * Verify the signature given the message, signature and public key.
     *
     * @param message   the message.
     * @param signature the signature.
     * @param publicKey the public key.
     * @return true if valid.
     */
    public static boolean verifyKey(@NotNull final byte[] message, @NotNull final byte[] signature, @NotNull final PublicKey publicKey)
    {
        try
        {
            final Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(publicKey);
            sig.update(message);
            return sig.verify(signature);
        }
        catch (final NoSuchAlgorithmException | SignatureException | InvalidKeyException e)
        {
            Log.getLogger().error("Issues in the key verification process!", e);
        }
        return false;
    }

    /**
     * Sign a message given the private key.
     * @param message the message to sign.
     * @param privateKey the private key to sign it with.
     * @return the resulting signature of null on failure.
     */
    @Nullable
    public static byte[] signMessage(@NotNull final byte[] message, @NotNull final PrivateKey privateKey)
    {
        try
        {
            final Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(privateKey);
            sig.update(message);
            return sig.sign();
        }
        catch (final NoSuchAlgorithmException | SignatureException | InvalidKeyException e)
        {
            Log.getLogger().error("Issues in the key verification process!", e);
        }
        return null;
    }

    /**
     * Encrypt a message with a public key.
     *
     * @param message   the message.
=     * @param publicKey the public key.
     * @return the encrypted message.
     */
    public static byte[] encryptMessage(@NotNull final byte[] message, @NotNull final PublicKey publicKey)
    {
        try
        {
            final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(message);
        }
        catch (final NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException e)
        {
            Log.getLogger().error("Issues in the encrypting process!", e);
        }
        return null;
    }

    /**
     * Decrypt a message with a given private key.
     * @param message the message to decrypt.
     * @param privateKey the private key to use.
     * @return the resulting message.
     */
    @Nullable
    public static byte[] decryptMessage(@NotNull final byte[] message, @NotNull final PrivateKey privateKey)
    {
        try
        {
            final Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(message);
        }
        catch (final NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e)
        {
            Log.getLogger().error("Issues in the decrypting process!", e);
        }
        return null;
    }


    /**
     * Private constructor to hide implicit one.
     */
    private KeyUtilities()
    {
        /*
         * Intentionally left empty.
         */
    }
}

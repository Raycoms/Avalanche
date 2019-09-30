package com.constantine.client;

import com.constantine.nettyhandlers.SizedMessageDecoder;
import com.constantine.nettyhandlers.SizedMessageEncoder;
import com.constantine.proto.MessageProto;
import com.constantine.server.ServerData;
import com.constantine.utils.KeyUtilities;
import com.constantine.utils.Log;
import com.constantine.views.GlobalView;
import com.constantine.views.utils.ViewLoader;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import sun.security.rsa.RSAKeyPairGenerator;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import static com.constantine.utils.Constants.CONFIG_LOCATION;

/**
 * Client used to connect to servers to propagate messages.
 */
public class Client extends Thread
{
    /**
     * The server id of the server to connect to.
     */
    private final int serverId;

    /**
     * The global system server view.
     */
    private final GlobalView view;

    /**
     * The private key which belongs to the client.
     */
    private final PrivateKey privateKey;

    /**
     * The public key which belongs to the client.
     */
    private final PublicKey publicKey;

    /**
     * The proto message builder to avoid creating a new builder for each message.
     */
    static final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

    /**
     * The client sender handler.
     */
    private ClientNettySenderHandler clientHandler;

    /**
     * Instantiate the client process and load the view.
     * @param serverId the server id to connect to.
     * @param gen the key generator.
     */
    public Client(final int serverId, final RSAKeyPairGenerator gen)
    {
        this.serverId = serverId;
        this.view = ViewLoader.loadView(CONFIG_LOCATION + "view.json");
        final KeyPair keyPair = gen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    /**
     * Connect to the specified server.
     */
    public void connect()
    {
        final Bootstrap b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);

        final ServerData data = view.getServer(serverId);

        this.clientHandler = new ClientNettySenderHandler(data);
        b.handler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch)
            {
                ch.pipeline().addLast(
                  new SizedMessageEncoder(),
                  new SizedMessageDecoder(),
                  clientHandler);
            }
        });
        Log.getLogger().warn("Starting connection to ServerReceiver: " + data.getId());
        b.connect(data.getIp(), data.getCport());
    }

    @Override
    public void run()
    {
        final Scanner in = new Scanner(System.in);
        while (!in.hasNext())
        {
            final MessageProto.ClientMessage msg = MessageProto.ClientMessage.newBuilder().setDif(10).setPkey(publicKey.toString()).build();
            builder.setClientMsg(msg).setSig(ByteString.copyFrom(KeyUtilities.signMessage(msg.toByteArray(), this.privateKey))).build();

            try
            {
                //todo configure sending frequency (config file).
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                /*
                 * Intentionally left empty.
                 */
            }
        }

        clientHandler.disconnect();
    }

    /**
     * Create a client thread, connect and start it.
     * @param serverId the server id to connect to.
     * @param gen key gen to generate keypair.
     */
    public static void createClient(final int serverId, final RSAKeyPairGenerator gen)
    {
        final Client client = new Client(serverId, gen);
        client.connect();
        client.start();
    }

    /**
     * Method to start the client handler.
     * Two Modes:
     * 1: Arg1: Number of Clients Arg2: Server to connect to.
     * 2: Arg1: Server to connect to. (1 Client only).
     * @param args the arguments to start the client.
     */
    public static void main(final String...args)
    {
        if (args == null || args.length == 0)
        {
            Log.getLogger().warn("Empty Arguments to start Client - aborting");
            return;
        }

        final RSAKeyPairGenerator gen = new RSAKeyPairGenerator();

        if (args.length == 1)
        {
            try
            {
                createClient(Integer.parseInt(args[0]), gen);
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid number for server id to connect to when starting client - aborting");
            }
        }
        else
        {
            try
            {
                final int numberOfServers = Integer.parseInt(args[0]);
                final int serverId = Integer.parseInt(args[1]);

                for (int i = 0; i < numberOfServers; i++)
                {
                    createClient(serverId, gen);
                }
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid number starting client - aborting");
            }
        }
    }
}

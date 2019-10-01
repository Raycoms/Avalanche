package com.constantine.comm.client;

import com.constantine.comm.nettyhandlers.SizedMessageDecoder;
import com.constantine.comm.nettyhandlers.SizedMessageEncoder;
import com.constantine.comm.proto.MessageProto;
import com.constantine.comm.server.ServerData;
import com.constantine.comm.utils.KeyUtilities;
import com.constantine.comm.utils.Log;
import com.constantine.comm.views.GlobalView;
import com.constantine.comm.views.utils.ViewLoader;
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

import static com.constantine.comm.utils.Constants.CONFIG_LOCATION;

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
    public final GlobalView view;

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
    private final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

    /**
     * The client sender handler.
     */
    private ClientNettySenderHandler clientHandler;

    /**
     * The client data.
     */
    private ClientData clientData;

    /**
     * Instantiate the client process and load the view.
     * @param serverId the server id to connect to.
     * @param gen the key generator.
     * @param ip the ip.
     * @param port the port.
     */
    public Client(final int serverId, final RSAKeyPairGenerator gen, final String ip, final int port)
    {
        this.serverId = serverId;
        this.view = ViewLoader.loadView(CONFIG_LOCATION, "view.json");
        final KeyPair keyPair = gen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();

        this.clientData = new ClientData(publicKey, ip, port);
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
        Log.getLogger().warn("Starting connection to ServerReceiver: " + data.getId() + " at port: " + data.getCport());
        b.connect(data.getIp(), data.getCport());
    }

    @Override
    public void run()
    {
        while (!clientHandler.isActive())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                /*
                 * Intentionally left empty.
                 */
            }
        }

        while (true)
        {
            final MessageProto.ClientMessage msg = MessageProto.ClientMessage.newBuilder().setDif(10).setPkey(ByteString.copyFrom(publicKey.getEncoded())).build();
            builder.setClientMsg(msg).setSig(ByteString.copyFrom(KeyUtilities.signMessage(msg.toByteArray(), this.privateKey))).build();

            clientHandler.write(builder.build());

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
    }

    /**
     * Create a client thread, connect and start it.
     * @param serverId the server id to connect to.
     * @param gen key gen to generate keypair.
     * @param ip the ip.
     * @param port the port.
     */
    public static void createClient(final int serverId, final RSAKeyPairGenerator gen, final String ip, final int port)
    {
        final Client client = new Client(serverId, gen, ip, port);
        client.connect();

        client.setupReceiver();

        client.start();
    }

    /**
     * Setup the client receiver.
     */
    private void setupReceiver()
    {
        final ClientReceiver receiver = new ClientReceiver(this);
        receiver.start();
    }

    /**
     * Get the client data of this client.
     * @return the client data.
     */
    public ClientData getClientData()
    {
        return this.clientData;
    }

    /**
     * Method to start the client handler.
     * Two Modes:
     * 1: Arg1: Number of Clients Arg2: Server to connect to. Arg3: Ip, Arg:4 Starting port
     * 2: Arg1: Server to connect to. (1 Client only), Arg2: Ip, Arg3: Port
     * @param args the arguments to start the client.
     */
    public static void main(final String...args)
    {
        if (args == null || args.length == 0)
        {
            Log.getLogger().warn("Invalid input parameters. #ServerId (int) #ClientIp (String) #ClientPort (int) or #NumberOfClients (int) #ServerId (int) #ClientIp (String) #ClientPort (int)");
            return;
        }

        final RSAKeyPairGenerator gen = new RSAKeyPairGenerator();

        if (args.length == 3)
        {
            try
            {
                createClient(Integer.parseInt(args[0]), gen, args[1], Integer.parseInt(args[2]));
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid input parameters. #ServerId (int) #ClientIp (String) #ClientPort (int) or #NumberOfClients (int) #ServerId (int) #ClientIp (String) #ClientPort (int)");
            }
        }
        else if (args.length == 4)
        {
            try
            {
                final int numberOfServers = Integer.parseInt(args[0]);
                final int serverId = Integer.parseInt(args[1]);

                for (int i = 0; i < numberOfServers; i++)
                {
                    createClient(serverId, gen, args[2], Integer.parseInt(args[3]));
                }
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid input parameters. #ServerId (int) #ClientIp (String) #ClientPort (int) or #NumberOfClients (int) #ServerId (int) #ClientIp (String) #ClientPort (int)");
            }
        }
        else
        {
            Log.getLogger().warn("Invalid input parameters. #ServerId (int) #ClientIp (String) #ClientPort (int) or #NumberOfClients (int) #ServerId (int) #ClientIp (String) #ClientPort (int)");
        }
    }
}

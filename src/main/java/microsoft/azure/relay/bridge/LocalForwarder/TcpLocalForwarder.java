package microsoft.azure.relay.bridge.LocalForwarder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.azure.relay.HybridConnectionClient;
import com.microsoft.azure.relay.RelayConnectionStringBuilder;
import com.microsoft.azure.relay.TokenProvider;

public class TcpLocalForwarder {

    private String listenerHostname;
    private int listenerPort;

    static final String CONNECTION_STRING_ENV_VARIABLE_NAME = "RELAY_CONNECTION_STRING";
    static final RelayConnectionStringBuilder connectionParams = new RelayConnectionStringBuilder(
            System.getenv(CONNECTION_STRING_ENV_VARIABLE_NAME));

    public TcpLocalForwarder(String host, int port) throws Exception {
        this.listenerHostname = host;
        this.listenerPort = port;

        TokenProvider tokenProvider = TokenProvider.createSharedAccessSignatureTokenProvider(
                connectionParams.getSharedAccessKeyName(),
                connectionParams.getSharedAccessKey());

        HybridConnectionClient hybridConnectionClient = new HybridConnectionClient(
                new URI(connectionParams.getEndpoint().toString() + connectionParams.getEntityPath()),
                tokenProvider);

        try (final AsynchronousServerSocketChannel socketListener = AsynchronousServerSocketChannel.open()) {

            TunnelContext tunnelContext = new TunnelContext();

            // Set socket listener context
            tunnelContext.setSocketListener(socketListener);

            // Set hybrid connection client context
            tunnelContext.setHybridConnectionClient(hybridConnectionClient);

            // Bind socket listener address
            InetSocketAddress listenerAddr = new InetSocketAddress(this.listenerHostname, this.listenerPort);
            socketListener.bind(listenerAddr);

            System.out.println("Listener bound to port: " + listenerAddr.getPort());
            System.out.println("Waiting for client to connect... ");

            // Accept the socket client connection
            socketListener.accept(tunnelContext, new TcpClientAcceptHandler());

            // Wait until the main thread is interrupted
            Thread.currentThread().join();
        } catch (Exception e) {
            Logger.getLogger(TcpLocalForwarder.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void main(String[] args) {

        try {
            new TcpLocalForwarder("localhost", 7979);
        } catch (Exception e) {
            Logger.getLogger(TcpLocalForwarder.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}

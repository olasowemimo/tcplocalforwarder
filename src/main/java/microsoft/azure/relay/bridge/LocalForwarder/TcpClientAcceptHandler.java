package microsoft.azure.relay.bridge.LocalForwarder;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpClientAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, TunnelContext> {

    private final int BUFFER_SIZE = 2048;

    @Override
    public void completed(AsynchronousSocketChannel socketClient, TunnelContext tunnelContext) {

        try {

            // Get the socket client address
            SocketAddress socketClientAddr = socketClient.getRemoteAddress();
            System.out.format("Accepted a connection from %s%n", socketClientAddr);

            // If socket listener is open, accept 'this' socket client
            if (tunnelContext.getSocketListener().isOpen()) {
                tunnelContext.getSocketListener().accept(tunnelContext, this); //
                System.out.println("Socket client connection accepted and opened!");
            }

            if ((socketClient != null) && (socketClient.isOpen())) {

                TunnelContext newContext = new TunnelContext();
                // Set new context
                newContext.setHybridConnectionClient(tunnelContext.getHybridConnectionClient());
                newContext.setSocketListener(tunnelContext.getSocketListener());
                newContext.setSocketClient(socketClient);
                newContext.setSocketClientAddr(socketClientAddr);
                newContext.setSocketBuffer(ByteBuffer.allocate(BUFFER_SIZE));
                newContext.setReadMode(true);

                // Handle the socket client connection
                TcpReadWriteHandler rwHandler = new TcpReadWriteHandler();
                socketClient.read(newContext.getSocketBuffer(), newContext, rwHandler);

            }

        } catch (IOException e) {
            Logger.getLogger(TcpClientAcceptHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void failed(Throwable e, TunnelContext tunnelContext) {
        Logger.getLogger(TcpClientAcceptHandler.class.getName()).log(Level.SEVERE, "Failed to accept a connection.",
                e);
    }
}

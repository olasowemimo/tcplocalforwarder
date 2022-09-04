package microsoft.azure.relay.bridge.LocalForwarder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.azure.relay.HybridConnectionChannel;

public class TcpReadWriteHandler implements CompletionHandler<Integer, TunnelContext> {
    @Override
    public void completed(Integer nBytesReceived, TunnelContext tunnelContext) {
        if (nBytesReceived == -1) {
            try {
                tunnelContext.getSocketClient().close();
                System.out.format("Stopped listening to the client %s%n", tunnelContext.getSocketClientAddr());
            } catch (IOException e) {
                Logger.getLogger(TcpReadWriteHandler.class.getName()).log(Level.SEVERE, null, e);
            }
            return;
        }

        CompletableFuture<HybridConnectionChannel> hybridConn = tunnelContext.getHybridConnectionClient()
                .createConnectionAsync();

        if (nBytesReceived > 0) {

            // Accept hybrid connection channel
            hybridConn.thenAccept((conn) -> {

                if (conn.isOpen()) {

                    if (tunnelContext.isReadMode()) {

                        tunnelContext.getSocketBuffer().flip();

                        conn.writeAsync(tunnelContext.getSocketBuffer());
                        ByteBuffer receivedByteBuffer = conn.readAsync().join(); // this is the breaking point

                        System.out
                                .println("Received: " + new String(receivedByteBuffer.array(),
                                        receivedByteBuffer.arrayOffset(),
                                        receivedByteBuffer.remaining()));

                        tunnelContext.setReadMode(false);

                        tunnelContext.getSocketClient().write(receivedByteBuffer, tunnelContext, this);

                    } else {

                        tunnelContext.setReadMode(true);

                        tunnelContext.getSocketBuffer().clear();

                        // Read subsequent data from the socket client
                        tunnelContext.getSocketClient().read(tunnelContext.getSocketBuffer(),
                                tunnelContext, this);
                    }

                }
            }).join();

        }

    }

    @Override
    public void failed(Throwable e, TunnelContext tunnelContext) {
        Logger.getLogger(TcpReadWriteHandler.class.getName()).log(Level.SEVERE, null, e);
    }

}

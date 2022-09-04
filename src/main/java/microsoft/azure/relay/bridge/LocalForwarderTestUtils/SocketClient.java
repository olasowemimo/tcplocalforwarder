package microsoft.azure.relay.bridge.LocalForwarderTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketClient {

    public SocketClient(String listener, int listenerPort) {

        // create a client socket channel
        try (final AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open()) {

            InetSocketAddress listenerAddr = new InetSocketAddress(listener, listenerPort);

            // The method connect() creates a new Thread to resolve
            Future<Void> clientConn = clientChannel.connect(listenerAddr);

            clientConn.get(); // block until the connection is successful or throw an exception.

            // Connection to the listener is now established
            System.out.println("Connection Established!!!");

            // handle this connection
            ClientAttachment attach = new ClientAttachment();
            attach.channel = clientChannel;
            attach.isReadMode = false;
            attach.buffer = ByteBuffer.allocate(2048);
            attach.mainThread = Thread.currentThread();

            Charset cs = Charset.forName("UTF-8");
            String msg = "Hello"; // send a test msg to the listener
            byte[] data = msg.getBytes(cs);
            attach.buffer.put(data);
            attach.buffer.flip();

            // Write to the listener
            ClientReadWriteTask readWriteTask = new ClientReadWriteTask();
            clientChannel.write(attach.buffer, attach, readWriteTask);

            attach.mainThread.join();

            System.out.println("Client terminating..");

        } catch (IOException | InterruptedException | ExecutionException e) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, "Client channel has been interrupted",
                    e);
        }
    }

    class ClientAttachment {
        AsynchronousSocketChannel channel;
        ByteBuffer buffer;
        boolean isReadMode;
        Thread mainThread;
    }

    static class ClientReadWriteTask implements CompletionHandler<Integer, ClientAttachment> {

        @Override
        public void completed(Integer result, ClientAttachment attach) {
            if (attach.isReadMode) {

                attach.buffer.flip();
                Charset cs = Charset.forName("UTF-8");
                int bufferLimits = attach.buffer.limit();
                byte byteData[] = new byte[bufferLimits];
                attach.buffer.get(byteData, 0, bufferLimits);
                String msg = new String(byteData, cs);

                // A read from the listener was completed
                System.out.format("Listener Responded: %s%n", msg);

                // Prompt the user for another message
                msg = this.getUserInput();
                if (msg.equalsIgnoreCase("q")) {
                    // Interrupt the main thread, so the program terminates
                    attach.mainThread.interrupt();
                    return;
                }
                // Prepare buffer to be filled in again
                attach.buffer.clear();
                byte[] data = msg.getBytes(cs);
                attach.buffer.put(data);

                // Prepared buffer to be read
                attach.buffer.flip();
                attach.isReadMode = false; // It is a write mode

                // Write to the listener
                attach.channel.write(attach.buffer, attach, this);
            } else {
                // A write to the listener was completed. Perform another read from the listener
                attach.isReadMode = true;

                // Prepare the buffer to be filled in
                attach.buffer.clear();

                // Read from the server
                attach.channel.read(attach.buffer, attach, this);
            }
        }

        @Override
        public void failed(Throwable e, ClientAttachment attach) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, e);
        }

        private String getUserInput() {
            System.out.print("Please enter a message (q to quit):");
            String msg = null;

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                msg = consoleReader.readLine();
            } catch (IOException e) {
                Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, e);
            }
            return msg;
        }

        public static void main(String... args) {

            System.out.println("Client channel connecting...");

            new SocketClient("127.0.0.1", 7979);
        }
    }
}

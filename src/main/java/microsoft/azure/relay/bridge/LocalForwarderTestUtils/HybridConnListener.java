package microsoft.azure.relay.bridge.LocalForwarderTestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.relay.HybridConnectionListener;
import com.microsoft.azure.relay.RelayConnectionStringBuilder;
import com.microsoft.azure.relay.TokenProvider;

public class HybridConnListener {
	static boolean quit = false;

	static final String CONNECTION_STRING_ENV_VARIABLE_NAME = "RELAY_CONNECTION_STRING";
	static final RelayConnectionStringBuilder connectionParams = new RelayConnectionStringBuilder(
			System.getenv(CONNECTION_STRING_ENV_VARIABLE_NAME));

	public static void main(String[] args) throws URISyntaxException {
		TokenProvider tokenProvider = TokenProvider.createSharedAccessSignatureTokenProvider(
				connectionParams.getSharedAccessKeyName(), connectionParams.getSharedAccessKey());
		HybridConnectionListener listener = new HybridConnectionListener(
				new URI(connectionParams.getEndpoint().toString() + connectionParams.getEntityPath()), tokenProvider);

		listener.openAsync().join();
		System.out.println("Listener is online.");

		CompletableFuture.runAsync(() -> {
			Scanner in = new Scanner(System.in);
			in.nextLine();

			// Closing the listener will cause it to stop accepting any more connections.
			// However, it will not shutdown the connections that are already established
			// and still running.
			listener.close();
			in.close();
		});

		while (listener.isOnline()) {

			listener.acceptConnectionAsync().thenAccept(connection -> {
				// connection may be null if the listener is closed before receiving a
				// connection
				if (connection != null) {
					System.out.println("New session connected.");

					while (connection.isOpen()) {
						ByteBuffer bytesReceived = connection.readAsync().join();
						// If the read operation is still pending when connection closes, the read
						// result is null.
						if (bytesReceived.remaining() > 0) {
							String msg = new String(bytesReceived.array(), bytesReceived.arrayOffset(),
									bytesReceived.remaining());
							ByteBuffer msgToSend = ByteBuffer.wrap(("Echo: " + msg).getBytes());

							System.out.println("Received: " + msg);
							connection.writeAsync(msgToSend);
						}
					}
					System.out.println("Session disconnected.");
				}
			}).join();
		}
	}
}

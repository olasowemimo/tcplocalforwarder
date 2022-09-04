package microsoft.azure.relay.bridge.LocalForwarder;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import com.microsoft.azure.relay.HybridConnectionClient;

public class TunnelContext {
    private AsynchronousServerSocketChannel socketListener;
    private AsynchronousSocketChannel socketClient;
    private HybridConnectionClient hybridConnectionClient;
    private ByteBuffer socketBuffer;
    private SocketAddress socketClientAddr;
    private Thread mainThread;
    private boolean isReadMode;
    private ByteBuffer receivedBuffer;

    public AsynchronousServerSocketChannel getSocketListener() {
        return socketListener;
    }

    public void setSocketListener(AsynchronousServerSocketChannel socketListener) {
        this.socketListener = socketListener;
    }

    public AsynchronousSocketChannel getSocketClient() {
        return socketClient;
    }

    public void setSocketClient(AsynchronousSocketChannel socketClient) {
        this.socketClient = socketClient;
    }

    public HybridConnectionClient getHybridConnectionClient() {
        return hybridConnectionClient;
    }

    public void setHybridConnectionClient(HybridConnectionClient hybridConnectionClient) {
        this.hybridConnectionClient = hybridConnectionClient;
    }

    public ByteBuffer getSocketBuffer() {
        return socketBuffer;
    }

    public void setSocketBuffer(ByteBuffer socketBuffer) {
        this.socketBuffer = socketBuffer;
    }

    public SocketAddress getSocketClientAddr() {
        return socketClientAddr;
    }

    public void setSocketClientAddr(SocketAddress socketClientAddr) {
        this.socketClientAddr = socketClientAddr;
    }

    public Thread getMainThread() {
        return mainThread;
    }

    public void setMainThread(Thread mainThread) {
        this.mainThread = mainThread;
    }

    public boolean isReadMode() {
        return isReadMode;
    }

    public void setReadMode(boolean isReadMode) {
        this.isReadMode = isReadMode;
    }

    public ByteBuffer getReceivedBuffer() {
        return receivedBuffer;
    }

    public void setReceivedBuffer(ByteBuffer receivedBuffer) {
        this.receivedBuffer = receivedBuffer;
    }

}

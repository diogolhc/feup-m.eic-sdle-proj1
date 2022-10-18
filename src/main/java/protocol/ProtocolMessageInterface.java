package protocol;

import org.zeromq.ZMQ;

public interface ProtocolMessageInterface {
    default void send(ZMQ.Socket socket) {
        socket.send(this.toString().getBytes(ZMQ.CHARSET), 0);
    }
}

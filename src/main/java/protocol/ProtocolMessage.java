package protocol;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import protocol.status.StatusMessage;

import java.util.ArrayList;
import java.util.List;

public abstract class ProtocolMessage {
    private final String id;

    public ProtocolMessage(String id) {
        this.id = id;
    }

    public abstract String getType();

    public List<String> getHeaderFields() {
        return new ArrayList<>();
    }

    public String getBody() {
        return null;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getType());
        sb.append(" ");
        sb.append(this.id);
        for (String field : this.getHeaderFields()) {
            sb.append(" ");
            sb.append(field);
        }
        sb.append("\r\n\r\n");
        if (this.getBody() != null) {
            sb.append(this.getBody());
        }
        return sb.toString();
    }

    public void send(ZMQ.Socket socket) {
        socket.send(this.toString().getBytes(ZMQ.CHARSET), 0);
    }

    public ProtocolMessage sendAngGetResponseBlocking(ZMQ.Socket socket) {
        this.send(socket);
        byte[] response = socket.recv(0);
        if (response == null) {
            return null;
        } else {
            return new MessageParser(response).getMessage();
        }
    }

    public ProtocolMessage sendWithRetriesAndTimeoutAndGetResponse(ZContext context, String address, ZMQ.Socket socket, Integer maxTries, Integer timeout) {
        ZMQ.Poller poller = context.createPoller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        int retriesLeft = maxTries;
        while (retriesLeft > 0 && !Thread.currentThread().isInterrupted()) {
            this.send(socket);

            while (true) {
                //  Poll socket for a reply, with timeout
                int rc = poller.poll(timeout);
                if (rc == -1)
                    break; //  Interrupted

                //  Here we process a server reply and exit our loop if the
                //  reply is valid. If we didn't a reply we close the client
                //  socket and resend the request. We try a number of times
                //  before finally abandoning:

                if (poller.pollin(0)) {
                    //  We got a reply from the server
                    String responseMessage = socket.recvStr();
                    if (responseMessage == null)
                        break; //  Interrupted

                    return new MessageParser(responseMessage).getMessage();

                } else if (--retriesLeft == 0) {
                    System.out.println("Exceeded number of tries. Timeout. No response");
                    return null;
                } else {
                    System.out.println("No response from server, retrying");
                    //  Old socket is confused; close it and open a new one
                    poller.unregister(socket);
                    context.destroySocket(socket);
                    System.out.println("Reconnecting to server\n");
                    socket = context.createSocket(SocketType.REQ);
                    if (!socket.connect("tcp://" + address)) {
                        System.out.println("Could not connect to " + address + ".");
                        continue;
                    }
                    poller.register(socket, ZMQ.Poller.POLLIN);
                    //  Send request again, on new socket
                    this.send(socket);
                }
            }
        }

        return null;
    }

}

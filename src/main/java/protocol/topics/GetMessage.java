package protocol.topics;

import java.util.List;

// GET <ID> <TOPIC> <COUNTER> CRLF CRLF
public class GetMessage extends TopicsMessage {
    public final static String TYPE = "GET";
    public final String counter;

    public GetMessage(String id, String topic, String counter) {
        super(id, topic);
        this.counter = counter;
    }

    public String getCounter() { return this.counter; }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getHeaderFields() {
        List<String> headerList = super.getHeaderFields();
        headerList.add(this.counter);
        return headerList;
    }
}

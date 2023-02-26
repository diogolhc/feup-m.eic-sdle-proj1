# Implementation of a Reliable Load Balanced Publisher-Subscriber Service

SDLE First Assignment of group T03G14.

Group members:

1. Catarina Pires (up201907925@edu.fe.up.pt)
2. Diogo Costa (up201906731@edu.fe.up.pt)
3. Francisco Colino (up201905405@edu.fe.up.pt)
4. Pedro Gonçalo Correia (up201905348@edu.fe.up.pt)

## Description
In this project, we present a reliable load
balanced publisher-subscriber service on top
of the ZeroMQ library. Our service enforces
exactly-once semantics and is robust to faults,
except in some rare circumstances.

The service supports four operations: subscribe, unsubscribe, get, and put, which can be executed by a client. We consider that we designed and implemented this system correctly, and its features are working as intended.


For more information about the project, please refer to the [project report](./doc/report.pdf).

## Compile instructions:
To compile the source code run: \
```./gradlew build```

## Run instructions:

### Proxy
To launch a proxy run: \
```./gradlew proxy --args="<IP>:<PORT>"```

### Server
To launch a server run: \
```./gradlew server --args="<IP>:<PORT>"```

### Client
To launch a client run: \
```./gradlew client --args="<ID> subscribe <TOPIC>"``` or\
```./gradlew client --args="<ID> unsubscribe <TOPIC>"``` or\
```./gradlew client --args="<ID> put <TOPIC> MESSAGE_PATH"``` or\
```./gradlew client --args="<ID> get <TOPIC>"```

# SDLE First Assignment

SDLE First Assignment of group T03G14.

Group members:

1. Catarina Pires (up201907925@edu.fe.up.pt)
2. Diogo Costa (up201906731@edu.fe.up.pt)
3. Francisco Colino (up201905405@edu.fe.up.pt)
4. Pedro Gonçalo Correia (up201905348@edu.fe.up.pt)

## Compile instructions:
You may compile the source code using the included makefile. Run: \
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
```./gradlew client --args="<ID> put <TOPIC> MESSAGE_PATH``` or\
```./gradlew client --args="<ID> get <TOPIC>```
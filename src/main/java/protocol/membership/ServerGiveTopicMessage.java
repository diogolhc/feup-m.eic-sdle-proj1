package protocol.membership;

/*
127.0.0.1:8051 1 3 4 5
127.0.0.1:8052 1 3 4 5
127.0.0.1:8053 1 3 4 5
127.0.0.1:8054 1 3 4 5
*1
msgsgsrhrdh1\smsgis\\gfjsigshjrgisr
*3
gakgjhaigihsurjgvoisjgv
aggag
fgsg
*4
fagagoajivs0ivjsgjr0gvskrg
 */
// \s == *
// TODO messageid only? or id + message
// TODO body stuffing só aqui
// GIVE_TOPIC <ID> <TOPIC> CRLF CRLF [[<sub1.getString()> CRLF ...] [/ID CRLF CONTEUDO ...]] CRLF
public class ServerGiveTopicMessage {

}

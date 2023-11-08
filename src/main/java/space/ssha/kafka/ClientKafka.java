package space.ssha.kafka;

public class ClientKafka extends Thread {
    String copy;

    public ClientKafka(String copy) {
        this.copy = copy;
    }

    public void run() {
        System.out.println(copy);
    }

}

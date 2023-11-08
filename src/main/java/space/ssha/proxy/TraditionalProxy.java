package space.ssha.proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TraditionalProxy {

    int localPort;
    int targetPort;
    String targetHost;
    String localHost;
    int maxThreads;
    SSLSocketFactory factory ;

    public TraditionalProxy(String localHost, int localPort, String targetHost, int targetPort, int maxThreads,SSLSocketFactory factory ) {
        this.localPort = localPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.localHost = localHost;
        this.maxThreads = maxThreads;
        this.factory=factory;
    }

    public void run() throws IOException {
        // Create a thread pool with a maximum of maxThreads threads
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
        ServerSocket serverSocket = null;

        try {
            // Create a server socket
            serverSocket = new ServerSocket(localPort, maxThreads * 2/* pending connections */,
                    InetAddress.getByName(localHost));
            System.out.println("Multi-threaded Server is listening on port " + localPort);

            while (true) {
                // Accept client connections
                Socket clientSocket = serverSocket.accept();
                // Submit a new task to the thread pool to handle the client connection
                threadPool.submit(new ClientSocketHandler(clientSocket,targetHost,targetPort,(SSLSocket) factory.createSocket(targetHost, targetPort)));
         
            }

        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (serverSocket != null && (!serverSocket.isClosed()))
                serverSocket.close();

        }
    }

}

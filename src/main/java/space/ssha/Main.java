package space.ssha;

import space.ssha.proxy.TraditionalProxy;
import space.ssha.utils.Defparams;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

//curl -X POST "http://localhost:4589/post" -H "accept: application/json" -d '{"test:test"}'
public class Main {
    public static void main(String[] args) throws IOException {

        SSLSocketFactory factory = testSSL(Defparams.TargetHost, Defparams.TargetPort);

        TraditionalProxy proxy = new TraditionalProxy(Defparams.DefLocalHost, Defparams.DefLocalPort,
                Defparams.TargetHost, Defparams.TargetPort, Defparams.DefMaxThreads,factory);
        try {
            proxy.run();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    static SSLSocketFactory testSSL(String targetHost, int targetPort) throws IOException {
        // System.setProperty("javax.net.ssl.trustStore", "clienttrust");
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(targetHost, targetPort);
        sslSocket.startHandshake();

        // Send an HTTP GET request
        // String request =
        // "POST /post HTTP/1.1\r\n"+
        // "Host: "+targetHost+" \r\n"+ //https://httpbin.org/post
        // "Content-Type: application/json\r\n"+
        // "Content-Length: 12\r\n"+
        // "\r\n"+
        // "{\"F\":\"FUCK\"}";
        // OutputStream out = sslSocket.getOutputStream();
        // out.write(request.getBytes(StandardCharsets.UTF_8));
        // out.flush();

        // // Read and print the response
        // BufferedReader in = new BufferedReader(new
        // InputStreamReader(sslSocket.getInputStream()));
        // String line;
        // while ((line = in.readLine()) != null) {
        // System.out.println(line);
        // }

        // in.close();
        // out.close();
        sslSocket.close();
        return factory;
    }
}
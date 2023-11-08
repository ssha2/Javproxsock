package space.ssha.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocket;

import space.ssha.kafka.ClientKafka;

public class ClientSocketHandler implements Runnable {
    Socket clientSocket;
    int targetPort;
    String targetHost;
    final int size = 1024 + 3;
    SSLSocket targsocket;

    public ClientSocketHandler(Socket clientSocket, String targetHost, int targetPort, SSLSocket targsocket) {
        this.clientSocket = clientSocket;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.targsocket = targsocket;
    }

    private StringBuilder tryReadBuffer(BufferedReader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        char[] buff = new char[size];
        int k = -1;

        while ((k = reader.read(buff)) > 0) {
            result.append(buff, 0, k);
            if (k < size) {
                break;
            }
        }
        return result;
    }

    private StringBuilder buildFullRequest(BufferedReader reader, boolean retarget) throws IOException {
        StringBuilder result = new StringBuilder();
        // heads
        int contentLength = -1;
        String line;
        boolean readHead = true;
        while (readHead) {
            line = reader.readLine();
            readHead = !line.isEmpty();
            if (readHead) {

                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                }
                if (line.startsWith("Host:") && retarget) {
                    result.append("Host: " + targetHost + ":").append(String.valueOf(targetPort)).append("\r\n");
                }
                else {
                    result.append(line).append("\r\n");
                }
            }
        }
        result.append("\r\n");
        // body
        if (contentLength > 0) {
            char[] c = new char[contentLength];
            reader.read(c);
            result.append(c);

        }
        else if (contentLength == -1) {
            result.append(tryReadBuffer(reader));
        }
        // call to Kafka
        new ClientKafka(result.toString()).run();
        // ret
        return result;
    }

    @Override
    public void run() {
        BufferedReader clientReader = null;
        OutputStream clientWriter = null;

        try {
            // from client
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder clientRequest = buildFullRequest(clientReader, true);
            BufferedReader targetReader = null;
            OutputStream targetWriter = null;
            StringBuilder targetResponse = null;
            try {
                // to target
                targsocket.startHandshake();
                targetWriter = targsocket.getOutputStream();
                targetWriter.write(String.valueOf(clientRequest).getBytes(StandardCharsets.UTF_8));
                targetWriter.flush();
                // from target
                targetReader = new BufferedReader(new InputStreamReader(targsocket.getInputStream()));
                targetResponse = buildFullRequest(targetReader, false);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {

                    if (targetReader != null)
                        targetReader.close();
                    if (targetWriter != null)
                        targetWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    targsocket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // to client
            clientWriter = clientSocket.getOutputStream();
            clientWriter.write(
                    (targetResponse == null) ? "HTTP/1.1 500 Internal Server Error".getBytes(StandardCharsets.UTF_8)
                            : String.valueOf(targetResponse).getBytes(StandardCharsets.UTF_8));
            clientWriter.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {

                if (clientReader != null)
                    clientReader.close();
                if (clientWriter != null)
                    clientWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

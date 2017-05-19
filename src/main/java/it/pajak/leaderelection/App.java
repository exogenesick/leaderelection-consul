package it.pajak.leaderelection;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import it.pajak.leaderelection.cluster.Leadership;
import it.pajak.leaderelection.cluster.Replica;
import it.pajak.leaderelection.cluster.ReplicaSession;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;

public class App {
    private static String SERVICE_NAME = "replica";
    private static String SERVICE_ID = "1";
    private static int HTTP_PORT = 8081;

    public static void main(String[] args) throws Exception {
        createHttpServer();

        Consul consul = Consul.builder().build();
        AgentClient agentClient = consul.agentClient();
        agentClient.register(HTTP_PORT, new URL("http://192.168.2.100:" + HTTP_PORT), 1, SERVICE_NAME, SERVICE_ID);

        ReplicaSession session = new ReplicaSession(consul, SERVICE_NAME, 10);
        System.out.println("MY SESSIONID IS " + session.getId());

        new Thread(session).start();

        Replica replica = new Replica(consul);
        new Thread(new Leadership(replica, session, SERVICE_NAME, 5)).start();
    }

    private static void createHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/", new MyHandler());
        server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

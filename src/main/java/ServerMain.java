import server.UserStore;
import server.MessageStore;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;
/*
=========================================================
SERVERMAIN
=========================================================

Esta clase:

- Arranca el servidor.
- Abre un puerto.
- Espera clientes.
- Crea un hilo ClientHandler por cada cliente.

*/

public class ServerMain {

    public static final int PORT = 15000;

    public static void main(String[] args) {

        System.out.println("=== SecureDrop Server v2 ===");
        System.out.println("Puerto: " + PORT);

        UserStore userStore = new UserStore("src/main/java/users.txt");
        MessageStore messageStore = new MessageStore("data");
        System.setProperty("javax.net.ssl.keyStore", "ssl/server.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456789");

        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try (SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT)) {

            while (true) {

                Socket client = serverSocket.accept();

                System.out.println("[+] Cliente conectado: "
                        + client.getRemoteSocketAddress());

                // =====================================================
                // TODO 2:
                // Se podría añadir:
                // - Timeout de conexión
                // - Registro en archivo log
                // =====================================================

                new Thread(
                        new server.ClientHandler(client, userStore, messageStore)
                ).start();
            }


        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e);
            e.printStackTrace();
        }
    }
}

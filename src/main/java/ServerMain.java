import server.UserStore;
import server.MessageStore;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

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

    // 1. Añadimos las variables estáticas para guardar las claves
    private static PrivateKey serverPrivateKey;
    private static PublicKey serverPublicKey;

    public static void main(String[] args) {

        System.out.println("=== SecureDrop Server v2 ===");
        System.out.println("Puerto: " + PORT);

        // 2. Llamamos al método para cargar las claves nada más arrancar
        cargarClavesRSA();

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

                // 3. Pasamos las claves al ClientHandler
                new Thread(
                        new server.ClientHandler(client, userStore, messageStore, serverPrivateKey, serverPublicKey)
                ).start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e);
            e.printStackTrace();
        }

    }


    public static void cargarClavesRSA() {
        char[] password = "123456789".toCharArray();

        try (FileInputStream fis = new FileInputStream("ssl/server.keystore")) {

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fis, password);

            // Recorremos el keystore buscando la primera clave disponible
            Enumeration<String> aliases = ks.aliases();
            if (aliases.hasMoreElements()) {
                String aliasDetectado = aliases.nextElement();
                serverPrivateKey = (PrivateKey) ks.getKey(aliasDetectado, password);
                serverPublicKey = ks.getCertificate(aliasDetectado).getPublicKey();
                System.out.println("[OK] Claves RSA cargadas automáticamente.");
            } else {
                System.err.println("[ERROR] No se encontraron certificados en el keystore.");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar claves RSA: " + e.getMessage());
        }
    }
}
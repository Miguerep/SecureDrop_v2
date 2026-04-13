import util.IOUtil;
import protocol.Protocol;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Scanner;

/*
=========================================================
CLIENTMAIN
=========================================================

Esta clase:

- Se conecta al servidor.
- Envía comandos.
- Muestra respuestas.

Ahora mismo usa Socket normal (SIN cifrado).
En la versión segura habrá que usar TLS.
*/

public class ClientMain {

    public static void main(String[] args) {
        System.out.println("=== SecureDrop Client v2 ===");

        Scanner sc = new Scanner(System.in);

        System.out.print("Servidor (host) [localhost]: ");
        String host = sc.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("Puerto [15000]: ");
        String portStr = sc.nextLine().trim();
        int port = portStr.isEmpty() ? 15000 : Integer.parseInt(portStr);
        System.setProperty("javax.net.ssl.trustStore", "ssl/client.truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456789");

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (
                SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            socket.startHandshake();

            String welcome = in.readLine();
            System.out.println("SERVER> " + welcome);
            // LOGIN
            System.out.println("LOGIN ...");
            System.out.print("Usuario: ");
            String user = sc.nextLine().trim();

            System.out.print("Contraseña: ");
            String pass = sc.nextLine().trim();

            out.println("LOGIN " + user + " " + pass);

            String resp = in.readLine();
            System.out.println("SERVER> " + resp);

            if (resp == null || !resp.startsWith(Protocol.OK)) {
                System.err.println("Login fallido.");
                return;
            }

            while (true) {

                System.out.println("\n--- Menú ---");
                System.out.println("1) Enviar mensaje");
                System.out.println("2) Listar mis mensajes");
                System.out.println("3) (Admin) Listar TODOS los mensajes");
                System.out.println("0) Salir");
                System.out.print("> ");

                String opt = sc.nextLine().trim();

                switch (opt) {
                    case "1":

                        System.out.println("Escribe el mensaje:");
                        String msg = sc.nextLine();

                        if (msg.length() > 500) {
                            System.err.println("Error, el mensaje es demasiado grande.");
                            break;
                        }

                        out.println("SEND " + msg);
                        System.out.println("SERVER> " + in.readLine());

                        break;
                    case "2":

                        out.println("LIST");
                        IOUtil.readMultilineUntilEnd(in);

                        break;
                    case "3":

                        out.println("LIST_ALL");
                        IOUtil.readMultilineUntilEnd(in);

                        break;
                    case "0":

                        out.println("QUIT");
                        System.out.println("SERVER> " + in.readLine());
                        break;

                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }

        } catch (Exception e) {

            System.err.println("Error cliente: " + e.getMessage());

        }
    }
}

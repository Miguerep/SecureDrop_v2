import util.IOUtil;
import protocol.Protocol;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

        try (
                // =====================================================
                // TODO 1:
                // Cambiar Socket por SSLSocket.
                // Esto activará comunicación cifrada (TLS).
                // =====================================================
                Socket socket = new Socket(host, port);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {

            // LOGIN
            System.out.print("Usuario: ");
            String user = sc.nextLine().trim();

            System.out.print("Contraseña: ");
            String pass = sc.nextLine().trim();

            out.println("LOGIN " + user + " " + pass);

            String resp = in.readLine();
            System.out.println("SERVER> " + resp);

            if (resp == null || !resp.startsWith(Protocol.WLCM)) {
                System.out.println("Login fallido.");
                return;
            }

            // MENÚ
            label:
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

                        // =====================================================
                        // TODO 2:
                        // Podría añadirse validación de tamaño aquí también
                        // antes de enviarlo al servidor.
                        // =====================================================

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
                        break label;

                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }

        } catch (Exception e) {

            // =====================================================
            // TODO 3:
            // En versión segura no mostrar detalles técnicos.
            // Mostrar solo mensaje genérico.
            // =====================================================

            System.err.println("Error cliente: " + e);
            e.printStackTrace();
        }
    }
}

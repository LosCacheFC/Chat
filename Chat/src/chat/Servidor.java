package chat;

import chat.AtencionCliente;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor extends Thread {

	private static ArrayList<AtencionCliente> conectados = new ArrayList<>();
	private ServerSocket server;
	private final int puerto = 9999;

	@Override
	public void run() {
		try {

			System.out.println("Iniciando el servidor...");
			server = new ServerSocket(puerto);
			System.out.println("Servidor esperando conexiones...");
			String ipRemota;

			while (true) {
				Socket cliente = server.accept();
				ipRemota = cliente.getInetAddress().getHostAddress();
				System.out.println(ipRemota + " se ha conectado");

				ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
				ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

				AtencionCliente atencion = new AtencionCliente(ipRemota, cliente, entrada, salida);
				atencion.start();
				conectados.add(atencion);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Servidor().start();
	}

	public static ArrayList<AtencionCliente> getConectados() {
		return conectados;
	}
}

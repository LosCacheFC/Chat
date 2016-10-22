package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;

public class Cliente extends Thread {

	private Socket cliente;
	private String miIp;
	private String nick;
	private String sala;
	private ObjectInputStream entrada;
	private ObjectOutputStream salida;
	private Semaphore semaforo;
	private final Gson gson = new Gson();

	public Cliente(String ip, int puerto) throws UnknownHostException, IOException {
		cliente = new Socket(ip, puerto);
		miIp = cliente.getInetAddress().getHostAddress();
		salida = new ObjectOutputStream(cliente.getOutputStream());
		entrada = new ObjectInputStream(cliente.getInputStream());
		semaforo = new Semaphore(0);
	}

	public void run() {
		try {
			String mensaje;
			Scanner scaner = new Scanner(System.in);

			Paquete paquete = new Paquete(null, null, miIp, null, "setSala");

			// Ingreso a la sala
			salida.writeObject(gson.toJson(paquete));
			salida.writeObject(scaner.nextLine());

			// Seleccion de nick
			salida.reset();
			paquete.setComando("setNick");
			salida.writeObject(gson.toJson(paquete));
			salida.writeObject(scaner.nextLine());

			// Espero a que el nick y la sala hayan sido seteados
			semaforo.acquire();
			salida.reset();
			paquete.setComando("conectado");
			paquete.setMensaje(nick + " se ha conectado");
			paquete.setSala(sala);
			salida.writeObject(gson.toJson(paquete));

			paquete.setComando("mensaje");
			paquete.setIp(miIp);
			paquete.setNick(nick);
			while (true) {
				mensaje = scaner.nextLine();
				salida.reset();

				if (mensaje.equals("_.desconectar")) {
					paquete.setComando("desconectar");
					salida.writeObject(gson.toJson(paquete));
					System.out.println("Te has desconectado del servidor");
					scaner.close();
				} else {
					paquete.setMensaje(mensaje);
					salida.writeObject(gson.toJson(paquete));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws ClassNotFoundException, IOException {

		Cliente cliente = new Cliente("localhost", 9999);
		cliente.start();
		new EscuchaMensajes(cliente).start();

	}

	public Socket getCliente() {
		return cliente;
	}

	public void setCliente(Socket cliente) {
		this.cliente = cliente;
	}

	public String getMiIp() {
		return miIp;
	}

	public void setMiIp(String miIp) {
		this.miIp = miIp;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public ObjectInputStream getEntrada() {
		return entrada;
	}

	public void setEntrada(ObjectInputStream entrada) {
		this.entrada = entrada;
	}

	public ObjectOutputStream getSalida() {
		return salida;
	}

	public void setSalida(ObjectOutputStream salida) {
		this.salida = salida;
	}

	public String getSala() {
		return sala;
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	public void liberarSemaforo() {
		semaforo.release();
	}

}

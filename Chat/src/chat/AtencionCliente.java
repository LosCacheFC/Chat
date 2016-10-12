package chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class AtencionCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private String salaCliente;

	public AtencionCliente(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
	}

	public void run() {
		try {

			Paquete paquete;
			String mensaje;

			while (!(paquete = (Paquete) entrada.readObject()).getComando().equals("desconectar"))
				switch (paquete.getComando()) {
				case "setSala":
					salida.writeObject("Seleccione la sala de chat (1 - 2 - 3): ");
					mensaje = (String) entrada.readObject();
					salida.writeObject(mensaje);
					salaCliente = mensaje;
					System.out.println(paquete.getIp() + " " + paquete.getComando() + " " + mensaje);
					break;

				case "setNick":
					salida.writeObject("Ingrese su nick: ");
					mensaje = (String) entrada.readObject();
					salida.writeObject(mensaje);
					System.out.println(paquete.getIp() + " " + paquete.getComando() + " " + mensaje);
					break;

				case "conectado":
					for (AtencionCliente conectado : Servidor.getConectados()) {
						if (paquete.getSala().equals(conectado.salaCliente))
							conectado.salida.writeObject(paquete);
					}
					break;

				case "mensaje":
					for (AtencionCliente conectado : Servidor.getConectados()) {
						if (paquete.getSala().equals(conectado.salaCliente) && !conectado.equals(this))
							conectado.salida.writeObject(paquete);
					}
					break;
				}

			entrada.close();
			salida.close();
			socket.close();

			paquete.setComando("desconectar");
			paquete.setMensaje(paquete.getNick() + " se ha desconectado.");
			Servidor.getConectados().remove(this);

			for (AtencionCliente conectado : Servidor.getConectados()) {
				if (paquete.getSala().equals(conectado.salaCliente))
					conectado.salida.writeObject(paquete);
			}

			System.out.println(paquete.getIp() + " se ha desconectado.");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

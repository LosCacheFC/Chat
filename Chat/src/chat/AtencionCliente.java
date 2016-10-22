package chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AtencionCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private String salaCliente;
	private final Gson gson = new Gson();

	public AtencionCliente(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
	}

	public void run() {
		try {

			Paquete paquete;
			String mensaje;

			while( !((paquete = gson.fromJson((String) entrada.readObject(), Paquete.class)).getComando().equals("desconectar")))
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
							conectado.salida.writeObject(gson.toJson(paquete));
					}
					break;

				case "mensaje":
					for (AtencionCliente conectado : Servidor.getConectados()) {
						if (paquete.getSala().equals(conectado.salaCliente) && !conectado.equals(this))
							conectado.salida.writeObject(gson.toJson(paquete));
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
					conectado.salida.writeObject(gson.toJson(paquete));
			}

			System.out.println(paquete.getIp() + " se ha desconectado.");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

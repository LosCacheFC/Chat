package chat;

import java.io.IOException;
import java.io.ObjectInputStream;

public class EscuchaMensajes extends Thread {

	private Cliente cliente;
	private ObjectInputStream entrada;

	public EscuchaMensajes(Cliente cliente) {
		this.cliente = cliente;
		entrada = cliente.getEntrada();
	}

	public void run() {
		try {
			// Lectura de sala
			System.out.println(entrada.readObject());
			cliente.setSala((String) entrada.readObject());

			// Lectura de nick
			System.out.println(entrada.readObject());
			cliente.setNick((String) entrada.readObject());

			// Libero el semaforo una vez seteada la sala y el nick
			cliente.liberarSemaforo();

			Paquete paquete;

			while (true) {
				paquete = (Paquete) entrada.readObject();
				switch (paquete.getComando()) {
				case "conectado":
					System.out.println(paquete.getMensaje());
					break;

				case "mensaje":
					System.out.println(paquete.getNick() + ": " + paquete.getMensaje());
					break;

				case "desconectar":
					System.out.println(paquete.getMensaje());
					break;
				}
			}

		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}

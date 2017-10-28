import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Client_User
{
	private String hostIP; //IP del servidor contra el que nos conectaremos
	private int puertoServ; // Puerto donde se encuentra el servidor alojado
	private byte[] bufferEnvio; //buffer donde se almacenarán los bytes que se enviarán al HOME
	private byte[] bufferRecepcion; //buffer donde se almacenrán los bytes que se recibirán del HOME
	private Socket comunicacion; //Representa el socket comunicación entre el HOME y el Usuario
	private OutputStream toHome; //Canal por el que se envían mensajes al HOME
	private InputStream  fromHome; //canal por el que se reciben mensajes del HOME



	public Client_User(String hostIP, int puertoServ, int tam_buffer)
	{
		this.hostIP = hostIP;
		this.puertoServ = puertoServ;
		bufferRecepcion = new byte[tam_buffer];
		

		try{
			comunicacion = new Socket(hostIP, puertoServ);//Se crea el socket entre el Usuario y el HOME
		}catch(IOException init_comunication){
			System.err.println("Error al intentar crear una comunicación entre el usuario y el host con la dirección "
			 + hostIP + " en el puertoServ " + puertoServ + ".");
		}

		try{
			toHome = comunicacion.getOutputStream();//Obtenemos el flujo de salida del cliente, para enviar mensaje al HOME.
		}catch(IOException init_outputstream){
			System.err.println("Error al crear el flujo de comunicacion hacia el servidor o no existe comunicación entre ambos.");
		}

		try{
			fromHome = comunicacion.getInputStream();//Obtenemos el flujo de entrada del clienta, para recibir mensajes del HOME.
		}catch(IOException init_inputstream){
			System.err.println("Error al crear el el fujo de comunicación desde el servidor o no está conectado el socket.");
		}
	}

	public void cerrarComunicacion()
	{
		try{
			toHome.close();
		}catch(IOException close_outputstream){
			System.err.println("Error al cerrar el flujo de comunicación hacia el servidor.");
		}

		try{
			fromHome.close();
		}catch(IOException close_inputstream){
			System.err.println("Error al cerrar el flujo de comunicación desde el servidor.");
		}

		try{
			comunicacion.close();
		}catch(IOException close_comunication){
			System.err.println("Error al cerrar la comunicación socket entre el cliente y el servidor.");
		}
	}

	public static void main (String argvs[])
	{
		Client_User usuario = new Client_User("23.2.97.6", 8082, 150);//Creamos el cliente usuario...
		InputStreamReader std_in = new InputStreamReader(System.in);//Para poder leer caracteres de la entrada estandar
		BufferedReader leer_teclado = new BufferedReader(std_in);//Para poder leer cadenas de texto de la entrada estandar
		String lineaTeclado = null;


		

		do{
			try{
				lineaTeclado = leer_teclado.readLine();
				lineaTeclado = lineaTeclado.toUpperCase().trim();//transformamos a Mayusculas y quitamos los espacios del principio y el final
			}catch(IOException err_read_teclado){
				System.err.println("Error al leer una linea del teclado");
			}
			
			usuario.bufferEnvio = lineaTeclado.getBytes();
			
			try{
				usuario.toHome.write(usuario.bufferEnvio,0,usuario.bufferEnvio.length);
				usuario.toHome.flush();
			}catch(IOException err_write){
				System.err.println("Error al escribir un mensaje para el servidor.");
			}
		}while(lineaTeclado.compareTo("EXIT") != 0);
		

		usuario.cerrarComunicacion();
	}
}
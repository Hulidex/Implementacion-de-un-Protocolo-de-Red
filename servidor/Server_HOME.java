import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;




public class Server_HOME
{
	//variables de clase
	final static private int MSG_TAM = 150;
	private int puerto;
	//variables de instancia
	private byte bufferRecepcion[];
	private byte bufferEnvio[];
	private ServerSocket home;
	private Socket comunicacion;


	public Server_HOME(int puerto, int tam_buff)
	{
		bufferRecepcion = new byte[tam_buff];
		this.puerto = puerto;

		try{
			home = new ServerSocket(puerto);

		}catch(IOException init_server){
			System.err.println("No se ha podido Iniciar el servidor debido a un problema de I/O.");
		}

	}

	public void closeServer()
	{
		if (!comunicacion.isClosed()){//debemos comprobar si la comunicación ya ha sido cerrada por el cliente o no...
			try{
				comunicacion.close();
			}catch(IOException close_socket){
				System.err.println("Error al cerrar la comunicacion entre el cliente con IP: " + comunicacion.getRemoteSocketAddress().toString() + ".");
			}
		}

		try{
			home.close();
		}catch(IOException close_server){
			System.err.println("Error al cerrar el servidor.");
		}
	}


	public static void main (String[] args)
	{
		Server_HOME servidor = new Server_HOME(8082, 150); //Creamos un servidor en el puerto 8082 con tamaño del buffer de 150 BYTES.
		InputStream fromClient = null;
		OutputStream toClient = null;
		int bytesLeidos = 0;
		String client_mssg = null;

		
		do{
			try{
				//El servidor se queda bloqueado escuchando en el puerto
				//hasta que algún programa le envía algun mensaje, entonces crea un socket de cominicación entre el programa y el
				servidor.comunicacion = servidor.home.accept();
				
				System.out.println("Se inición una comunicacion con un cliente con dirección: " + servidor.comunicacion.getRemoteSocketAddress().toString() 
					+ " en el puerto " + servidor.comunicacion.getPort() + ".");


				try{
					fromClient = servidor.comunicacion.getInputStream();
				}catch(IOException crear_entrada){
					System.err.println("Error al crear el flujo de entrada del servidor");
				}

				try{
					toClient = servidor.comunicacion.getOutputStream();
				}catch(IOException crear_salida){
					System.err.println("Error al crear el flujo de salida del servidor");
				}



				do{
					try{
						servidor.bufferRecepcion = new byte[MSG_TAM];//IMPORTANTE VACIAL EL BUFFER ANTES DE UTILIZARLO PARA NO IR ACUMULANDO BASURA...
						bytesLeidos = fromClient.read(servidor.bufferRecepcion);
					}catch(IOException err_lectura){
						System.err.println("Error al leer del flujo de entrada del servidor");
					}

					client_mssg = new String(servidor.bufferRecepcion);
					client_mssg = client_mssg.toUpperCase().trim();
					System.out.println("Recibido el mensaje:\t" + client_mssg);
				}while(client_mssg.compareTo("EXIT") != 0);




			}catch(IOException init_comunication){
				System.err.println("No se ha podido iniciar la comunicación entre el servidor y alguno de los clientes.");
			}



		}while(false);

		servidor.closeServer();
	}
}
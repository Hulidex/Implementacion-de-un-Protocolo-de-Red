import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;




public class Server_HOME
{
	private int puerto;
	private ServerSocket home;
	private Socket comunicacion;


	public Server_HOME(int puerto, int tam_buff)
	{
		this.puerto = puerto;

		try{
			home = new ServerSocket(puerto);

		}catch(IOException init_server){
			System.err.println("No se ha podido Iniciar el servidor debido a un problema de I/O.");
		}

	}

	public void closeServer()
	{
		try{
			home.close();
		}catch(IOException close_server){
			System.err.println("Error al cerrar el servidor.");
		}
	}


	public static void main (String[] args)
	{
		Server_HOME servidor = new Server_HOME(8082, 150); //Creamos un servidor en el puerto 8082 con tamaño del buffer de 150 BYTES.
		Home instancia; //Representa una instancia del servidor que será manejada por una hebra
		CuentasUsuario acounts= new CuentasUsuario("./cuentas"); //Generamos las cuentas de usuario que administrará el servidor, dichas cuentas se encuentran en el fichero "cuentas"
		


		do{
			try{
				//El servidor se queda bloqueado escuchando en el puerto
				//hasta que algún programa (Cliente) le envía algun mensaje, entonces crea un socket de cominicación entre el programa y él.
				servidor.comunicacion = servidor.home.accept();
				
				System.out.println("Se inición una comunicacion con un cliente con dirección: " + servidor.comunicacion.getRemoteSocketAddress().toString() 
					+ " en el puerto " + servidor.comunicacion.getPort() + ".");


				//CREAMOS UN THREAD PARA CADA CLIENTE QUE SE QUIERA CONECTAR CON EL SERVIDOR.
				//Cada thread tendrá una comunicación distinta con un cliente distinto...
				instancia = new Home(servidor.comunicacion, servidor.comunicacion.getRemoteSocketAddress().toString(), acounts);

				instancia.start();//iniciamos la instancia del servidor en una hebra

			}catch(Exception e){
				e.printStackTrace();
			}



		}while(true);
	}
}
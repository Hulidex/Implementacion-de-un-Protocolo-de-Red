import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class Home extends Thread
{
	final static private int MSG_TAM = 200;//TAMAÑO DE LOS BUFFERS DONDE SE RECIBIRÁN LOS MENSAJES
	private Socket comunicacion;//socket que comunica un cliente con el servidor
	private String direccion_Cliente;//direccion IP y puerto del cliente
	private byte bufferRecepcion[]; //buffer donde ser recibiran los bytes de datos enviados por el cliente
	private byte bufferEnvio[];//buffer donde se almacenarán los bytes que se quieren enviar al cliente
	private InputStream fromClient = null;//flujo de comunicacion con dirección cliente al servidor
	private	OutputStream toClient = null;//flujo de comunicación con dirección servidor al cliente
	private	int bytesLeidos = 0;
	private String client_mssg = null;



	/*
	*la instancia del servidor HOME supoen que el socket de comunicación esta ya abierto y funcional...
	**/
	public Home(Socket comunicacion, String direccion_Cliente)
	{
		this.direccion_Cliente = direccion_Cliente;
		this.comunicacion = comunicacion;
		try{
			fromClient = comunicacion.getInputStream();//creamos flujo de comunicacion hacia el cliente 
			toClient = comunicacion.getOutputStream();//creamos flujo de comunicación desde el cliente		

		}catch(Exception e){
			e.printStackTrace();
		}

	}



	public void run()
	{
		try{
			do{
				bufferRecepcion = new byte[MSG_TAM];//IMPORTANTE VACIAL EL BUFFER ANTES DE UTILIZARLO PARA NO IR ACUMULANDO BASURA...
				bytesLeidos = fromClient.read(bufferRecepcion);


				client_mssg = new String(bufferRecepcion);
				client_mssg = client_mssg.toUpperCase().trim();
				System.out.println("Recibido el mensaje:\t" + client_mssg + "\tdel cliente " + direccion_Cliente);
			}while(client_mssg.compareTo("EXIT") != 0);

			
			//UNA VEZ QUE TERMINAMOS DE SERVIR PETICIONES DEL CLIENTE CERRAMOS LA COMUNICACIÓN.
			if (!comunicacion.isClosed())//debemos comprobar si la comunicación ya ha sido cerrada por el cliente o no...
				comunicacion.close();
	
		
		}catch(Exception e){
			e.printStackTrace();
		}


	}
}
/*
* CLASE QUE REPRESENTA UNA INSTANCIA DEL SERVIDOR, CADA UNA DE ESTAS INSTANCIAS ESTÁ GESTIONADA POR UN
* THREAD, EN FUNCIÓN DE LA CUENTA DE USUARIO UTILIZADA LA INSTACIA DEL SERVIDOR DA UN SERVICIO U OTRO.
**/

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;




public class Home extends Thread
{
	final static private int MSG_TAM = 300;//TAMAÑO DE LOS BUFFERS DONDE SE RECIBIRÁN LOS MENSAJES
	private int state;//Estado en el que se encuentra el servidor
	private Socket comunicacion;//socket que comunica un cliente con el servidor
	private String direccion_Cliente;//direccion IP y puerto del cliente
	private InputStream fromClient = null;//flujo de comunicacion con dirección cliente al servidor
	private	OutputStream toClient = null;//flujo de comunicación con dirección servidor al cliente
	private int bytesLeidos = 0;
	private byte bufferRecepcion[]; //buffer donde ser recibiran los bytes de datos enviados por el cliente
	private byte bufferEnvio[];//buffer donde se almacenarán los bytes que se quieren enviar al cliente
 	private Mensaje client_mssg = null; //Mensaje del cliente
 	private Mensaje server_mssg = null; //Mensaje para el cliente
	private int cod_mensaje;
	private CuentasUsuario acounts;

	



	/*
	*la instancia del servidor HOME supone que el socket de comunicación esta ya abierto y funcional...
	**/
	public Home(Socket comunicacion, String direccion_Cliente , CuentasUsuario acounts)
	{
		this.direccion_Cliente = direccion_Cliente;
		this.comunicacion = comunicacion;
		this.acounts = acounts;
		this.state = 0;
		try{
			fromClient = comunicacion.getInputStream();//creamos flujo de comunicacion hacia el cliente 
			toClient = comunicacion.getOutputStream();//creamos flujo de comunicación desde el cliente		

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void ServicioUsuario()
	{
		try{
			do{
				bufferRecepcion = new byte[MSG_TAM];//IMPORTANTE VACIAR EL BUFFER ANTES DE UTILIZARLO PARA NO IR ACUMULANDO BASURA...
				
				bytesLeidos = fromClient.read(bufferRecepcion);//leemos petición del cliente
				client_mssg = new Mensaje(new String(bufferRecepcion));
				System.out.println("Recibido el mensaje:\t" + client_mssg.toString() + "\tdel cliente " + direccion_Cliente);



				if (client_mssg.isValid()){ //si la sintaxis del mensaje es correcta lo procesamos
					switch (state){
						default://Servidor está en estado NOT AUTHENTICATED

							server_mssg = new Mensaje("BYE");//creo el mensaje
							bufferEnvio = server_mssg.toString().getBytes();
							toClient.write(bufferEnvio,0, bufferEnvio.length);
							toClient.flush();

							break;
					}
				}
				else{
					server_mssg = new Mensaje("INV_OP");//creo el mensaje
					bufferEnvio = server_mssg.toString().getBytes();
					toClient.write(bufferEnvio,0, bufferEnvio.length);
					toClient.flush();
				}
			}while(client_mssg.comando.compareTo("EXIT") != 0);


			server_mssg = new Mensaje("BYE");//creo el mensaje
			bufferEnvio = server_mssg.toString().getBytes();
			toClient.write(bufferEnvio,0, bufferEnvio.length);
			toClient.flush();
	
		
		}catch(Exception e){
			e.printStackTrace();
		}

	}



	public void run()
	{
		int n_usuario;
		try{
			do{
				bufferRecepcion = new byte[MSG_TAM];//IMPORTANTE VACIAR EL BUFFER ANTES DE UTILIZARLO PARA NO IR ACUMULANDO BASURA...
				
				bytesLeidos = fromClient.read(bufferRecepcion);//leemos petición del cliente
				client_mssg = new Mensaje(new String(bufferRecepcion));
				System.out.println("Recibido el mensaje:\t" + client_mssg.toString() + "\tdel cliente " + direccion_Cliente);



				if (client_mssg.compare(new Mensaje("LOGIN"))){ //si es un mensaje de autentificación lo procesamos
					n_usuario = acounts.find(client_mssg.args[0]);//Buscamos al usuario
					if ((n_usuario != -1) && (acounts.checkPassword(n_usuario, client_mssg.args[1]))){ //si es un login reconocido por el sistema y la cotraseña es correcta

						switch (client_mssg.args[0]){
							case "HULIDEX":
							case "USUARIO":
								//okAUTH
								server_mssg = new Mensaje("OK_AUTH");//creo el mensaje
								bufferEnvio = server_mssg.toString().getBytes();
								toClient.write(bufferEnvio, 0, bufferEnvio.length);
								toClient.flush();
								ServicioUsuario();
								break;
							default:
								bufferEnvio = new String("Solo tengo funcionalidad para el cliente").getBytes();
								toClient.write(bufferEnvio,0,bufferEnvio.length);
								toClient.flush();
								break;
						}
					}
					else{
						server_mssg = new Mensaje("ERR_AUTH");//creo el mensaje
						bufferEnvio = new String(server_mssg.toString() + " Usuario o contraseña Incorrectos.").getBytes();
						toClient.write(bufferEnvio,0, bufferEnvio.length);
						toClient.flush();
					}
				}
				else if (!client_mssg.compare(new Mensaje("EXIT"))){//si no es el mensaje de exit
					server_mssg = new Mensaje("INV_OP");//creo el mensaje
					bufferEnvio = server_mssg.toString().getBytes();
					toClient.write(bufferEnvio,0, bufferEnvio.length);
					toClient.flush();
				}
			}while(client_mssg.comando.compareTo("EXIT") != 0);

			server_mssg = new Mensaje("BYE");//creo el mensaje
			bufferEnvio = server_mssg.toString().getBytes();
			toClient.write(bufferEnvio,0, bufferEnvio.length);
			toClient.flush();
	
		
		}catch(Exception e){
			e.printStackTrace();
		}


	}
}
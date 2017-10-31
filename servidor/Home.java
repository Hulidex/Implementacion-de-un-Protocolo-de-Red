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
	private int cod_mensaje;
	private CuentasUsuario acounts;

	//DISPOSITIVOS CONECTADOS
	boolean puerta = true;
	boolean puerta_lock = true;
	boolean ventana = true;
	boolean ventana_lock = true;
	boolean calefactor = false;
	int grados_calefactor = 0;


	//MENSAJES QUE ENVÍA EL SERVIDOR
	Mensaje exit = new Mensaje("EXIT");
	Mensaje inv_op = new Mensaje("INV_OP");
	Mensaje bye = new Mensaje("BYE");
	Mensaje login = new Mensaje("LOGIN");
	Mensaje err_auth = new Mensaje("ERR_AUTH");
	Mensaje ok_auth = new Mensaje("OK_AUTH");
	Mensaje mod = new Mensaje("MOD");
	Mensaje query = new Mensaje("QUERY");
	Mensaje set_degrees = new Mensaje("SET_DEGREES");
	Mensaje turn_off = new Mensaje("TURN_OFF");
	Mensaje turn_on = new Mensaje("TURN_ON");
	Mensaje lock = new Mensaje("LOCK");
	Mensaje unlock = new Mensaje("UNLOCK");
	Mensaje que_degrees = new Mensaje("QUE_DEGREES");
	Mensaje que_lock = new Mensaje("QUE_LOCK");
	Mensaje que_state = new Mensaje("QUE_STATE");
	Mensaje home = new Mensaje("HOME");
	



	/*
	*la instancia del servidor HOME supone que el socket de comunicación esta ya abierto y funcional...
	**/
	public Home(Socket comunicacion, String direccion_Cliente , CuentasUsuario acounts)
	{
		this.direccion_Cliente = direccion_Cliente;
		this.comunicacion = comunicacion;
		this.acounts = acounts;
		this.state = 0;//por defecto el estado es el 0
		try{
			fromClient = comunicacion.getInputStream();//creamos flujo de comunicacion hacia el cliente 
			toClient = comunicacion.getOutputStream();//creamos flujo de comunicación desde el cliente		

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/*Método que implementa la recepción de un mensaje por parte del servidor desde el cliente*/
	public Mensaje Receive()
	{
		Mensaje client_mssg = null;


		try{
			bufferRecepcion = new byte[MSG_TAM];//IMPORTANTE VACIAR EL BUFFER ANTES DE UTILIZARLO PARA NO IR ACUMULANDO BASURA...	
			bytesLeidos = fromClient.read(bufferRecepcion);//leemos petición del cliente
			client_mssg = new Mensaje(new String(bufferRecepcion));
		}catch(Exception e){
			e.printStackTrace();
		}


		return client_mssg;
	}


	/*Método para enviar un mensaje desde el cliente al servidor*/
	public void Send(Mensaje server_mssg)
	{
		try{
			bufferEnvio = server_mssg.toString().getBytes();
			toClient.write(bufferEnvio, 0, bufferEnvio.length);
			toClient.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/*Método para enviar un mensaje desde el cliente al servidor y adjuntarle un texto*/
	public void Send(Mensaje server_mssg, String attachment)
	{
		try{
			bufferEnvio = new String(server_mssg.toString() + "\t" + attachment).getBytes();
			toClient.write(bufferEnvio, 0, bufferEnvio.length);
			toClient.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public static boolean isNumeric(String str)
	{
		try{
			Integer.parseInt(str);
			return true;

		}catch(Exception e){
			return false;
		}
	}



	/*
	* Rutina que ejecuta cuando determina que el cliente que se ha conectado con el servidor es un Usuario.
	*/
	public void ServicioUsuario()
	{
		Mensaje recibido, enviar;



		try{
			do{
				
				recibido = Receive();
				System.out.println("Recibido el mensaje:\t" + recibido.toString() + "\tdel cliente " + direccion_Cliente);


				if (recibido.compare(exit))//si es un mensaje de exit cierro la comunicación
					
					Send(bye);
				else if (recibido.isValid()){ //si la sintaxis del mensaje es correcta lo procesamos
					
					switch (state){
						case 0://EL SERVIDOR SE ENCUENTRA EN EL ESTADO HOME
							if (recibido.compare(mod)){
								
								state = 1;//cambiamos del estado HOME al estado MODYFING_DEVICES, para controlar los dispositivos
								Send(new Mensaje("MODIFYING_DEVICES"));
							}
							else if (recibido.compare(query)){
								
								state = 2;//Cambiamos del estado HOME al estado QUERY, para consultar el estado de los dispositivos
								Send(new Mensaje("QUERYING_DEVICES"));
							}
							else{//en otro caso no es una operación válida 
								
								Send(inv_op);
							}
							
							break;
						

						case 1://EL SERVIDOR SE ENCUENTRA EN EL ESTADO MODYFING_DEVICES
							if(recibido.compare(set_degrees)){//Si se quieren modificar los grados del calefactor

								if (recibido.args.length == 1){
									if (isNumeric(recibido.args[0])){

										if((Integer.parseInt(recibido.args[0]) >= 0) && (Integer.parseInt(recibido.args[0]) < 50)){//si se quiere poner el calefactor a una temperatura adecuada  
										
										grados_calefactor = Integer.parseInt(recibido.args[0]);
										calefactor = true;
										Send(new Mensaje("SUCCESS_MOD"), "Se modificó satisfactoriamente la temperatura a " + grados_calefactor + " grados.");
										}
										else{
											
											Send(new Mensaje("FAIL_MOD"), "La temperatura " + recibido.args[0] + " no es valida o soportada por el calefactor.");
										}

									}
									else Send(new Mensaje("FAIL_MOD"), "El argumento pasado no es numérico.");
								}
								else Send(new Mensaje("FAIL_MOD"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(turn_off)){//si se quiere desconectar algun dispositivo
								
								if(recibido.args.length == 1){
									
									if (recibido.args[0].compareTo("PUERTA") == 0){
										
										if (puerta == true){
											puerta = false;
											Send(new Mensaje("SUCCESS_MOD"), "Se cerró el dispositivo Puerta.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo Puerta ya está cerrado.");
									}
									else if (recibido.args[0].compareTo("VENTANA") == 0){
										
										if (ventana == true){
											ventana = false;
											Send(new Mensaje("SUCCESS_MOD"),"Se cerró el dispositivo Ventana.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo Ventana ya está cerrado.");
									}
									else if (recibido.args[0].compareTo("CALEFACTOR") == 0){
										
										if (calefactor == true){
											calefactor = false;
											grados_calefactor = 0;
											Send(new Mensaje("SUCCESS_MOD"), "Se desconectó el dispositivo Calefactor.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo Calefactor ya está desconectado.");

									}
									else{//El dispositivo no se reconoce
										
										Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos son:\n{Puerta,Calefactor,Ventana}");
									}
								}
								else{
									
									Send(new Mensaje("FAIL_MOD"), "Número erroneo de parámetros, no se realizó modificación.");
								}
							}
							else if (recibido.compare(turn_on)){//si se quiere encender algun dispositivo

								if(recibido.args.length == 1){
									
									if (recibido.args[0].compareTo("PUERTA") == 0){

										if (puerta == false){
											puerta = true;
											Send(new Mensaje("SUCCESS_MOD"), "Se abrió la puerta.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo puerta ya está abierto.");
									}
									else if (recibido.args[0].compareTo("VENTANA") == 0){

										if(ventana == false){
											ventana = true;
											Send(new Mensaje("SUCCESS_MOD"), "Se abrió la ventana.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo ventana ya está abierto.");
									}
									else if (recibido.args[0].compareTo("CALEFACTOR") == 0){

										if(calefactor == false){
											calefactor = true;
											grados_calefactor = 20;
											Send(new Mensaje("SUCCESS_MOD"), "Se encendió el calefactor a la temperatura por defecto(20 grados).");
										}
										else Send(new Mensaje("FAIL_MOD"), "El dispositivo calefactor ya está encendido.");
									}
									else Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos son:\n{Puerta,Calefactor,Ventana}");
								}
								else Send(new Mensaje("FAIL_MOD"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(lock)){//si se quiere bloquear algún dispositivo
								
								if (recibido.args.length == 1){
									
									if(recibido.args[0].compareTo("PUERTA") == 0){
										
										if (puerta_lock == false){
											puerta_lock = true;
											Send(new Mensaje("SUCCESS_MOD"), "Se activó el cierre del dispositivo Puerta.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El cierre del dispositivo Puerta ya está activo.");
									}
									else if (recibido.args[0].compareTo("VENTANA") == 0){
										
										if (ventana_lock == false){
											ventana_lock = true;
											Send(new Mensaje("SUCCESS_MOD"), "Se activó el cierre del dispositivo Ventana.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El cierre del dispositivo Ventana ya está activo.");

									}
									else Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos CON CIERRE son:\n{Puerta,Ventana}");
								}
								else Send(new Mensaje("FAIL_MOD"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(unlock)){//si se quiere desbloquear algún dispositivo
								
								if (recibido.args.length == 1){
									
									if (recibido.args[0].compareTo("PUERTA") == 0){
										
										if (puerta_lock == true){
											puerta_lock = false;
											Send(new Mensaje("SUCCESS_MOD"), "Se desactivó el cierre del dispositivo Puerta.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El cierre del dispositivo puerta ya está desactivado.");
									}
									else if (recibido.args[0].compareTo("VENTANA") == 0){

										if(ventana_lock == true){
											ventana_lock = false;
											Send(new Mensaje("SUCCESS_MOD"), "Se desactivó el cierre del dispositivo Ventana.");
										}
										else Send(new Mensaje("FAIL_MOD"), "El cierre del dispositivo Ventana ya está desactivado.");
									}
									else Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos CON CIERRE son:\n{Puerta,Ventana}");
								}
								else Send(new Mensaje("FAIL_MOD"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(home)){//si se quiere dejar de modificar parámetros en los dispositivos
								
								state = 0;//volvemos al estado home
								Send(new Mensaje("IN_HOME"));
							}
							else Send(inv_op);

							break;
						
						case 2://EL SERVIDOR SE ENCUENTRA EN EL ESTADO QUERY
							if (recibido.compare(que_degrees)){//cosultar los grados del calefactor
								
								Send(new Mensaje("SUCCESS_QUER"), "La temperatura del Calefactor es de " + grados_calefactor +".");
							}
							else if (recibido.compare(que_lock)){//consultar el estado del cierre de un dispositivo
								
								if (recibido.args.length == 1){
									if(recibido.args[0].compareTo("PUERTA") == 0){

										if (puerta_lock){
											Send(new Mensaje("SUCCESS_QUER"), "El cierre de la Puerta se encuentra activo.");
										}
										else Send(new Mensaje("SUCCESS_QUER"), "El cierre de la puerta no se encuentra activo.");
									}
									else if (recibido.args[0].compareTo("VENTANA") == 0){
										
										if(ventana_lock){
											Send(new Mensaje("SUCCESS_QUER"), "El cierre de la Ventana se encuentra activo.");
										}
										else Send(new Mensaje("SUCCESS_QUER"), "El cierre de la ventana no se encuentra activo.");
									}
									else Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos CON CIERRE son:\n{Puerta,Ventana}");
								}
								else Send(new Mensaje("FAIL_QUER"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(que_state)){//consultar el estado de un dispositivo

								if(recibido.args.length == 1){

									if (recibido.args[0].compareTo("PUERTA") == 0){
										
										if (puerta){
											Send(new Mensaje("SUCCESS_QUER"), "La Puerta se encuentra abierta.");
										}
										else Send(new Mensaje("SUCCESS_QUER"), "La  Pueta se encuentra cerrada.");
									}
									else if(recibido.args[0].compareTo("VENTANA") == 0){
										
										if(ventana){
											Send(new Mensaje("SUCCESS_QUER"), "La Ventana se encuentra abierta");
										}
										else Send(new Mensaje("SUCCESS_QUER"), "La Ventana se encuentra cerrada.");
									}
									else if (recibido.args[0].compareTo("CALEFACTOR") == 0){

										if(calefactor){
											Send(new Mensaje("SUCCESS_QUER"), "El Calefactor se encuentra encendido.");
										}
										else Send(new Mensaje("SUCCESS_QUER"), "El calefactor se encuentra apagado.");
									}
									else Send(new Mensaje("FAIL_MOD"), "Dispositivo " + recibido.args[0] + " no reconocido. Los dispositivos reconocidos son:\n{Puerta,Calefactor,Ventana}");
								}
								else Send(new Mensaje("FAIL_QUER"), "Número erroneo de parámetros, no se realizó modificación.");
							}
							else if (recibido.compare(home)){
								state = 0;
								Send(new Mensaje("IN_HOME"));
							}
							else Send(inv_op);
							break;
						default://Servidor está en estado NOT AUTHENTICATED

							Send(inv_op);//envío el mensaje
							break;
					}
				}
				else {//en otro caso envío que la solicitud de operación es inválida
					
					Send(inv_op);
				}
			
			}while(!recibido.compare(exit));


		
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void run()
	{
		Mensaje recibido, enviar;
		int n_usuario;
		
		try{
			do{

				recibido = Receive();//Recibimos un mensaje del cliente
				System.out.println("Recibido el mensaje:\t" + recibido.toString() + "\tdel cliente " + direccion_Cliente);

				if (recibido.compare(exit)){
					Send(bye);//si recibo un exit cierro la comunicación.
				}
				else if (recibido.compare(login)){ //si es un mensaje de autentificación lo procesamos
					n_usuario = acounts.find(recibido.args[0]);//Buscamos al usuario
					if ((n_usuario != -1) && (acounts.checkPassword(n_usuario, recibido.args[1]))){ //si es un login reconocido por el sistema y la cotraseña es correcta

						switch (recibido.args[0]){
							case "HULIDEX":
							case "USUARIO":
								//okAUTH
								Send(ok_auth);//envío el mensaje al cliente.
								ServicioUsuario();//Ejecuto la funcionalidad asociada al cliente USUARIO.
								recibido = exit;//si salgo del procedimiento es porque el usuario ha manifestado la intención de salir

								break;
							default:
								bufferEnvio = new String("Solo tengo funcionalidad para el cliente").getBytes();
								toClient.write(bufferEnvio,0,bufferEnvio.length);
								toClient.flush();
								break;
						}
					}
					else{
						Send(err_auth);//envío el mensaje
					}
				}
				else{//si no niguno de los mensajes anteriores respondo como operación inválida
					Send(inv_op);
				}
			
			}while(!recibido.compare(exit));//Mientras no sea el mensaje de EXIT

	
		
		}catch(Exception e){
			e.printStackTrace();
		}


	}
}
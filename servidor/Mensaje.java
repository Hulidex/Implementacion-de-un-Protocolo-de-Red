import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;



class Cod_mssg //clase para representar internamente la sintaxis de los distintos mensajes que manejará el servidor.
{
	public String cod_op;
	public String comando;
	public int n_args;


	public Cod_mssg()
	{
		cod_op = null;
		comando= null;
		n_args = 0;
	}
}

public class Mensaje
{
	//variables de clase:
	private static Cod_mssg[] mensajes; //En esta variable estara la sintaxis de todos los mensajes que gestiona el servidor.
	private static String nombre_fichero = "./codigos"; //Ruta del fichero donde se encontrarán todos los mensajes que gestiona el servidor.

	//variables instancia
	public	String cod_op; //Codigo de operación
	public	String comando; //comando
	public String[] args; //Argumentos del comando

	/**	EJEMPLO:
	*	<cod_op> <comando> <arg1,arg2,...,argn>
	*	
	*	101 LOGIN pepe contraseña1
	*/

	static
	{ //Se llama ha este método una única vez cuando se crea el primer objeto de la clase 
		File archivo = null;
		FileReader lect_archivo = null;
		BufferedReader bufferLec = null;
		int n_mensajes;
		String linea = null;
		int aux;


		try{
			archivo = new File(nombre_fichero);//abrimos el fichero
			lect_archivo = new FileReader(archivo);//lo convertimos a fileReader
			bufferLec = new BufferedReader(lect_archivo);//Lo pasamos a BufferedREader para poder leer el archivo linea a linea en formato String


			n_mensajes = Integer.parseInt(bufferLec.readLine());//la primera linea del archivo contendrá el número de mensajes que se van a leer.

			//Reservamos memoria para los mensajes:
			mensajes = new Cod_mssg[n_mensajes];
			for (int i = 0; i < n_mensajes; i++)
				mensajes[i] = new Cod_mssg();

			//Comenzamos a leer los mensajes 1 por 1.
			for (int i = 0; i < n_mensajes; i++){
				linea = bufferLec.readLine();//leemos una línea del fichero
				
				//Extraemos de la línea el codigo del mensaje
				aux = linea.indexOf(':');
				mensajes[i].cod_op = new String(linea.substring(0,aux));
				linea = linea.substring(aux+1);
				

				//Extrameos de la línea el comando del mensaje
				aux = linea.indexOf(':');
				mensajes[i].comando = new String(linea.substring(0, aux));
				linea = linea.substring(aux+1);

				//Extraemos de línea el numero de parametros del mensaje
				mensajes[i].n_args = Integer.parseInt(linea);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (lect_archivo != null) //cerramos el archivo se haya producido algún error o no.
					lect_archivo.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
	}

	public static void printMensajes()
	{
		for (int i  = 0; i < mensajes.length; i++)
			System.out.println(mensajes[i].cod_op + " " + mensajes[i].comando + " " + mensajes[i].n_args);
	}

	public static void main (String args[])
	{
		Mensaje m1;

		Mensaje.printMensajes();
	}

}
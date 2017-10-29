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
	public int pos_msg; //Posición que ocupa el mensaje dentro del vector mensajes

	/**	EJEMPLO:
	*	<cod_op> <comando> <arg1,arg2,...,argn>
	*	
	*	101 LOGIN pepe contraseña1
	*/

	/**Construye un mensaje de forma adecuada apartir de
	* una cadena del estilo:
	* <COMANDO> <PARAMETRO1> <PARAMETRO2> ... <PARAMETRON>
	*
	* EJEMPLO de la cadena que se le pasaría:
	* login hulidex juanitomakande
	*
	* CONSTRUIRIA EL MENSAJE:
	* cod_op = 012
	* comando = LOGIN
	* args[1] = HULIDEX, args[2] = JUANITOMAKANDE
	* pos_msg = 1
	*
	* SI EL COMANDO NO ES RECONIDO DENTRO DEL VECTOR mensajes GENERA UN MENSAJE VACÍO de
	* de la siguiente forma:
	* cod_op = null
	* comando = null
	* args = null
	* pos_msg = -1
	*/
	public Mensaje(String mensajeBruto)
	{
		String msg = mensajeBruto.toUpperCase().trim();//convertimos todo a mayus y eliminamos los espacios que puedan haber al principio y/o al final de la cadena
		String comand;
		int aux;
		int pos;


		//leemos el comando del mensaje
		aux = msg.indexOf(' ');
		if(aux != -1){
			comand = msg.substring(0,aux);
			while(msg.charAt(aux+1) == ' '){//si hay más de un espacio junto en la cadena los elimino
				aux++;
			}
		}
		else comand = msg;
	
		
		msg = msg.substring(aux+1);

		//Buscamos el comando dentro del vector mensajes
		pos = find(comand);


		if (pos == -1){//si no lo encontramos generamos un mensaje vacío
			this.comando = "null";
			this.cod_op = null;
			this.args = null;
			this.pos_msg = -1;
		}
		else{//en caso de econtrar el mensaje se construye correctamente
			this.comando = comand;
			this.cod_op = mensajes[pos].cod_op;
			this.pos_msg = pos;

			//leemos los argumentos
			if (mensajes[pos].n_args > 0){//si el comando tiene algún argumento los leemos
				this.args = new String[mensajes[pos].n_args];

				for (int i = 0; i < mensajes[pos].n_args; i++){
					aux = msg.indexOf(' ');
					while(msg.charAt(aux+1) == ' '){//si hay más de un espacio junto en la cadena los elimino
						aux++;
					}
					
					if (aux != -1)
						this.args[i] = msg.substring(0,aux);
					else this.args[i] = msg.substring(0);
					
					msg = msg.substring(aux+1);
				}
			}
			else{//si el comando no tiene argumentos dejamos el vector de argumentos del mensaje vacío
				this.args = null;
			}
		}
	}

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


	//Busca el código del comando que se le pasa como parámetro
	//Si lo encuentra retorna la posición que ocupa dicho código en el vector mensajes, en otro caso retorna -1.
	private static int find(String comando)
	{
		boolean encontrado = false;
		int pos = -1;

		
		for (int i = 0; i < mensajes.length && !encontrado; i++){
			if (mensajes[i].comando.compareTo(comando) == 0){
				pos = i;
				encontrado = true;
			}
		}


		return pos;
	}

	/*
	* Covierte un dato de tipo Mensaje a tipo String
	* SI EL MENSAJE ES NULO ESCRIBE LA CADENA "null"
	**/
	public String toString()
	{
		if (!this.isValid())
			return "null";
		else{
			String msg = new String(this.cod_op + "\t" + this.comando + "\t");


			if (args != null){
				for (String str : args)
					msg = msg + " " + str;
			}

			return msg;
		}
	}

	/*
	* Devuelve true si el cod_op de un mensaje es igual al del otro pasado como argumento o false en caso contrario
	*/
	public boolean compare(Mensaje msj)
	{
		boolean iguales = true;
		

		if ((msj.cod_op == null && this.cod_op != null) || (msj.cod_op != null && this.cod_op == null))
			iguales = false;
		else if (msj.cod_op == null && this.cod_op == null)
			iguales = true;
		else if (msj.cod_op.compareTo(this.cod_op) != 0)
			iguales = false;
		else iguales = true;


		return iguales;
	}

	/*
	* Comprueba si un mensaje es valido, es decir que no es nulo y por tanto
	* cumple con la sintaxis de mensajes (está contenido dentro del vector mensajes)
	* Devuelve true si es valido y false en otro caso.
	*/
	public boolean isValid()
	{
		return this.cod_op != null; 
	}
	
	public static void printMensajes()
	{
		for (int i  = 0; i < mensajes.length; i++)
			System.out.println(mensajes[i].cod_op + " " + mensajes[i].comando + " " + mensajes[i].n_args);
	}



	public static void main (String args[])
	{
		Mensaje m1 = new Mensaje("    LOGIN manuela 1234");
		Mensaje m2 = new Mensaje("LOGIN   pepa   1234");
		Mensaje m3 = new Mensaje("a");

		System.out.println(m1.toString());
		System.out.println(m1.isValid());

		String str = "041";
		int numero = Integer.parseInt(str);

		System.out.println(numero);

		System.out.println(m1.compare(m2));
		System.out.println(m1.compare(m3));
		System.out.println(m3.compare(m1));
		System.out.println(m3.compare(m3));


	}

}
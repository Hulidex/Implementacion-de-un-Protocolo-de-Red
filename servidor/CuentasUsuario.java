/**
* CLASE PARA REPRESENTAR UN REGISTRO DE CUENTAS
*/
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;


public class CuentasUsuario
{
	public String[] usuarios;
	private String[] passwords;


	//Constructor para leer los usuarios y contraseñas desde un fichero
	//El parámetro que se le pasa es un string que contiene la ruta donde está el fichero
	public CuentasUsuario(String nombre_fichero)
	{
		File archivo = null;
		FileReader lect_archivo = null;
		BufferedReader bufferLec = null;
		int n_usuarios;
		String linea = null;
		int aux;


		try{
			archivo = new File(nombre_fichero);//abrimos el fichero
			lect_archivo = new FileReader(archivo);//lo convertimos a fileReader
			bufferLec = new BufferedReader(lect_archivo);//Lo pasamos a BufferedREader para poder leer el archivo linea a linea en formato String


			n_usuarios = Integer.parseInt(bufferLec.readLine());//la primera linea del archivo contendrá el número de usuarios que se van a leer.

			usuarios = new String[n_usuarios];
			passwords = new String[n_usuarios];

			for (int i  = 0; i < n_usuarios; i++){
				linea = bufferLec.readLine();//leemos una línea del fichero
				
				//Extraemos de la línea el  nombre de usuario
				aux = linea.indexOf(':');
				usuarios[i] = new String(linea.substring(0,aux));
				linea = linea.substring(aux+1);

				//Extraemos de la línea la constraseña asociada a dicho usuario
				passwords[i] = linea;
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


	public void print()
	{
		for (int i = 0; i < usuarios.length; i++)
			System.out.println("Usuario: " + usuarios[i] + "\tConstraseña: " + passwords[i]);
	}


	/*Método para buscar un usuario retorna -1 si ni lo encuentra o la posición en donde lo ha encontrado*/
	public int find(String nombreUsuario)
	{
		int pos = -1;
		boolean encontrado = false;


		for(int i = 0; i < usuarios.length && !encontrado; i++){
			if (usuarios[i].compareTo(nombreUsuario) == 0){
				encontrado = true;
				pos = i;
			}
		}


		return pos;
	}

	/**
	* Comprueba si la contraseña en la posicion pos coincide con la cadena password
	*/
	public boolean checkPassword(int pos, String password)
	{
		return passwords[pos].compareTo(password) == 0;
	}

	public static void main (String []args)
	{
		CuentasUsuario count1= new CuentasUsuario("./cuentas");

		count1.print();

		System.out.println(count1.find("CALEFaCCION"));
	}

}
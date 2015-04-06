package rs232;

import giovynet.nativelink.SerialPort;
import giovynet.serial.Baud;
import giovynet.serial.Com;
import giovynet.serial.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cliente {

	public static Trama enviarMensaje(Trama miTrama)throws Exception{
		boolean confirmacion = false;
		Trama tTemp = null;

		//Ver y modificar informacion de la trama
		miTrama = informacionTrama(miTrama);

		while(!confirmacion){
			System.out.print("Envio: ");
			miTrama.imprimirTipo();
			//System.out.println("A");
			System.out.print("Recibo: ");
			enviar(miTrama);
			tTemp = escuchar();
			//ConfirmacionPositiva
			if(tTemp != null){
				tTemp.imprimirTipo();
			}

			if(tTemp!= null && tTemp.getControl() == Trama.CONFIRMACION_POSITIVA_CERO&& tTemp.esValida()){
				confirmacion = true;
			}

		}
		return tTemp;
	}

	private static Trama informacionTrama(Trama miTrama) {

		int longitud = miTrama.getFlagInicio().SIZE + miTrama.getControl().SIZE + (miTrama.getDatos().length*8) + (miTrama.getCheckSum().length*8)+miTrama.getFlagFin().SIZE;

		imprimirTrama(miTrama);

		System.out.println("DESEA MODIFICAR LA TRAMA S/N");
		String opcion = new String();
		Scanner entrada = new Scanner(System.in); 
		opcion = entrada.next();

		if(opcion.toUpperCase().equalsIgnoreCase("S")){
			System.out.println("DIGITE EL BIT QUE DESEA MODIFICAR");
			int bitModificar;
			bitModificar = entrada.nextInt();
			miTrama = modificarTrama(miTrama, bitModificar);
		}else{
			if(opcion.toUpperCase().equalsIgnoreCase("N")){
				System.out.println("LA TRAMA NO SE HA MODIFICADO");
			}
		}
		imprimirTrama(miTrama);
		Scanner  waitForKeypress = null;
		waitForKeypress = new Scanner ( System.in );
		System.out.println("\nPRESIONE ENTER PARA CONTINUAR");
		waitForKeypress.nextLine (); 
		return miTrama;
	}

	private static Trama modificarTrama(Trama miTrama, int bitModificar) {

		int longitud = miTrama.getFlagInicio().SIZE + miTrama.getControl().SIZE + (miTrama.getDatos().length*8) + (miTrama.getCheckSum().length*8)+miTrama.getFlagFin().SIZE;

		//FlagInicio
		if(bitModificar >= 1 && bitModificar<=8){
			System.out.println("FLAG INICIO");
			byte [] miB = new byte[1];
			byte a = miTrama.getFlagInicio();
			String bit = Integer.toBinaryString(a & 255 | 256).substring(1);
			System.out.println("BYTE ANTERIOR: "+bit);
			char [] bits = bit.toCharArray();
			for(int i = 0; i < 8; i++){
				if(i == bitModificar-1){
					if(bits[i] == '0')
					{
						bits[i] = '1';
					}else{
						bits[i] = '0';
					}
				}
			}
			String bModificados = new String(bits);
			//byte bytes = Byte.decode(bModificados);
			byte bytes = (byte)Short.parseShort(bModificados, 2);
			System.out.println("BYTE NUEVO:    "+Integer.toBinaryString(bytes & 255 | 256).substring(1));
			Byte info = new Byte(bytes);
			miTrama.setFlagInicio(info);
		}
		//Control
		if(bitModificar >= 9 && bitModificar<=16){
			System.out.println("CAMPO DE CONTROL");
			bitModificar= bitModificar-8;
			byte [] miB = new byte[1];
			byte a = miTrama.getControl();
			String bit = Integer.toBinaryString(a & 255 | 256).substring(1);
			System.out.println("BYTE ANTERIOR: "+bit);
			char [] bits = bit.toCharArray();
			for(int i = 0; i < 8; i++){
				if(i == bitModificar-1){
					if(bits[i] == '0')
					{
						bits[i] = '1';
					}else{
						bits[i] = '0';
					}
				}
			}
			String bModificados = new String(bits);
			//byte bytes = Byte.decode(bModificados);
			byte bytes = (byte)Short.parseShort(bModificados, 2);
			System.out.println("BYTE NUEVO:    "+Integer.toBinaryString(bytes & 255 | 256).substring(1));
			Byte info = new Byte(bytes);
			miTrama.setControl(info);
		}
		//FlagFinal
		if(bitModificar >= longitud-7 && bitModificar<=longitud){
			System.out.println("FLAG FINAL");
			bitModificar= bitModificar-(longitud-8);
			byte [] miB = new byte[1];
			byte a = miTrama.getFlagFin();
			String bit = Integer.toBinaryString(a & 255 | 256).substring(1);
			System.out.println("BYTE ANTERIOR: "+bit);
			char [] bits = bit.toCharArray();
			for(int i = 0; i < 8; i++){
				if(i == bitModificar-1){
					if(bits[i] == '0')
					{
						bits[i] = '1';
					}else{
						bits[i] = '0';
					}
				}
			}
			String bModificados = new String(bits);
			//byte bytes = Byte.decode(bModificados);
			byte bytes = (byte)Short.parseShort(bModificados, 2);
			System.out.println("BYTE NUEVO:    "+Integer.toBinaryString(bytes & 255 | 256).substring(1));
			Byte info = new Byte(bytes);
			miTrama.setFlagFin(info);
		}
		//CHECKSUM
		if(bitModificar >= longitud-23 && bitModificar<=longitud-8){
			System.out.println("CHECKSUM");
			bitModificar= bitModificar-(longitud-24);

			byte [] miB = new byte[2];
			miB = Trama.Btob(miTrama.getCheckSum());	
			int posicion = bitModificar/8;
			if(bitModificar%8 != 0){
				posicion++;
			}
			posicion = posicion -1;

			if(bitModificar > 8){
				bitModificar = bitModificar -8;
			}

			byte a = miB[posicion];
			String bit = Integer.toBinaryString(a & 255 | 256).substring(1);
			System.out.println("BYTE ANTERIOR: "+bit);
			char [] bits = bit.toCharArray();
			for(int i = 0; i < 8; i++){
				if(i == bitModificar-1){
					if(bits[i] == '0')
					{
						bits[i] = '1';
					}else{
						bits[i] = '0';
					}
				}
			}
			String bModificados = new String(bits);
			//byte bytes = Byte.decode(bModificados);
			byte bytes = (byte)Short.parseShort(bModificados, 2);
			System.out.println("BYTE NUEVO:    "+Integer.toBinaryString(bytes & 255 | 256).substring(1));
			Byte info = new Byte(bytes);
			miB[posicion] = info;
			miTrama.setCheckSum(Trama.btoB(miB));
		}

		//DATOS
		if(bitModificar>=17 && bitModificar <= longitud-24){
			System.out.println("CAMPO DE DATOS");
			bitModificar= bitModificar-16;
			System.out.println(longitud);
			byte [] miB = new byte[miTrama.getDatos().length];
			miB = Trama.Btob(miTrama.getDatos());	
			int posicion = bitModificar/8;
			if(bitModificar%8 != 0){
				posicion++;
			}
			posicion = posicion -1;
			System.out.println(posicion);
			if(bitModificar > 8){
				bitModificar = bitModificar -8*posicion;
			}
			System.out.println(bitModificar);
			byte a = miB[posicion];
			String bit = Integer.toBinaryString(a & 255 | 256).substring(1);
			System.out.println("BYTE ANTERIOR: "+bit);
			char [] bits = bit.toCharArray();
			for(int i = 0; i < 8; i++){
				if(i == bitModificar-1){
					if(bits[i] == '0')
					{
						bits[i] = '1';
					}else{
						bits[i] = '0';
					}
				}
			}
			String bModificados = new String(bits);
			//byte bytes = Byte.decode(bModificados);
			byte bytes = (byte)Short.parseShort(bModificados, 2);
			System.out.println("BYTE NUEVO:    "+Integer.toBinaryString(bytes & 255 | 256).substring(1));
			Byte info = new Byte(bytes);
			miB[posicion] = info;
			miTrama.setDatos(Trama.btoB(miB));
		}

		return miTrama;
	}

	public static void imprimirTrama(Trama miTrama){
		System.out.println("\nLA TRAMA ES");

		byte [] miB = new byte[1];
		byte a = miTrama.getFlagInicio();
		miB[0] = a;
		//Flag Inicio
		for (byte b : miB) {
			System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
			System.out.print(".");
		}
		a = miTrama.getControl();
		miB[0] = a;
		//Flag Control
		for (byte b : miB) {
			System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
			System.out.print(".");
		}
		//Datos
		for (byte b : miTrama.getDatos()) {
			System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
			System.out.print(".");
		}
		//ChechSum
		for (byte b : miTrama.getCheckSum()) {
			System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
			System.out.print(".");
		}
		//FlagFin
		a = miTrama.getFlagFin();
		miB[0] = a;
		//Flag Inicio
		for (byte b : miB) {
			System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
		}
		System.out.println();
	}

	public static Trama escucharMensaje() throws Exception{

		Trama miTrama=null;
		boolean llegaValida=false;
		while(!llegaValida){
			miTrama= escuchar();
			if(miTrama!=null&&miTrama.esValida()){
				llegaValida=true;
				Thread.sleep(500);
				Trama confirmacionP = new Trama();
				confirmacionP.crearTramaComfirmacionRecepcionPositiva(0);
				enviar(confirmacionP);
				System.out.print("Recibo: ");
				miTrama.imprimirTipo();
				System.out.print("Envio: ");
				confirmacionP.imprimirTipo();
			}else{

				Trama confirmacionN = new Trama();
				//modificar trama negativa
				Thread.sleep(500);
				confirmacionN.crearTramaComfirmacionRecepcionNegativa();
				enviar(confirmacionN);
				//System.out.println("mando");
				//confirmacionN.imprimirTipo();
			}
			//System.out.println("escucho");
		}
		//System.out.println("dasda");	

		//Ver trama
		imprimirTrama(miTrama);
		return miTrama;
	}

	public static void enviar(Trama miTrama) throws Exception{

		//Se revisan que puertos estan disponibles
		SerialPort free = new SerialPort();
		List<String> portList = free.getFreeSerialPort();
		if(portList.size() == 0){
			throw new Exception("NO HAY PUERTOS DISPONIBLES"); 
		}

		//Se configura el puerto
		Parameters settings = new Parameters();
		settings.setPort("COM1");
		settings.setBaudRate(Baud._9600);
		Com com1 = new Com(settings);

		//Se envia
		byte[] miTramaTemp = miTrama.convertirAByteArray(); 
		for(int i = 0; i < miTramaTemp.length; i++){
			//System.out.println(miTramaTemp[i]+ "");
			com1.sendSingleData(miTramaTemp[i]);
		}
		//System.out.println("ENVIE");
		com1.close();
	}

	public static Trama escuchar() throws Exception{
		SerialPort free = new SerialPort();
		//System.out.println("llego");
		Parameters settings = new Parameters();
		settings.setPort("COM1");
		settings.setBaudRate(Baud._9600);
		Com com1 = new Com(settings);

		String linea="";
		boolean termina=false;
		Trama trama= new Trama();
		String lineaCompleta="";
		int primero=0;
		Integer tiempo=0;
		HiloTiempo h_tiempo= new HiloTiempo(tiempo);
		h_tiempo.start();

		do{
			linea="";
			primero++;

			while(linea.equals("")&&tiempo<10){
				//com1.receiveToString(5);

				tiempo=h_tiempo.getTiempo();

				linea=com1.receiveSingleString();
				if(tiempo>=10){
					com1.close();
					//System.out.println("me salgo");	

					return null;	
				}

			}

			//  System.out.println("linea es "+linea);
			lineaCompleta+=linea;
			// System.out.println("cant leidos "+primero);
		}while(!linea.equals("~")||primero==1);


		//System.out.println("linea completa es: "+lineaCompleta);
		Trama retorno= new Trama(lineaCompleta);

		com1.close();
		return retorno;
	}   

	private static String leerArchivo(String nombreArchivo) {
		String miTexto = "";
		File f = new File(nombreArchivo);
		BufferedReader entradita;
		try {
			entradita = new BufferedReader( new FileReader( f ) );
			String linea;
			while(entradita.ready()){
				linea = entradita.readLine();
				miTexto+= '\n'+linea;
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return miTexto;
	}   

	private static void enviarInformacionArchivo(String miTexto) throws Exception {
		//miTexto=miTexto.replace(" ", "_");
		byte [] misBytes = miTexto.getBytes();
		int numBytes = miTexto.getBytes().length;

		int cantidadTramas = numBytes/(Trama.TAMANO_MAXIMO_TRAMA-1);

		if(numBytes%Trama.TAMANO_MAXIMO_TRAMA != 0){
			cantidadTramas++;
		}

		boolean seguir = true;

		//System.out.println("Numero De Bytes: " + numBytes);
		//System.out.println("Cantidad De Tramas: " + cantidadTramas);
		int inicioT = 1;
		int finalT = Trama.TAMANO_MAXIMO_TRAMA;
		if(misBytes.length < Trama.TAMANO_MAXIMO_TRAMA){
			finalT = misBytes.length+1;
		}

		for (int x = 0; x < cantidadTramas; x++) {


			byte[] datos = new byte[finalT - inicioT + 1];
			if(x == cantidadTramas-1){
				datos[0] = Trama.FIN_ARCHIVO;
			}else
			{
				datos[0] = Trama.INICIO_ARCHIVO;
			}

			if(numBytes < Trama.TAMANO_MAXIMO_TRAMA-1){
				finalT = numBytes;
			}

			if(numBytes < Trama.TAMANO_MAXIMO_TRAMA){
				for(int i = inicioT; i <= finalT; i++){
					datos[i] = misBytes[(x*(Trama.TAMANO_MAXIMO_TRAMA-1))+(i-1)];
					int a = (x*(Trama.TAMANO_MAXIMO_TRAMA-1))+(i-1);
					//System.out.println("I: " + i + " X:" + a);
				}
			}
			else{
				for(int i = inicioT; i < finalT; i++){
					datos[i] = misBytes[(x*(Trama.TAMANO_MAXIMO_TRAMA-1))+(i-1)];
					int a = (x*(Trama.TAMANO_MAXIMO_TRAMA-1))+(i-1);
					//System.out.println("I: " + i + " X:" + a);
				}
			}

			//System.out.println("LO QUE DEBERIA ENVIAR "+ new String(misBytes));
			//System.out.println("ENVIO " + datos.length);
			//System.out.println("LO QUE ENVIO "+new String(datos));

			Trama tramaArchivo = new Trama();
			tramaArchivo.crearTramaInformacion("");
			Byte [] bytes = Trama.btoB(datos);
			tramaArchivo.setDatos(bytes);
			Thread.sleep(500);
			enviarMensaje(tramaArchivo);

			numBytes -= Trama.TAMANO_MAXIMO_TRAMA-1;
		}
	} 

	public static void main(String[] args){

		Scanner entrada = new Scanner(System.in); 
		int opcion = 0;
		String CD = new String();
		String CA = new String();
		int miClave = 0;
		do{
			try{

				SerialPort free = new SerialPort();

				Trama miTrama = new Trama();

				System.out.println("-----------Escoja una opcion ------------");
				System.out.println("1.Enviar Trama De Inicio De Conexion");
				System.out.println("2.CIFRAR O DECIFRAR");
				System.out.println("3.Solicitatr tipos de cifrado/descifrado");
				System.out.println("4.Decir Tipo");
				System.out.println("5.Seleccionar Archivo");
				System.out.println("6.Finalizar conexion");
				System.out.println("7. Salir");
				opcion = entrada.nextInt();
				switch(opcion){
				case 1: 
					miTrama.crearTramaInicioConexion();
					enviarMensaje(miTrama);
					break;
				case 2:
					System.out.println("Escriba C para cifrar, D para descifrar: ");
					CD = entrada.next();
					//CD = "C";
					if(CD.equals("C")){
						miTrama.crearTramaInformacion("C");
					}else{
						if(CD.equals("D"))
							miTrama.crearTramaInformacion("D");
						else{
							System.out.println("Error Fatal");
						}
					}
					enviarMensaje(miTrama);

					break;
				case 3:
					miTrama.crearTramaInformacion("Tipos");
					enviarMensaje(miTrama);
					Trama miT;
					miTrama=escucharMensaje();


					byte []array = new byte[miTrama.getDatos().length]; 
					for (int i = 0; i < array.length; i++) {
						array[i] = miTrama.getDatos()[i];

					}
					String miS = new String(array);
					System.out.println("LOS TIPOS DE CIFRADO SON " + miS);
					break;
				case 4:

					System.out.println("Escriba CESAR para cesar, ATBASH para atbash: ");
					CA = entrada.next();
					//CA = "ATBASH";
					if(CA.equals("CESAR")){
						miTrama.crearTramaInformacion("CESAR");
					}else{
						if(CA.equals("ATBASH"))
							miTrama.crearTramaInformacion("ATBASH");
						else{
							System.out.println("Error Fatal");
						}
					}
					enviarMensaje(miTrama);
					Thread.sleep(500);
					if(CA.equals("CESAR")){
						miTrama = escucharMensaje();
					}


					byte []arrayT = new byte[miTrama.getDatos().length]; 
					for (int i = 0; i < arrayT.length; i++) {
						arrayT[i] = miTrama.getDatos()[i];

					}
					String miST = new String(arrayT);

					if(miST.equalsIgnoreCase("DIGITE_CLAVE")){
						System.out.println("DIGITE CLAVE");
						miClave = entrada.nextInt();
						miTrama.crearTramaInformacion(miClave+"");
						enviarMensaje(miTrama);
					}

					break;
				case 5:
					//System.out.print("\033[H\033[2J");
					//System.out.flush();

					System.out.println("Digite El nombre del archivo");
					String nombreArchivo = new String();
					nombreArchivo = entrada.next();
					//nombreArchivo = "RINRIN.txt";

					String miTexto = leerArchivo(nombreArchivo);
					enviarInformacionArchivo(miTexto);

					Trama mi_trama = escucharMensaje();

					String mensaje="";
					Trama archivo= mi_trama;

					boolean ab = true;

					while( ab || archivo.getDatos()[0]!=Trama.FIN_ARCHIVO){

						if(archivo.tieneDatos() && archivo.getDatos()[0]==Trama.FIN_ARCHIVO){
							ab = false;
							break;
						}

						String a =new String(archivo.sacarMensajeArchivo());
						mensaje += a;
						archivo=escucharMensaje();
					}

					String a =new String(archivo.sacarMensajeArchivo());
					mensaje += a;

					guardarArchivo(nombreArchivo, mensaje);

					break;
				case 6:
					miTrama.crearTramaFinConexion();
					enviarMensaje(miTrama);
					break;
				default:
					if(opcion!=7)
						System.out.println("Opcion incorrecta");
					break;
				}

			}catch(Exception e){
				System.out.println("Error fatal");
				e.printStackTrace();
			}
		}while(opcion!= 7);
	}

	private static void guardarArchivo(String nombreArchivo, String mensaje) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(nombreArchivo + "_RESULTADO.txt", "ASCII");
			writer.println(mensaje);	
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){

		}

	}
}
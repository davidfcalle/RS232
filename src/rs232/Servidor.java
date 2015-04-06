package rs232;

import giovynet.nativelink.SerialPort;
import giovynet.serial.Baud;
import giovynet.serial.Com;
import giovynet.serial.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.naming.LimitExceededException;


public class Servidor { 
	
	
	public final static String CESAR= "CESAR";
	public final static String ATBASH= "ATBASH";
	

	
	private static void enviarInformacionArchivo(String miTexto) throws Exception {
		//miTexto=miTexto.replace(" ", "_");
		byte [] misBytes = miTexto.getBytes();
		int numBytes = miTexto.getBytes().length;

		int cantidadTramas = numBytes/(Trama.TAMANO_MAXIMO_TRAMA-1);

		if(numBytes%Trama.TAMANO_MAXIMO_TRAMA != 0){
			cantidadTramas++;
		}

		boolean seguir = true;

		System.out.println("Numero De Bytes: " + numBytes);
		System.out.println("Cantidad De Tramas: " + cantidadTramas);
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
			
			
			
			System.out.println("LO QUE DEBERIA ENVIAR "+ new String(misBytes));
			System.out.println("ENVIO " + datos.length);
			System.out.println("LO QUE ENVIO "+new String(datos));
			
			Trama tramaArchivo = new Trama();
			tramaArchivo.crearTramaInformacion("");
			Byte [] bytes = Trama.btoB(datos);
			tramaArchivo.setDatos(bytes);
			Thread.sleep(500);
			enviarMensaje(tramaArchivo);
			
			numBytes -= Trama.TAMANO_MAXIMO_TRAMA-1;
		}
	} 
	
	
	public static Trama enviarMensaje(Trama miTrama)throws Exception{
		boolean confirmacion = false;
		Trama tTemp = null;
		miTrama = informacionTrama(miTrama);
		while(!confirmacion){
			//System.out.println("A");
			enviar(miTrama);
			tTemp = escuchar();
			if(tTemp!=null&&tTemp.getControl()==Trama.CONFIRMACION_POSITIVA_CERO&&tTemp.esValida()){
				confirmacion = true;
			}
		}
		//System.out.print("envio ");
		//miTrama.imprimirTipo();
		return tTemp;
	}
	public static Trama escucharMensaje() throws Exception{
		
		Trama miTrama=null;
		boolean llegaValida=false;
		while(!llegaValida){
			miTrama= escuchar();
			if(miTrama!=null&&miTrama.esValida()){
				llegaValida=true;
				Thread.sleep(300);
				Trama confirmacionP = new Trama();
				confirmacionP.crearTramaComfirmacionRecepcionPositiva(0);
				enviar(confirmacionP);
				//System.out.print("me llega ");
				//miTrama.imprimirTipo();
				//System.out.print("mando ");
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
		imprimirTrama(miTrama);
		return miTrama;
	}
	
	public static void enviar(Trama miTrama) throws Exception{
		//Se revisan que puertos estan disponibles
		SerialPort free = new SerialPort();
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
		//System.out.println("envié");
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
       
  
  
  Trama retorno= new Trama(lineaCompleta);

  com1.close();
  return retorno;
    }    
	
	public static String cifrarCesar(String Cadena, int clave){ 
        int VInt=0; 
        String CCifrado=""; 
         
        for (int i=0; i<Cadena.length();i++){ 
            if((Cadena.codePointAt(i)>=65 && Cadena.codePointAt(i)<=90) || (Cadena.codePointAt(i)>=97 && Cadena.codePointAt(i)<=122) ){ 
                VInt= Cadena.codePointAt(i) + clave;             
                CCifrado = CCifrado + new Character((char) VInt).toString(); 
            }else
                CCifrado=CCifrado+ Cadena.charAt(i); 
             
        } 
         
        return CCifrado; 
    } 
     
 
 
    public static String descifrarCesar(String Cadena, int clave){ 
        int VInt=0; 
        String DCifrado=""; 
         
        for (int i=0; i<Cadena.length();i++){ 
             
            if((Cadena.codePointAt(i)>=65 && Cadena.codePointAt(i)<=90) || (Cadena.codePointAt(i)>=97 && Cadena.codePointAt(i)<=122) ){ 
                VInt= Cadena.codePointAt(i) - clave;             
                DCifrado = DCifrado + new Character((char) VInt).toString(); 
            }else
                DCifrado=DCifrado+ Cadena.charAt(i); 
           
        } 
         return DCifrado;
      
    }

    public static String cipher(String msg, int shift){
        String s = "";
        int len = msg.length();
        for(int x = 0; x < len; x++){
            char c = (char)(msg.charAt(x) + shift);
            if (c > 'z')
                s += (char)(msg.charAt(x) - (26-shift));
            else
                s += (char)(msg.charAt(x) + shift);
        }
        return s;
    }

	public static String descifrarAtbash(String message ) {
        StringBuilder decodificar = new StringBuilder();
 
        for( char c : message.toUpperCase().toCharArray()) {
            if(Character.isLetter(c)) {
                int newChar = ('Z' - c) + 'A';
                decodificar.append((char) newChar);
            } else {
                decodificar.append(c);
            }
        }
        return decodificar.toString();
    }
	
	
	public static void main(String[] args){
		boolean contectado=false;// looks for free serial ports
		String cifrado="";
		String operacion="";
		int clave=-1;
		
	
		while(!contectado){
			
			try{
		Trama trama=escucharMensaje();
		// falta que si es inicio de conexion
		if(trama.esValida()){
				
			contectado=true;
			while(contectado){
				//System.out.println("Esperando Instruccion");
				
				Trama mi_trama=escucharMensaje();
				if(mi_trama.esDarTipos()){
					
					//System.out.println("Me llega una trama de dar tipos");
			    Trama recepcion= new Trama();
				recepcion.crearTramaInformacion(ATBASH+" "+CESAR);;
				//Thread.sleep(1000);
				enviarMensaje(recepcion);
				}else if(mi_trama.esDefinirOperacion()){
					//System.out.println("Me llega una trama de definir operacion");
					
					String responde = new String(mi_trama.Btob(mi_trama.getDatos()));
					operacion= responde;
			
				}else if(mi_trama.esDefinirCifrado()){
					
					//System.out.println("Me llega una trama de definir cifrado");
					String responde = new String(mi_trama.Btob(mi_trama.getDatos()));
					cifrado= responde;
					
					if(cifrado.equals(CESAR)){
						//deme la clave
						  Trama recepcion= new Trama();
						  recepcion.crearTramaInformacion("DIGITE_CLAVE");;
						  Thread.sleep(1000);
						  System.out.print("envio ");
						  enviarMensaje(recepcion);
						  recepcion.imprimirTipo();
						  recepcion= escucharMensaje();
						
						  responde = new String(recepcion.Btob(recepcion.getDatos()));
						  clave= Integer.parseInt(responde);
						  
					}
				}else if(mi_trama.tieneDatos()&&mi_trama.esArchivo() ){
					//es un trama de archivo
				
					String mensaje="";
					Trama archivo= mi_trama;
				
					boolean ab=true;
					while(ab||archivo.getDatos()[0]!=Trama.FIN_ARCHIVO){
						if(archivo.tieneDatos()&&archivo.getDatos()[0]==Trama.FIN_ARCHIVO){
							ab=false;
							break;
						}
						
						String a =new String(archivo.sacarMensajeArchivo());
						mensaje += a;
						archivo=escucharMensaje();
					}
					//Runtime.getRuntime().exec("cls");
					String a =new String(archivo.sacarMensajeArchivo());
					mensaje += a;
					//System.out.println("me llegaron "+a.getBytes().length);
					//ya me lleg� el mensaje completo y lo debo decodificar
					//mensaje=mensaje.toUpperCase();
					String mensajeCodificado="";
					if(cifrado.equals(ATBASH)&&operacion.equals("C")){
				
						mensajeCodificado=descifrarAtbash(mensaje);
					}else if(cifrado.equals(ATBASH)&&operacion.equals("D")){
					
						mensajeCodificado=descifrarAtbash(mensaje);
					}else if(cifrado.equals(CESAR)&&operacion.equals("D")){
					
						mensajeCodificado=descifrarCesar(mensaje, clave);
					}else{
					
						mensajeCodificado=cipher(mensaje,clave);
						
					}
					enviarInformacionArchivo(mensajeCodificado);
				}
				
			}
			
		}
		// estoy conectado
		
			
		}catch(Exception e){
			System.out.println("Se  ha desconectado el cable, verifique el error");
		e.printStackTrace();
	
		}
	}
	
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
}
package rs232;

import java.math.BigInteger;
import java.util.Arrays;

public class Trama {
	
	public final static byte FLAG=126; 
	public final static byte INICIO_CONEXION=1;
	public final static byte CONFIRMACION_POSITIVA_CERO= 127;
	public final static byte CONFIRMACION_NEGATIVA= 5;
	public final static byte CONFIRMACION_POSITIVA_UNO=125;
	public final static byte CONSULTA_CIFRADOS=2;
	public final static byte INFORMACION=3;
	public final static byte TIPOS=4;
	public final static int TAMANO_MAXIMO_TRAMA=500;
	public final static byte INICIO_ARCHIVO = 6;
	public final static byte FIN_ARCHIVO = 7;
	public final static byte FIN_CONEXION=8;
	public final static String CESAR= "CESAR";
	public final static String ATBASH= "ATBASH";
	
	private Byte flagInicio;
	private Byte control;
	private Byte[] datos;
	private Byte[] checkSum;
	private Byte flagFin;
	
	public Trama (String trama){
		byte [] miTrama = trama.getBytes();
		this.flagInicio = miTrama[0];
		this.control = miTrama[1];
		
		this.flagFin = miTrama[miTrama.length-1];
		this.checkSum = new Byte[2];
		this.checkSum[1] = miTrama[miTrama.length-2];
		this.checkSum[0] = miTrama[miTrama.length-3];
		this.datos = new Byte[miTrama.length-5];
		for (int i = 2; i < miTrama.length -3; i++) {
			datos[i-2]=miTrama[i];
		}
	}
	
	public Trama(Byte flagInicio, Byte control, Byte[] datos, Byte[] checkSum, Byte flagFin) {
		this.flagInicio = flagInicio;
		this.control = control;
		this.datos = datos;
		this.checkSum = checkSum;
		this.flagFin = flagFin;
	}
	public Trama(){
		
	}
	

	public void crearTramaFinConexion() {
			flagInicio=FLAG;
			control=FIN_CONEXION;
			datos= new Byte[0];
			checkSum= new Byte[2];
			checkSum[0]=FIN_CONEXION;////////////////////////////
			checkSum[1]= FIN_CONEXION;
			flagFin= FLAG;
	}
	public void crearTramaInicioConexion(){
		flagInicio=FLAG;
		control=INICIO_CONEXION;
		datos= new Byte[0];
		checkSum= new Byte[2];
		checkSum[0]=INICIO_CONEXION;////////////////////////////
		checkSum[1]= INICIO_CONEXION;
		flagFin= FLAG;
		
	}
	
	public void crearTramaComfirmacionRecepcionPositiva(int numeroSecuencia){
		flagInicio=FLAG;
		if(numeroSecuencia == 0){
			control=CONFIRMACION_POSITIVA_CERO;
		}	
		else{
			control=CONFIRMACION_POSITIVA_UNO;
		}
			
		datos= new Byte[0];
		checkSum= new Byte[2];
		checkSum[0]=INICIO_CONEXION;////////////////////////////
		checkSum[1]= INICIO_CONEXION;
		flagFin= FLAG;
	}
	
	public byte[] convertirAByteArray(){
		byte[] retorno= new byte[1+1+datos.length+checkSum.length+1];
		retorno[0]=flagInicio;
		retorno[1]=control;
		int total=2;
		for (int i = 0; i < datos.length; i++) {
			retorno[total]=datos[i];
			total++;
		}
		for (int i = 0; i < 2; i++) {
			retorno[total]=checkSum[i];
			total++;
		}
		retorno[total]= flagFin;
		return retorno;
	}
	
    public boolean esValida(){	
    	if(control==INFORMACION){
    		//cojo el CRC lo uno a datos y hago crc16 si es cero està buena
    		byte[] validacion= new byte[datos.length+checkSum.length];
    		for (int i = 0; i < validacion.length; i++) {
				if(i<datos.length){
    			validacion[i] = datos[i];
				}else{
					validacion[i]=checkSum[i-datos.length];
				}
			}
    		Integer resultado= crc16(validacion);
    		if(resultado==0){
    			return true;
    		}
    		return false;
    	}
    	//verificar CRC
    	return true;
    }
	public Byte getFlagInicio() {
		return flagInicio;
	}
	public void setFlagInicio(Byte flagInicio) {
		this.flagInicio = flagInicio;
	}
	public Byte getControl() {
		return control;
	}
	public void setControl(Byte control) {
		this.control = control;
	}
	public void setDatos(Byte[] datos) {
		this.datos = datos;
	}
	public Byte[] getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(Byte[] checkSum) {
		this.checkSum = checkSum;
	}
	public Byte getFlagFin() {
		return flagFin;
	}
	public void setFlagFin(Byte flagFin) {
		this.flagFin = flagFin;
	}

	@Override
	public String toString() {
		return "Trama [flagInicio=" + flagInicio + ", control=" + control
				+ ", datos=" + Arrays.toString(datos) + ", checkSum="
				+ Arrays.toString(checkSum) + ", flagFin=" + flagFin + "]";
	}

	public Byte[] getDatos() {
		return datos;
	}

	public void crearTramaInformacion(String string) {
		flagInicio=FLAG;
		control=INFORMACION;
		datos= new Byte[1];
		
		if(string.equalsIgnoreCase("Tipos")){
			datos[0] = TIPOS;
		}else{
		
			datos = new Byte[string.getBytes().length];
			for (int i = 0; i < datos.length; i++) {
				datos[i] = string.getBytes()[i];
			}
		}

			
		checkSum= new Byte[2];
		checkSum[0]=INICIO_CONEXION;////////////////////////////
		checkSum[1]= INICIO_CONEXION;
		flagFin= FLAG;
	}
	
	public static byte [] Btob(Byte[] miB){
		byte [] mib = new byte[miB.length];
		for (int i = 0; i < mib.length; i++) {
			mib[i] = miB[i];
		}
		return mib;
	}
	
	public static Byte [] btoB(byte[] mib){
		Byte [] miB = new Byte[mib.length];
		for (int i = 0; i < miB.length; i++) {
			miB[i] = mib[i];
		}
		return miB;
	}
	
		public boolean esDefinirCifrado(){
		String hola= new  String(Btob(datos));
		if(hola.equals(CESAR)||hola.equals(ATBASH)){
			return true;
		}
		return false;
	}
	
	public boolean esDefinirOperacion(){
		String hola= new  String(Btob(datos));
		if(hola.equals("C")||hola.equals("D")){
			return true;
		}
		return false;
	}
	
	public boolean esDarTipos() {
		// TODO Auto-generated method stub
		if(datos.length>0&&datos[0]==TIPOS){
			return true;
		}
		return false;
	}
	public byte[] sacarMensajeArchivo(){
		byte[] retorno= new byte[datos.length-1];
		for (int i = 0; i < retorno.length; i++) {
			retorno[i]=datos[i+1];
		}
		return retorno;
	}
	
	public void crearTramaComfirmacionRecepcionNegativa() {
		flagInicio=FLAG;
		control=CONFIRMACION_NEGATIVA;	
		datos= new Byte[0];
		checkSum= new Byte[2];
		
		
		//meter el checksum en la trama de informaciòn
		checkSum= btoB(getCRCAsByteArray(crc16(Btob(datos))));
		checkSum[0]=INICIO_CONEXION;////////////////////////////
		checkSum[1]= INICIO_CONEXION;
		flagFin= FLAG;
	}
	public void imprimirTipo(){
		if(control==CONFIRMACION_POSITIVA_CERO){
			System.out.println(" confirmaci�n positiva");
		}
		
		if(control==CONFIRMACION_NEGATIVA){
			
			System.out.println(" confirmaci�n negativa");
		}
		if(control==INFORMACION){
			if(esDefinirCifrado()){
				System.out.println("Definir Cifrado");
			}else if(esDefinirOperacion()){
				System.out.println("Definir Operacion");
			}else if(datos[0] == TIPOS)
			{
				System.out.println("PEDIR TIPOS");
			}else{
				String a = new String(Btob(datos));
				System.out.println("Trama de informaci�n "+a);
			}
		}
		if(control==INICIO_CONEXION){
			System.out.println("inicio conexi�n");
		}
		
	}
	public boolean tieneDatos(){
		if(datos.length==0){
			return false;
		}
		return true;
	}

	public boolean esArchivo() {
		// TODO Auto-generated method stub
		
		if(datos[0]==INICIO_ARCHIVO||datos[0]==FIN_ARCHIVO){
			return true;
		}
		return false;
	}
	
	
	private static int ModRTU_CRC(byte[] buf, int len)
	{
	  int crc = 0xFFFF;

	  for (int pos = 0; pos < len; pos++) {
	    crc ^= (int)buf[pos];          // XOR byte into least sig. byte of crc

	    for (int i = 8; i != 0; i--) {    // Loop over each bit
	      if ((crc & 0x0001) != 0) {      // If the LSB is set
	        crc >>= 1;                    // Shift right and XOR 0xA001
	        crc ^= 0xA001;
	      }
	      else                            // Else LSB is not set
	        crc >>= 1;                    // Just shift right
	    }
	  }
	// Note, this number has low and high bytes swapped, so use it accordingly (or swap bytes)
	return crc;  
	}
	public static int crc16(final byte[] bytes) {
	    int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
	    return crc;
	}
	public static byte[] getCRCAsByteArray(Integer numero){
		byte[]byteStr= new byte[2];
		byteStr[0] = (byte) ((numero & 0x000000ff));
	    byteStr[1] = (byte) ((numero & 0x0000ff00) >>> 8);
	    return byteStr;
	}
	
	public static int[] table = {
        0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
        0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
        0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
        0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
        0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
        0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
        0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
        0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
        0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
        0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
        0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
        0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
        0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
        0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
        0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
        0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
        0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
        0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
        0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
        0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
        0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
        0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
        0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
        0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
        0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
        0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
        0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
        0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
        0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
        0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
        0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
        0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
    };
	
	
}
package rs232;

public class Crc16 {
public static void main(String... a) {
    byte[] bytes = new byte[] { (byte) 0x08, (byte) 0x68, (byte) 0x14, (byte) 0x93, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x13, (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x09,
            (byte) 0x11 };
    byte[] byteStr = new byte[4];
    Integer crcRes = new Crc16().calculate_crc(bytes);
    System.out.println(Integer.toHexString(crcRes));

    byteStr[0] = (byte) ((crcRes & 0x000000ff));
    byteStr[1] = (byte) ((crcRes & 0x0000ff00) >>> 8);

    System.out.printf("%02X\n%02X", byteStr[0],byteStr[1]);
} 

int calculate_crc(byte[] bytes) {
    int i;
    int crc_value = 0;
    for (int len = 0; len < bytes.length; len++) {
        for (i = 0x80; i != 0; i >>= 1) {
            if ((crc_value & 0x8000) != 0) {
                crc_value = (crc_value << 1) ^ 0x8005;
            } else {
                crc_value = crc_value << 1;
            }
            if ((bytes[len] & i) != 0) {
                crc_value ^= 0x8005;
            }
        }
    }
    return crc_value;
}

}
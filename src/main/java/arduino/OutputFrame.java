package arduino;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class OutputFrame {

    public enum Function {
        SET_KP('a'), SET_KI('b'), SET_KD('c'),
        SET_X_MIN('d'), SET_X_MAX('e'),
        SET_KALMAN_UNCERTAINTY('f'), SET_KALMAN_VARIANCE('g'),
        SET_INPUT('h'), SET_LOOP('i'), SET_CONTROL_MODE('j'),
        SET_CORRECTION_MODE('k');

        private final byte id;

        Function(char id) {
            this.id = (byte)id;
        }

        public byte getId() {
            return id;
        }

    }

    private static final DecimalFormat dataFormat = new DecimalFormat("000.000");

    public static final int SIZE = 10;

    public static final int START_CHAR = '^';

    public static final float DATA_MIN = -1000;

    public static final float DATA_MAX = 1000;

    private final Function function;

    private final byte[] data;

    private final byte sign;

    public OutputFrame(Function function, float data) {
        if(data <= DATA_MIN || data >= DATA_MAX) {
            throw new IllegalArgumentException("value " + data +
                    " is out of range: [" + DATA_MIN + ", " + DATA_MAX + "]");
        }

        this.function = function;
        this.data = dataFormat.format(Math.abs(data)).replace(",", ".").getBytes();
        this.sign = data < 0 ? (byte) '-' : (byte) '+';

        if(this.data.length != SIZE) {
            //throw new IllegalArgumentException("arg");
        }
    }

    public OutputFrame(Function function, boolean data) {
        this(function, data ? 1 : 0);
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[SIZE];

        bytes[0] = START_CHAR;
        bytes[1] = function.id;
        bytes[2] = sign;

        for(int i=0; i<data.length; i++) {
            bytes[i+3] = data[i];
        }

        return bytes;
    }

    @Override
    public String toString() {
        return new String(getBytes());
    }

    public static Byte[] calculateCRC16_CCITT(List<Byte> data, int crcPolynomial) {
        int crc = 0xFFFF; //initial value

        for (byte b : data) {
            for (int i = 0; i<8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if(c15 ^ bit) {
                    crc ^= crcPolynomial;
                }
            }
        }

        crc &= 0xFFFF;

        Byte[] crcBytes = new Byte[2];
        for(int i=0; i<2; i++) {
            crcBytes[i] = (byte) ((crc >> i*8) & 0xff);
        }

        return crcBytes;
    }

    public static Byte[] calculateCRC16_CCITT(Byte[] data, int crcPolynomial) {
        return calculateCRC16_CCITT(Arrays.asList(data), crcPolynomial);
    }

}

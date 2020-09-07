package arduino;

public class InputFrame {

    public static final byte START_BYTE = (byte) '^';

    public static final byte SPLIT_BYTE = (byte) '#';

    public static final int DATA_SIZE = 8;

    public static final int DATA_COUNT = 4;

    public static final int SIZE = DATA_COUNT * (DATA_SIZE + 1);

    private final float[] data;

    public InputFrame(float[] data) {
        if(data.length != DATA_COUNT) {
            throw  new IllegalArgumentException("arg");
        }

        this.data = data;
    }

    public float getData(int index) {
        return data[index];
    }

}

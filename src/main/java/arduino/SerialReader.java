package arduino;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;

public class SerialReader {

    private final ArrayList<Byte> receivedData;

    private SerialPort serialPort;

    public SerialReader() {
        receivedData = new ArrayList<>();
    }

    protected void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public InputFrame read() {
        return null;
//        if(receivedData.size() >= InputFrame.SIZE) {
//            List<Byte> frameBytes = receivedData.subList(0, InputFrame.SIZE);
//            int pos1 = frameBytes.indexOf(InputFrame.START_BYTE);
//
//            if(pos1 != -1) {
//                if(pos1 == 0) {
//                    int pos2 = receivedData.subList(1, SIZE).indexOf(START_BYTE);
//
//                    if(pos2 != -1) {
//                        //System.out.println("c0");
//                        receivedData.subList(0, pos2 + 1).clear();
//                        return null;
//                    }
//
//                    String[] frameSplit = splitFrame(frameBytes);
//                    if(frameSplit.length == InputFrame.DATA_COUNT) {
//                        float[] frameData = new float[InputFrame.DATA_COUNT];
//                        for(int i=0; i<InputFrame.DATA_COUNT; i++) {
//                            if(frameSplit[i].length() == InputFrame.DATA_SIZE) {
//                                frameData[i] = Float.parseFloat(frameSplit[i]);
//                            } else {
//                                System.out.println("c1");
//                                synchronized (this) {
//                                    frameBytes.clear();
//                                }
//                                return;
//                            }
//                        }
//                        inputFrameStack.push(new InputFrame(frameData));
//                    }
//                } else {
//                    receivedData.subList(0, pos1).clear();
//                }
//            }
//
//            frameBytes.clear();
//            //FIXME concurrent modification ???
//        }
    }

}

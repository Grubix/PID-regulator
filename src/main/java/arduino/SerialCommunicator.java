package arduino;

import com.fazecast.jSerialComm.SerialPort;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static arduino.InputFrame.SIZE;
import static arduino.InputFrame.START_BYTE;

public final class SerialCommunicator {

    private final Stack<Byte> receivedData;

    private final Stack<OutputFrame> outputFrameStack;

    private final Stack<InputFrame> inputFrameStack;

    private InputFrame currentFrame;

    private SerialPort serialPort;

    public SerialCommunicator() {
        receivedData = new Stack<>();
        outputFrameStack = new Stack<>();
        inputFrameStack = new Stack<>();
    }

    public SerialCommunicator(SerialPort serialPort) throws Exception {
        this();
        connect(serialPort);
    }

    public void connect(SerialPort serialPort) throws Exception {
        if(!serialPort.openPort()) {
            throw new Exception("Cannot open port " + serialPort.getSystemPortName());
        }

        this.serialPort = serialPort;
        serialPort.setComPortParameters(57600, 8, 1, 0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0 , 0);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future task = executorService.submit(() -> {
            while(true) {
                int bytesAvailable = serialPort.bytesAvailable();
                if(bytesAvailable > 0) {
                    byte[] bytes = new byte[bytesAvailable];
                    serialPort.readBytes(bytes, bytesAvailable);
                    if(new String(bytes).contains("s")) {
                        serialPort.writeBytes("sss".getBytes(), 3);
                        break;
                    }
                }
            }
        });
        executorService.shutdown();
        task.get(3000, TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        if(serialPort != null) {
            serialPort.closePort();
        }

        receivedData.clear();
        inputFrameStack.clear();
        outputFrameStack.clear();
        currentFrame = null;
    }

    public boolean checkConnection() {
        if(serialPort == null) {
            return false;
        } else {
            return serialPort.isOpen();
        }
    }

    public synchronized void read() {
        if(!checkConnection()) {
            return;
        }

        int bytesAvailable = serialPort.bytesAvailable();

        if(bytesAvailable > InputFrame.SIZE) {
            byte[] buffer = new byte[InputFrame.SIZE];
            serialPort.readBytes(buffer, InputFrame.SIZE);
            for(byte b : buffer) {
                receivedData.push(b);
                //System.out.print(b);
            }
           //System.out.println();
        }

        if(receivedData.size() >= InputFrame.SIZE) {
            List<Byte> frameBytes = receivedData.subList(0, InputFrame.SIZE);
            int pos1 = frameBytes.indexOf(InputFrame.START_BYTE);

            if(pos1 != -1) {
                if(pos1 == 0) {
                    int pos2 = receivedData.subList(1, SIZE).indexOf(START_BYTE);

                    if(pos2 != -1) {
                        //System.out.println("c0");
                        receivedData.subList(0, pos2 + 1).clear();
                        return;
                    }

                    String[] frameSplit = splitFrame(frameBytes);
                    if(frameSplit.length == InputFrame.DATA_COUNT) {
                        float[] frameData = new float[InputFrame.DATA_COUNT];
                        for(int i=0; i<InputFrame.DATA_COUNT; i++) {
                            if(frameSplit[i].length() == InputFrame.DATA_SIZE) {
                                frameData[i] = Float.parseFloat(frameSplit[i]);
                            } else {
                                System.out.println("c1");
                                frameBytes.clear();
                                return;
                            }
                        }
                        inputFrameStack.push(new InputFrame(frameData));
                    }
                } else {
                    receivedData.subList(0, pos1).clear();
                }
            }

            frameBytes.clear();
            //TODO concurrent modification ???
        }
    }

    private static String[] splitFrame(List<Byte> frameBytes) {
        StringBuilder builder = new StringBuilder();

        for(int i=1; i<frameBytes.size(); i++) {
            builder.append((char)((byte) frameBytes.get(i)));
        }

        return builder.toString().split((char) InputFrame.SPLIT_BYTE + "");
    }

    public synchronized void write() {
        if(!checkConnection() || outputFrameStack.empty()) {
            return;
        }

        OutputFrame frame = outputFrameStack.get(0);
        outputFrameStack.remove(0);

        //System.out.println(new String(frame.getBytes()));
        serialPort.writeBytes(frame.getBytes(), OutputFrame.SIZE);
    }

    public synchronized void push(OutputFrame outputFrame) {
        outputFrameStack.push(outputFrame);
    }

    public synchronized InputFrame getCurrentFrame() {
        return currentFrame;
    }

    public synchronized InputFrame getNextFrame() {
        if(inputFrameStack.size() == 0) {
            return null;
        }

        currentFrame = inputFrameStack.pop();
        return currentFrame;
    }

    public synchronized boolean hasNextFrame() {
        return checkConnection() && inputFrameStack.size() > 0;
    }

}

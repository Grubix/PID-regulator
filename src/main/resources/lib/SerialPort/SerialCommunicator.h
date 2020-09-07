#include <ByteList.h>
#include <InputFrame.h>
#include <OutputFrame.h>

class SerialCommunicator {

    private:
        ByteList receivedData = ByteList(128);

    public:
        void begin(unsigned long baudrate, unsigned int timeout) {
		    Serial.begin(baudrate);
		    Serial.setTimeout(timeout);
		    while(!Serial) {
			    continue;
		    }

            Serial.print("sss");

            while(true) {
                if(Serial.available() == 3 && Serial.readString().indexOf('s') != -1) {
                    break;
                }
            }
	    }

        void print() {
            if(receivedData.size() > 0) {
                for(unsigned int i=0; i<receivedData.size(); i++) {
                    Serial.write(receivedData.get(i));
                }
            }
        }

        InputFrame * read() {
            unsigned int bytesAvailable = Serial.available();

            if(bytesAvailable >= InputFrame::SIZE) {
                byte * bytes = new byte[InputFrame::SIZE];
                Serial.readBytes(bytes, InputFrame::SIZE);
                receivedData.addAll(bytes, InputFrame::SIZE);
                delete bytes;
            }
            
            if(receivedData.size() >= InputFrame::SIZE) {
                int pos1 = receivedData.find(InputFrame::START_BYTE, 0, InputFrame::SIZE);

                if(pos1 != -1) {
                    if(pos1 == 0) {
                        int pos2 = receivedData.find(InputFrame::START_BYTE, 1, InputFrame::SIZE);
                        
                        if(pos2 != -1) {
                            receivedData.remove(0, pos2);
                            return nullptr;
                        }

                        byte functionId = receivedData.get(1);
                        if(functionId < 97 || functionId > 107) {
                            receivedData.remove(0, InputFrame::SIZE);
                            return nullptr;  
                        }

                        byte * functionDataBytes = receivedData.get(2, InputFrame::SIZE);

                        float functionData = atof((char*) functionDataBytes);
                        delete functionDataBytes;

                        receivedData.remove(0, InputFrame::SIZE);  
                        return new InputFrame(functionId, functionData);
                    } else {
                        receivedData.remove(0, pos1);
                    }
                } else {
                    receivedData.remove(0, InputFrame::SIZE);  
                }
            }

            return nullptr;
        }

        void write(float * data, unsigned int dataSize) {
            Serial.write(OutputFrame::START_BYTE);
            
            for(unsigned int i=0; i<dataSize; i++) {
                float d = data[i];
                if(d < -1000 || d > 1000) {
                    return;
                }

                byte sign = d > 0 ? (byte) '+' : (byte) '-';
                unsigned int zeros;
                d = abs(d);

                if(d < 10) {
                    zeros = 2;
                } else if(d < 100) {
                    zeros = 1;
                } else {
                    zeros = 0;
                }

                Serial.write(sign);

                for(unsigned int j=0; j<zeros; j++) {
                    Serial.write((byte) '0');
                }

                Serial.print(d, 3);
                if(i < dataSize - 1) {
                    Serial.write(OutputFrame::SPLIT_BYTE);
                }
            }
        }

};
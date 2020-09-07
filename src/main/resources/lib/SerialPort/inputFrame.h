class InputFrame {
    
    private:
        byte functionId; 
        float functionData;

    public:
        static const unsigned int SIZE = 10; 
        static const byte START_BYTE = (byte) '^';

        InputFrame(byte functionId, float functionData) {
            this->functionId = functionId;
            this->functionData = functionData;
        }

        byte getFunctionId() {
            return functionId;
        }

        float getFunctionData() {
            return functionData;
        }

};
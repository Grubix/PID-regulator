class OutputFrame {
    
    private:
        float * data;
        unsigned int dataSize;

    public:
        static const unsigned int SIZE = 10; 
        static const byte START_BYTE = (byte) '^';
        static const byte SPLIT_BYTE = (byte) '#';

        OutputFrame(float * data, unsigned int dataSize) {
            this->data = data;
            this->dataSize = dataSize;
        }

        byte * getBytes() {
            return new byte[2];
        }
};
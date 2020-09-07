#ifndef ByteList_H
#define ByteList_H

class ByteList {

    private:
        unsigned int _capacity;
        unsigned int _size;
        byte * data;

    public:
        ByteList(unsigned int capacity) {
            this->_capacity = capacity;
            data = new byte[capacity];
            _size = 0;
        }

        void add(byte element) {
            if(_size + 1 <= _capacity) {
                data[_size++] = element; 
            }
        }

        
        void addAll(byte * elements, unsigned int dataSize) {
            if(_size + dataSize <= _capacity) {
                for(unsigned int i=0; i<dataSize; i++) {
                    data[_size++] = elements[i];
                }
            }
        }

        void remove(unsigned int index) {
            remove(index, index + 1);
        }

        //from - inclusive, to - exclusive
        void remove(unsigned int from, unsigned int to) {
            if(to > _size || to > _capacity || to < from) {
                return;
            }
            
            unsigned int shift = to - from;

            for(unsigned int i=from, end=_capacity-shift; i<end ; i++) {
                data[i] = data[i + shift];
            }

            _size -= shift;
        }

        void clear() {
            _size = 0;
        }

        byte get(unsigned int index) {
            if(index > _size - 1) {
                return 0;
            }

            return data[index];
        }

        byte * get(unsigned int from, unsigned int to) {
            if(to > _size || to > _capacity || to < from) {
                return nullptr;
            }

            byte * bytes = new byte[to - from];
            for(unsigned int i=from; i<to; i++) {
                bytes[i - from] = data[i];
            }

            return bytes;
        }

        //from - inclusive, to - exclusive
        int find(byte element, unsigned int from, unsigned int to) {
            if(to > _size || to > _capacity || to < from) {
                return -1;
            }
            
            for(unsigned int i=from; i<to; i++) {
                if(data[i] == element) {
                    return i;
                }
            }

            return -1;
        }

        //from - inclusive
        int find(byte element, unsigned int from) {
            return find(element, from, _size);
        }

        int find(byte element) {
            return find(element, 0);
        }

        unsigned int size() {
            return _size;
        }

        unsigned int capacity() {
            return _capacity;
        }

};

#endif
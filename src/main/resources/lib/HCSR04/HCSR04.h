#ifndef HCSR04_H
#define HCSR04_H

#include "Arduino.h"

class HCSR04 {
	private:
		int triggerPin;
		int echoPin;
		float measurement;

	public:
		static const float SAMPLE_TIME;
		
		HCSR04(int triggerPin, int echoPin);
		float measureDistance();

};

#endif

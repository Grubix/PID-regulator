#include "HCSR04.h"

HCSR04::HCSR04(int triggerPin, int echoPin) {
    this->triggerPin = triggerPin;
    this->echoPin = echoPin;

    pinMode(triggerPin, OUTPUT);
    pinMode(echoPin, INPUT);
}

float HCSR04::measureDistance() {
	digitalWrite(triggerPin, LOW);
    delayMicroseconds(2);
	
	digitalWrite(triggerPin, HIGH);
	delayMicroseconds(10);
	digitalWrite(triggerPin, LOW);	
	float distance = pulseIn(echoPin, HIGH, 3200) / 2.0 * 0.0343;

	if(distance == 0) {
		return -1;
	}

	return distance;
}


#include "math.h"
#include "Arduino.h"
#include "KalmanFilter.h"

KalmanFilter::KalmanFilter(float uncertainty, float variance, int errorFlag) {
	this->uncertainty = uncertainty;
	this->error = uncertainty;
	this->variance= variance;
	this->errorFlag = errorFlag;
}

float KalmanFilter::compute(float measurement) {	
	if(measurement == errorFlag || measurement > 38.9)  {
		return lastEstimate;		
	}
	
	currentEstimate = lastEstimate + kalmanGain * (measurement - lastEstimate);	
	kalmanGain = error / (error + uncertainty);
	error = (1 - kalmanGain) * error + fabs(lastEstimate - currentEstimate) * variance;
	lastEstimate = currentEstimate;
	
	return currentEstimate;
}

void KalmanFilter::setUncertainty(float uncertainty) {
	this->uncertainty = uncertainty;
}

void KalmanFilter::setVariance(float variance) {
	this->variance= variance;
}
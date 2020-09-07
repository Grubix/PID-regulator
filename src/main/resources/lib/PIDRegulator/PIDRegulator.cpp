#include "PIDRegulator.h"

const float PIDRegulator::SAMPLE_TIME = 5096.0 / 1000000.0;
const float PIDRegulator::A_COEFF = 0.153;

PIDRegulator::PIDRegulator(float kp, float ki, float kd, float xMin, float xMax) {
	this->kp = kp;
	this->ki = ki;
	this->kd = kd;

	this->error = 0;
	this->integral = 0;
	this->output = 0;

	setXmin(xMin);
	setXmax(xMax);
}

float PIDRegulator::getKp() {
	return kp;
}

void PIDRegulator::setKp(float kp) {
	if(kp >= KP_MIN && kp <= KP_MAX) {
		this->kp = kp;
	}
}

float PIDRegulator::getKi() {
	return ki;
}

void PIDRegulator::setKi(float ki) {
	if(ki >= KI_MIN && ki <= KI_MAX) {
		if(ki == 0) {
			integral = 0;
		}
		
		this->ki = ki;
	}
}

float PIDRegulator::getKd() {
	return kd;
}

void PIDRegulator::setKd(float kd) {
	if(kd >= KD_MIN && kd <= KD_MAX) {
		this->kd = kd;
	}
}

void PIDRegulator::setXmin(float xmin) {
	if(xmin >= X_MIN_MIN && xmin <= X_MIN_MAX) {
		alphaMin = xmin / A_COEFF;
	}
}

void PIDRegulator::setXmax(float xmax) {
	if(xmax >= X_MAX_MIN && xmax <= X_MAX_MAX) {
		alphaMax = xmax / A_COEFF;
	}
}

float PIDRegulator::getAlphaMin() {
	return alphaMin;
}

float PIDRegulator::getAlphaMax() {
	return alphaMax;
}

float PIDRegulator::compute(float error) {	
	if(!windup && abs(error) >= 1){
		integral += ki * error * SAMPLE_TIME;
	}

	output = kp * error + integral + kd * (error - this->error) / SAMPLE_TIME;
	this->error = error;	

	if(output < alphaMin) {
		output = alphaMin;
		windup = true;
	} else if(output > alphaMax){
		output = alphaMax;
		windup = true;
	} else {
		windup = false;
	}
	
	return output;
}
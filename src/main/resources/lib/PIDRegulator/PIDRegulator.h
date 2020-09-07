#ifndef PIDRegulator_h
#define PIDRegulator_h

#include <arduino.h>

#define KP_MIN 0
#define KP_MAX 10
#define KI_MIN 0
#define KI_MAX 10
#define KD_MIN 0
#define KD_MAX 10
#define X_MIN_MIN -5
#define X_MIN_MAX -1
#define X_MAX_MIN 1
#define X_MAX_MAX 5

class PIDRegulator {
	public:		
		static const float SAMPLE_TIME;
		static const float A_COEFF; //x ~= A_COEFF * alpha
	
		bool windup;

		float kp;
		float ki;
		float kd;
	
		float alphaMin;
		float alphaMax;
	
		float error;
		float integral;
		float output;
			
	public:
		PIDRegulator(float kp, float ki, float kd, float xMin, float xMax);

		float getKp();
		void setKp(float kp);
	
		float getKi();
		void setKi(float ki);
	
		float getKd();
		void setKd(float kd);
	
		void setXmin(float xmin);
		void setXmax(float xmax);

		float getAlphaMin();
		float getAlphaMax();
		
		float compute(float error);
};

#endif

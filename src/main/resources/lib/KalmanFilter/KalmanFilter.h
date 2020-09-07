#ifndef KalmanFilter_h
#define KalmanFilter_h

class KalmanFilter {
	private:
		float uncertainty;
		float error;
		float variance;
		float lastEstimate;
		float currentEstimate;
		float kalmanGain;
		
		int errorFlag;
	
	public:
		KalmanFilter(float uncertainty, float variance, int errorFlag);
		float compute(float measurement);
		void setUncertainty(float measurementError);
		void setVariance(float variance);

};

#endif
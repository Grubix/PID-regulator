#include <SerialCommunicator.h>
#include <KalmanFilter.h>
#include <PIDRegulator.h>
#include <HCSR04.h>
#include <Servo.h>

#define MANUAL_MODE_POT_PIN 0
#define CONNECTION_LED_PIN 4
#define OPEN_LOOP_LED_PIN 5
#define MANUAL_MODE_LED_PIN 6
#define SERVO_HRZ_POS_ANGLE 53
#define BEAM_WIDTH 38

SerialCommunicator serialCommunicator;
PIDRegulator regulator(1.5, 0.0, 0.7, -2.5, 2.5);
KalmanFilter filter(7, 0.3, -1);
HCSR04 sensor(3,2);
Servo servo;

bool manualMode= false;
bool correctionMode = false;
bool openLoop = false;
bool timerFlag = false;

float stepAmplitude = 22.5;
float thetaAngle = 60;

bool counterFlag;
byte counter;

void setupTimer() {
  cli(); //disable interrupts
  
  TCCR2A = 0;
  TCCR2B = 0;
  TCNT2  = 0;
  OCR2A = 78;                                     //16Mhz (clk) / 1024 (prescaler) / 200Hz (freq) ~= 78
  TCCR2A |= _BV(WGM21);                           //CTC mode
  TCCR2B |= _BV(CS22) | _BV(CS21) | _BV(CS20);    //1024 prescaler 
  TIMSK2 |= _BV(OCIE2A);                          //enable compare interrupt

  sei(); //enable interrupts
}

void setup() {
  serialCommunicator.begin(57600, 100);
  servo.attach(9);
  setupTimer();

  digitalWrite(CONNECTION_LED_PIN, HIGH);
}

ISR(TIMER2_COMPA_vect) {
  timerFlag = true;
}

void loop() {
  if(timerFlag) {
    InputFrame * inputFrame = serialCommunicator.read();
    
    //serialCommunicator.print();
    if(inputFrame != nullptr) {
      char functionId = inputFrame->getFunctionId();
      float functionData = inputFrame->getFunctionData();
      switch(functionId) {
        case 'a': //set regulator kp
          regulator.setKp(functionData);
          break;
        case 'b': //set regulator ki
          regulator.setKi(functionData);
          break;
        case 'c': //set regulator kd
          regulator.setKd(functionData);
          break;
        case 'd': //set xmin -> set regulator alpha min 
          regulator.setXmin(functionData);
          break;
        case 'e': //set xmax -> set regulator alpha max 
          regulator.setXmax(functionData);
          break;
        case 'f': //set kalman filter uncertainty
          filter.setUncertainty(functionData);
          break;
        case 'g': //set kalman filter variance 
          filter.setVariance(functionData);
          break;
        case 'h': //set input -> step amplitude
          stepAmplitude = functionData;
          break;
        case 'i': //open / close feedback loop
          openLoop = functionData > 0;
          break;
        case 'j': //select manual / automatic mode
          manualMode = functionData > 0;
          break;
        case 'k': //correction mode
          correctionMode = functionData > 0;
          if(correctionMode) {
            digitalWrite(CONNECTION_LED_PIN, LOW);
            digitalWrite(OPEN_LOOP_LED_PIN, LOW);
            digitalWrite(MANUAL_MODE_LED_PIN, LOW);
            counterFlag = false;
            counter = 0;
          } else {
            digitalWrite(CONNECTION_LED_PIN, HIGH);
          }
          break;
      }
    }

    if(correctionMode){
      if(++counter == 100) {
        counterFlag = !counterFlag;
        counter = 0;
      }

      digitalWrite(CONNECTION_LED_PIN, counterFlag);

      thetaAngle = potScale(MANUAL_MODE_POT_PIN, 70, 20);
      servo.write(thetaAngle);
      timerFlag = false;
      return;
    }

    float * data = new float[4];
    data[0] = BEAM_WIDTH - sensor.measureDistance();   //measured distance
    data[1] = filter.compute(data[0]);                 //filtered distance = y(t)
    data[2] = stepAmplitude - data[1];                 //error = e(t) = x(t) - y(t)
                                                       //data[3] = alpha angle
    if(manualMode) {
      digitalWrite(MANUAL_MODE_LED_PIN, HIGH);
      data[3] = potScale(MANUAL_MODE_POT_PIN, regulator.getAlphaMin(), regulator.getAlphaMax());
    } else {
      digitalWrite(MANUAL_MODE_LED_PIN, LOW);
      if(openLoop) {
        digitalWrite(OPEN_LOOP_LED_PIN, HIGH);
        data[3] = regulator.compute(stepAmplitude);  //regulator input = x(t) = A * step(t)
      } else {  
        digitalWrite(OPEN_LOOP_LED_PIN, LOW);
        data[3] = regulator.compute(data[2]);        //regulator input = e(t) = x(t) - y(t)
      }   
    }

    servo.write(thetaAngle - data[3]);
    serialCommunicator.write(data, 4);

    delete data;
    delete inputFrame;
    timerFlag = false;
  }
}

float potScale(unsigned int potPin, float lowerBound, float upperBound) {
  return analogRead(potPin) * (upperBound - lowerBound) / 1010 + lowerBound;
}

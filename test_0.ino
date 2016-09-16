#include <SoftwareSerial.h>

SoftwareSerial mySerial(7, 8); // RX, TX  
// Connect HM10      Arduino Uno
//     Pin 1/TXD          Pin 7
//     Pin 2/RXD          Pin 8
String inString = "";
bool on = false;
bool temp = false;
bool toggle = true;
int digit1 = 11; //PWM Display pin 1
int digit2 = 10; //PWM Display pin 2
int digit3 = 9; //PWM Display pin 6
int digit4 = 6; //PWM Display pin 8
int digit5 = 12;
float temperature = 0;

int segA = A1; //Display pin 14
int segB = 3; //Display pin 16
int segC = 4; //Display pin 13
int segD = 5; //Display pin 3
int segE = A0; //Display pin 5
int segF = A2; //Display pin 11
int segG = A3; //Display pin 15
int dp = 2;

void setup() {  
  pinMode(segA, OUTPUT);
  pinMode(segB, OUTPUT);
  pinMode(segC, OUTPUT);
  pinMode(segD, OUTPUT);
  pinMode(segE, OUTPUT);
  pinMode(segF, OUTPUT);
  pinMode(segG, OUTPUT);
  pinMode(dp,OUTPUT);

  pinMode(digit1, OUTPUT);
  pinMode(digit2, OUTPUT);
  pinMode(digit3, OUTPUT);
  pinMode(digit4, OUTPUT);
  pinMode(digit5, OUTPUT);
  
  pinMode(A5, OUTPUT);
  Serial.begin(9600);
  // If the baudrate of the HM-10 module has been updated,
  // you may need to change 9600 by another value
  // Once you have found the correct baudrate,
  // you can update it using AT+BAUDx command 
  // e.g. AT+BAUD0 for 9600 bauds
  mySerial.begin(9600);
  delay(1000);

  // Should respond with OK
  mySerial.print("AT+RENEW");
  waitForResponse();

  mySerial.print("AT+RESET");
  waitForResponse();

  mySerial.print("AT");
  waitForResponse();

  mySerial.print("AT+MARJ0x0199");
  waitForResponse();

  mySerial.print("AT+MINO0x0199");
  waitForResponse();

  mySerial.print("AT+ADVI5");
  waitForResponse();

  mySerial.print("AT+NAMEDASHANXF");
  waitForResponse();

  mySerial.print("AT+IBEA1");
  waitForResponse();

  mySerial.print("AT+RESET");
  waitForResponse();
  Serial.println("Finished!");
}

void waitForResponse() {
  delay(1000);
  while (mySerial.available()) {
    Serial.write(mySerial.read());
  }
  Serial.write("\n");
}

void loop() {  
  char c;
  /*mySerial.print("AT+TEMP?");
  delay(500);
  while (mySerial.available()) {
    c = mySerial.read();
    inString +=c;
    Serial.print(c);
  }*/
  while(mySerial.available()){
    int a = mySerial.parseInt();
    switch (a){
      case 1: on = true;
      break;  
      case 2: on = false;
      break;
      case 3: temp = true;
      break;
      case 4: temp = false;
      break;
      default:
      on = false;
      temp = false;
      break;
    }
  }
  if(on){
    toggle = !toggle;
    digitalWrite(A5,toggle);
  }
  if(temp){
    mySerial.print("AT+TEMP?");
    delay(500);
    while(mySerial.available()){
      c=mySerial.read();
      inString +=c;
    }
    inString = inString.substring(8,12);
    Serial.println(inString);
    temperature = inString.toFloat();
    temp = false;
    inString = "";
  }
  int t = temperature*1000;
  if(t!=0)
  displayNumber(t);


  //inString = inString.substring(8);
  //inString="";
  //Serial.print("\n");
  //delay(500);
}

void displayNumber(int toDisplay) {
#define DISPLAY_BRIGHTNESS  500

#define DIGIT_ON  HIGH
#define DIGIT_OFF  LOW

  long beginTime = millis();
  for(int digit = 5 ; digit > 0 ; digit--) {
    //Turn on a digit for a short amount of time
    switch(digit) {
    case 1:
      digitalWrite(digit1, DIGIT_ON);
      digitalWrite(dp,HIGH);
      break;
    case 2:
      digitalWrite(digit2, DIGIT_ON);
      digitalWrite(dp,LOW);
      break;
    case 3:
      digitalWrite(digit3, DIGIT_ON);
      digitalWrite(dp,HIGH);
      break;
    case 4:
      digitalWrite(digit4, DIGIT_ON);
      digitalWrite(dp,HIGH);
      break;
    case 5:
      digitalWrite(digit5, DIGIT_ON);
      digitalWrite(dp,HIGH);
      digitalWrite(segC,LOW);
      digitalWrite(segA,HIGH);
      digitalWrite(segB,HIGH);
      break;
    }

    //Turn on the right segments for this digit
    if(digit == 5){
    }
    else if(digit ==4){
      lightNumber(12);
    }
    else{
      lightNumber(toDisplay % 10);
    }
    toDisplay /= 10;

    delayMicroseconds(DISPLAY_BRIGHTNESS); 
    //Display digit for fraction of a second (1us to 5000us, 500 is pretty good)

    //Turn off all segments
    lightNumber(10); 

    //Turn off all digits
    digitalWrite(digit1, DIGIT_OFF);
    digitalWrite(digit2, DIGIT_OFF);
    digitalWrite(digit3, DIGIT_OFF);
    digitalWrite(digit4, DIGIT_OFF);
  }

  while( (millis() - beginTime) < 10) ; 
  //Wait for 20ms to pass before we paint the display again
}

//Given a number, turns on those segments
//If number == 10, then turn off number
void lightNumber(int numberToDisplay) {

#define SEGMENT_ON  LOW
#define SEGMENT_OFF HIGH

  switch (numberToDisplay){

  case 0:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_OFF);
    break;

  case 1:
    digitalWrite(segA, SEGMENT_OFF);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_OFF);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_OFF);
    break;

  case 2:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_OFF);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 3:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 4:
    digitalWrite(segA, SEGMENT_OFF);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_OFF);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 5:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_OFF);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 6:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_OFF);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 7:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_OFF);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_OFF);
    break;

  case 8:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 9:
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_ON);
    digitalWrite(segC, SEGMENT_ON);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_ON);
    break;

  case 10:
    digitalWrite(segA, SEGMENT_OFF);
    digitalWrite(segB, SEGMENT_OFF);
    digitalWrite(segC, SEGMENT_OFF);
    digitalWrite(segD, SEGMENT_OFF);
    digitalWrite(segE, SEGMENT_OFF);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_OFF);
    break;
    
  case 12://C
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_OFF);
    digitalWrite(segC, SEGMENT_OFF);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_ON);
    digitalWrite(segG, SEGMENT_OFF);
          digitalWrite(12,SEGMENT_ON);
    break;
    
  case 15://F
    digitalWrite(segA, SEGMENT_ON);
    digitalWrite(segB, SEGMENT_OFF);
    digitalWrite(segC, SEGMENT_OFF);
    digitalWrite(segD, SEGMENT_ON);
    digitalWrite(segE, SEGMENT_ON);
    digitalWrite(segF, SEGMENT_OFF);
    digitalWrite(segG, SEGMENT_ON);
  }
}

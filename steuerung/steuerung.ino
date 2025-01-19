#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif




// Pin connected to ESC signal wire
const int escPin = 18; // Change this to the pin you use

// PWM settings
const int pwmFreq = 50;       // 50 Hz for ESC
const int pwmChannel = 0;     // PWM channel (0-15 on ESP32)
const int pwmResolution = 16; // 16-bit resolution
const int minPulse = 1000;    // 1 ms pulse width (in microseconds)
const int maxPulse = 2000;    // 2 ms pulse width (in microseconds)





BluetoothSerial SerialBT;


uint8_t power_input = 0;
bool on = false;
bool lights = false;

void setup() {
  Serial.begin(115200);

  initializeESC();


  SerialBT.begin("ESP32test"); // Bluetooth device name
  Serial.println("The device started, now you can pair it with Bluetooth!");
}

void loop() {
  if (SerialBT.available() >= 2) {
    power_input = SerialBT.read();
    uint8_t flagsByte = SerialBT.read();
    update_flags(flagsByte);
  }

  Serial.println("Power:");
  Serial.println(power_input);
  Serial.println("On:");
  Serial.println(on);
  Serial.println("Lights:");
  Serial.println(lights);

  if (on){
    setMotorPower(power_input);
  }
  else {
    setMotorPower(0);
  }

  delay(10); // Delay for readability
}

void update_flags(uint8_t flags) {
  on = (flags & 1) != 0;   // Check bit 8
  lights = (flags & 2) != 0; // Check bit 9
}

// Function to initialize the ESC
void initializeESC() {
  // Set up the PWM channel
  ledcSetup(pwmChannel, pwmFreq, pwmResolution);
  // Attach the PWM channel to the pin
  ledcAttachPin(escPin, pwmChannel);

  // Send minimum signal to arm the ESC
  int dutyCycle = pulseWidthToDutyCycle(minPulse);
  ledcWrite(pwmChannel, dutyCycle);
  delay(1000); // Wait for the ESC to arm (check your ESC manual)
}

// Function to set motor power (0 to 100%)
void setMotorPower(int powerInput) {
  // Ensure the input is within valid bounds
  powerInput = constrain(powerInput, 0, 100);

  // Map the power input to pulse width (microseconds)
  int pwmValue = map(powerInput, 0, 100, minPulse, maxPulse);

  // Convert pulse width to duty cycle and write it to the ESC
  int dutyCycle = pulseWidthToDutyCycle(pwmValue);
  ledcWrite(pwmChannel, dutyCycle);
}

// Helper function to convert pulse width (microseconds) to duty cycle
int pulseWidthToDutyCycle(int pulseWidth) {
  return (pulseWidth * 65535) / 20000; // 20ms period
}

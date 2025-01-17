#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

int bluetooth_input = 592;
int power_input = 0;
bool on = false;
bool lights = false;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); // Bluetooth device name
  Serial.println("The device started, now you can pair it with Bluetooth!");
}

void loop() {
  if (SerialBT.available()) {
    // Read Bluetooth input and store it as an integer
    bluetooth_input = SerialBT.parseInt();
    update_data(bluetooth_input);
  }

  // Print the decoded values
  Serial.print("Power Input: ");
  Serial.println(power_input);

  Serial.print("On State: ");
  Serial.println(on);

  Serial.print("Lights State: ");
  Serial.println(lights);

  delay(100); // Delay for readability
}

void update_data(int input) {
  power_input = input & 255;  // Extract bits 0–7
  on = (input & 256) != 0;   // Check bit 8
  lights = (input & 512) != 0; // Check bit 9
}

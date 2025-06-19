#include <ESP8266WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>
#include <LiquidCrystal_I2C.h>

#define API_KEY "AIzaSyBPKWO3M8J0ktT9STkFdJ0_fDS2aRxyFbo"
#define USER_EMAIL "ghozali@gmail.com"
#define USER_PASSWORD "12345678"
#define DATABASE_URL "https://loker-1ccc1-default-rtdb.firebaseio.com"
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
LiquidCrystal_I2C lcd(0x27, 16, 2);

const char* ssid = "hmm";
const char* password = "hihihihi";
unsigned long dataMillis = 0;
unsigned long lcdLastUpdate = 0;
int lcdState = 0;
bool isLokerActionActive = false; 
String activeLokerName = ""; 
void setup() {
  
  Serial.begin(115200);
   pinMode(D5, OUTPUT);
   pinMode(D6, OUTPUT);
   pinMode(D7, OUTPUT);
   pinMode(D4, OUTPUT);
   digitalWrite(D5, HIGH);  
   digitalWrite(D6, HIGH);  
   digitalWrite(D7, HIGH);   
   digitalWrite(D4, HIGH);  
   
   lcd.init();  
   lcd.backlight();  
   lcd.print("Inisialisasi...");  
   delay(2000);
   lcd.clear();  
   connectToWiFi();
}

void loop() {
      // Cek apakah Wi-Fi terhubung
  while (WiFi.status() != WL_CONNECTED) {
    Serial.println("Tidak terhubung ke Wi-Fi. Mencoba lagi...");
    modeAdmin();
  }
  Firebase.reconnectNetwork(true); // Coba sambung ulang ke Firebase
  
  checkLokerAction("/loker/loker-a/action", D5, "Loker A");
  checkLokerAction("/loker/loker-b/action", D6, "Loker B");
  checkLokerAction("/loker/loker-c/action", D7, "Loker C");
  checkLokerAction("/loker/loker-d/action", D4, "Loker D");

  unsigned long inputTimeout = 3000; // Waktu tunggu (ms)
  unsigned long lastInputTime = millis();
  bool waitingForInput = true;
  while (waitingForInput) {
    if (Serial.available() > 0) {
      String input = Serial.readStringUntil('\n');
      input.trim(); // Hapus spasi di awal/akhir data

      // Tampilkan input ke LCD
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Input: ");
      lcd.setCursor(0, 1);
      lcd.print(input);

      // Reset waktu tunggu
      lastInputTime = millis();

      // Proses input
      if (input == "Message1") {
        lcd.clear();
        digitalWrite(D5, LOW); // Aktifkan pin D5
        lcd.setCursor(0, 0);
        lcd.print("Password Benar");
        lcd.setCursor(0, 1);
        lcd.print("Loker A Terbuka");
        delay(5000); // Loker terbuka selama 5 detik
        digitalWrite(D5, HIGH); // Matikan pin D5
      } else if (input == "Message2") {
        lcd.clear();
        digitalWrite(D6, LOW); // Aktifkan pin D6
        lcd.setCursor(0, 0);
        lcd.print("Password Benar");
        lcd.setCursor(0, 1);
        lcd.print("Loker B Terbuka");
        delay(5000); // Loker terbuka selama 5 detik
        digitalWrite(D6, HIGH); // Matikan pin D5
      } else if (input == "Message3") {
        lcd.clear();
        digitalWrite(D7, LOW); // Aktifkan pin D7
        lcd.setCursor(0, 0);
        lcd.print("Password Benar");
        lcd.setCursor(0, 1);
        lcd.print("Loker C Terbuka");
        delay(5000); // Loker terbuka selama 5 detik
        digitalWrite(D7, HIGH); // Matikan pin D5
      } else if (input == "Message4") {
        lcd.clear();
        digitalWrite(D4, LOW); // Aktifkan pin D8
        lcd.setCursor(0, 0);
        lcd.print("Password Benar");
        lcd.setCursor(0, 1);
        lcd.print("Loker D Terbuka");
        delay(5000); // Loker terbuka selama 5 detik
        digitalWrite(D4, HIGH); // Matikan pin D5
      } else {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Mode Admin");
        lcd.setCursor(0, 1);
        lcd.print("Input: ");
        lcd.print(input); // Tampilkan data ke LCD
      }
    }

    // Cek apakah waktu tunggu sudah habis
    if (millis() - lastInputTime > inputTimeout) {
      waitingForInput = false;
    }
  }


  // Jika Firebase sudah terhubung, lanjutkan eksekusi program
  int32_t rssi = WiFi.RSSI();
  Serial.print("Kekuatan sinyal WiFi: ");
  Serial.print(rssi);
  Serial.println(" dBm");
  
  int rssiClassification;
  if (rssi > -50) {
      rssiClassification = 4;
  } else if (rssi > -60) {
      rssiClassification = 3;
  } else if (rssi > -70) {
      rssiClassification = 2; 
  } else {
      rssiClassification = 1;
  }
  
  if (Firebase.RTDB.setInt(&fbdo, "/rssi", rssiClassification)) {
      Serial.println("Klasifikasi sinyal WiFi berhasil dikirim ke Firebase.");
  } else {
      Serial.println("Gagal mengirim klasifikasi sinyal WiFi ke Firebase.");
  }
  
  displayLokerStatus();

  if (millis() - dataMillis > 5000 && Firebase.ready()) {
    dataMillis = millis();
  }
}


// Fungsi untuk mencoba menghubungkan ke Wi-Fi
void connectToWiFi() {
    const int maxRetries = 2;  // Jumlah percobaan maksimum
    const unsigned long timeout = 10000;  // Waktu timeout (30 detik)
    int retries = 0;  // Counter untuk percobaan
    bool connected = false;

    while (retries < maxRetries) {
        Serial.print("Menghubungkan ke Wi-Fi... (Percobaan ");
        Serial.print(retries + 1);
        Serial.println(")");
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Menghubungkan ke");
        lcd.setCursor(0, 1);
        lcd.print("Jaringan WiFi");

        WiFi.begin(ssid, password);
        unsigned long startAttemptTime = millis();
        while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < timeout) {
            Serial.print(".");
            delay(1000);  // Tunda 1 detik
        }
        if (WiFi.status() == WL_CONNECTED) {
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print("Wifi Terhubung");
            lcd.setCursor(0, 1);
            lcd.print("Yeyy");
            connected = true;
            break;  // Keluar dari loop jika berhasil terhubung
        }
        Serial.println();
        Serial.println("Gagal terhubung ke Wi-Fi. Mencoba lagi...");
        retries++;
    }

    if (connected) {
        Serial.println();
        Serial.print("Terhubung ke Wi-Fi dengan IP: ");
        Serial.println(WiFi.localIP());
        connectToFirebase();
    } else {
        Serial.println("Gagal terhubung ke Wi-Fi setelah 2 kali percobaan.");
        modeAdmin();  // Jalankan fungsi modeAdmin jika tidak berhasil
    }
}
void modeAdmin() {
    Serial.println("Masuk ke mode admin. Konfigurasi manual diperlukan.");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Mode Admin");
    lcd.setCursor(0, 1);
    lcd.print("Menunggu Input");

    while (true) { // Tetap di mode admin hingga terhubung ke Wi-Fi atau menerima input
        if (Serial.available() > 0) { // Jika data tersedia di Serial
            String input = Serial.readStringUntil('\n'); // Baca data hingga newline
            input.trim(); // Hapus spasi di awal/akhir data

            // Cek pesan dan kontrol pin digital sesuai dengan pesan
            if (input == "Message1") {
                lcd.clear();
                digitalWrite(D5, LOW); // Aktifkan pin D5
                lcd.setCursor(0, 0);
                lcd.print("Password Benar");
                lcd.setCursor(0, 1);
                lcd.print("Loker A Terbuka");
                delay(5000); // Tampilkan hasil selama 3 detik
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Mode Admin");
                lcd.setCursor(0, 1);
                lcd.print("Menunggu Input");
            } else if (input == "Message2") {
                lcd.clear();
                digitalWrite(D6, LOW); // Aktifkan pin D6
                lcd.setCursor(0, 0);
                lcd.print("Password Benar");
                lcd.setCursor(0, 1);
                lcd.print("Loker B Terbuka");
                delay(5000); // Tampilkan hasil selama 3 detik
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Mode Admin");
                lcd.setCursor(0, 1);
                lcd.print("Menunggu Input");
            } else if (input == "Message3") {
                lcd.clear();
                digitalWrite(D7, LOW); // Aktifkan pin D7
                lcd.setCursor(0, 0);
                lcd.print("Password Benar");
                lcd.setCursor(0, 1);
                lcd.print("Loker C Terbuka");
                delay(5000); // Tampilkan hasil selama 3 detik
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Mode Admin");
                lcd.setCursor(0, 1);
                lcd.print("Menunggu Input");
            } else if (input == "Message4") {
                lcd.clear();
                digitalWrite(D8, LOW); // Aktifkan pin D8
                lcd.setCursor(0, 0);
                lcd.print("Password Benar");
                lcd.setCursor(0, 1);
                lcd.print("Loker D Terbuka");
                delay(5000); // Tampilkan hasil selama 3 detik
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Mode Admin");
                lcd.setCursor(0, 1);
                lcd.print("Menunggu Input");
            } else {
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Mode Admin");
                lcd.setCursor(0, 1);
                lcd.print("Input: ");
                lcd.print(input); // Tampilkan data ke LCD
            }
            // Reset semua pin ke HIGH (OFF)
            digitalWrite(D5, HIGH);
            digitalWrite(D6, HIGH);
            digitalWrite(D7, HIGH);
            digitalWrite(D8, HIGH);
        } else {
            // Jika tidak ada input dari Serial, coba hubungkan ke Wi-Fi
            WiFi.begin(ssid, password);
            unsigned long startAttemptTime = millis();
            while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 30000) {
                if (Serial.available() > 0) {
                    // Jika input dari Serial diterima, hentikan upaya koneksi Wi-Fi
                    Serial.println("Input diterima. Menghentikan upaya koneksi Wi-Fi.");
                    break;
                }
                delay(1000);
            }

            if (WiFi.status() == WL_CONNECTED) {
                Serial.println("Terhubung ke Wi-Fi!");
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("WiFi Terhubung");
                lcd.setCursor(0, 1);
                lcd.print(WiFi.localIP());
                delay(2000);
                break;
            }

            Serial.println("Gagal terhubung ke Wi-Fi. Coba lagi...");
        }
    }
}


void connectToFirebase(){
  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);
  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.database_url = DATABASE_URL;
  Firebase.begin(&config, &auth);
  config.token_status_callback = tokenStatusCallback;
  fbdo.setBSSLBufferSize(4096, 1024);
  fbdo.setResponseSize(4096);
  while (Firebase.ready() == false) {
    Serial.print(".");
    delay(1000);  // Tunggu 1 detik
  }
  Serial.println();
  Serial.println("Berhasil terhubung ke Firebase!");
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Firebase Ok");
                lcd.setCursor(0, 1);
                lcd.print("Yeyy");
}
void checkLokerAction(String path, int pin, String lokerName) {
    if (Firebase.RTDB.getBool(&fbdo, path)) {
        bool actionStatus = fbdo.boolData();
        if (actionStatus) {
            Serial.print(lokerName);
            Serial.println(" dibuka selama 5 detik.");
            digitalWrite(pin, LOW);
            isLokerActionActive = true;
            activeLokerName = lokerName;
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print(lokerName + " Terbuka");
            delay(5000);
            digitalWrite(pin, HIGH);
            if (Firebase.RTDB.setBool(&fbdo, path, false)) {
                Serial.print(lokerName);
                Serial.println(" action diubah menjadi false.");
            } else {
                Serial.print("Gagal mengubah action di ");
                Serial.println(lokerName);
            }
            isLokerActionActive = false;
        }
    } else {
        Serial.print("Gagal membaca action dari ");
        Serial.println(lokerName);
    }
}

void displayLokerStatus() {
    if (isLokerActionActive) {
        return;
    }
    if (millis() - lcdLastUpdate >= 2000) {
        lcdLastUpdate = millis();
        switch (lcdState) {
            case 0:
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Status Loker A: ");
                lcd.setCursor(0, 1);
                lcd.print(Firebase.RTDB.getString(&fbdo, "/loker/loker-a/status/") ? fbdo.stringData() : "Error");
                break;
            case 1:
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Status Loker B: ");
                lcd.setCursor(0, 1);
                lcd.print(Firebase.RTDB.getString(&fbdo, "/loker/loker-b/status/") ? fbdo.stringData() : "Error");
                break;
            case 2:
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Status Loker C: ");
                lcd.setCursor(0, 1);
                lcd.print(Firebase.RTDB.getString(&fbdo, "/loker/loker-c/status/") ? fbdo.stringData() : "Error");
                break;
            case 3:
                lcd.clear();
                lcd.setCursor(0, 0);
                lcd.print("Status Loker D: ");
                lcd.setCursor(0, 1);
                lcd.print(Firebase.RTDB.getString(&fbdo, "/loker/loker-d/status/") ? fbdo.stringData() : "Error");
                break;
        }
        lcdState = (lcdState + 1) % 4;
    }
}

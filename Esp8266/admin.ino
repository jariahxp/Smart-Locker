#include <Keypad.h>

// Konfigurasi pin keypad
const byte ROWS = 4; // Jumlah baris
const byte COLS = 4; // Jumlah kolom

// Array untuk mendefinisikan tombol pada keypad
char keys[ROWS][COLS] = {
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}  // Tombol D untuk menghapus karakter
};

// Pin pada ESP8266 yang terhubung dengan baris dan kolom keypad
byte rowPins[ROWS] = {D1, D2, D3, D4}; // Sesuaikan dengan pin ESP8266
byte colPins[COLS] = {D5, D6, D7, D8};

// Inisialisasi library Keypad
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);

// Deklarasi password dan data yang dikirim
const String passwords[4] = {"12345", "54321", "11223", "33211"}; // 4 password
const String messages[4] = {"Message1", "Message2", "Message3", "Message4"}; // Pesan yang dikirim
String inputPassword = ""; // Variabel untuk menyimpan input dari keypad
const int maxLength = 5;  // Panjang maksimal password

void setup() {
  Serial.begin(115200);  // Kecepatan komunikasi dengan ESP penerima
}

void loop() {
  char key = keypad.getKey(); // Membaca tombol yang ditekan

  if (key) {
    // Jika tombol * ditekan, reset input
    if (key == '*') {
      inputPassword = "";
      Serial.println("Direset");
    }
    // Jika tombol # ditekan, periksa password
    else if (key == '#') {
      // Periksa apakah input cocok dengan salah satu password
      bool isValid = false;
      for (int i = 0; i < 4; i++) {
        if (inputPassword == passwords[i]) {
          isValid = true;
          Serial.println(messages[i]); // Kirim pesan yang sesuai
          break;
        }
      }
      if (!isValid) {
        Serial.println("Salah");
      }
      inputPassword = ""; // Reset setelah pemeriksaan
    }
    // Jika tombol D ditekan, hapus karakter terakhir dari input
    else if (key == 'D') {
      if (inputPassword.length() > 0) {
        inputPassword.remove(inputPassword.length() - 1); // Menghapus karakter terakhir
        Serial.println(inputPassword); // Menampilkan password setelah dihapus
      }
    }
    // Tambahkan karakter ke input password
    else {
      if (inputPassword.length() < maxLength) {
        inputPassword += key;
        Serial.println(inputPassword);
      } else {
        // Jika panjang password melebihi batas, tampilkan pesan dan reset
        Serial.println("Max 5");
        inputPassword = ""; // Reset password jika terlalu panjang
      }
    }
  }
}

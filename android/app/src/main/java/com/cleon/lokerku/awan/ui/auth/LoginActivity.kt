package com.cleon.lokerku.awan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cleon.lokerku.awan.R
import com.cleon.lokerku.awan.databinding.ActivityLoginBinding
import com.cleon.lokerku.awan.helper.preference.UserPreference
import com.cleon.lokerku.awan.helper.repository.UserRepository
import com.cleon.lokerku.awan.helper.viewmodel.LoginViewModel
import com.cleon.lokerku.awan.ui.dashboard.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi UserRepository dan UserPreference
        val userRepository = UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val userPreference = UserPreference(this)

        // Inisialisasi LoginViewModel
        loginViewModel = LoginViewModel(userRepository, userPreference)

        // Setup loading dialog
        setupLoadingDialog()

        // Tombol login
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.editText?.text.toString()
            val password = binding.edLoginPassword.editText?.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                loadingDialog.show()
                loginViewModel.login(email, password, { username ->
                    // Login berhasil
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Welcome $username!", Toast.LENGTH_SHORT).show()

                    // Beralih ke DashboardActivity
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }, { error ->
                    // Login gagal
                    loadingDialog.dismiss()
                    showLoginFailedDialog(error)
                })
            } else {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk berpindah ke halaman registrasi
        binding.tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Menampilkan dialog error saat login gagal
    private fun showLoginFailedDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Login Gagal")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
    }

    // Menyiapkan loading dialog
    private fun setupLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.show_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
    }
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah Anda yakin ingin keluar aplikasi?")
            .setCancelable(false) // Dialog tidak bisa dibatalkan dengan klik di luar
            .setPositiveButton("Ya") { dialog, id ->
                finishAffinity() // Menutup seluruh aktivitas dan keluar aplikasi
            }
            .setNegativeButton("Tidak") { dialog, id ->
                dialog.dismiss() // Menutup dialog dan tidak melakukan apa-apa
            }
        val alert = builder.create()
        alert.show()

        // Tidak memanggil super.onBackPressed() agar kita bisa menangani tombol kembali dengan custom
    }
}

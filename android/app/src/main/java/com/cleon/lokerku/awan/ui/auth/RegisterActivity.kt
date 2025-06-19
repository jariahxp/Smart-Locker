package com.cleon.lokerku.awan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cleon.lokerku.awan.R
import com.cleon.lokerku.awan.databinding.ActivityRegisterBinding
import com.cleon.lokerku.awan.helper.preference.UserPreference
import com.cleon.lokerku.awan.helper.repository.UserRepository
import com.cleon.lokerku.awan.helper.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi UserRepository dan RegisterViewModel
        val userRepository = UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        registerViewModel = RegisterViewModel(userRepository)

        // Setup loading dialog
        setupLoadingDialog()

        // Tombol registrasi
        binding.btnRegister.setOnClickListener {
            val email = binding.edLoginEmail.editText?.text.toString()
            val password = binding.edLoginPassword.editText?.text.toString()
            val konfirmasiPassword = binding.edLoginKonfirmasiPassword.editText?.text.toString()
            val username = binding.edLoginUsername.editText?.text.toString()

            // Validasi input
            if (email.isBlank()) {
                binding.edLoginEmail.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }
            if (username.isBlank()) {
                binding.edLoginUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                binding.edLoginPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }
            if (password != konfirmasiPassword) {
                binding.edLoginKonfirmasiPassword.error = "Password tidak cocok"
                return@setOnClickListener
            }

            // Proses registrasi
            loadingDialog.show()
            registerViewModel.register(email, password, username, {
                // Registrasi berhasil
                loadingDialog.dismiss()
                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, { error ->
                // Registrasi gagal
                loadingDialog.dismiss()
                showRegisterFailedDialog(error)
            })
        }

        // Tombol untuk berpindah ke halaman login
        binding.tvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Menampilkan dialog error saat registrasi gagal
    private fun showRegisterFailedDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Registrasi Gagal")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    // Menyiapkan loading dialog
    private fun setupLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.show_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false) // Dialog tidak bisa ditutup sebelum proses selesai
        loadingDialog = builder.create()
    }
}

package com.cleon.lokerku.awan.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cleon.lokerku.awan.databinding.ActivityDashboardBinding
import com.cleon.lokerku.awan.helper.preference.UserPreference
import com.google.firebase.database.*
import com.cleon.lokerku.awan.R
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import com.cleon.lokerku.awan.ui.auth.LoginActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var userPreference: UserPreference
    private lateinit var database: DatabaseReference

    private lateinit var biometricExecutor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        userPreference = UserPreference(this)
        val username = userPreference.getUsername()
        binding.username.text = username
        binding.username.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi Logout")
            builder.setMessage("Apakah Anda yakin ingin keluar?")

            // Tombol Ya
            builder.setPositiveButton("Ya") { dialog, which ->
                // Menghapus data username yang tersimpan di shared preferences
                userPreference.deleteUsername()

                // Membuat intent untuk berpindah ke LoginActivity
                val intent = Intent(this, LoginActivity::class.java)

                // Memulai activity LoginActivity
                startActivity(intent)

                // Menutup aktivitas saat ini (opsional)
                finish()
            }

            // Tombol Tidak
            builder.setNegativeButton("Tidak") { dialog, which ->
                // Menutup dialog tanpa melakukan apa-apa
                dialog.dismiss()
            }

            // Menampilkan dialog
            builder.show()
        }

        getLokerStatusFromFirebase()
        getSignalLevelFormFirebase()
        binding.lokerA.setOnClickListener {
            if (isInternetAvailable(it.context)) {
                checkLokerStatus("loker-a")
            } else {
                showNoInternetDialog(it.context)
            }
        }

        binding.lokerB.setOnClickListener {
            if (isInternetAvailable(it.context)) {
                checkLokerStatus("loker-b")
            } else {
                showNoInternetDialog(it.context)
            }
        }

        binding.lokerC.setOnClickListener {
            if (isInternetAvailable(it.context)) {
                checkLokerStatus("loker-c")
            } else {
                showNoInternetDialog(it.context)
            }
        }

        binding.lokerD.setOnClickListener {
            if (isInternetAvailable(it.context)) {
                checkLokerStatus("loker-d")
            } else {
                showNoInternetDialog(it.context)
            }
        }
        biometricExecutor = ContextCompat.getMainExecutor(this)
    }

    private fun getSignalLevelFormFirebase() {
        val signalRef = database.child("rssi")
        signalRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lokerA = snapshot.value.toString()

                    // Menangani level sinyal berdasarkan nilai RSSI
                    when (lokerA) {
                        "1" -> {
                            // Set gambar sinyal untuk semua jaringan
                            binding.sinyalA.setImageResource(R.drawable.satu)
                            binding.sinyalB.setImageResource(R.drawable.satu)
                            binding.sinyalC.setImageResource(R.drawable.satu)
                            binding.sinyalD.setImageResource(R.drawable.satu)

                            // Set teks untuk status jaringan
                            binding.jaringanA.setText("Sangat Buruk")
                            binding.jaringanB.setText("Sangat Buruk")
                            binding.jaringanC.setText("Sangat Buruk")
                            binding.jaringanD.setText("Sangat Buruk")
                        }
                        "2" -> {
                            binding.sinyalA.setImageResource(R.drawable.dua)
                            binding.sinyalB.setImageResource(R.drawable.dua)
                            binding.sinyalC.setImageResource(R.drawable.dua)
                            binding.sinyalD.setImageResource(R.drawable.dua)

                            binding.jaringanA.setText("Buruk")
                            binding.jaringanB.setText("Buruk")
                            binding.jaringanC.setText("Buruk")
                            binding.jaringanD.setText("Buruk")
                        }
                        "3" -> {
                            binding.sinyalA.setImageResource(R.drawable.tiga)
                            binding.sinyalB.setImageResource(R.drawable.tiga)
                            binding.sinyalC.setImageResource(R.drawable.tiga)
                            binding.sinyalD.setImageResource(R.drawable.tiga)

                            binding.jaringanA.setText("Cukup")
                            binding.jaringanB.setText("Cukup")
                            binding.jaringanC.setText("Cukup")
                            binding.jaringanD.setText("Cukup")
                        }
                        "4" -> {
                            binding.sinyalA.setImageResource(R.drawable.empat)
                            binding.sinyalB.setImageResource(R.drawable.empat)
                            binding.sinyalC.setImageResource(R.drawable.empat)
                            binding.sinyalD.setImageResource(R.drawable.empat)

                            binding.jaringanA.setText("Sangat Bagus")
                            binding.jaringanB.setText("Sangat Bagus")
                            binding.jaringanC.setText("Sangat Bagus")
                            binding.jaringanD.setText("Sangat Bagus")
                        }
                        else -> {
                            binding.sinyalA.setImageResource(R.drawable.satu)
                            binding.sinyalB.setImageResource(R.drawable.satu)
                            binding.sinyalC.setImageResource(R.drawable.satu)
                            binding.sinyalD.setImageResource(R.drawable.satu)

                            binding.jaringanA.setText("Tidak Tersedia")
                            binding.jaringanB.setText("Tidak Tersedia")
                            binding.jaringanC.setText("Tidak Tersedia")
                            binding.jaringanD.setText("Tidak Tersedia")
                        }
                    }

                    // Set tampilan loker berdasarkan data pengguna dan status
                    setLokerImage(binding.imgLokerA, lokerA, snapshot.child("loker-a").child("pemakai").value.toString())

                } else {
                    Toast.makeText(applicationContext, "Data signal tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getLokerStatusFromFirebase() {
        val lokerRef = database.child("loker")

        lokerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lokerA = snapshot.child("loker-a").child("status").value.toString()
                    val lokerB = snapshot.child("loker-b").child("status").value.toString()
                    val lokerC = snapshot.child("loker-c").child("status").value.toString()
                    val lokerD = snapshot.child("loker-d").child("status").value.toString()

                    binding.tvLokerStatusA.text = lokerA
                    binding.tvLokerStatusB.text = lokerB
                    binding.tvLokerStatusC.text = lokerC
                    binding.tvLokerStatusD.text = lokerD

                    setLokerImage(binding.imgLokerA, lokerA, snapshot.child("loker-a").child("pemakai").value.toString())
                    setLokerImage(binding.imgLokerB, lokerB, snapshot.child("loker-b").child("pemakai").value.toString())
                    setLokerImage(binding.imgLokerC, lokerC, snapshot.child("loker-c").child("pemakai").value.toString())
                    setLokerImage(binding.imgLokerD, lokerD, snapshot.child("loker-d").child("pemakai").value.toString())
                } else {
                    Toast.makeText(applicationContext, "Data loker tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLokerImage(imageView: ImageView, status: String, pemakai: String) {
        val currentUser = userPreference.getUsername()
        when (status) {
            "kosong" -> imageView.setImageResource(R.drawable.lokerkosong)
            "terisi" -> {
                if (pemakai == currentUser) {
                    imageView.setImageResource(R.drawable.lokermu)
                } else {
                    imageView.setImageResource(R.drawable.lokerterisi)
                }
            }
            "booking" -> imageView.setImageResource(R.drawable.lokerbooking)
            else -> imageView.setImageResource(R.drawable.lokerkosong)
        }
    }

    private fun checkLokerStatus(lokerId: String) {
        val lokerRef = FirebaseDatabase.getInstance().getReference("loker")

        lokerRef.get().addOnSuccessListener { snapshot ->
            var usedLokerId: String? = null
            var bookedLokerId: String? = null

            for (child in snapshot.children) {
                val pemakai = child.child("pemakai").value.toString()
                val lokerStatus = child.child("status").value.toString()
                val currentLokerId = child.key.toString()

                if (pemakai == userPreference.getUsername()) {
                    if (lokerStatus == "terisi") {
                        usedLokerId = currentLokerId
                    } else if (lokerStatus == "booking") {
                        bookedLokerId = currentLokerId
                    }
                }
            }

            when {
                usedLokerId != null && usedLokerId != lokerId -> {
                    showSingleLokerDialog(usedLokerId)
                }
                bookedLokerId != null && bookedLokerId != lokerId -> {
                    showSingleBookingDialog(bookedLokerId)
                }
                else -> {
                    val loker = snapshot.child(lokerId)
                    val status = loker.child("status").value.toString()
                    val pemakai = loker.child("pemakai").value.toString()
                    showLokerStatusDialog(pemakai, status, lokerId)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Gagal mengambil data loker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLokerStatusDialog(dbUsername: String, status: String, lokerId: String) {
        val builder = AlertDialog.Builder(this)

        when (status) {
            "kosong" -> {
                builder.setMessage("Loker ini kosong. Apakah Anda ingin memakai atau pesan?")
                    .setPositiveButton("Memakai") { dialog, id ->
                        handleTransisiKosongKeTerisi(lokerId)
                    }
                    .setNegativeButton("Pesan") { dialog, id ->
                        handleTransisiKosongKeBooking(lokerId)
                    }
            }

            "terisi" -> {
                if (dbUsername != userPreference.getUsername()){
                    builder.setMessage("Loker ini sudah terisi.")
                        .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
                }else{
                    builder.setMessage("Loker ini sedang Anda pakai, ingin mengambil barang?")
                        .setPositiveButton("Iya") { dialog, id ->
                            handleTransisiTerisiKeKosong(lokerId)
                        }
                        .setNeutralButton("Saya ingin membuka sementara") { dialog, id ->
                            handleTransisiTerisiKeTerisi(lokerId)
                        }
                        .setNegativeButton("Cancel") { dialog, id -> dialog.dismiss() }
                }
            }
            "booking" -> {
                if (dbUsername != userPreference.getUsername()){
                    builder.setMessage("Loker ini sedang dipesan.")
                        .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
                        .setNegativeButton("Cancel") { dialog, id -> dialog.dismiss() }
                }else{
                    builder.setMessage("Loker ini sudah anda pesan, apakah ingin menggunakan sekarang?")
                        .setPositiveButton("Iya") { dialog, id -> dialog.dismiss()
                            // Jika pengguna memilih "Iya", lanjutkan ke transisi booking ke terisi
                            handleTransisiBookingKeTerisi(lokerId)
                        }
                        .setNeutralButton("Batalkan Booking") { dialog, id ->dialog.dismiss()
                            handleTransisiBookingToKosong(lokerId)
                        }
                        .setNegativeButton("Tutup") { dialog, id -> dialog.dismiss() }
                }
            }
            else -> {
                builder.setMessage("Status loker tidak diketahui.")
                    .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
            }
        }

        val alert = builder.create()
        alert.show()
    }


    private fun verifyBiometric(onSuccess: (Boolean) -> Unit) {
        val biometricPrompt = BiometricPrompt(this, biometricExecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(true)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onSuccess(false)
            }
        })

        val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Sidik Jari")
            .setSubtitle("Gunakan sidik jari untuk memverifikasi identitas Anda")
            .setNegativeButtonText("Batal")
            .build()

        biometricPrompt.authenticate(biometricPromptInfo)
    }
    private fun handleTransisiKosongKeTerisi(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi terisi
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "terisi", true)
            }
        }
    }

    private fun handleTransisiKosongKeBooking(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi booking
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "booking")
            }
        }
    }

    private fun handleTransisiBookingKeTerisi(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi terisi
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "terisi", true)
            }
        }
    }

    private fun handleTransisiTerisiKeTerisi(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi terisi sementara
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "terisi", true)
            }
        }
    }

    private fun handleTransisiTerisiKeKosong(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi kosong
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "kosong", true)
            }
        }
    }
    private fun handleTransisiBookingToKosong(lokerId: String) {
        // Verifikasi sidik jari dan perbarui status loker menjadi terisi sementara
        verifyBiometric { success ->
            if (success) {
                updateLokerStatus(lokerId, "kosong", false)
            }
        }
    }

    private fun updateLokerStatus(lokerId: String, status: String, action: Boolean = false) {
        val lokerRef = database.child("loker").child(lokerId)
        val user = userPreference.getUsername()

        // Mengubah pemakai menjadi "none" jika status loker adalah "kosong"
        if (status == "kosong") {
            lokerRef.child("pemakai").setValue("none")
        } else {
            lokerRef.child("pemakai").setValue(user)
        }

        // Set status loker
        lokerRef.child("status").setValue(status)

        // Update action hanya jika perubahan status sesuai kriteria
        if (status == "terisi" || status == "kosong" || status == "booking") {
            lokerRef.child("action").setValue(action)
        }

        Toast.makeText(applicationContext, "Status loker berhasil diperbarui!", Toast.LENGTH_SHORT).show()
    }

    private fun showSingleLokerDialog(usedLokerId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Anda sudah menggunakan loker $usedLokerId. Hanya satu loker diperbolehkan per pengguna.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }

    private fun showSingleBookingDialog(bookedLokerId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Anda sudah mem-booking loker $bookedLokerId. Hanya satu loker yang dapat di-booking.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Fungsi untuk menampilkan dialog
    fun showNoInternetDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Koneksi Internet")
            .setMessage("Anda tidak terhubung ke internet.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

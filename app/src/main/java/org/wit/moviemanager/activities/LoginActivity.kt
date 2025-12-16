package org.wit.moviemanager.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import org.wit.moviemanager.databinding.ActivityLoginBinding
import timber.log.Timber.i

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if already logged in
        if (auth.currentUser != null) {
            startMovieList()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(it, "Please enter email and password", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    i("Login successful")
                    startMovieList()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(binding.root, "Login failed: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
        }

        binding.btnSignup.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(it, "Please enter email and password", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    i("Signup successful")
                    startMovieList()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(binding.root, "Signup failed: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun startMovieList() {
        val intent = Intent(this, MovieListActivity::class.java)
        startActivity(intent)
        finish()
    }
}
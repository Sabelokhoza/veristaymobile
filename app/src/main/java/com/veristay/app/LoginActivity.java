package com.veristay.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.veristay.app.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        api     = RetrofitClient.getInstance(session).getApiService();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // ✅ API call goes here inside attemptLogin()
        api.login(new Models.LoginRequest(email, password))
                .enqueue(new Callback<Models.ApiResponse<Models.LoginResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Models.LoginResponse>> call,
                            @NonNull Response<Models.ApiResponse<Models.LoginResponse>> response) {

                        setLoading(false);

                        android.util.Log.d("VeriStay", "Response code: " + response.code());

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success) {

                            Models.LoginResponse data = response.body().data;

                            // Log what we got
                            android.util.Log.d("VeriStay", "Token: "    + data.token);
                            android.util.Log.d("VeriStay", "UserId: "   + data.userId);
                            android.util.Log.d("VeriStay", "UserName: " + data.userName);
                            android.util.Log.d("VeriStay", "Email: "    + data.email);

                            if (data.userId == null) {
                                Toast.makeText(LoginActivity.this,
                                        "Login error: id is null",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Save session
                            session.saveSession(
                                    data.token,
                                    data.userId,
                                    data.email,
                                    data.userName,
                                    "Student"       // default role
                            );

                            // Re-init Retrofit with new token
                            RetrofitClient.reset();

                            startActivity(new Intent(LoginActivity.this,
                                    MainActivity.class));
                            finish();

                        } else {
                            try {
                                String error = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "Unknown error";
                                android.util.Log.e("VeriStay", "Login failed: " + error);
                            } catch (Exception e) {
                                android.util.Log.e("VeriStay", "Error reading body");
                            }
                            Toast.makeText(LoginActivity.this,
                                    "Invalid email or password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Models.LoginResponse>> call,
                            @NonNull Throwable t) {
                        setLoading(false);
                        android.util.Log.e("VeriStay", "Network error: " + t.getMessage());
                        Toast.makeText(LoginActivity.this,
                                "Connection error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setText(loading ? "" : "Sign In");
    }
}
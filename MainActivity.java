package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
TextView name,id,reg,spot,hours,total;
Button confirm;
TextView message;
List<String> spots=new ArrayList<>();
    private String KEY_NAME = "somekeyname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = findViewById(R.id.name);
        id = findViewById(R.id.stdid);
        reg = findViewById(R.id.registration);
        spot = findViewById(R.id.spot);
        hours = findViewById(R.id.hours);
        total = findViewById(R.id.total);
        confirm = findViewById(R.id.btn);
        message = findViewById(R.id.message);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        if (!fingerprintManager.isHardwareDetected()) {
            Log.e("Hardware", "Finger print hardware not detected");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Permission", "Fingerprint permission rejected");
            return;
        }
        if (!keyguardManager.isKeyguardSecure()) {
            Log.e("Keyguard", "Keyguard not enabled");
        }
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            Log.e("KeyStore", e.getMessage());
            return;
        }
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (Exception e) {
            Log.e("KeyGenerator", e.getMessage());
            return;
        }
        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            Log.e("Generating Keys", e.getMessage());
            return;
        }
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES +
                    "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (Exception e) {
            Log.e("Cipher", e.getMessage());
            return;
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            Log.e("Secret key", e.getMessage());
            return;
        }
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new AuthenticationHandler(this), null);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().isEmpty() || reg.getText().toString().isEmpty() || id.getText().toString().isEmpty() || spot.getText().toString().isEmpty() || hours.getText().toString().isEmpty()) {
                        message.setTextColor(Color.RED);
                        message.setText("Fill in everything");
                    } else {
                        if (spots.contains(spot.getText().toString())) {
                            message.setTextColor(Color.RED);
                            message.setText("Spot already token");

                        } else {
                            spots.add(spot.getText().toString());
                            message.setTextColor(Color.GREEN);
                            message.setText("Parking confirmed");
                        }
                        total.setText(Double.toString(calculateTotal()));
                    }
                }
            });
        }
private double calculateTotal(){
        double i=Double.parseDouble(hours.getText().toString())*3.5;
        return i;
}
}

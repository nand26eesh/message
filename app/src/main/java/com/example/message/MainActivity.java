package com.example.message;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private static final String CHANNEL_ID = "sms_channel";
    private static final int NOTIFICATION_ID = 1;

    private TextView txtMessage;
    private SMSReceiver smsReceiver;
    private EditText edtRecipient;
    private EditText edtMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMessage = findViewById(R.id.txtMessage);
        edtRecipient = findViewById(R.id.editRecipient);
        edtMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        // Check for SMS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted
                registerSmsReceiver();
            }
        } else {
            // Permission granted by default on older devices
            registerSmsReceiver();
        }

        // Set click listener for send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = edtRecipient.getText().toString();
                String message = edtMessage.getText().toString();
                sendSms(recipient, message);
            }
        });
    }

    private void registerSmsReceiver() {
        // Create an instance of the SMSReceiver class
        smsReceiver = new SMSReceiver();

        // Set the received message in the TextView
        smsReceiver.setOnMessageReceivedListener(new SMSReceiver.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String messageContent, String senderNumber) {
                txtMessage.setText("Message: " + messageContent + ", From: " + senderNumber);
                showNotification(messageContent, senderNumber);
            }
        });

        // Register the SMSReceiver dynamically
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                registerReceiver(smsReceiver, SMSReceiver.getIntentFilter());
            } else {
                // Handle the case when permission is not granted
                Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            // Handle the security exception gracefully
            Toast.makeText(this, "SecurityException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String recipient, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(recipient, null, message, null, null);
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }


    private void showNotification(String messageContent, String senderNumber) {
        String notificationTitle = "New SMS Received";
        String notificationText = "Message: " + messageContent + ", From: " + senderNumber;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Create a notification channel (if necessary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Toast.makeText(this, "SecurityException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sending the SMS
                String recipient = edtRecipient.getText().toString();
                String message = edtMessage.getText().toString();
                sendSms(recipient, message);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the SMSReceiver when the activity is destroyed
        unregisterReceiver(smsReceiver);
    }
}

package com.example.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    private OnMessageReceivedListener messageReceivedListener;

    public interface OnMessageReceivedListener {
        void onMessageReceived(String messageContent, String senderNumber);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String messageContent = smsMessage.getMessageBody();
                        String senderNumber = smsMessage.getOriginatingAddress();

                        // Display a toast notification
                        Toast.makeText(context, "Message: " + messageContent + ", From: " + senderNumber, Toast.LENGTH_LONG).show();

                        if (messageReceivedListener != null) {
                            messageReceivedListener.onMessageReceived(messageContent, senderNumber);
                        }
                    }
                }
            }
        }
    }





    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        return intentFilter;
    }
}

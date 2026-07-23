package com.priyanshu.spend_buddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("SMS_TEST", "Receiver Started");

        Bundle bundle = intent.getExtras();

        if (bundle != null) {

            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus == null) return;

            String format = bundle.getString("format");

            for (Object pdu : pdus) {

                SmsMessage sms;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    sms = SmsMessage.createFromPdu((byte[]) pdu);
                }

                String sender = sms.getDisplayOriginatingAddress();
                String message = sms.getMessageBody();

                // ================= TRANSACTION ID =================

                String transactionId = "";

                Pattern txnPattern = Pattern.compile(
                        "(?:UPI Ref(?: No)?|Ref(?: No)?|RRN|Txn ID|Transaction ID)[:\\s]*([A-Za-z0-9]+)",
                        Pattern.CASE_INSENSITIVE
                );

                Matcher txnMatcher = txnPattern.matcher(message);

                if (txnMatcher.find()) {

                    transactionId = txnMatcher.group(1);

                }

                Log.e("SMS_TEST","TXN : "+transactionId);

                // ================= BANK NAME =================

                String bank = "Unknown";

                if(sender.toLowerCase().contains("sbi"))
                    bank="SBI";

                else if(sender.toLowerCase().contains("hdfc"))
                    bank="HDFC";

                else if(sender.toLowerCase().contains("icici"))
                    bank="ICICI";

                else if(sender.toLowerCase().contains("axis"))
                    bank="AXIS";

                else if(sender.toLowerCase().contains("kotak"))
                    bank="KOTAK";

                else if(sender.toLowerCase().contains("indian"))
                    bank="Indian Bank";

                Log.e("SMS_TEST","BANK : "+bank);

                // ================= MERCHANT EXTRACTION =================

                String merchant = "Unknown";

                Pattern merchantPattern = Pattern.compile(
                        "(?:at|to|paid to|from)\\s+([A-Za-z0-9&\\-\\. ]{2,40})",
                        Pattern.CASE_INSENSITIVE);

                Matcher merchantMatcher = merchantPattern.matcher(message);

                if (merchantMatcher.find()) {

                    merchant = merchantMatcher.group(1).trim();

                    merchant = merchant.replaceAll(
                            "(?i)(using|via|txn|upi|ref|avl|bal|card|ending|on).*",
                            "");

                    merchant = merchant.trim();
                }

                Log.e("SMS_TEST", "MERCHANT : " + merchant);

                Log.e("SMS_TEST", "MESSAGE: " + message);

                String lower = message.toLowerCase();

                // 🔥 Detect expense SMS
                // ================= IGNORE CREDIT SMS =================

                boolean isExpense =
                        lower.contains("debited")
                                || lower.contains("spent")
                                || lower.contains("purchase")
                                || lower.contains("withdrawn")
                                || lower.contains("paid")
                                || lower.contains("sent");

                boolean isCredit =
                        lower.contains("credited")
                                || lower.contains("received")
                                || lower.contains("refund")
                                || lower.contains("cashback")
                                || lower.contains("salary")
                                || lower.contains("interest")
                                || lower.contains("deposit");

                if (isCredit) {

                    Log.e("SMS_TEST", "CREDIT SMS IGNORED");
                    return;
                }

                if (isExpense) {

                    Log.e("SMS_TEST", "EXPENSE DETECTED");

                    // 🔥 Extract amount
                    Pattern pattern = Pattern.compile(
                            "(?:₹|rs\\.?|inr)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
                            Pattern.CASE_INSENSITIVE
                    );

                    Matcher matcher = pattern.matcher(message);

                    if (matcher.find()) {

                        String amountStr = matcher.group(1);

                        amountStr = amountStr.replace(",", "");
                        double amount = Double.parseDouble(amountStr);

                        Log.e("SMS_TEST", "AMOUNT: " + amount);

                        // 🔥 Detect category
                        String category = MerchantCategory.detectCategory(message);

                        Log.e("SMS_TEST",
                                "MERCHANT : "
                                        + MerchantCategory.detectMerchant(message));

                        // 🔥 Check user login
                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

                            Log.e("SMS_TEST", "USER NOT LOGGED IN");
                            return;
                        }

                        String userId = FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getUid();

                        // 🔥 Create expense object
                        Map<String, Object> expense = new HashMap<>();
                        expense.put("smsText", message);
                        expense.put("amount", amount);
                        expense.put("category", category);
                        expense.put("title", MerchantCategory.detectMerchant(message));
                        expense.put("date", System.currentTimeMillis());
                        expense.put("transactionId", transactionId);
                        expense.put("bank", bank);
                        expense.put("sender", sender);


                        // 🔥 Save to Firebase
                        db.collection("users")
                                .document(userId)
                                .collection("expenses")
                                .whereEqualTo("smsText", message)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {

                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        Log.e("SMS_TEST", "DUPLICATE SMS IGNORED");
                                        return;
                                    }

                                    db.collection("users")
                                            .document(userId)
                                            .collection("expenses")
                                            .add(expense)
                                            .addOnSuccessListener(documentReference -> {

                                                Log.e("SMS_TEST",
                                                        "EXPENSE SAVED SUCCESSFULLY");

                                            })
                                            .addOnFailureListener(e -> {

                                                Log.e("SMS_TEST",
                                                        "FIREBASE ERROR: " + e.getMessage());

                                            });

                                });
                    }
                }
            }
        }
    }
}
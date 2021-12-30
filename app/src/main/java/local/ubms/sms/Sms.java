package local.ubms.sms;

import android.telephony.SmsManager;
import android.text.format.DateFormat;

import java.sql.Date;
import java.util.ArrayList;

public class Sms {

    public Sms dueSms(String id,String contactNumber,String customerName, String dueDate,String amount,String meter,String from,String to) {

        try {
            String message = "Hello, "+ customerName+". You need to pay your bill with an amount of ₱"+ amount +" before " + dueDate+"."+ System.getProperty("line.separator") +"Meter no: "+meter+ System.getProperty("line.separator")+"Period from: "+from+ System.getProperty("line.separator")+"Period to: "+to;

            String mymessage = message+ System.getProperty("line.separator") + System.getProperty("line.separator") +"-UBMS";
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> msgArray = smsManager.divideMessage(mymessage);

            try {
                smsManager.sendMultipartTextMessage(contactNumber, null,msgArray, null, null);
                MainActivity.logDetails("[Bill] Sms sent to ID#" + id +" " + customerName +"("+contactNumber+")");
                MainActivity.smsCount +=1;
                MainActivity.activityCount.setText(String.valueOf(MainActivity.smsCount));
            }catch (Exception exception){
                MainActivity.logDetails("[Send failed to "+customerName+"] "+exception.getMessage().toString());
            }



        }catch (Exception e){
            MainActivity.logDetails(e.getMessage().toString());
        }



        return null;
    }

    public Sms paidSms(String contactNumber,String customerName, String billNumber, String amount,String date,String paymentMethod) {
        try {
            String message = "You have successfully paid your bill #"+billNumber+"."+ System.getProperty("line.separator") +"Customer: "+customerName+ System.getProperty("line.separator")+"Amount: ₱"+amount+ System.getProperty("line.separator")+"Payment method: "+paymentMethod+ System.getProperty("line.separator")+"Date paid: "+date;

            String mymessage = message+ System.getProperty("line.separator") + System.getProperty("line.separator") +"-UBMS";
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> msgArray = smsManager.divideMessage(mymessage);

            try {
                smsManager.sendMultipartTextMessage(contactNumber, null,msgArray, null, null);
                MainActivity.logDetails("[Paid] Sms sent to " + customerName +"("+contactNumber+")");
                MainActivity.smsCount +=1;
                MainActivity.activityCount.setText(String.valueOf(MainActivity.smsCount));
            }catch (Exception exception){
                MainActivity.logDetails("[Send failed to "+customerName+"] "+exception.getMessage().toString());
            }



        }catch (Exception e){
            MainActivity.logDetails(e.getMessage().toString());
        }



        return null;
    }

    public Sms registerSms(String contactNumber,String customerName, String accNumber, String address,String date) {
        try {
            String message = "Congratulations, "+ customerName+". You are successfully registered to UBMS (Utility Billing Management System). Use your account number to log in our app."+ System.getProperty("line.separator") +"Account number: "+accNumber+ System.getProperty("line.separator")+"Address: "+address+ System.getProperty("line.separator")+"Date registered: "+date;

            String mymessage = message+ System.getProperty("line.separator") + System.getProperty("line.separator") +"-UBMS";
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> msgArray = smsManager.divideMessage(mymessage);

            try {
                smsManager.sendMultipartTextMessage(contactNumber, null,msgArray, null, null);
                MainActivity.logDetails("[Registered] Sms sent to " + customerName +"("+contactNumber+")");
                MainActivity.smsCount +=1;
                MainActivity.activityCount.setText(String.valueOf(MainActivity.smsCount));
            }catch (Exception exception){
                MainActivity.logDetails("[Send failed to "+customerName+"] "+exception.getMessage().toString());
            }



        }catch (Exception e){
            MainActivity.logDetails(e.getMessage().toString());
        }



        return null;
    }

}

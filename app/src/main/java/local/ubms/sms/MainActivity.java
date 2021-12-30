package local.ubms.sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    static Button activityRun;
    static TextView activityCount;
    static TextView activityElapsedTime;

    static TextView activityStatusBills,activityStatusPayment,activityStatusCustomer;
    static CardView statusCardBills,statusCardPayment,statusCardCustomer;
    static CountDownTimer runInBackground,runInBackgroundBill,runInBackgroundCustomerAndPayment;


    static ArrayList<String> logList=new ArrayList<String>();
    static ArrayAdapter<String> adapter;
    static ListView logsListView;
    static Context context;

    private static final int PERMISSION_SEND_SMS = 123;

    static DatabaseConnection databaseConnection;
    static int smsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        requestSmsPermission();

        ImageView background = findViewById(R.id.background);
        Glide.with(this).load(R.drawable.background).centerCrop().into(background);

        databaseConnection = new DatabaseConnection();

        activityRun = findViewById(R.id.activityRun);
        activityCount = findViewById(R.id.activityCount);
        activityElapsedTime = findViewById(R.id.activityElapsedTime);

        activityStatusBills = findViewById(R.id.activityStatusBills);
        activityStatusPayment = findViewById(R.id.activityStatusPayment);
        activityStatusCustomer = findViewById(R.id.activityStatusCustomer);
        statusCardBills = findViewById(R.id.statusCardBills);
        statusCardPayment = findViewById(R.id.statusCardPayment);
        statusCardCustomer = findViewById(R.id.statusCardCustomer);

        logsListView = findViewById(R.id.logsListView);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                logList);

        logsListView.setAdapter(adapter);

        TinyDB tinydb = new TinyDB(context);




        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        activityRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (activityRun.getText().toString().matches("Run")){

                    runServer();

                }else{
                    stopServer();
                }

            }
        });



    }

    static void logDetails(String log){

        logList.add(DateFormat.format("MM/dd/yyyy hh:mm:ss aa",new Date())+": "+log);
        adapter.notifyDataSetChanged();
        logsListView.setSelection(logsListView.getAdapter().getCount()-1);


    }


    static void stopServer(){
        if (runInBackground != null){
            runInBackground.cancel();
            logDetails("Server stopped");
        }
        if (runInBackgroundBill != null){
            runInBackgroundBill.cancel();

        }

        if (runInBackgroundCustomerAndPayment != null){
            runInBackgroundCustomerAndPayment.cancel();

        }
        smsCount = 0;
        activityRun.setText("Run");
        activityRun.setBackgroundColor(ContextCompat.getColor(context,R.color.color7));
        activityStatusBills.setText("Not Connected");
        activityElapsedTime.setVisibility(GONE);
        statusCardBills.setCardBackgroundColor(ContextCompat.getColor(context,R.color.red));
        statusCardPayment.setVisibility(GONE);
        statusCardCustomer.setVisibility(GONE);
        databaseConnection.restartConnected();
    }


    public void runServer(){

        if (runInBackground != null){
            runInBackground.cancel();
        }

        if (runInBackgroundBill != null){
            runInBackgroundBill.cancel();
        }
        if (runInBackgroundCustomerAndPayment != null){
            runInBackgroundCustomerAndPayment.cancel();
        }
        logDetails("Server started");
        activityRun.setText("Stop");
        activityStatusBills.setText("Connecting...");
        statusCardBills.setCardBackgroundColor(ContextCompat.getColor(this,R.color.orange));
        logDetails("Establishing connection to database [https://utilitybilling.000webhostapp.com]");
        runTime(new Date());
        BillsListener billsListener = new BillsListener().runListener(context,false);

     //




    }




    public void runTime(Date startTime){
        //7200000
        runInBackgroundBill = new CountDownTimer(Long.MAX_VALUE,7200000 ) {

            @Override
            public void onFinish() {

            }


            @Override
            public void onTick(long millisUntilFinished) {
                if (DatabaseConnection.allHasConnection()){
                    BillsListener billsListener = new BillsListener().runListener(context,true);
                }
            }
        }.start();


        runInBackgroundCustomerAndPayment = new CountDownTimer(Long.MAX_VALUE,20000 ) {

            @Override
            public void onFinish() {

            }


            @Override
            public void onTick(long millisUntilFinished) {
                PaymentListener paymentListener = new PaymentListener().runListener(context);
                Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        }.start();


         activityElapsedTime.setVisibility(VISIBLE);
         runInBackground = new CountDownTimer(Long.MAX_VALUE,1000 ) {
            StringBuilder time = new StringBuilder();
            @Override
            public void onFinish() {



            }


            @Override
            public void onTick(long millisUntilFinished) {

                try
                {

                    Date past = startTime;
                    Date now = new Date();
                    long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                    long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                    long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                    long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());



                    if(seconds<60)
                    {
                        activityElapsedTime.setText("Elapsed time: "+seconds + " seconds");
                        //System.out.println(seconds+" seconds ago");
                    }
                    else if(minutes<=1)
                    {
                        activityElapsedTime.setText("Elapsed time: "+minutes+" minute");
                        // System.out.println(minutes+" minutes ago");
                    }

                    else if(minutes<60)
                    {
                        activityElapsedTime.setText("Elapsed time: "+minutes+" minutes");
                        // System.out.println(minutes+" minutes ago");
                    }
                    else if(hours<=1)
                    {
                        activityElapsedTime.setText("Elapsed time: "+" hour");
                        //  System.out.println(hours+" hours ago");
                    }
                    else if(hours<24)
                    {
                        activityElapsedTime.setText("Elapsed time: "+hours+" hours");
                        //  System.out.println(hours+" hours ago");
                    }
                    else
                    {
                        activityElapsedTime.setText("Elapsed time: "+days+" day(s)");
                        //  System.out.println(days+" days ago");
                    }
                }
                catch (Exception j){

                    logDetails(j.getMessage().toLowerCase());
                }




            }
        }.start();

    }



    public static void setWindowFlag(MainActivity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        } else {


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                  //  sendSms(phone, message);
                } else {
                    super.onBackPressed();
                    // permission denied
                }
                return;
            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (runInBackground != null)
            runInBackground.cancel();
    }

}
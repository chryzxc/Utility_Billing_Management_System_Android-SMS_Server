package local.ubms.sms;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PaymentListener {
    MainActivity mainActivity;
    DatabaseConnection databaseConnection;


    public PaymentListener runListener(Context context){
        mainActivity = new MainActivity();
        databaseConnection = new DatabaseConnection();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="https://utilitybilling.000webhostapp.com/android/sms_payment.php";

        MainActivity.statusCardPayment.setVisibility(View.VISIBLE);
        MainActivity.activityStatusPayment.setText("Connecting..");
        MainActivity.statusCardPayment.setCardBackgroundColor(ContextCompat.getColor(context,R.color.orange));

        MainActivity.logDetails("Attempting to connect [Payment]");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        MainActivity.logDetails("Connected to database [Payment]");
                        MainActivity.activityStatusPayment.setText("Connected [Payment]");
                        MainActivity.statusCardPayment.setCardBackgroundColor(ContextCompat.getColor(context,R.color.green));

                        databaseConnection.dbIsConnected(context,true);

                        try {
                            JSONArray array = new JSONArray(response);
                            JSONObject obj;
                            TinyDB tinydb = new TinyDB(context);
                            List<String> fromList = new ArrayList<>();
                            fromList =  tinydb.getListString("paymentList");

                            List<String> list= new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                obj = array.getJSONObject(i);

                                list.add(obj.getString("id"));

                                String  billNumber,amount,datePaid,paymentMethod;

                                billNumber=obj.getString("bill_id");
                                amount=obj.getString("amount_tendered");
                                datePaid=obj.getString("date_paid");
                                paymentMethod=obj.getString("payment_method");


                                if(!fromList.contains(obj.getString("id"))){

                                    RequestQueue queue = Volley.newRequestQueue(context);
                                    String url ="https://utilitybilling.000webhostapp.com/android/fetch_contact.php";

                                    String cus_id = obj.getString("customer_id");
                                    StringRequest fetchRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    String contactNumber = null,customerName = null,accountNumber = null,address= null,date= null;

                                                    try {
                                                        JSONArray array = new JSONArray(response);
                                                        JSONObject obj;
                                                        for (int i = 0; i < array.length(); i++) {
                                                            obj = array.getJSONObject(i);
                                                            contactNumber= obj.getString("contact");
                                                            customerName=(obj.getString("fname") + " " + obj.getString("lname"));
                                                            accountNumber=obj.getString("acc_number");
                                                            address= obj.getString("addr");
                                                            date= obj.getString("date_created");
                                                        }
                                                        Sms sms = new Sms().paidSms(
                                                                contactNumber,
                                                                customerName,
                                                                billNumber,
                                                                amount,
                                                                datePaid,
                                                                paymentMethod);


                                                    }catch (Exception e){
                                                        MainActivity.logDetails("[Error] " +e.getMessage());

                                                    }

                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            MainActivity.logDetails("[Error] Failed to fetch customer name (SMS not sent)");
                                        }
                                    }){
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            HashMap<String,String> param = new HashMap<>();

                                            param.put("customer_id",cus_id);
                                            return param;

                                        }
                                    };
                                    queue.add(fetchRequest);





                                }



                                tinydb.putListString("paymentList", (ArrayList<String>) list);



                            }


                        }catch (Exception e){
                            MainActivity.logDetails("[Failed] "+e.getMessage().toString());

                        }

                        CustomerListener customerListener = new CustomerListener().runListener(context);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                MainActivity.logDetails("[Error] Failed to connect (Payment)");
                mainActivity.stopServer();
                MainActivity.logDetails("Please run again");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                return param;

            }
        };
        queue.add(stringRequest);

        return null;
    }



}

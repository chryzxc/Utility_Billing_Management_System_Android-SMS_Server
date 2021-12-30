package local.ubms.sms;

import android.content.Context;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

 class BillsListener {
    MainActivity mainActivity;
    DatabaseConnection databaseConnection;
    static JSONArray array;
    static String receivedResponse;


    public BillsListener runListener(Context context,Boolean billOnly){
        mainActivity = new MainActivity();
        databaseConnection = new DatabaseConnection();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="https://utilitybilling.000webhostapp.com/android/sms_bills.php";
        if (billOnly== true){
            MainActivity.logDetails("Fetching updates [Bills]");
        }else{
            MainActivity.logDetails("Attempting to connect [Bills]");
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        receivedResponse = response;

                        if (billOnly == true){
                            MainActivity.logDetails("[Success] Data fetched");
                        }else{
                            MainActivity.logDetails("Connected to database [Bills]");
                        }
                        MainActivity.activityStatusBills.setText("Connected [Bills]");
                        MainActivity.statusCardBills.setCardBackgroundColor(ContextCompat.getColor(context,R.color.green));

                        databaseConnection.dbIsConnected(context,true);

                        if (billOnly == false){
                            PaymentListener paymentListener = new PaymentListener().runListener(context);

                        }else{
                            BillsListener.runProcess(context);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MainActivity.logDetails("[Error] Failed to connect (Bills)");
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

    static BillsListener runProcess(Context context){

        try {
            array = new JSONArray(receivedResponse);
            JSONObject obj;
            for (int i = 0; i < array.length(); i++) {
                obj = array.getJSONObject(i);
                if (obj.getString("status").matches("Unpaid")){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date strDate = sdf.parse(obj.getString("due_date"));

                    String id,dueDate ,amount,meter,from,to;
                    id=obj.getString("id");
                    dueDate=obj.getString("due_date");
                    amount=obj.getString("bill_amount");
                    meter=obj.getString("meter_no");
                    from=obj.getString("period_from");
                    to=obj.getString("period_to");

                    long days= TimeUnit.MILLISECONDS.toDays(new Date().getTime() - strDate.getTime());

                    if(strDate.after(new Date()) && days<7){

                        RequestQueue queue = Volley.newRequestQueue(context);
                        String url ="https://utilitybilling.000webhostapp.com/android/fetch_contact.php";

                        String cus_id = obj.getString("customer_id");

                        StringRequest fetchRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        String contactNumber = null,customerName = null;

                                        try {
                                            JSONArray array = new JSONArray(response);
                                            JSONObject obj;
                                            for (int i = 0; i < array.length(); i++) {
                                                obj = array.getJSONObject(i);
                                                contactNumber= obj.getString("contact");
                                                customerName=(obj.getString("fname") + " " + obj.getString("lname"));

                                            }
                                            Sms sms = new Sms().dueSms(id,
                                                    contactNumber,
                                                    customerName,
                                                    dueDate,
                                                    amount,
                                                    meter,
                                                    from,
                                                    to);


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

                    }else{
                     //   Toast.makeText(context, "Hirayo pa ", Toast.LENGTH_SHORT).show();
                    }

                }

            }


        }catch (Exception e){
            MainActivity.logDetails("[Failed] "+e.getMessage().toString());

        }

        return null;
    }




}

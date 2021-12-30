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

public class CustomerListener {

    MainActivity mainActivity;
    DatabaseConnection databaseConnection;


    public CustomerListener runListener(Context context){
        mainActivity = new MainActivity();
        databaseConnection = new DatabaseConnection();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="https://utilitybilling.000webhostapp.com/android/sms_customer.php";

        MainActivity.statusCardCustomer.setVisibility(View.VISIBLE);
        MainActivity.activityStatusCustomer.setText("Connecting..");
        MainActivity.statusCardCustomer.setCardBackgroundColor(ContextCompat.getColor(context,R.color.orange));


        MainActivity.logDetails("Attempting to connect [Customer]");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        MainActivity.logDetails("Connected to database [Customer]");
                        MainActivity.activityStatusCustomer.setText("Connected [Customer]");
                        MainActivity.statusCardCustomer.setCardBackgroundColor(ContextCompat.getColor(context,R.color.green));

                        databaseConnection.dbIsConnected(context,true);

                        try {
                            JSONArray array = new JSONArray(response);
                            JSONObject obj;
                            TinyDB tinydb = new TinyDB(context);
                            List<String> fromList = new ArrayList<>();
                            fromList =  tinydb.getListString("registerList");

                            List<String> list= new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                obj = array.getJSONObject(i);

                                list.add(obj.getString("id"));

                                if(!fromList.contains(obj.getString("id"))){

                                    Toast.makeText(context, obj.getString("contact") +
                                            (obj.getString("fname") + " " + obj.getString("lname"))+
                                            obj.getString("acc_number")+
                                            obj.getString("addr")+
                                            obj.getString("date_created"), Toast.LENGTH_LONG).show();



                                    Sms sms = new Sms().registerSms(
                                            obj.getString("contact"),
                                            (obj.getString("fname") + " " + obj.getString("lname")),
                                            obj.getString("acc_number"),
                                            obj.getString("addr"),
                                            obj.getString("date_created"));

                                }

                            }


                                tinydb.putListString("registerList", (ArrayList<String>) list);






                        }catch (Exception e){
                            MainActivity.logDetails("[Failed] "+e.getMessage().toString());

                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MainActivity.logDetails("[Error] Failed to connect (Customer)");
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

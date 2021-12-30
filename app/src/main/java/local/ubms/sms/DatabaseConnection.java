package local.ubms.sms;

import android.content.Context;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class DatabaseConnection {

    static int numOfDatabase = 0;

    public void dbIsConnected(Context context , Boolean connected){
        if (connected == true){
            numOfDatabase +=1;
        }

        if (numOfDatabase == 3){
           BillsListener.runProcess(context);
        }
    }

    public void restartConnected(){
        numOfDatabase = 0;

    }


    static Boolean allHasConnection(){
        if (numOfDatabase == 3){
           return true;
        }

        return false;
    }


}

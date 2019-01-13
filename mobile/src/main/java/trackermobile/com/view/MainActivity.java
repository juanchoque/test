package trackermobile.com.view;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import java.util.ArrayList;

import trackermobile.com.miranda_client.R;
import trackermobile.com.schedule.SocketJobService;
import trackermobile.com.util.ConstantsMiranda;
import trackermobile.com.util.TCPCliente;
import trackermobile.com.util.Util;

public class MainActivity extends AppCompatActivity {
    private ToggleButton btnEnableDisable;
    private boolean enableButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnEnableDisable = findViewById(R.id.btn_enable_disable);

        this.btnEnableDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDisable();
            }
        });

        //set permission get imei
        Util util = Util.getInstance();
        //set permmision for get imei for celphone
        util.setPermissionStatePhone(this);
        //set permission for get location gps
        util.setPermissionLocation(this);
    }

    private void enableDisable() {
        Util util = Util.getInstance();
        ConstantsMiranda.IMEI = util.setPermissionStatePhone(this);

        this.enableButton = !this.enableButton;
        if(this.enableButton){
            ComponentName componentName = new ComponentName(this, SocketJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(123, componentName)
                    .setRequiresCharging(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(16 * 60 * 1000)
                    .build();

            JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            int codeResult = jobScheduler.schedule(jobInfo);

            if(codeResult == JobScheduler.RESULT_SUCCESS){
                Log.v("..>",">run job>");
            }
            else{
                Log.v("..>",">not run job>");
            }
        }
        else{
            JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(123);

            TCPCliente tcpCliente = TCPCliente.getInstance(null);
            if(tcpCliente != null){
                tcpCliente.closeSocket();
                Log.v(".>",">CLOSED");
            }
            else{
                Log.v(".>",">NOT CLOSED");
            }

            Log.v("..>",">stop run job>");
        }
    }
}

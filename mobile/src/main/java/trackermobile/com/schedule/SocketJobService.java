package trackermobile.com.schedule;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import trackermobile.com.util.GpsTracker;
import trackermobile.com.util.TCPCliente;
import trackermobile.com.util.ConstantsMiranda;

public class SocketJobService extends JobService {
    private TCPCliente mTcpClient = null;

    private ConnectTask connectTask = null;
    private SendDataTask sendDataTask = null;

    //atributes for location
    private GpsTracker gpsTracker = null;
    private Location location = null;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        this.gpsTracker = new GpsTracker(getApplicationContext());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //run receive data
        this.connectTask = new ConnectTask();
        this.connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //run send data
        this.sendDataTask = new SendDataTask();
        this.sendDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class ConnectTask extends AsyncTask<String, String, TCPCliente> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TCPCliente doInBackground(String... message){
            //creamos un objeto TCPCliente
            if(mTcpClient == null){
                mTcpClient = TCPCliente.getInstance(new TCPCliente.OnMessageReceived()
                {
                    @Override
                    //aquí se implementa el método messageReceived
                    public void messageReceived(String message)
                    {
                    try{
                        publishProgress(message);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    }
                });

                mTcpClient.run();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            try {
                String value = values[0];
                sendBroadcastMessage(value);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendBroadcastMessage(String data) {
        if (data != null) {
            Intent intent = new Intent(ConstantsMiranda.ACTION_LOCATION_BROADCAST);
            intent.putExtra(ConstantsMiranda.VALUE_LOCATION, data);
            //LocalBroadcastManager.getInstance().sendBroadcast(intent);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //task for send
    private class SendDataTask extends AsyncTask<String, String, TCPCliente> {
        private double latitude = 0;
        private double longitude = 0;
        private String state = "X";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TCPCliente doInBackground(String... message){
            //creamos un objeto TCPCliente
            ConstantsMiranda.CONTINUE_SEND_DATA = true;

            while (ConstantsMiranda.CONTINUE_SEND_DATA){
                if(mTcpClient != null){
                    publishProgress();
                    mTcpClient.sendMessage("{\"cmd\":\"A\",\"latitude\": " + latitude + ",\"longitude\": " + longitude + ", \"imei\": \"" + ConstantsMiranda.IMEI + "\"}");
                }
                else{
                    Log.v(">>","->ES NULO TCP CLIENT");
                }

                //wait for send data
                try {
                    Thread.sleep(ConstantsMiranda.WAIT_THRIRTY_SECONDS);
                }catch (Exception e){
                }
            }
            Log.v(">>",">>EXIT SEND>>");
        
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            location = gpsTracker.getLocation();
            if( location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                state = "A";
                Log.v("=>","GPS Lat = "+latitude+"\n lon = "+longitude);
            }
            else {
                state = "X";
                Log.v("=>",">>>NO ACTIVADO GPS>>>");
            }
        }
    }

}

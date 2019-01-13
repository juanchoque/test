package trackermobile.com.util;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPCliente {
    public static TCPCliente tcpCliente = null;
    public static Socket socket = null;

    private String serverMessage;
    private OnMessageReceived mMessageListener = null;

    private PrintWriter out = null;
    private BufferedReader in = null;

    /**
     *  Constructor de la clase. OnMessagedReceived Escucha los mensajes resibidos desde el servidor
     */
    private TCPCliente(final OnMessageReceived listener)
    {
        this.mMessageListener = listener;
    }

    public static TCPCliente getInstance(final OnMessageReceived listener){
        if(tcpCliente == null){
            tcpCliente = new TCPCliente(listener);
        }
        return  tcpCliente;
    }

    /**
     * Enviamos los mensajes del cliente al servidor
     * @param message textos del cliente
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            System.out.println("message: "+ message);
            byte[] str = new byte[10];
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        ConstantsMiranda.RUN = false;
    }

    /**
     * method for conect to server with socket
     */
    public void run() {
        ConstantsMiranda.RUN  = true;
        try {
            //aqui pones la IP de tu computadora
            InetAddress serverAddr = InetAddress.getByName(ConstantsMiranda.SERVER_IP);
            //Creamos un socket y hacemos la coneccion con el servidor
            if(socket == null){
                socket = new Socket(serverAddr, ConstantsMiranda.SERVER_PORT);
            }

            try{
                //Enviamos los mensajes al servidor
                this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP SI Client", "SI: Done.");

                //recibimos el mensaje que el servidor envía de vuelta
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //Aquí, mientras que el cliente escucha los mensajes enviados por el servidor
                //Leemos las líneas
                while (ConstantsMiranda.RUN){
                    this.serverMessage = this.in.readLine();

                    if (this.serverMessage != null && this.mMessageListener != null) {
                        //llamamos al metodo messageReceived de la clase MyActivity
                        this.mMessageListener.messageReceived(this.serverMessage);
                        Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + this.serverMessage + "'");
                    }
                    this.serverMessage = null;
                }
            }
            catch (Exception e){
                Log.e("TCP SI Error", "SI: Error", e);
                e.printStackTrace();
                if(ConstantsMiranda.RUN){
                    try {
                        Thread.sleep(ConstantsMiranda.WAIT_THRIRTY_SECONDS);
                    }catch (Exception ex){
                    }

                    socket = null;
                    run();
                }
            }
            finally{
                closeSocket();
            }
        }catch (Exception e) {
            Log.e("TCP SI Error", "SI: Error", e);
        }

    }

    /**
     * method for cloe socket
     */
    public void closeSocket(){
        try {
            ConstantsMiranda.RUN = false;
            ConstantsMiranda.CONTINUE_SEND_DATA = false;
            socket.close();
            socket = null;
        }catch (Exception ex){
        }
    }

    /**
     * method for recive information
     */
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
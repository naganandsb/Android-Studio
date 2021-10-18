package com.example.bluemsgsend;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_ENABLE_BT = 101;

    BluetoothAdapter mbt ;
    BluetoothSocket btSocket = null;

    Set<BluetoothDevice> deviceips;

    EditText  msgtxt;
    TextView msgdisplay;
    Button sendbut;
    InputStream getmsg=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendbut = (Button)findViewById(R.id.button);
        msgtxt = (EditText)findViewById(R.id.editTextTextPersonName);
        msgdisplay = (TextView)findViewById(R.id.textView2);
        sendbut.setEnabled(false);
        mbt=BluetoothAdapter.getDefaultAdapter();
        if(!mbt.isEnabled())
        {
            mbt.enable();

        }

        deviceips = mbt.getBondedDevices();

        String tobconnect = "HC-05";
        for(BluetoothDevice bt : deviceips)
        {
            if(tobconnect.matches(bt.getName()))
            {
                int counter = 0;
                do {
                    try {
                        btSocket = bt.createRfcommSocketToServiceRecord(mUUID);
                        btSocket.connect();
                        sendbut.setEnabled(true);
                        Toast.makeText(this,"Bluetooth Connected",Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    counter++;
                } while (!btSocket.isConnected() && counter<3);


            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    receive();
                }


            }
        }).start();

    }

    public void clear(View v)
    {
        msgtxt.setText("");
    }
    public void transmit(View p)
    {
        String msg = msgtxt.getText().toString();
        try {
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(msg.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void receive()
    {
        try {
            if(!btSocket.isConnected())
            {
                this.finishAffinity();
            }
            String readmsg="a";
            int flag=0;
            getmsg = btSocket.getInputStream();
            getmsg.skip(getmsg.available());
            byte b = (byte)getmsg.read();
            readmsg = readmsg+(char)b;
            do {
                getmsg.skip(getmsg.available());
                byte c = (byte)getmsg.read();
                if(c=='@')
                {
                    flag++;
                }
                readmsg = readmsg+(char)c;

            }while(flag<3);

            readmsg = readmsg.split("@@")[1];
            readmsg =readmsg.replace('@',' ');
            msgdisplay.setText(readmsg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
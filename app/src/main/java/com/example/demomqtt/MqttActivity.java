package com.example.demomqtt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demomqtt.databinding.ActivityMqttBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttActivity extends AppCompatActivity {

    private static final String TAG = "Osama";
    private static final String MQTT_BROKER_URL = "mqtt://homeassistant.mk:1883";
//    private static final String MQTT_BROKER_URL = "tcp://broker.hivemq.com:1883";

    private MqttAndroidClient mqttClient;
    EditText clientId, topic;
    Button con, subscribe;
    TextView status, uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        clientId = (EditText) findViewById(R.id.client);
        topic = (EditText) findViewById(R.id.topic);
        con = (Button) findViewById(R.id.button);
        subscribe = (Button) findViewById(R.id.subscribe);
        uri = (TextView) findViewById(R.id.mqttServer);

        status = (TextView) findViewById(R.id.state);
        status.setText("Welcome to MQTT Demo \nThis text will show status updates");
        uri.setText(MQTT_BROKER_URL);

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MqttActivity.this, "client ID:"+clientId.getText()+"\n length: "+clientId.getText().length(), Toast.LENGTH_LONG);
                Log.d("osama", "client ID:"+clientId.getText()+"\n length: "+clientId.getText().length());
                if(clientId.getText().toString().toLowerCase().equals("enter client id") || clientId.getText().length() < 2){
                    status.setText("Please enter client ID");
                    Log.d("osama", "in if");
                }else {
                    Log.d("osama", "in else");
                    connect();
                }
            }
        });

        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(topic.getText().length() > 2){
                    subscribeToTopic();
                }else{
                    status.setText(status.getText()+"\n no topic provided");
                }
            }
        });

    }

    private void subscribeToTopic() {
        String tempTopic = topic.getText().toString();
        try {
//            mqttClient.subscribe(topic, 0);
            if(tempTopic.length() < 2){
                status.setText(status.getText()+"\n no topic provided");
                con.setVisibility(View.GONE);
                subscribe.setVisibility(View.VISIBLE);
            }else{
                status.setText(status.getText()+"\n Subscribed to Topic:"+tempTopic);
                mqttClient.subscribe(tempTopic, 0, msgListner);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error subscribing to topic: " + e.getMessage());
            status.setText("Error subscribing to topic :( ");
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE},
                    1);
        }
    }

    private void connect(){
        String serverUri = MQTT_BROKER_URL;
        String clientId = MqttClient.generateClientId();

        checkPermissions();

        mqttClient = new MqttAndroidClient(this, serverUri, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to MQTT broker");
                    status.setText("MQTT broker Connected!!");

                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to MQTT broker: " + exception.getMessage());
                    status.setText("MQTT broker Connection Failed :( ");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with MQTT connection
                recreate();
            } else {
                // Permissions not granted, handle accordingly
                Log.e(TAG, "Required permissions not granted");
                status.setText("permissions missing :( ");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    IMqttMessageListener msgListner =  new IMqttMessageListener() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload());
            Log.d(TAG, "Received message on topic " + topic + ": " + payload);
            // Handle the received payload here
            status.setText("Status: "+payload);
        }
    };

}
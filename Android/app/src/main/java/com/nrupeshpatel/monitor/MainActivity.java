package com.nrupeshpatel.monitor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.Action;
import com.amazonaws.services.iot.model.CreateTopicRuleRequest;
import com.amazonaws.services.iot.model.LambdaAction;
import com.amazonaws.services.iot.model.MessageFormat;
import com.amazonaws.services.iot.model.SnsAction;
import com.amazonaws.services.iot.model.TopicRulePayload;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.github.anastr.speedviewlib.SpeedView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {




    static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "";//AWS IoT Endpoint

    private static final String COGNITO_POOL_ID = "";//Cognito Pool ID

    private static final Regions MY_REGION = Regions.US_WEST_2;

    private String data_node = "000001";

    AWSIotMqttManager mqttManager;
    static String clientId;

    AWSCredentials awsCredentials;
    CognitoCachingCredentialsProvider credentialsProvider;
    FloatingActionButton connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SpeedView temperatureView = (SpeedView) findViewById(R.id.temperatureView);
        final SpeedView humidityView = (SpeedView) findViewById(R.id.humidityView);

        connect = (FloatingActionButton) findViewById(R.id.fab);
        connect.setEnabled(false);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Log.d(LOG_TAG, "clientId = " + clientId);

                try {
                    mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                        @Override
                        public void onStatusChanged(final AWSIotMqttClientStatus status,
                                                    final Throwable throwable) {
                            Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (status == AWSIotMqttClientStatus.Connecting) {
                                        Snackbar.make(view, "Connecting...", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                    } else if (status == AWSIotMqttClientStatus.Connected) {
                                        Snackbar.make(view, "Connected", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                        final String topic = "pi/data";

                                        Log.d(LOG_TAG, "topic = " + topic);

                                        try {
                                            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                                                    new AWSIotMqttNewMessageCallback() {
                                                        @Override
                                                        public void onMessageArrived(final String topic, final byte[] data) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        String message = new String(data, "UTF-8");
                                                                        Log.d(LOG_TAG, "Message arrived:");
                                                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                                                        Log.d(LOG_TAG, " Message: " + message);

                                                                        JSONObject jo = new JSONObject(message);

                                                                        if (jo.getString("serialNumber").equals(data_node)) {
                                                                            temperatureView.speedTo(jo.getInt("temperature"));
                                                                            humidityView.speedTo(jo.getInt("humidity"));
                                                                        }

                                                                    } catch (UnsupportedEncodingException e) {
                                                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });

                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, "Subscription error.", e);
                                        }

                                    } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                        if (throwable != null) {
                                            Log.e(LOG_TAG, "Connection error.", throwable);
                                        }
                                        Snackbar.make(view, "Reconnecting...", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                        if (throwable != null) {
                                            Log.e(LOG_TAG, "Connection error.", throwable);
                                            throwable.printStackTrace();
                                        }
                                        Snackbar.make(view, "Disconnected", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    } else {
                                        Snackbar.make(view, "Disconnected", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                    }
                                }
                            });
                        }
                    });
                } catch (final Exception e) {
                    Log.e(LOG_TAG, "Connection error.", e);
                    Snackbar.make(view, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        clientId = UUID.randomUUID().toString();


        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        Region region = Region.getRegion(MY_REGION);

        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                awsCredentials = credentialsProvider.getCredentials();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connect.setEnabled(true);
                    }
                });
            }
        }).start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_node1:
                data_node = "000001";
            case R.id.action_node2:
                data_node = "000002";
            case R.id.action_node3:
                data_node = "000003";
        }

        return super.onOptionsItemSelected(item);
    }


}

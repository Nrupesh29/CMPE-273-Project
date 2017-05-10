package com.nrupeshpatel.monitor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
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
import com.nrupeshpatel.monitor.helper.PrefManager;

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

import br.com.sapereaude.maskedEditText.MaskedEditText;

public class RegisterActivity extends AppCompatActivity {

    //Static headers used in the request
    private static final String CONTENT_LENGTH = "content-length";
    private static final String CONTENT_HEADER = "content-type";
    private static final String HOST_HEADER = "host";
    private static final String XAMZDATE_HEADER = "x-amz-date";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    //Static format parameters
    private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    private static final String DATE_TIMEZONE = "UTC";
    private static final String UTF8_CHARSET = "UTF-8";

    //Signature calculation related parameters
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String HASH_SHA256_ALGORITHM = "SHA-256";
    private static final String AWS_SHA256_ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String KEY_QUALIFIER = "AWS4";
    private static final String TERMINATION_STRING = "aws4_request";

    //User and instance parameters
    private static final String awsKeyID = "AKIAJTEHDRD7N6DQYJWQ"; // Your KeyID
    private static final String awsSecretKey = "HeWX/j4j2+ap6gJciaw5sHm2SncL/3yu8paX6DOb"; // Your Key

    //Service and target (API) parameters
    private static final String regionName = "us-west-2";//lowercase!
    private static final String serviceName = "lambda";

    //Parameters used in the message header
    private static String dateTimeString = null; // use null for service call. Code below will add current x-amz-date time stamp
    private static final String host = "lambda.us-west-2.amazonaws.com";
    private static final String protocol = "https";
    private static final String queryString = "";

    private static String contentType;

    AWSIot iotClient;

    AmazonSNS sendEmail;

    static String clientId;

    ProgressDialog loading;

    EditText emailAddress;

    String emailText;

    private PrefManager prefManager;

    CreateTopicRuleRequest createTopicRuleRequest;

    private static final Regions MY_REGION = Regions.US_WEST_2;

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PrefManager(this);

        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }


        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final MaskedEditText phoneNumber = (MaskedEditText) findViewById(R.id.phone);
        emailAddress = (EditText) findViewById(R.id.email);
        final EditText threshold = (EditText) findViewById(R.id.threshold);

        Button register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNumber.getRawText() == null || emailAddress.getText() == null || threshold.getText() == null) {
                    Toast.makeText(RegisterActivity.this, "Enter all details!!", Toast.LENGTH_SHORT).show();
                } else {

                    emailText = emailAddress.getText().toString();

                    clientId = UUID.randomUUID().toString();

                    iotClient = new AWSIotClient(new BasicAWSCredentials("AKIAJTEHDRD7N6DQYJWQ", "HeWX/j4j2+ap6gJciaw5sHm2SncL/3yu8paX6DOb"));
                    iotClient.setRegion(Region.getRegion(MY_REGION));
                    iotClient.setEndpoint("iot.us-west-2.amazonaws.com");

                    ArrayList<Action> ruleActions = new ArrayList<>();
                    Action action = new Action();
                    LambdaAction lambdaAction = new LambdaAction();
                    lambdaAction.setFunctionArn("arn:aws:lambda:us-west-2:384585473965:function:SendSMS");
                    action.setLambda(lambdaAction);

                    SnsAction snsAction = new SnsAction();
                    snsAction.setMessageFormat(MessageFormat.RAW);
                    snsAction.setRoleArn("arn:aws:iam::384585473965:role/SNS_ROLE");
                    snsAction.setTargetArn("arn:aws:sns:us-west-2:384585473965:EmailAlert");
                    action.setSns(snsAction);

                    ruleActions.add(action);

                    TopicRulePayload topicRulePayload = new TopicRulePayload();
                    topicRulePayload.setAwsIotSqlVersion("2016-03-23");
                    topicRulePayload.setActions(ruleActions);
                    topicRulePayload.setSql("SELECT temperature, '+1" + phoneNumber.getRawText() + "' AS to_phone FROM 'pi/data' WHERE temperature > " + threshold.getText().toString());

                    createTopicRuleRequest = new CreateTopicRuleRequest();
                    createTopicRuleRequest.setRuleName("SendAlert_" + clientId.replace("-", "_"));
                    createTopicRuleRequest.setTopicRulePayload(topicRulePayload);

                    sendEmail = new AmazonSNSClient(new BasicAWSCredentials("AKIAJTEHDRD7N6DQYJWQ", "HeWX/j4j2+ap6gJciaw5sHm2SncL/3yu8paX6DOb"));
                    sendEmail.setRegion(Region.getRegion(Regions.US_WEST_2));

                    new AddPermissionToInvoke().execute();

                }
            }
        });

    }

    private class AddPermissionToInvoke extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(RegisterActivity.this, null, "Registering AWS Resources...", true, true);
            loading.setCancelable(false);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loading.dismiss();
            launchHomeScreen();

        }

        @Override
        protected String doInBackground(String... params) {

            try {

                iotClient.createTopicRule(createTopicRuleRequest);
                sendEmail.subscribe("arn:aws:sns:us-west-2:384585473965:EmailAlert", "email", emailText);

                StringBuilder payload = new StringBuilder();
                setPayload(payload);

                URL hostConnection = new URL("https://lambda.us-west-2.amazonaws.com/2015-03-31/functions/SendSMS/policy");
                HttpURLConnection conn = (HttpURLConnection) hostConnection.openConnection();

                conn.setRequestMethod("POST");

                //conn.setFixedLengthStreamingMode(payload.length());

                signRequestAWSv4(conn, payload);

                //Indicate that the connection will write data to the URL connection
                conn.setDoOutput(true);

                //Write the output message to the connection, if it errors it will generate an IOException
                OutputStream output = conn.getOutputStream();
                output.write(payload.toString().getBytes(UTF8_CHARSET));

                //Read input or error stream based on response code
                BufferedReader in = null;
                String inputLine;
                if (conn.getResponseCode() != 201) {
                    in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                }
                System.out.println("\nRESPONSE:");
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                conn.disconnect();
            } catch (Exception e) {
                //If any element of the signing, payload creation, or connection throws an exception then terminate since we cannot continue.
                System.out.println(e.toString());
                System.exit(1);
            }

            return null;

        }
    }


    private static String buildCanonicalRequest(StringBuilder payload) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //Create a SHA256 hash of the payload, used in authentication
        String payloadHash = hash(payload.toString());

        //Canonical request headers should be sorted by lower case character code
        StringBuilder canonicalRequest = new StringBuilder();
        canonicalRequest.append("POST\n")
                .append("/2015-03-31/functions/SendSMS/policy").append("\n")
                .append("").append("\n")
                .append(CONTENT_LENGTH).append(":").append(payload.length()).append("\n")
                .append(CONTENT_HEADER).append(":").append(contentType).append("\n")
                .append(HOST_HEADER).append(":").append(host).append("\n")
                .append(XAMZDATE_HEADER).append(":").append(dateTimeString).append("\n")
                .append("\n")
                .append(CONTENT_LENGTH).append(";").append(CONTENT_HEADER).append(";").append(HOST_HEADER).append(";").append(XAMZDATE_HEADER).append("\n")
                .append(payloadHash);
        return canonicalRequest.toString();
    }


    private static String buildStringToSign(String canonicalRequestHash, String dateString) {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(AWS_SHA256_ALGORITHM).append("\n")
                .append(dateTimeString).append("\n")
                .append(dateString).append("/").append(regionName).append("/").append(serviceName).append("/").append(TERMINATION_STRING).append("\n")
                .append(canonicalRequestHash);
        return stringToSign.toString();
    }

    private static byte[] buildDerivedKey(String dateString) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        StringBuilder signatureAWSKey = new StringBuilder();
        signatureAWSKey.append(KEY_QUALIFIER);
        signatureAWSKey.append(awsSecretKey);

        //Calculate the derived key from given values
        byte[] derivedKey = hmac(TERMINATION_STRING,
                hmac(serviceName,
                        hmac(regionName,
                                hmac(dateString, signatureAWSKey.toString().getBytes(UTF8_CHARSET)))));
        return derivedKey;
    }

    private static String buildAuthSignature(String stringToSign, String dateString) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        //Use derived key and "String to Sign" to make the final signature
        byte[] derivedKey = buildDerivedKey(dateString);

        byte[] finalSignature = hmac(stringToSign.toString(), derivedKey);

        String signatureString = bytesToHex(finalSignature);
        StringBuilder authorizationValue = new StringBuilder();
        authorizationValue.append(AWS_SHA256_ALGORITHM)
                .append(" Credential=").append(awsKeyID).append("/")
                .append(dateString).append("/")
                .append(regionName).append("/")
                .append(serviceName).append("/")
                .append(TERMINATION_STRING).append(",")
                .append(" SignedHeaders=").append(CONTENT_LENGTH).append(";")
                .append(CONTENT_HEADER).append(";")
                .append(HOST_HEADER).append(";")
                .append(XAMZDATE_HEADER).append(",")
                .append(" Signature=").append(signatureString);

        return authorizationValue.toString();
    }

    private static void printRequestInfo(StringBuilder payload, String canonicalRequest, String canonicalRequestHash, String stringToSign, String authorizationValue, String dateString) throws Exception {
        //Print everything to be sent:
        System.out.println("\nPAYLOAD:");
        System.out.println(payload.toString());
        System.out.println("\nHASHED PAYLOAD:");
        System.out.println(hash(payload.toString()));
        System.out.println("\nCANONICAL REQUEST:");
        System.out.println(canonicalRequest);
        System.out.println("\nHASHED CANONICAL REQUEST:");
        System.out.println(canonicalRequestHash);
        System.out.println("\nSTRING TO SIGN:");
        System.out.println(stringToSign);
        System.out.println("\nDERIVED SIGNING KEY:");
        System.out.println(bytesToHex(buildDerivedKey(dateString)));
        System.out.println("\nSIGNATURE:");

        //Check that the signature is moderately well formed to do string manipulation on
        if (authorizationValue.indexOf("Signature=") < 0 || authorizationValue.indexOf("Signature=") + 10 >= authorizationValue.length()) {
            throw new Exception("Malformed Signature");
        }

        //Get the text from after the word "Signature=" to the end of the authorization signature
        System.out.println(authorizationValue.substring(authorizationValue.indexOf("Signature=") + 10));
        System.out.println("\nENDPOINT:");
        System.out.println(host);
        System.out.println("\nSIGNED REQUEST");
        System.out.println("POST " + "https://lambda.us-west-2.amazonaws.com/2015-03-31/functions/SendSMS/policy" + " HTTP/1.1");
        System.out.println(CONTENT_LENGTH + ":" + payload.length());
        System.out.println(CONTENT_HEADER + ":" + contentType);
        System.out.println(HOST_HEADER + ":" + host);
        System.out.println(XAMZDATE_HEADER + ":" + dateTimeString);
        System.out.println(AUTHORIZATION_HEADER + ":" + authorizationValue);
        System.out.println(payload.toString());

    }

    private static void signRequestAWSv4(URLConnection conn, StringBuilder payload) throws Exception {
        if (conn == null) {
            throw new ConnectException();
        }

        //If no date provided or the one provided is too short then create new x-amz-date.
        if (dateTimeString == null || dateTimeString.length() < 8) {
            dateTimeString = timestamp();
        }
        //Convert full date to x-amz-date by ignoring fields we don't need
        //dateString only needs digits for the year(4), month(2), and day(2).
        String dateString = dateTimeString.substring(0, 8);

        //Set proper request properties for the connection, these correspond to what was used creating a canonical request
        //and the final Authorization

        conn.setRequestProperty(CONTENT_HEADER, contentType);
        conn.setRequestProperty(HOST_HEADER, host);
        conn.setRequestProperty(XAMZDATE_HEADER, dateTimeString);

        //Begin Task 1: Creating a Canonical Request
        String canonicalRequest = buildCanonicalRequest(payload);
        String canonicalRequestHash = hash(canonicalRequest);

        //Begin Task 2: Creating a String to Sign
        String stringToSign = buildStringToSign(canonicalRequestHash, dateString);

        //Begin Task 3: Creating a Signature
        String authorizationValue = buildAuthSignature(stringToSign, dateString);

        //set final connection header
        conn.setRequestProperty(AUTHORIZATION_HEADER, authorizationValue);

        //Print everything to be sent:
        printRequestInfo(payload, canonicalRequest, canonicalRequestHash, stringToSign, authorizationValue, dateString);
    }

    private static void setPayload(StringBuilder payload) throws IllegalArgumentException {

        contentType = "application/json";

        payload.append("{\"Action\": \"").append("lambda:*")
                .append("\", \"Principal\": \"").append("iot.amazonaws.com")
                .append("\", \"SourceArn\": \"").append("arn:aws:iot:us-west-2:384585473965:rule/SendAlert_" + clientId.replace("-", "_"))
                .append("\", \"StatementId\": \"").append("SendAlert_" + clientId.replace("-", "_"))
                .append("\"}");

        //payload.append("{\"Action\": \"lambda:*\", \"Principal\": \"iot.amazonaws.com\", \"SourceArn\": \"arn:aws:iot:us-west-2:384585473965:rule/SendSMS_\"" + clientId.replace("-", "_") + "\", \"StatementId\": \"SendSMS_" + clientId.replace("-", "_") + "\" }");

    }

    private static String hash(String toHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String result = null;
        MessageDigest md = MessageDigest.getInstance(HASH_SHA256_ALGORITHM);
        md.update(toHash.getBytes(UTF8_CHARSET));
        byte[] hashed = md.digest();
        result = bytesToHex(hashed);
        return result;
    }

    private static byte[] hmac(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        byte[] result = null;
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_SHA256_ALGORITHM));
        result = mac.doFinal(data.getBytes(UTF8_CHARSET));
        return result;
    }
    
    private static String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat(DATE_FORMAT);
        dfm.setTimeZone(TimeZone.getTimeZone(DATE_TIMEZONE));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private static String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}

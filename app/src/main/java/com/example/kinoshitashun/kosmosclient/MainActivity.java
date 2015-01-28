package com.example.kinoshitashun.kosmosclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//import android.app.FragmentManager;

public class MainActivity extends FragmentActivity {
    public static boolean isConnectedToInternet;
    public static Context contextVar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.container);
        isConnectedToInternet = isConnected();
        contextVar = getBaseContext();
        if (f == null) {
            f = PlaceholderFragment.newInstance("Start");
            fm.beginTransaction().add(R.id.container, f).commit();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        System.out.println("the code is catch");
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanData = scanningResult.getContents();
            FragmentManager fm = getSupportFragmentManager();
            Fragment newFrame = PlaceholderFragment.newInstance(scanData);
            fm.beginTransaction().replace(R.id.container, newFrame).commit();
            Log.d("debug", scanData);
        } else {
            Toast.makeText(contextVar, "バーコードを正常に読み取れませんでした", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        private static final String EXTRA_CODE = "com.example.kinoshitashun.kosmosclient";
        private String isbn;
        TextView textArea;
        TextView resultView;
        EditText isbnField;
        public PlaceholderFragment() {
        }
        public static PlaceholderFragment newInstance(String code) {
            Bundle args = new Bundle();
            args.putSerializable(EXTRA_CODE, code);

            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            textArea = (TextView) rootView.findViewById(R.id.textArea);
            resultView = (TextView) rootView.findViewById(R.id.resultView);
            isbnField = (EditText) rootView.findViewById(R.id.isbnField);
            Log.d("dbbb", EXTRA_CODE);
            if (!getArguments().getSerializable(EXTRA_CODE).equals("Start")) {
                isbnField.setText((String) getArguments().getSerializable(EXTRA_CODE));
            }else {
                Log.d("debug", "initial instance");
            }
            Button scanBtn = (Button) rootView.findViewById(R.id.scanCode);
            Button btn = (Button) rootView.findViewById(R.id.button);
            scanBtn.setOnClickListener(this);
            btn.setOnClickListener(this);
            return rootView;
        }
        private class HttpAsyncTask extends AsyncTask<String, Void, String> {
            private String str;
            private String volumeStr, reservationStr, memoStr;
            @Override
            protected String doInBackground(String... urls) {
                return GET(urls[0]);
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(contextVar, "Received!", Toast.LENGTH_LONG).show();
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.isNull("error")) {
                        String titleStr = "タイトル：" + json.getString("Title") + "\n";
                        String requestURLStr = json.getString("RequestURL");
                        String divisionStr = "区分：" + json.getString("Division") + "\n";
                        String statusStr = "貸出状況：" + json.getString("Status") + "\n";
                        if (json.isNull("Volume")) {
                            volumeStr = "";
                        } else {
                            volumeStr = "巻：" + json.getString("Volume") + "\n";
                        }
                        String campusStr = "キャンパス：" + json.getString("Campus") + "\n";
                        String placeStr = "配架場所：" + json.getString("Place") + "\n";
                        String symbolStr = "請求記号：" + json.getString("Symbol") + "\n";
                        String bookIDStr = "Book ID：" + json.getString("BookID") + "\n";
                        if (json.isNull("Reservation")) {
                            reservationStr = "";
                        } else {
                            reservationStr = "予約数：" + json.getString("Reservation") + "\n";
                        }
                        if (json.isNull("Memo")) {
                            memoStr = "";
                        } else {
                            memoStr = "メモ：" + json.getString("Memo") + "\n";
                        }
                        str = titleStr + divisionStr + statusStr + volumeStr + campusStr+ placeStr +symbolStr + bookIDStr+ reservationStr + memoStr;
                        resultView.setText(str);
                    } else {
                        resultView.setText("ご希望の本を見つける事ができませんでした。");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        public static String GET(String url){
            InputStream inputStream = null;
            String result = "";
            try {
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                } else {
                    result = "Did not work!";
                }
            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return result;
        }

        private static String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            return result;

        }
        @Override
        public void onClick(View rootView){
//            Log.d("debug msg","clicked");
            switch(rootView.getId()){
                case R.id.button:
                    if (!isbnField.getText().toString().isEmpty()) {
                        if(isConnectedToInternet) {
//                            isbnField.setBackgroundColor(0xFFFFFF);
                            textArea.setBackgroundColor(0xFF00CC00);
                            textArea.setText("You are connected to Internet");
                            Toast.makeText(contextVar, "Please wait for a second...", Toast.LENGTH_LONG).show();
                            isbn = isbnField.getText().toString();
                            Log.d("debug", isbn);
                            new HttpAsyncTask().execute("https://kosmosapi.herokuapp.com/kosmos?isbn="+isbn);
                        } else {
                            textArea.setText("You are not connected to Internet");
                        }
                    } else {
                        Toast.makeText(contextVar, "ISBNを入力してください", Toast.LENGTH_LONG).show();
                        isbnField.setText("testes");
//                        isbnField.setBackgroundColor(0xFF7F50);
                    }
                    break;
                case R.id.scanCode:
                    Log.d("debug","code scan button has been pushed");
                    IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
                    scanIntegrator.initiateScan();
                    break;
            }
        }
//        @Override
//        public void onActivityResult (int requestCode, int resultCode, Intent intent) {
//            super.onActivityResult(requestCode, resultCode, intent);
//            System.out.println("never here");
//            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//            if (scanResult != null) {
//                String scanStr = scanResult.toString();
//                Log.d("debug", scanStr);
//                isbnField.setText(scanStr);
//            }else {
//                Toast.makeText(contextVar, "バーコードを正常に読み取れませんでした", Toast.LENGTH_LONG).show();
//            }
//        }
    }
}

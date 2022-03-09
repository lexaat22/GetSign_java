package uz.lexa.getsign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edCertSN;
    private EditText edPIN;
    private EditText edStringToSign;
    private EditText edResponse;

    private void savePref(String name, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, value);
        editor.apply();
    }

    private String getPref(String name) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        return sharedPref.getString(name, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edCertSN = findViewById(R.id.edCertSN);
        edCertSN.setError(null);
        edPIN = findViewById(R.id.edPIN);
        edPIN.setError(null);
        edStringToSign = findViewById(R.id.edStringToSign);
        edStringToSign.setError(null);
        edResponse = findViewById(R.id.edResponse);
        Button btSign = findViewById(R.id.btSign);
        btSign.setOnClickListener(view -> {
            savePref("edCertSN", edCertSN.getText().toString());
            savePref("edPIN", edPIN.getText().toString());
            savePref("edStringToSign", edStringToSign.getText().toString());

            if (edCertSN.getText().toString().isEmpty()) {
                edCertSN.setError("Enter Certificate Serial Number");
                return;
            }
            if (edPIN.getText().toString().isEmpty()) {
                edPIN.setError("Enter PIN for certificate");
                return;
            }
            if (edStringToSign.getText().toString().isEmpty()) {
                edStringToSign.setError("Enter string to be signed");
                return;
            }

            try {
                String url = MessageFormat.format("metinmobile://sign?app_name=getsign&serial_number={0}&message_to_sign={1}&pin={2}",
                        edCertSN.getText().toString(),
                        URLEncoder.encode(edStringToSign.getText().toString(), "utf-8"),
                        edPIN.getText().toString()
                );
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (edCertSN.getText().toString().isEmpty())
            edCertSN.setText(getPref("edCertSN"));
        if (edPIN.getText().toString().isEmpty())
            edPIN.setText(getPref("edPIN"));
        if (edStringToSign.getText().toString().isEmpty())
            edStringToSign.setText(getPref("edStringToSign"));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null && data.isHierarchical()) {
                String uri = this.getIntent().getDataString();
                if (uri != null && uri.startsWith("getsign")) {
                    Map<String, String> params = null;
                    try {
                        params = getUrlParameters(uri);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (params != null) {
                        for (String key : params.keySet()) {
                            String value = params.get(key);
                            Log.i("MyApp", "Key: " + key + " Value: " + value);
                            if (key.equalsIgnoreCase("status"))
                            {
                                savePref("sign_status", value);
                            }
                            if (key.equalsIgnoreCase("cms"))
                            {
                                savePref("sign_cms", value);
                                edResponse.setText(value);
                            }
                        }
                    }//success
                }
            }
        }
    }

    public static Map<String, String> getUrlParameters(String url)
            throws UnsupportedEncodingException {
        url = URLDecoder.decode(url, "UTF-8");
        Map<String, String> params = new HashMap<>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                params.put(key, value);
            }
        }
        return params;
    }
}
package saveme.sureshm.com.saveme;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.SettingInjectorService;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    ImageButton saveme, nic, name, other;
    Button editprofile;
    LocationManager locationManager;
    String provider;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT"); // This will contain your scan result
                report(contents, "", "");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveme = (ImageButton) findViewById(R.id.savecard);
        nic = (ImageButton) findViewById(R.id.nic);
        name = (ImageButton) findViewById(R.id.name);
        other = (ImageButton) findViewById(R.id.noinfo);
        editprofile = (Button) findViewById(R.id.updateprofile);

        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Main Activity
                Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                startActivity(intent);
            }
        });

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        saveme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Report by person's name");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        report(name, "", "");
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        nic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Report via NIC number");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nicno = input.getText().toString();
                        report("", nicno, "");
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public String report(String scard, String nic, String name) {
        //Get shared prefs
        SharedPreferences sp = getSharedPreferences("Login", 0);
        String reporter = sp.getString("username", "email");
        String reporterhash = sp.getString("hash", "hash");
        String finalloc = "";

        double latitude, longlat;
        Location location;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        latitude = location.getLatitude();
        longlat = location.getLongitude();

        finalloc = latitude+","+longlat;

        ProceedReport proceedReport = new ProceedReport();
        proceedReport.execute(reporter, reporterhash, scard, nic, name, finalloc);

        return null;
    }


    class ProceedReport extends AsyncTask<String, String, String> {

        private ProgressDialog reportprogress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reportprogress = new ProgressDialog(MainActivity.this);
            reportprogress.setIndeterminate(true);
            reportprogress.setMessage("Reporting...");
            reportprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String g_reporter = params[0];
            String g_reporterhash = params[1];
            String g_savemecard = params[2];
            String g_nic = params[3];
            String g_nane = params[4];
            String g_location = params[5];

            try{

                String link = "http://192.168.1.8/saveme/client.php?task=report&reporter="+Uri.encode(g_reporter)+"&hash="+Uri.encode(g_reporterhash)+"&savecard="+Uri.encode(g_savemecard)+"&nic="+Uri.encode(g_nic)+"&name="+Uri.encode(g_nane)+"&location="+Uri.encode(g_location);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String json = in.readLine();
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.getString("message");

                return message;
            }

            catch(Exception e){
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            reportprogress.dismiss();
        }
    }

}

package saveme.sureshm.com.saveme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import butterknife.InjectView;

public class Login extends AppCompatActivity {

    @InjectView(R.id.input_email) EditText email;
    @InjectView(R.id.input_password) EditText password;
    @InjectView(R.id.btn_login) Button login;
    @InjectView(R.id.link_signup) TextView signuplink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("Login", 0);
        String reporter = sp.getString("username", "email");

        if(!reporter.isEmpty()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);
        }

        ButterKnife.inject(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        signuplink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);

            }
        });

    }

    public void login()  {
        if(validate() == false){
            onLoginFailed();
            return;
        }

        login.setEnabled(true);

        ProceedLogin proceedlogin = new ProceedLogin();
        proceedlogin.execute(email.getText().toString(), password.getText().toString());
    }

    public boolean validate() {
        boolean valid = true;

        String email_in = email.getText().toString();
        String password_in = password.getText().toString();

        if (email_in.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email_in).matches()) {
            email.setError("invalid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        if (password_in.isEmpty() || password_in.length() < 4 || password_in.length() > 10) {
            password.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    private void onLoginSuccess() {
        return;
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        login.setEnabled(true);
    }

    class ProceedLogin extends AsyncTask<String, String, String> {

        private ProgressDialog loginprogress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginprogress = new ProgressDialog(Login.this);
            loginprogress.setIndeterminate(true);
            loginprogress.setMessage("Authenticating...");
            loginprogress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String email_in = params[0];
            String password_in = params[1];

            try{
                String link = "http://icts.stcmount.edu.lk/saveme/client.php?task=login&username="+Uri.encode(email_in)+"&password="+Uri.encode(password_in);
                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String json = in.readLine();
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.getString("message");
                String hash = jsonObject.getString("hash");

                SharedPreferences sp=getSharedPreferences("Login", 0);
                SharedPreferences.Editor Ed=sp.edit();
                Ed.putString("username",email_in);
                Ed.putString("hash",hash);
                Ed.commit();

                in.close();
                return message;
            }

            catch(Exception e){
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            loginprogress.dismiss();

            if(s.equalsIgnoreCase("success")){

                //Open Main Activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                //finish();

            } else {
                Toast.makeText(Login.this, s, Toast.LENGTH_LONG).show();
            }
        }
    }

}
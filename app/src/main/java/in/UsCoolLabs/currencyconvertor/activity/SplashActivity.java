package in.UsCoolLabs.currencyconvertor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import in.UsCoolLabs.currencyconvertor.R;

public class SplashActivity extends Activity {

    TextView textView;
    String str = " Currency Converter ";
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        textView = findViewById(R.id.textView);
        handler(str.charAt(i));
    }

    void handler(final char c) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i < str.length() - 1) {
                    textView.append("" + c);
                    i++;
                    handler(str.charAt(i));
                } else {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            }
        }, 200);
    }
}

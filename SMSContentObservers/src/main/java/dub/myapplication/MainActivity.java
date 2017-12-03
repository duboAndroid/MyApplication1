package dub.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private TextView get;
    private SmsContent content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        get = (TextView) findViewById(R.id.get);

        //test 2
        content = new SmsContent(MainActivity.this, new Handler(), get);
        this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, content);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.getContentResolver().unregisterContentObserver(content);
    }

}

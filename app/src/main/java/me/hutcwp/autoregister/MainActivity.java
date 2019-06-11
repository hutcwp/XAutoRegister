package me.hutcwp.autoregister;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String s = "当前自动注入的managerA data size is " + new ManagerA().getData().size();
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();

        // String s2 = "当前自动注入的managerB data size is " + new ManagerB().getData().size();
        // Toast.makeText(this, s2, Toast.LENGTH_LONG).show();
    }
}

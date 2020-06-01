package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

public class LoginActivity extends AppCompatActivity {

    private EditText ipEditText;
    private EditText portEditText;
    private EditText codeEditText;
    private EditText groupIDEditText;
    private TextView loginInfo;
    private Button startButton;

    private ReentrantLock loginLock;

    private final LoginActivity.MyHandler mHandler = new LoginActivity.MyHandler(this);

    static class MyHandler extends Handler {
        private final WeakReference<LoginActivity> mActivity;

        public MyHandler(LoginActivity activity) {
            mActivity = new WeakReference<>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == 0) {
                    activity.loginInfo.setText("连接服务器失败");
                } else {
                    System.out.println("123123123123");
                    activity.changeActivity();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginLock = new ReentrantLock();

        ipEditText = findViewById(R.id.ip);
        portEditText = findViewById(R.id.port);
        groupIDEditText = findViewById(R.id.group_id);
        loginInfo = findViewById(R.id.login_info);
        codeEditText = findViewById(R.id.attendance_code);
        startButton = findViewById(R.id.startButton);

        ipEditText.setText("192.168.123.136");
        portEditText.setText("11234");
        groupIDEditText.setText("1");
        codeEditText.setText("jECysvTpOkhQjsQg");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ip = ipEditText.getText().toString();
                final String port = portEditText.getText().toString();
                final String groupID = groupIDEditText.getText().toString();
                final String code = codeEditText.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loginLock.lock();

                        try {
                            boolean ok = SocketUtil.testServer(ip, port, groupID, code);
                            if (ok) {
                                mHandler.sendEmptyMessage(1);
                            } else {
                                mHandler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        loginLock.unlock();
                    }
                }).start();


            }
        });
    }

    void changeActivity() {
        Intent t = new Intent();
        t.setClass(this, MainActivity.class);
        startActivity(t);
    }
}

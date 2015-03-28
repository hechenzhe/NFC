package com.nuaa.manso_he.nfctest1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.Button;


public class MainActivity extends Activity {

    //初始化一个按钮变量
    private Button btnRead;
    private Button btnWrite;
    //用于监听Click事件的类
    private class btnListener_read implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //创建一个Intent，从MainActivity到MainActivity2
            Intent intent1 = new Intent(MainActivity.this,ReadURL.class);
            //使用这个Intent打开指定的Activity
            startActivity(intent1);
        }
    }
    private class btnListener_write implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //创建一个Intent，从MainActivity到MainActivity3
            Intent intent1 = new Intent(MainActivity.this,WriteURL.class);
            //使用这个Intent打开指定的Activity
            startActivity(intent1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //通过xml里面写好的id来获取绑定布局中的按钮（R是一个索引，把所有的资源生成一个代码）
        btnRead = (Button) findViewById(R.id.btnRead);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        //为btnSwitch这个按钮设置一个Click事件的监听
        btnRead.setOnClickListener(new btnListener_read());
        btnWrite.setOnClickListener(new btnListener_write());
    }


}

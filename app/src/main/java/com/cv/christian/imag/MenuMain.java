package com.cv.christian.imag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;



public class MenuMain extends Activity {

    private static Button button_sbm;
private static Button button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_main);
        OnClickButtonListener();
        OnClickButtonListener2();
    }

    public void OnClickButtonListener(){
        button_sbm = (Button)findViewById(R.id.button);
        button_sbm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.cv.christian.imag.learn_braille");
                        startActivity(intent);
                    }
                }
        );
    }
    public void OnClickButtonListener2(){
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        Intent intent2 = new Intent("com.cv.christian.imag.MainActivity");
                        startActivity(intent2);
                    }

                }
        );
    }

   /* @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
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
    }*/
}

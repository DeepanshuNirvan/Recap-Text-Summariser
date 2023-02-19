package com.example.recap_textsummariser;

import static com.example.recap_textsummariser.R.drawable.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    static int wordcount(String string) {
        int count = 0;
        char[] ch;
        ch = new char[string.length()];
        for (int i = 0; i < string.length(); i++) {
            ch[i] = string.charAt(i);
            if (((i > 0) && (ch[i] != ' ') && (ch[i - 1] == ' ')) || ((ch[0] != ' ') && (i == 0)))
                count++;
        }
        return count;
    }

    String word_count = "20";
    String url = "https://deepanshunirvan.pythonanywhere.com/summary";
    String summ1, summ2, input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText textarea = findViewById(R.id.textarea);
        TextView size = findViewById(R.id.textView3);
        Button summarise = findViewById(R.id.summarise);
        Button paste = findViewById(R.id.paste);
        Button clear = findViewById(R.id.clear);
        TextView count = findViewById(R.id.textView4);
        ScrollView Scroll = findViewById(R.id.Scroll);
        LinearLayout linear = findViewById(R.id.linear);
        ConstraintLayout constr = findViewById(R.id.constr);
        Intent isummary = new Intent(MainActivity.this, Summary.class);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        Slider slider = findViewById(R.id.slider);


        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                size.setText("Summary Length ( in words):  " + (int) value);
                int val = (int) value;
                word_count = Integer.toString(val);
            }
        });


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int c = wordcount(textarea.getText().toString());
                                String cnt = Integer.toString(c);
                                count.setText("Words: " + cnt);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


        AndroidNetworking.initialize(this);
        summarise.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (textarea.getText().toString().length() < 50) {
                    Toast toast = new Toast(getApplicationContext());
                    View view2 = getLayoutInflater().inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast));
                    toast.setView(view2);
                    EditText tst = view2.findViewById(R.id.toast_tf);
                    tst.setText("Text Given Was too small!");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    if (wordcount(textarea.getText().toString()) < (Integer.parseInt(word_count))) {
                        Toast toast = new Toast(getApplicationContext());
                        View view2 = getLayoutInflater().inflate(R.layout.toast2, (ViewGroup) findViewById(R.id.toast2));
                        toast.setView(view2);
                        EditText tst = view2.findViewById(R.id.toast_tf2);
                        tst.setText(" Summary Length Should be less than Input length ");
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        linear.setVisibility(View.VISIBLE);
                        getSupportActionBar().hide();
                        constr.setVisibility(View.INVISIBLE);
                        AndroidNetworking.post(url)
                                .setPriority(Priority.HIGH)
                                .addBodyParameter("data", textarea.getText().toString())
                                .addBodyParameter("word_count", word_count)
                                .build()
                                .getAsJSONArray(new JSONArrayRequestListener() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            constr.setVisibility(View.VISIBLE);
                                            linear.setVisibility(View.INVISIBLE);
                                            summ1 = response.getString(0);
                                            summ2 = response.getString(1);
                                            input = textarea.getText().toString();
                                            isummary.putExtra("summ1", summ1);
                                            isummary.putExtra("summ2", summ2);
                                            isummary.putExtra("input", input);
                                            getSupportActionBar().show();
                                            startActivity(isummary);
                                        } catch (JSONException e) {

                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        constr.setVisibility(View.VISIBLE);
                                        linear.setVisibility(View.INVISIBLE);
                                        getSupportActionBar().show();
                                        Toast toast = new Toast(getApplicationContext());
                                        View view2 = getLayoutInflater().inflate(R.layout.toast2, (ViewGroup) findViewById(R.id.toast2));
                                        toast.setView(view2);
                                        EditText tst = view2.findViewById(R.id.toast_tf2);
                                        tst.setText("Network Error");
                                        toast.setDuration(Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();


                                    }
                                });

                    }
                }
            }
        });


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textarea.setText("");
            }
        });


        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clip = cb.getPrimaryClip();
                if (clip != null) {
                    ClipData.Item item = clip.getItemAt(0);
                    String data= "Nothing to Paste,please Copy Something First...";
                    if(item!=null) data= item.getText().toString();
                    textarea.append(data);
                    Toast toast = new Toast(getApplicationContext());
                    View view2 = getLayoutInflater().inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast));
                    toast.setView(view2);
                    EditText tst = view2.findViewById(R.id.toast_tf);
                    tst.setText("Text Pasted!");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                    paste.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            paste.setEnabled(true);

                        }
                    }, 1000);
                } else {
                    Toast toast = new Toast(getApplicationContext());
                    View view2 = getLayoutInflater().inflate(R.layout.toast2, (ViewGroup) findViewById(R.id.toast2));
                    toast.setView(view2);
                    EditText tst = view2.findViewById(R.id.toast_tf2);
                    tst.setText("Nothing To Paste");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                    paste.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            paste.setEnabled(true);

                        }
                    }, 1000);

                }

            }
        });


        textarea.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                if (view.getId() == R.id.textarea) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.tb_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {
            String data = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            Intent ishare = new Intent(Intent.ACTION_SEND);
            ishare.putExtra(Intent.EXTRA_TEXT, data);
            ishare.setType("text/plain");
            startActivity(Intent.createChooser(ishare, "Share Via:"));

        } else {

            try{
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
            }
            catch(ActivityNotFoundException e){
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/detail?id=" + BuildConfig.APPLICATION_ID)));
            }

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
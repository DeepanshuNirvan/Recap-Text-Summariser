package com.example.recap_textsummariser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Summary extends AppCompatActivity {


    private static final int STORAGE_CODE = 1000;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Button summary2 = findViewById(R.id.summary2);
        Button copy = findViewById(R.id.copy);
        Button download = findViewById(R.id.download);
        Button share = findViewById(R.id.share_s);
        TextView count = findViewById(R.id.textView4);
        Intent isummary2 = new Intent(Summary.this, Summary2.class);
        Intent s1 = getIntent();
        EditText textarea2 = findViewById(R.id.textarea2);
        String summ1 = s1.getStringExtra("summ1");
        String summ2 = s1.getStringExtra("summ2");
        String input=s1.getStringExtra("input");
        textarea2.setText(summ1);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("SUMMARY");
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int c = wordcount(textarea2.getText().toString());
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

        summary2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isummary2.putExtra("summ2", summ2);
                isummary2.putExtra("input",input);
                startActivity(isummary2);
                summary2.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        summary2.setEnabled(true);

                    }
                }, 1000);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = textarea2.getText().toString();
                Intent ishare = new Intent(Intent.ACTION_SEND);
                ishare.putExtra(Intent.EXTRA_TEXT, data);
                ishare.setType("text/plain");
                startActivity(Intent.createChooser(ishare, "Share Via:"));
                share.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        share.setEnabled(true);

                    }
                }, 1500);
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Summary", textarea2.getText().toString());
                cb.setPrimaryClip(clip);
                Toast toast = new Toast(getApplicationContext());
                View view2 = getLayoutInflater().inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast));
                toast.setView(view2);
                EditText tst = view2.findViewById(R.id.toast_tf);
                tst.setText("Text Copied!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
                copy.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        copy.setEnabled(true);

                    }
                }, 800);

            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textarea2.getText().toString().length() > 10) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, STORAGE_CODE);

                        } else {
                            savePdf();

                        }
                    } else {
                        savePdf();
                    }
                }
                else {
                    Toast toast = new Toast(getApplicationContext());
                    View view2 = getLayoutInflater().inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast));
                    toast.setView(view2);
                    EditText tst = view2.findViewById(R.id.toast_tf);
                    tst.setText("Text Given Was too small!");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                download.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        download.setEnabled(true);

                    }
                }, 1500);
            }
        });

        textarea2.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                if (view.getId() == R.id.textarea2) {
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

    private void savePdf() {
        EditText textarea2 = findViewById(R.id.textarea2);
        Document mDoc = new Document();
        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        mFileName=mFileName.toString().replaceAll(":",".");
        String mFilePath = Environment.getExternalStoragePublicDirectory("Download") + "/" + mFileName + ".pdf";

        try {
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            mDoc.open();
            String mText = textarea2.getText().toString();
            mDoc.addAuthor("");
            mDoc.add(new Paragraph(mText));
            mDoc.close();
            Toast.makeText(this, mFileName + ".pdf\nis Saved to Download Folder of this Device", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePdf();
                } else {
                    Toast.makeText(this, "PERMISSION DENIED...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

        } else if(item.getItemId()==R.id.rate){
            try{
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
            }
            catch(ActivityNotFoundException e){
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/detail?id=" + BuildConfig.APPLICATION_ID)));
            }
        }
        else{
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
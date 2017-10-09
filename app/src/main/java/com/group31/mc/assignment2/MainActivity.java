package com.group31.mc.assignment2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.BoolRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import android.os.Handler;
import android.widget.Toast;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {


    Handler uiHandler = new Handler();
    static boolean stopGraph;
    GraphView detailView;
    Object lock = new Object();
    DatabaseManager db;
    ProgressDialog dialog = null;
    final String uploadFilePath = "/tmp/";
    final String uploadFileName = "group32.db";
    String upLoadServerUri = null;
    int serverResponseCode = 0;


    private Runnable task1 = new Runnable() {
        @Override
        public void run() {


            while (stopGraph == false) {
                uiHandler.post(new Runnable() {
                    public void run() {
                        Log.i("ui update alive?", String.valueOf(graphThread.isAlive()));


                        //                        Toast.makeText(MainActivity.this, "This is my Toast message!",
                        //                                Toast.LENGTH_LONG).show();

                        float[] array = new float[10];
//                        Random r = new Random();
//                        for (int i = 0; i < 100; i++) {
//                            array[i] = r.nextFloat() * 100;
//                        }
                        Cursor c = db.returnLastTenSecondsData();
                        int i = 0;
                        while(c.moveToNext()){
                            Log.i("values from database ", c.getString(1)+ "timestamp " + c.getString(0) );
                            array[i] = c.getFloat(1);
                            i++;

                        }

                        detailView.setValues(array);
                        detailView.invalidate();
                        Log.i("inside graph update", String.valueOf(stopGraph));

                    }

                });


                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(stopGraph == true)
                {
                    try {
                        waitThread();
                        Log.i("inside graph update", String.valueOf(stopGraph));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }



        }
    };

    private Runnable task2 = new Runnable() {
        @Override
        public void run() {

//            uiHandler.removeCallbacks(task1);
            stopGraph = true;
            Log.i("inside graph clear", String.valueOf(stopGraph));


            uiHandler.postAtFrontOfQueue(new Runnable() {
                public void run() {
//                        Toast.makeText(MainActivity.this, "This is my Toast message!",
//                                Toast.LENGTH_LONG).show();

                    float[] array = new float[10];
//                        Random r = new Random();
                    for (int i = 0; i < 10  ; i++) {
                        array[i] = (float) 0.0;
                    }

                    detailView.setValues(array);
//                        detailView.refreshDrawableState();

                    detailView.invalidate();
                    Log.i("task2","done task 2");
                    Log.i("ui update alive?", String.valueOf(graphThread.isAlive()));


                }

            });
        }
    };
    Thread graphThread = new Thread(task1);
//    Thread clearGraph = new Thread(task2);

    protected synchronized void waitThread() throws InterruptedException {

        wait();
    }
    protected synchronized void interruptGraphThread() throws InterruptedException {

        notify();
    }
    public ProgressDialog getProgressDialog() {
        return dialog;
    }


    boolean firstCall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseManager(this);
        db.createTable();


        stopGraph = false;

        float[] array = new float[10];

        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            array[i] = r.nextFloat() * 100;
        }
        String[] hor_titles = {"1","22222","32222"};
        String[] ver_titles = {"1","23232","32222"};

        detailView = new GraphView(this,array,"hello",hor_titles,ver_titles,true);

        final ConstraintLayout cns = (ConstraintLayout) findViewById(R.id.constraint_layout);
        LinearLayout graph_container = (LinearLayout) findViewById(R.id.linear_layout_graph);
        graph_container.addView(detailView);

        final Button start_bn = (Button)findViewById(R.id.start);
        final Button stop_bn = (Button)findViewById(R.id.stop);
        final Button submit_bn = (Button)findViewById(R.id.Enter);
        final EditText name = (EditText)findViewById(R.id.patient_name);
        final EditText id = (EditText)findViewById(R.id.patient_id);
        final EditText age = (EditText)findViewById(R.id.patient_age);
        final EditText gender = (EditText)findViewById(R.id.patient_sex);
        final Button upload_bn = (Button)findViewById(R.id.upload);

        final Intent serviceIntent = new Intent(MainActivity.this, AccelerometerDataService.class);


        submit_bn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v){


                MainActivity.this.startService(serviceIntent);

//                db.insertIntoTable((float)1.09, (float)2.08, (float)1.008);
//
//                db.insertIntoTable((float)1.04, (float)12.08, (float)14.008);
//
//                db.insertIntoTable((float)133.09, (float)26.08, (float)15.008);



            }
        });

        upload_bn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v){
                upLoadServerUri = "http://drowsier-threshold.000webhostapp.com";
                dialog = ProgressDialog.show(MainActivity.this, "", "Uploading Database ", true);

                MainActivity.this.stopService(serviceIntent);
                Log.i("upload","upload started");

                String path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/Data/CSE535_ASSIGNMENT2/").getAbsolutePath();

                new UploadDatabase().execute(path);


            }

        });


        start_bn.setEnabled(true);
        stop_bn.setEnabled(true);



        start_bn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("hii","before");

                stopGraph = false;
                if(firstCall){
                    firstCall = false;
                    graphThread.start();
                } else {

                try {
                    interruptGraphThread();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }
                start_bn.setEnabled(false);
                stop_bn.setEnabled(true);

            }
        });
        stop_bn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                stopGraph = true;

                Thread clearGraph = new Thread(task2);

                clearGraph.start();
                try {
                    clearGraph.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                start_bn.setEnabled(true);
                stop_bn.setEnabled(false);
            }
        });
    }


    class UploadDatabase extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            final String uploadString = params[0];

            Log.i("upload",uploadString+"/group32.db");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadFile(uploadString+"/group32.db");
                }
            });
            t.start();

            for (int count = 0; count <= 1000 && t.isAlive(); count++) {

                publishProgress(count);
            }
            return "Upload Completed.";
        }

        @Override
        protected void onPostExecute(String result) {

//            progressBar.setVisibility(View.GONE);
//            txt.setText(result);
//            btn.setText("Restart");
        }

        @Override
        protected void onPreExecute() {
//            txt.setText("Task Starting...");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            txt.setText("Running..."+ values[0]);
//            progressBar.setProgress(values[0]);
        }

        public int uploadFile(String sourceFileUri) {


            String fileName = sourceFileUri;

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            Log.i("sourcefile", sourceFileUri);
            File sourceFile = new File(sourceFileUri);

            if (!sourceFile.isFile()) {

                dialog.dismiss();

                Log.e("uploadFile", "Source File not exist :"
                        + uploadFilePath + "" + uploadFileName);

                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.show(MainActivity.this, "", "Source File not exist :"
                                + uploadFilePath + "" + uploadFileName, true);
                    }
                });

                return 0;

            } else {
                try {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    Log.i("URL",upLoadServerUri);
                    URL url = new URL(upLoadServerUri);



                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    Log.i("upload","in try");

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        Log.i("writing","file");

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    Log.i("upload","server should respond");
                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {

                        runOnUiThread(new Runnable() {
                            public void run() {

                                String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                        + " http://www.androidexample.com/media/uploads/"
                                        + uploadFileName;

//                                messageText.setText(msg);
                                Toast.makeText(MainActivity.this, "File Upload Complete.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {

                    dialog.dismiss();
                    ex.printStackTrace();

                    runOnUiThread(new Runnable() {
                        public void run() {
//                            messageText.setText("MalformedURLException Exception : check script url.");
                            Toast.makeText(MainActivity.this, "MalformedURLException",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {

                    dialog.dismiss();
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        public void run() {
//                            messageText.setText("Got Exception : see logcat ");
                            Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Upload file to server", "Exception : "
                            + e.getMessage(), e);
                }
                dialog.dismiss();
                return serverResponseCode;

            } // End else block
        }
    }

}


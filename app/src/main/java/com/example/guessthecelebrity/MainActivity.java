package com.example.guessthecelebrity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    TextView textView2;
    ImageView imageView;
    TextView textView;
    Button b1;
    Button b2;
    Button b3;
    Button b4;

    ArrayList<String> celebritiesUrl = new ArrayList<>();
    ArrayList<String> celebritiesName = new ArrayList<>();

    String[] answer = new String[4];
    String result;
    int score = 0;
    int questions = 0;
    int chosenCelebrity = 0;
    int correctAnswer = 0;

    public static SharedPreferences sharedpreferences;
    public static final String mypreference = "LocalFolder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageView2 = findViewById(R.id.imageView2);
        final Button bStart = findViewById(R.id.bStart);
        final Button bUpdate=findViewById(R.id.bUpdate);

        textView2 = findViewById(R.id.textView2);

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView2.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                bStart.setVisibility(View.GONE);
                bUpdate.setVisibility(View.GONE);


                new CountDownTimer(1001, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        System.out.println(millisUntilFinished / 1000 + 1);
                    }

                    @Override
                    public void onFinish() {
                        run();
                    }
                }.start();
            }
        });


        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);
    }


    public void run() {

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        if (!sharedpreferences.contains("arrayListUrls")) {
            DownloadTask downloadTask = new DownloadTask();

            try {

                result = downloadTask.execute("https://www.imdb.com/list/ls052283250/").get();

                Pattern pattern1 = Pattern.compile("src=\"(.*?)\"\n" +
                        "width");
                Pattern pattern2 = Pattern.compile("pst\"\n" +
                        "> <img alt=\"(.*?)\"");
                Matcher matcher1 = pattern1.matcher(result);
                Matcher matcher2 = pattern2.matcher(result);

                while (matcher1.find()) {
                    celebritiesUrl.add(matcher1.group(1));
                }
                while (matcher2.find()) {
                    celebritiesName.add(matcher2.group(1));
                }

                try {
                    sharedpreferences.edit().putString("arrayListUrls", ObjectSerializer.serialize(celebritiesUrl)).apply();
                    sharedpreferences.edit().putString("arrayListNames", ObjectSerializer.serialize(celebritiesName)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                createQuestion();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            try {

                celebritiesUrl = (ArrayList<String>) ObjectSerializer.deserialize(MainActivity.sharedpreferences.getString("arrayListUrls", ObjectSerializer.serialize(new ArrayList<>())));
                celebritiesName = (ArrayList<String>) ObjectSerializer.deserialize(MainActivity.sharedpreferences.getString("arrayListNames", ObjectSerializer.serialize(new ArrayList<>())));

            } catch (IOException e) {
                e.printStackTrace();
            }

            createQuestion();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void createQuestion() {

        Random random = new Random();
        chosenCelebrity = random.nextInt(celebritiesUrl.size());

        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap celebritiesImage;

        try {

            celebritiesImage = imageDownloader.execute(celebritiesUrl.get(chosenCelebrity)).get();
            imageView.setImageBitmap(celebritiesImage);
            textView2.setVisibility(View.GONE);
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);
            b4.setVisibility(View.VISIBLE);

            correctAnswer = random.nextInt(4);

            int incorrectAnswer;

            for (int i = 0; i < 4; i++) {

                if (i == correctAnswer) {
                    answer[i] = celebritiesName.get(chosenCelebrity);
                } else {
                    incorrectAnswer = random.nextInt(celebritiesUrl.size());
                    while (incorrectAnswer == chosenCelebrity) {
                        incorrectAnswer = random.nextInt(celebritiesUrl.size());
                    }
                    answer[i] = celebritiesName.get(incorrectAnswer);
                }

            }

            b1.setText(answer[0]);
            b2.setText(answer[1]);
            b3.setText(answer[2]);
            b4.setText(answer[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            try {

                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void choose(View view) {

        if (view.getTag().toString().equals(Integer.toString(correctAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_SHORT).show();
            score++;
        } else {
            Toast.makeText(getApplicationContext(), "Nope. The correct answer is " + celebritiesName.get(chosenCelebrity), Toast.LENGTH_SHORT).show();
        }
        questions++;
        textView.setText(score + " correct answer(s) out of " + questions);

        createQuestion();

    }

    public void update(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want do download everything again?\nThis proccess will take a few moments.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        celebritiesUrl=new ArrayList<>();
                        celebritiesName=new ArrayList<>();

                        SharedPreferences.Editor editor = MainActivity.sharedpreferences.edit();
                        editor.remove("arrayListUrls").commit();
                        editor.remove("arrayListNames").commit();
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}

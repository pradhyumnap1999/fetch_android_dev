package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView mTextViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewResult = findViewById(R.id.text_view_result);
        RetrieveDataTask myTask = new RetrieveDataTask(this);
        myTask.execute();
    }
}

 class RetrieveDataTask extends AsyncTask<Void, Void, String> {
     private MainActivity mMainActivity;

     public RetrieveDataTask(MainActivity mainActivity) {
         mMainActivity = mainActivity;
     }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            Log.e("RetrieveDataTask", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            mMainActivity.mTextViewResult.setText("Unable to retrieve data");
            return;
        }

        Gson gson = new Gson();
        List<Item> items = gson.fromJson(result, new TypeToken<List<Item>>() {}.getType());

        // Filter out items where "name" is blank or null
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.getName() == null || item.getName().isEmpty()) {
                iterator.remove();
            }
        }

        // Group items by "listId" and add names as array list by just extracting values to it
        Map<Integer,List<Integer>> groupedValues = new HashMap<>();
        for (Item item : items) {
            int listId = item.getListId();

            String str1 = item.getName();
            str1 = str1.replaceAll("[^0-9]", " ");
            str1 = str1.replaceAll(" +", " ");
            String str2 = str1.substring(1,str1.length());
            int a = Integer.parseInt(str2);
            if(!groupedValues.containsKey(listId)){
                groupedValues.put(listId, new ArrayList<>());
            }
            groupedValues.get(listId).add(a);

        }
        for(int i: groupedValues.keySet()){
            Collections.sort(groupedValues.get(i));
        }

        // Display the results to the user
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();

        for (Map.Entry<Integer, List<Integer>> entry : groupedValues.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> values = entry.getValue();

            System.out.println("Key: " + key);
            System.out.print("Values: ");
            sb.append(" List ID: ").append(key).append("\n");
            for (Integer value : values) {
                System.out.print(value + " ");
                sb.append("     Name: ").append(value).append("\n");
            }
            sb.append("\n");
            System.out.println();
        }
        mMainActivity.mTextViewResult.setText(sb.toString());
    }
}

 class Item {
    private int listId;
    private String name;

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.example.tutorial6;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoadCSV extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private LineChart lineChart;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);
        backButton = (Button) findViewById(R.id.button_back);
        lineChart = (LineChart) findViewById(R.id.line_chart);

        Button FindFileBtn = (Button) findViewById(R.id.FindFile);
        FindFileBtn.setOnClickListener(new View.OnClickListener() {
            //@RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.FileName);
                String FileName= editText.getText().toString();
                Pair<ArrayList<String[]>, Integer> csvContent = CsvRead("/sdcard/csv_dir/"+FileName+".csv");
                ArrayList<String[]> csvData = csvContent.first;
                Integer estimatedSteps = csvContent.second;

                LineDataSet lineDataSetX =  new LineDataSet(DataValues(csvData, 1),"X");
                LineDataSet lineDataSetY =  new LineDataSet(DataValues(csvData,2),"Y");
                LineDataSet lineDataSetZ =  new LineDataSet(DataValues(csvData,3),"Z");
                lineDataSetY.setColor(Color.rgb(99,99,99));
                lineDataSetY.setCircleColor(Color.rgb(99,99,99));

                lineDataSetZ.setColor(Color.rgb(200,200,200));
                lineDataSetZ.setCircleColor(Color.rgb(200,200,200));
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(lineDataSetX);
                dataSets.add(lineDataSetY);
                dataSets.add(lineDataSetZ);

                LineData data = new LineData(dataSets);
                lineChart.setData(data);
                lineChart.invalidate();

                // Find the TextView by its ID and update its text
                TextView tvEstimatedSteps = (TextView) findViewById(R.id.tv_estimated_steps);
                tvEstimatedSteps.setText("Estimated Steps: " + estimatedSteps);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickBack();
            }
        });
    }


    private Pair<ArrayList<String[]>, Integer> CsvRead(String path){
        Integer EstimatedSteps = 0;
        ArrayList<String[]> CsvData = new ArrayList<>();
        try {
            File file = new File(path);
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextline;
            Integer count = 1;
            while((nextline = reader.readNext())!= null){
                if(nextline != null && count >= 8 ){
                    CsvData.add(nextline);

                }
                if(nextline != null && count == 5){
                    EstimatedSteps = Integer.valueOf(nextline[1]);
                }
                count+=1;
            }

        }catch (Exception e){}
        return new Pair<ArrayList<String[]>, Integer>(CsvData, EstimatedSteps);
    }



    private ArrayList<Entry> DataValues(ArrayList<String[]> csvData, Integer index){
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        for (int i = 0; i < csvData.size(); i++){

            dataVals.add(new Entry(Float.parseFloat(csvData.get(i)[0]),
                    Float.parseFloat(csvData.get(i)[index])));
        }

        return dataVals;
    }


    private void ClickBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



}


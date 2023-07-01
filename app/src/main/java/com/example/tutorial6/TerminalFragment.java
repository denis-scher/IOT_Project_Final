package com.example.tutorial6;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    int punchNumber = 0;
    int punchFlag = 0;
    int maxPunchPowerFlag = 0;
    int[] numOfEachPunch = {0,0,0};
    private String deviceAddress;
    private SerialService service;

    private enum trainingType {
        POWER,
        SPEED,
        NONE
    }
    trainingType selectedTrainingType;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    LineChart mpLineChart;
    LineDataSet lineDataSetN;
    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    LineData data;

    private int count_real_time = 0;
    private int count_recording = 0;
    private int count_smooth = 0;



    private String stepsNum = "";
    private String fileName = "";
    private String activitySelected = "";

    private Button startBtn;
    private Button stopButton;
    private Button resetButton;
    private Button saveButton;

    private Button goButton;

    private boolean recording = false;

    private ArrayList<Entry> X_entries;
    private ArrayList<Entry> Y_entries;
    private ArrayList<Entry> Z_entries;
    private ArrayList<Entry> T_entries;

    private ArrayList<Entry> Force_entries;
    private float time_recording = 0;
    private float time = 0;
    String strDate;
    Queue<Float> norm_lst;
    Queue<Float> smooth_lst;
    private int estimated_punch_count = 0;
    private TextView punch_counter;
    private TextView power_punch_counter;
    private TextView punchingPower;
    private TextView timer;
    private Spinner timeSpinner;
    private Spinner modeSpinner;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
        selectedTrainingType = trainingType.NONE;
        power_punch_counter.setText("0");
        punch_counter.setText("Number of Punches");
        timer.setText("Timer");
        punchNumber = 0;
        estimated_punch_count = 0;
        for (int i = 0; i < numOfEachPunch.length; i++) {
            numOfEachPunch[i] = 0;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        mpLineChart = (LineChart) view.findViewById(R.id.line_chart);
        lineDataSetN =  new LineDataSet(emptyDataValues(), "N");
        lineDataSetN.setColor(Color.rgb(99,99,99));
        lineDataSetN.setCircleColor(Color.rgb(99,99,99)); // set the color of data points to red
        lineDataSetN.setDrawValues(false);


        dataSets.add(lineDataSetN);
        data = new LineData(dataSets);
        mpLineChart.setData(data);
        mpLineChart.invalidate();

        Button buttonCsvShow = (Button) view.findViewById(R.id.openBtn);

        // Define Spinner and the options to choose from
        modeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);
        timeSpinner = (Spinner) view.findViewById(R.id.time_spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.activity_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);

        startBtn = view.findViewById(R.id.startBtn);
        stopButton = view.findViewById(R.id.stopBtn);
        resetButton = view.findViewById(R.id.resetBtn);
        saveButton = view.findViewById(R.id.saveBtn);
        goButton = view.findViewById(R.id.goBtn);
        timer = view.findViewById(R.id.timer);
        punch_counter = view.findViewById(R.id.punch_counter);
        punch_counter.setFreezesText(true);
        power_punch_counter = view.findViewById(R.id.punch_counter_power);
        punchingPower = view.findViewById(R.id.punch_power);
        X_entries = new ArrayList<Entry>();
        Y_entries = new ArrayList<Entry>();
        Z_entries = new ArrayList<Entry>();
        T_entries = new ArrayList<Entry>();
        Force_entries = new ArrayList<Entry>();
        norm_lst = new LinkedList<>();
        smooth_lst = new LinkedList<>();

        // Set up click listeners for each button
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when Start button is pressed
                if (!recording){
                    TODO: Toast.makeText(getActivity(), "Recording started", Toast.LENGTH_LONG).show();

//                    if (activitySelected.equals("")){
//                        Toast.makeText(getActivity(), "Please choose an activity before recording", Toast.LENGTH_LONG).show();
//                        return;
//                    }
                    if(activitySelected.equals("Training - Speed")){
                        Toast.makeText(getActivity(), "test", Toast.LENGTH_LONG).show();
                    }
                    recording = true;
                    time_recording = time;

                    Date currentTime = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    strDate = dateFormat.format(currentTime);


                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording){
                    Toast.makeText(getActivity(), "There is no active recording", Toast.LENGTH_LONG).show();
                } else {
                    recording = false;
                    Toast.makeText(getActivity(), "Recording stopped", Toast.LENGTH_LONG).show();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                count_recording = 0;
                count_real_time = 0;

                X_entries.clear();
                Y_entries.clear();
                Z_entries.clear();

                LineData data = mpLineChart.getData();
                ILineDataSet setN = data.getDataSetByIndex(0);

                while(setN.removeLast()){}

                recording = false;
                estimated_punch_count = 0;
                punch_counter.setText("Steps: " + estimated_punch_count);

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count_recording == 0) {
                    Toast.makeText(getActivity(), "There is no active recording", Toast.LENGTH_LONG).show();
                } else {
                    if (recording) {
                        Toast.makeText(getActivity(), "You need to stop the recording first", Toast.LENGTH_LONG).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Enter File Name");

                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String fileName = input.getText().toString();

                            File file = new File("/sdcard/csv_dir/");
                            file.mkdirs();
                            String csv = "/sdcard/csv_dir/" + fileName + ".csv";
                            CSVWriter csvWriter = null;
                            try {
                                csvWriter = new CSVWriter(new FileWriter(csv, true));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            csvWriter.writeNext(new String[]{"NAME:", fileName});
                            csvWriter.writeNext(new String[]{"EXPERIMENT TIME:", strDate});
                            csvWriter.writeNext(new String[]{"ACTIVITY TYPE:", activitySelected});
                            csvWriter.writeNext(new String[]{"COUNT OF ACTUAL STEPS:", stepsNum});
                            csvWriter.writeNext(new String[]{"ESTIMATED NUMBER OF STEPS:", String.valueOf(estimated_punch_count)});
                            csvWriter.writeNext(new String[]{"", ""});
                            csvWriter.writeNext(new String[]{"Time [sec]", "ACC X", "ACC Y", "ACC Z"});

                            for (int i = 0; i < count_recording; i++) {
                                csvWriter.writeNext(new String[]{String.valueOf(T_entries.get(i).getY() / 1000), String.valueOf(X_entries.get(i).getY()), String.valueOf(Y_entries.get(i).getY()), String.valueOf(Z_entries.get(i).getY())});
                            }

                            try {
                                csvWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            count_recording = 0;
                            count_real_time = 0;
                            estimated_punch_count = 0;
                            punch_counter.setText("Steps: " + estimated_punch_count);


                            X_entries.clear();
                            Y_entries.clear();
                            Z_entries.clear();

                            LineData data = mpLineChart.getData();
                            ILineDataSet setN = data.getDataSetByIndex(0);

                            while (setN.removeLast()) {
                            }

                            Toast.makeText(getActivity(), "Saved successfully", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = modeSpinner.getSelectedItem().toString();
                int time = Integer.parseInt(timeSpinner.getSelectedItem().toString());

                if (text.equals("Power Training")){
                    new CountDownTimer(3000, 1000) { // 3000 milli seconds is 3 seconds.

                        public void onTick(long millisUntilFinished) {
                            // You can display the time left here every second.
                            timer.setText("Get Ready: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            // Start your main timer after 3 seconds have passed.
                            selectedTrainingType = trainingType.POWER;
                            new CountDownTimer(time *1000, 1000) { // This timer is just an example, adjust as needed.

                                public void onTick(long millisUntilFinished) {
                                    timer.setText("Time remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    timer.setText("Done!");
                                    openStrengthTrainingActivity();

                                }
                            }.start();
                        }
                    }.start();
                } else if (text.equals("Speed Training")) {
                    new CountDownTimer(3000, 1000) { // 3000 milli seconds is 3 seconds.

                        public void onTick(long millisUntilFinished) {
                            // You can display the time left here every second.
                            timer.setText("Get Ready: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            // Start your main timer after 3 seconds have passed.
                            selectedTrainingType = trainingType.SPEED;
                            new CountDownTimer(time *1000, 1000) { // This timer is just an example, adjust as needed.

                                public void onTick(long millisUntilFinished) {
                                    timer.setText("Time remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    timer.setText("Done!");
                                    openSpeedTrainingActivity();

                                }
                            }.start();
                        }
                    }.start();
                } else if (text.equals("Fight")) {
                    openFightActivity();
                }



            }
        });


        // Set the listener to capture selected option
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    // This is the "Select Activity" item, so do nothing
                }
                else {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    activitySelected = selectedItem;
                    Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                    activitySelected = selectedItem;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Code to perform some action when nothing is selected
            }
        });



        buttonCsvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenLoadCSV();

            }
        });

        return view;
    }

    /*
     * Serial + UI
     */
    private String[] clean_str(String[] stringsArr){
         for (int i = 0; i < stringsArr.length; i++)  {
             stringsArr[i]=stringsArr[i].replaceAll(" ","");
             stringsArr[i] = stringsArr[i].replaceAll("(\\r|\\n)", "");
        }


        return stringsArr;
    }
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.d("TerminalFragment", "Connecting to device: " + deviceAddress);  // ADD THIS
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }


    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }






    private void receive(byte[] message) {

            String msg = new String(message);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                String msg_to_save = msg;
                msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
                // check message length
                if (msg_to_save.length() > 1){
                // split message string by ',' char
                String[] parts = msg_to_save.split(",");
                // function to trim blank spaces
                parts = clean_str(parts);



                float accX = Float.parseFloat(parts[0]);
                float accY = Float.parseFloat(parts[1]);
                float accZ = Float.parseFloat(parts[2]);
                float F = Float.parseFloat(parts[3]);
                time = Float.parseFloat(parts[4]);


                if (recording){
                    Log.d("T", "recording");
                    X_entries.add(new Entry(count_recording, accX));
                    Y_entries.add(new Entry(count_recording, accY));
                    Z_entries.add(new Entry(count_recording, accZ));
                    T_entries.add(new Entry(count_recording, time - time_recording));
                    Force_entries.add(new Entry(count_recording, F));
                    count_recording++;
                }

                // Calculate the norm of the acceleration
                float N = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
                smooth_lst.add(N);
                count_smooth++;


                if (smooth_lst.size() < 10){
                    return;
                } else {
                    if (count_smooth % 3 == 0){
                        float sum = 0.0f;
                        for(Float f : smooth_lst) {
                            sum += f;
                        }

                        float average = sum / smooth_lst.size();
                        data.addEntry(new Entry(count_real_time,average),0);
                        count_real_time++;

                        lineDataSetN.notifyDataSetChanged(); // let the data know a dataSet changed
                        mpLineChart.notifyDataSetChanged(); // let the chart know it's data changed
                        mpLineChart.invalidate(); // refresh

                        norm_lst.add(average);
                        if (norm_lst.size() == 5){
                            Float[] normArray = new Float[norm_lst.size()];
                            normArray = norm_lst.toArray(normArray);


                            if (! Python.isStarted()) {
                                Python.start(new AndroidPlatform(getActivity()));
                            }


                            Python py = Python.getInstance();
                            PyObject pyf = py.getModule("main"); // Python file name (without .py)
                            PyObject obj = pyf.callAttr("detect_peak", normArray); // Python function name
                            boolean result = obj.toBoolean();
                            if(selectedTrainingType == trainingType.SPEED) {
                                if (result) {
                                    estimated_punch_count++;
                                    power_punch_counter.setText(Integer.toString(estimated_punch_count));
                                }
                            }

                            norm_lst.clear();
                        }
                    }
                    smooth_lst.poll();

                }
            }

            }

    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            Log.d("TerminalFragment", "Received: " + new String(data) + "Array: " + Arrays.toString(numOfEachPunch));  // ADD THIS
            if (selectedTrainingType == trainingType.POWER) {
                String[] parts = new String(data).split(",");
                parts = clean_str(parts);
                String the_power_val = parts[4];
                punchingPower.setText(the_power_val);
                int power = Integer.parseInt(the_power_val);
                if (power > 0 && power < 500) {
                    punchingPower.setBackgroundColor(Color.RED);
                    if (punchFlag == 0) {
                        maxPunchPowerFlag = 1;
                    }
                    punchFlag = 1;
                } else if (power >= 500 && power < 3000) {
                    punchingPower.setBackgroundColor(Color.YELLOW);
                    if (punchFlag == 0 || maxPunchPowerFlag == 1) {
                        maxPunchPowerFlag = 2;
                    }
                    punchFlag = 1;
                } else if (power >= 3000) {
                    punchingPower.setBackgroundColor(Color.GREEN);
                    if (maxPunchPowerFlag == 0 || maxPunchPowerFlag == 1 || maxPunchPowerFlag == 2) {
                        maxPunchPowerFlag = 3;
                    }
                    punchFlag = 1;
                } else {
                    punchingPower.setBackgroundColor(Color.GRAY);
                    if (punchFlag == 1) {
                        punchNumber = punchNumber + 1;
                        punchFlag = 0;
                        power_punch_counter.setText(String.valueOf(punchNumber));
                        if (maxPunchPowerFlag == 1) {
                            numOfEachPunch[0] = numOfEachPunch[0] + 1;
                        } else if (maxPunchPowerFlag == 2) {
                            numOfEachPunch[1] = numOfEachPunch[1] + 1;
                        } else if (maxPunchPowerFlag == 3) {
                            numOfEachPunch[2] = numOfEachPunch[2] + 1;
                        }
                        maxPunchPowerFlag = 0;
                    }
                }
            }


            receive(data);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    private ArrayList<Entry> emptyDataValues()
    {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        return dataVals;
    }

    private void OpenLoadCSV(){
        Intent intent = new Intent(getContext(),LoadCSV.class);
        startActivity(intent);
    }

    private void openStrengthTrainingActivity(){
        Intent intent = new Intent(getContext(), StrengthTraining.class);
        int[] message = numOfEachPunch;
        intent.putExtra("KEY_MESSAGE", message);
        intent.putExtra("TIME_TRAINED", timeSpinner.getSelectedItem().toString());
        startActivity(intent);
    }

    private void openSpeedTrainingActivity(){
        Intent intent = new Intent(getContext(), SpeedTraining.class);
        intent.putExtra("TIME_TRAINED", timeSpinner.getSelectedItem().toString());
        intent.putExtra("NUM_OF_PUNCHES", estimated_punch_count);
        startActivity(intent);
    }

    private void openFightActivity(){
        Intent intent = new Intent(getContext(), FightActivity.class);

        startActivity(intent);
    }
}

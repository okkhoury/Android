package dubstepboard.okkhoury.com.edmpad;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class EDMActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    Button loop1;
    Button loop2;
    Button loop3;

    private static Context context;

    // Set up the looping sound pools
    private static SoundPool spLoop1 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    private static SoundPool spLoop2 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    private static SoundPool spLoop3 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

    //Boolean variables to tell if a loop is being recorded
    private static boolean loop1IsRecording;
    private static boolean loop2IsRecording;
    private static boolean loop3IsRecording;

    //Queues to store the sounds in the loops
    private Queue<Integer> loop1SoundQueue = new ArrayBlockingQueue<Integer>(1000);
    private Queue<Integer> loop2SoundQueue = new ArrayBlockingQueue<Integer>(1000);
    private Queue<Integer> loop3SoundQueue = new ArrayBlockingQueue<Integer>(1000);

    //Queues to store the pauses between sounds in the loops
    private static Queue<Long> loop1PauseQueue = new ArrayBlockingQueue<Long>(1000);
    private static Queue<Long> loop2PauseQueue = new ArrayBlockingQueue<Long>(1000);
    private static Queue<Long> loop3PauseQueue = new ArrayBlockingQueue<Long>(1000);

    // number to store the time at which the previous sound finsihed playing (used for loops)
    private static long loop1EndOfLastSound;
    private static long loop2EndOfLastSound;
    private static long loop3EndOfLastSound;

    // Keep track of which loops are currently being played
    private boolean loop1IsOn = false;
    private boolean loop2IsOn = false;
    private boolean loop3IsOn = false;


    //Recording
    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonResetRecording ;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edm);

        context = getApplicationContext();

        // Prevent screen from getting dim
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_dropdown);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dropdown_list, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP);


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new EDMFragment1();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

        // Set up the looping sound pools with on load listeners
        setUpLoopSP(spLoop1, loop1SoundQueue);
        setUpLoopSP(spLoop2, loop2SoundQueue);
        setUpLoopSP(spLoop3, loop3SoundQueue);

        loop1 = (Button) findViewById(R.id.loop1);
        loop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loop1.getText().equals("Loop 1")) {
                    loop1.setText("Stop");
                    loop1EndOfLastSound = System.currentTimeMillis();
                    loop1IsRecording = true;
                } else if (loop1.getText().equals("Stop")) {
                    loop1.setText("End");
                    Log.d("OWEN OWEN", loop1SoundQueue.toString());
                    Log.d("OWEN OWEN", loop1PauseQueue.toString());
                    // Add the time from last sound pressed to clicking stop
                    // Delete the first element from the queue.
                    // Don't want to include time from first press of loop to first sound press
                    loop1PauseQueue.add(System.currentTimeMillis() - loop1EndOfLastSound);
                    loop1PauseQueue.remove();

                    loop1IsOn = true;
                    loop1IsRecording = false;

                    runLoop1();
                } else if (loop1.getText().equals("End")) {
                    loop1.setText("Loop 1");

                    loop1IsOn = false;
                    loop1SoundQueue.clear();
                    loop1PauseQueue.clear();
                }
            }
        });
        loop2 = (Button) findViewById(R.id.loop2);
        loop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loop2.getText().equals("Loop 2")) {
                    loop2.setText("Stop");
                    loop2EndOfLastSound = System.currentTimeMillis();
                    loop2IsRecording = true;
                } else if (loop2.getText().equals("Stop")) {
                    loop2.setText("End");

                    loop2IsOn = true;
                    loop2PauseQueue.add(System.currentTimeMillis() - loop2EndOfLastSound);
                    loop2PauseQueue.remove();
                    loop2IsRecording = false;

                    runLoop2();
                } else if (loop2.getText().equals("End")) {
                    loop2.setText("Loop 2");

                    loop2IsOn = false;
                    loop2SoundQueue.clear();
                    loop2PauseQueue.clear();
                }
            }
        });
        loop3 = (Button) findViewById(R.id.loop3);
        loop3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loop3.getText().equals("Loop 3")) {
                    loop3.setText("Stop");
                    loop3EndOfLastSound = System.currentTimeMillis();
                    loop3IsRecording = true;
                } else if (loop3.getText().equals("Stop")) {
                    loop3.setText("End");

                    loop3IsOn = true;
                    loop3PauseQueue.add(System.currentTimeMillis() - loop3EndOfLastSound);
                    loop3PauseQueue.remove();
                    loop3IsRecording = false;

                    runLoop3();
                } else if (loop3.getText().equals("End")) {
                    loop3.setText("Loop 3");

                    loop3IsOn = false;
                    loop3SoundQueue.clear();
                    loop3PauseQueue.clear();
                }
            }
        });

        buttonStart = (Button) findViewById(R.id.startRecording);
        buttonStop = (Button) findViewById(R.id.stopRecording);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.playRecording);
        buttonResetRecording = (Button)findViewById(R.id.stopPlayRecording);

        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonResetRecording.setEnabled(false);

        buttonStart.setVisibility(View.VISIBLE);
        buttonStop.setVisibility(View.GONE);
        buttonPlayLastRecordAudio.setVisibility(View.GONE);
        buttonResetRecording.setVisibility(View.GONE);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    CreateAudioFileName() + "AudioRecording.3gp";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    buttonStart.setVisibility(View.GONE);
                    buttonStop.setVisibility(View.VISIBLE);

                    //Toast.makeText(EDMActivity.this, "Recording started", Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonResetRecording.setEnabled(true);
                buttonStart.setEnabled(false);

                buttonStop.setVisibility(View.GONE);
                buttonPlayLastRecordAudio.setVisibility(View.VISIBLE);
                buttonResetRecording.setVisibility(View.VISIBLE);

                //Toast.makeText(EDMActivity.this, "Recording Completed", Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                //Toast.makeText(EDMActivity.this, "Recording Playing", Toast.LENGTH_LONG).show();
            }
        });

        buttonResetRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonPlayLastRecordAudio.setEnabled(false);

                buttonPlayLastRecordAudio.setVisibility(View.GONE);
                buttonResetRecording.setVisibility(View.GONE);
                buttonStart.setVisibility(View.VISIBLE);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.i("Here", "I got here");
        Fragment fragment = null;
        switch (pos) {
            case 0:
                releaseEverything();
                fragment = new EDMFragment1(); // Gradient
                break;
            case 1:
                releaseEverything();
                fragment = new EDMFragment2(); // Ethereal
                break;
            case 2:
                releaseEverything();
                fragment = new EDMFragment3(); // AutoPilot
                break;
            case 3:
                releaseEverything();
                fragment = new EDMFragment4(); // Dreamers
                break;
            case 4:
                releaseEverything();
                fragment = new EDMFragment5(); // Roses
                break;
            default:
                releaseEverything();
                fragment = new EDMFragment1(); // Ethereal
                break;
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // Fill in the sound and pause for the respective queues
    public static void handleLoopingQueues(final int resID) {
        if (loop1IsRecording) {
            Long time = System.currentTimeMillis() - loop1EndOfLastSound;
            loop1PauseQueue.add(time);
            loop1EndOfLastSound = System.currentTimeMillis();
            spLoop1.load(context, resID, 0);
        }

        if (loop2IsRecording) {
            Long time = System.currentTimeMillis() - loop2EndOfLastSound;
            loop2PauseQueue.add(time);
            loop2EndOfLastSound = System.currentTimeMillis();
            spLoop2.load(context, resID, 0);
        }

        if (loop3IsRecording) {
            Long time = System.currentTimeMillis() - loop3EndOfLastSound;
            loop3PauseQueue.add(time);
            loop3EndOfLastSound = System.currentTimeMillis();
            spLoop3.load(context, resID, 0);
        }
    }

    // Stop playing the loops if the user exits the app
    @Override
    public void onPause() {
        super.onPause();

        loop1SoundQueue.clear();
        loop1PauseQueue.clear();
        loop1IsOn = false;

        loop2SoundQueue.clear();
        loop2PauseQueue.clear();
        loop2IsOn = false;

        loop3SoundQueue.clear();
        loop3PauseQueue.clear();
        loop3IsOn = false;
    }

    public void releaseEverything() {

        loop1.getText().equals("Loop 1");
        loop2.getText().equals("Loop 2");
        loop3.getText().equals("Loop 3");

        loop1.setText("Loop 1");
        loop2.setText("Loop 2");
        loop3.setText("Loop 3");

        loop1IsRecording = false;
        loop2IsRecording = false;
        loop3IsRecording = false;

        loop1IsOn = false;
        loop2IsOn = false;
        loop3IsOn = false;

        loop1SoundQueue.clear();
        loop2SoundQueue.clear();
        loop3SoundQueue.clear();

        loop1PauseQueue.clear();
        loop2PauseQueue.clear();
        loop3PauseQueue.clear();
    }

    // Adds on load listeners to the looping sound pools
    public void setUpLoopSP(SoundPool spLoop, final Queue<Integer> soundQueue) {
        spLoop.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundQueue.add(sampleId);
                Log.d("hi owen", "Sound added");
            }
        });
    }

    public void runLoop1 () {
        final Handler handler1 = new Handler();

        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (loop1IsOn && !loop1SoundQueue.isEmpty()) {
                    int soundID = loop1SoundQueue.remove();
                    loop1SoundQueue.add(soundID);
                    spLoop1.play(soundID, (float).9, (float).9, 0, 0, 1);

                    // Pop the pause off the top and move it to the back of the queue
                    Long pause = loop1PauseQueue.remove();
                    loop1PauseQueue.add(pause);

                    handler1.postDelayed(this, pause);
                } else {
                    handler1.removeCallbacks(this);
                    loop1PauseQueue.clear();
                    loop1SoundQueue.clear();
                }

            }
        }, 0);
    }

    public void runLoop2 () {
        final Handler handler2 = new Handler();

        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (loop2IsOn && !loop2SoundQueue.isEmpty()) {
                    int soundID = loop2SoundQueue.remove();
                    loop2SoundQueue.add(soundID);
                    spLoop2.play(soundID, (float).9, (float).9, 0, 0, 1);

                    // Pop the pause off the top and move it to the back of the queue
                    Long pause = loop2PauseQueue.remove();
                    loop2PauseQueue.add(pause);

                    handler2.postDelayed(this, pause);
                } else {
                    handler2.removeCallbacks(this);
                    loop2SoundQueue.clear();
                    loop2PauseQueue.clear();
                }
            }
        }, 0);
    }

    public void runLoop3 () {
        final Handler handler3 = new Handler();

        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (loop3IsOn && !loop3SoundQueue.isEmpty()) {
                    int soundID = loop3SoundQueue.remove();
                    loop3SoundQueue.add(soundID);
                    spLoop3.play(soundID, (float).9, (float).9, 0, 0, 1);

                    // Pop the pause off the top and move it to the back of the queue
                    Long pause = loop3PauseQueue.remove();
                    loop3PauseQueue.add(pause);

                    handler3.postDelayed(this, pause);
                } else {
                    handler3.removeCallbacks(this);
                    loop3SoundQueue.clear();
                    loop3PauseQueue.clear();
                }
            }
        }, 0);
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateAudioFileName(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        //Log.i("Date:", (dateFormat.format(date)).toString());
        return (dateFormat.format(date)).toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(EDMActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(EDMActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(EDMActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}
package dubstepboard.okkhoury.com.edmpad;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class EDMFragment3 extends Fragment {

    // Set up the sound pools
    final SoundPool spNormal = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    final SoundPool spHold1 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    final SoundPool spHold2 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

    // Set up the looping sound pools
    private SoundPool spLoop1 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    private SoundPool spLoop2 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    private SoundPool spLoop3 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

    //Boolean variables to tell if a loop is being recorded
    private boolean loop1IsRecording;
    private boolean loop2IsRecording;
    private boolean loop3IsRecording;

    //Queues to store the sounds in the loops
    private Queue<Integer> loop1SoundQueue = new ArrayBlockingQueue<Integer>(1000);
    private Queue<Integer> loop2SoundQueue = new ArrayBlockingQueue<Integer>(1000);
    private Queue<Integer> loop3SoundQueue = new ArrayBlockingQueue<Integer>(1000);

    //Queues to store the pauses between sounds in the loops
    private Queue<Long> loop1PauseQueue = new ArrayBlockingQueue<Long>(1000);
    private Queue<Long> loop2PauseQueue = new ArrayBlockingQueue<Long>(1000);
    private Queue<Long> loop3PauseQueue = new ArrayBlockingQueue<Long>(1000);

    // number to store the time at which the previous sound finsihed playing (used for loops)
    private long loop1EndOfLastSound;
    private long loop2EndOfLastSound;
    private long loop3EndOfLastSound;

    // Keep track of which loops are currently being played
    private boolean loop1IsOn = false;
    private boolean loop2IsOn = false;
    private boolean loop3IsOn = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        // set up view and inflate the xml layout
        View v;   // = new View(getActivity());
        v = inflater.inflate(R.layout.fragment3_edm, parent, false);


        // Set up the buttons
        final Button soundButton1 = (Button) v.findViewById(R.id.soundButton1);
        final Button soundButton2 = (Button) v.findViewById(R.id.soundButton2);
        final Button soundButton3 = (Button) v.findViewById(R.id.soundButton3);
        final Button soundButton4 = (Button) v.findViewById(R.id.soundButton4);
        final Button soundButton5 = (Button) v.findViewById(R.id.soundButton5);
        final Button soundButton6 = (Button) v.findViewById(R.id.soundButton6);
        final Button soundButton7 = (Button) v.findViewById(R.id.soundButton7);
        final Button soundButton8 = (Button) v.findViewById(R.id.soundButton8);
        final Button soundButton9 = (Button) v.findViewById(R.id.soundButton9);
        final Button soundButton10 = (Button) v.findViewById(R.id.soundButton10);
        final Button soundButton11 = (Button) v.findViewById(R.id.soundButton11);
        final Button soundButton12 = (Button) v.findViewById(R.id.soundButton12);

        // set up the sounds
        final int sound1ID = spHold1.load(getActivity(), R.raw.ap1, 1);
        final int sound2ID = spHold2.load(getActivity(), R.raw.ap2, 1);

        final int sound3ID = spNormal.load(getActivity(), R.raw.ap3, 1);
        final int sound4ID = spNormal.load(getActivity(), R.raw.ap4, 1);
        final int sound5ID = spNormal.load(getActivity(), R.raw.ap5, 1);
        final int sound6ID = spNormal.load(getActivity(), R.raw.ap6, 1);
        final int sound7ID = spNormal.load(getActivity(), R.raw.ap7, 1);
        final int sound8ID = spNormal.load(getActivity(), R.raw.ap8, 1);
        final int sound9ID = spNormal.load(getActivity(), R.raw.ap9, 1);
        final int sound10ID = spNormal.load(getActivity(), R.raw.ap10, 1);
        final int sound11ID = spNormal.load(getActivity(), R.raw.ap11, 1);
        final int sound12ID = spNormal.load(getActivity(), R.raw.ap12, 1);


        // wire up the top button, which you hold down to play
        setUpHoldDownButton(soundButton1, sound1ID, spHold1);
        setUpHoldDownButton(soundButton2, sound2ID, spHold2);
        //setUpHoldDownButton(soundButton3, sound3ID, spHold3);

        // wire up the rest of the button, which you touch to play
        setUpRegularButton(soundButton3, sound4ID, spNormal, R.raw.ap3);
        setUpRegularButton(soundButton4, sound4ID, spNormal, R.raw.ap4);
        setUpRegularButton(soundButton5, sound5ID, spNormal, R.raw.ap5);
        setUpRegularButton(soundButton6, sound6ID, spNormal, R.raw.ap6);
        setUpRegularButton(soundButton7, sound7ID, spNormal, R.raw.ap7);
        setUpRegularButton(soundButton8, sound8ID, spNormal, R.raw.ap8);
        setUpRegularButton(soundButton9, sound9ID, spNormal, R.raw.ap9);
        setUpRegularButton(soundButton10, sound10ID, spNormal, R.raw.ap10);
        setUpRegularButton(soundButton11, sound11ID, spNormal, R.raw.ap11);
        setUpRegularButton(soundButton12, sound12ID, spNormal, R.raw.ap12);

        // Set up the looping sound pools with on load listeners
        setUpLoopSP(spLoop1, loop1SoundQueue);
        setUpLoopSP(spLoop2, loop2SoundQueue);
        setUpLoopSP(spLoop3, loop3SoundQueue);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sound_board_bar, menu);

        // Set it so that the home button can't be used as a button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().getActionBar().setHomeButtonEnabled(false);
        }
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

    }

    public void setUpRegularButton(final Button soundButton, final int sound, final SoundPool sp, final int resID) {
        soundButton.setSoundEffectsEnabled(false);
        soundButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        soundButton.setPressed(true);
                        sp.play(sound, 1, 1, 0, 0, 1);
                        handleLoopingQueues(resID);
                    }
                    break;

                    case MotionEvent.ACTION_UP: {
                        soundButton.setPressed(false);
                    }
                    break;
                }
                return true;
            }
        });
    }


    // Fill in the sound and pause for the respective queues
    public void handleLoopingQueues(final int resID) {
        if (loop1IsRecording) {
            Long time = System.currentTimeMillis() - loop1EndOfLastSound;
            loop1PauseQueue.add(time);
            loop1EndOfLastSound = System.currentTimeMillis();
            spLoop1.load(getActivity(), resID, 0);
        }

        if (loop2IsRecording) {
            Long time = System.currentTimeMillis() - loop2EndOfLastSound;
            loop2PauseQueue.add(time);
            loop2EndOfLastSound = System.currentTimeMillis();
            spLoop2.load(getActivity(), resID, 0);
        }

        if (loop3IsRecording) {
            Long time = System.currentTimeMillis() - loop3EndOfLastSound;
            loop3PauseQueue.add(time);
            loop3EndOfLastSound = System.currentTimeMillis();
            spLoop3.load(getActivity(), resID, 0);
        }
    }

    // Stop playing the loops if the user exits the app
    @Override
    public void onPause() {
        super.onPause();
        loop1SoundQueue.clear();
        loop1PauseQueue.clear();

        loop2SoundQueue.clear();
        loop2PauseQueue.clear();

        loop3SoundQueue.clear();
        loop3PauseQueue.clear();

        loop1IsOn = false;
        loop2IsOn = false;
        loop3IsOn = false;
    }

    // Handle all of the actions for button clicks on the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.loop1:
                if (item.getTitle().equals("LOOP1")) {
                    item.setTitle("STOP1");
                    loop1EndOfLastSound = System.currentTimeMillis();
                    loop1IsRecording = true;
                    return true;
                } else if (item.getTitle().equals("STOP1")) {
                    item.setTitle("END1");
                    // Add the time from last sound pressed to clicking stop
                    // Delete the first element from the queue.
                    // Don't want to include time from first press of loop to first sound press
                    loop1PauseQueue.add(System.currentTimeMillis() - loop1EndOfLastSound);
                    loop1PauseQueue.remove();

                    loop1IsOn = true;

                    loop1IsRecording = false;
                    //loopTask = new loopingTask();
                    //loopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

                    runLoop1();
                    return true;
                } else if (item.getTitle().equals("END1")) {
                    item.setTitle("LOOP1");

                    loop1IsOn = false;
                    loop1SoundQueue.clear();
                    loop1PauseQueue.clear();
                    //loopTask.cancel(true);
                    return true;
                }
                break;

            case R.id.loop2:
                if (item.getTitle().equals("LOOP2")) {
                    item.setTitle("STOP2");
                    loop2EndOfLastSound = System.currentTimeMillis();
                    loop2IsRecording = true;
                    return true;
                } else if (item.getTitle().equals("STOP2")) {
                    item.setTitle("END2");

                    loop2IsOn = true;

                    loop2PauseQueue.add(System.currentTimeMillis() - loop2EndOfLastSound);
                    loop2PauseQueue.remove();

                    loop2IsRecording = false;

                    runLoop2();

                    return true;
                } else if (item.getTitle().equals("END2")) {
                    item.setTitle("LOOP2");

                    loop2IsOn = false;

                    loop2SoundQueue.clear();
                    loop2PauseQueue.clear();

                    return true;
                }
                break;

            case R.id.loop3:
                if (item.getTitle().equals("LOOP3")) {
                    item.setTitle("STOP3");
                    loop3EndOfLastSound = System.currentTimeMillis();
                    loop3IsRecording = true;
                    return true;
                } else if (item.getTitle().equals("STOP3")) {
                    item.setTitle("END3");

                    loop3IsOn = true;

                    loop3PauseQueue.add(System.currentTimeMillis() - loop3EndOfLastSound);
                    loop3PauseQueue.remove();

                    loop3IsRecording = false;

                    runLoop3();

                    return true;
                } else if (item.getTitle().equals("END3")) {
                    item.setTitle("LOOP3");

                    loop3IsOn = false;

                    loop3SoundQueue.clear();
                    loop3PauseQueue.clear();

                    return true;
                }
                break;

            case R.id.preset_1:
                releaseEverything();
                moveToNewPreset("preset1"); // Ethereal
                return true;

            case R.id.preset_2:
                releaseEverything();
                moveToNewPreset("preset2"); // Ethereal
                return true;

            case R.id.preset_3:
                return true;

            case R.id.preset_4:
                releaseEverything();
                moveToNewPreset("preset4"); // Ethereal
                return true;

            case R.id.preset_5:
                releaseEverything();
                moveToNewPreset("preset5"); // Ethereal
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;

    }

    public void moveToNewPreset (String preset) {
        if (preset.equals("preset1")) {
            Fragment fragment = new EDMFragment1();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } else if (preset.equals("preset2")) {
            Fragment fragment = new EDMFragment2();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } else if (preset.equals("preset3")) {
            Fragment fragment = new EDMFragment3();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } else if (preset.equals("preset4")) {
            Fragment fragment = new EDMFragment4();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } else if (preset.equals("preset5")) {
            Fragment fragment = new EDMFragment5();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }
    }

    public void releaseEverything() {
        spNormal.release();
        spHold1.release();
        spHold2.release();

        loop1IsOn = false;
        loop2IsOn = false;
        loop3IsOn = false;

        spLoop1.release();
        spLoop2.release();
        spLoop3.release();

        loop1SoundQueue.clear();
        loop2SoundQueue.clear();
        loop3SoundQueue.clear();

        loop1PauseQueue.clear();
        loop2PauseQueue.clear();
        loop3PauseQueue.clear();
    }

    public void setUpHoldDownButton(final Button soundButton, final int sound, final SoundPool sp) {
        soundButton.setSoundEffectsEnabled(false);
        soundButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        soundButton.setPressed(true);
                        sp.play(sound, 1, 1, 0, 0, 1);
                    }
                    break;

                    case MotionEvent.ACTION_UP: {
                        sp.stop(sp.play(sound, 1, 1, 0, 0, 1));
                        soundButton.setPressed(false);
                    }
                    break;
                }
                return true;
            }
        });
    }

    // Adds on load listeners to the looping sound pools
    public void setUpLoopSP(SoundPool spLoop, final Queue<Integer> soundQueue) {
        spLoop.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundQueue.add(sampleId);
                //Log.d("hi owen", "Sound added");
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


//    // These three AsyncTasks handle the loops in the background
//    class loopingTask extends AsyncTask<Object, Object, Void> {
//        @Override
//        protected Void doInBackground(Object... arg0) {
//            if (loop1SoundQueue.size() != 0) {
//                while (!this.isCancelled()) {
//
//                    // Play the next sound in the queue
//                    int soundID = loop1SoundQueue.remove();
//                    loop1SoundQueue.add(soundID);
//                    spLoop1.play(soundID, (float).9, (float).9, 0, 0, 1);
//
//                    // Pop the pause off the top and move it to the back of the queue
//                    Long pause = loop1PauseQueue.remove();
//                    loop1PauseQueue.add(pause);
//
//                    try {
//                        Thread.sleep(pause);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            return null;
//        }
//    }
//
//    class loopingTask2 extends AsyncTask<Object, Object, Void> {
//        @Override
//        protected Void doInBackground(Object... arg0) {
//            if (loop2SoundQueue.size() != 0) {
//                while (!this.isCancelled()) {
//
//                    int soundID = loop2SoundQueue.remove();
//                    loop2SoundQueue.add(soundID);
//                    spLoop2.play(soundID, (float).9, (float).9, 0, 0, 1);
//
//                    // Pop the pause off the top and move it to the back of the queue
//                    Long pause = loop2PauseQueue.remove();
//                    loop2PauseQueue.add(pause);
//                    try {
//                        Thread.sleep(pause);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            return null;
//        }
//    }
//
//    class loopingTask3 extends AsyncTask<Object, Object, Void> {
//        @Override
//        protected Void doInBackground(Object... arg0) {
//            if (loop3SoundQueue.size() != 0) {
//                while (!this.isCancelled()) {
//
//                    int soundID = loop3SoundQueue.remove();
//                    loop3SoundQueue.add(soundID);
//                    spLoop3.play(soundID, (float).9, (float).9, 0, 0, 1);
//
//                    // Pop the pause off the top and move it to the back of the queue
//                    Long pause = loop3PauseQueue.remove();
//                    loop3PauseQueue.add(pause);
//                    try {
//                        Thread.sleep(pause);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            return null;
//        }
//    }

}

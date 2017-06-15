package dubstepboard.okkhoury.com.edmpad;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

public class EDMFragment2 extends Fragment {

    // Set up the sound pools
    final SoundPool spNormal = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    final SoundPool spHold1 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    final SoundPool spHold2 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    final SoundPool spHold3 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

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
        v = inflater.inflate(R.layout.fragment2_edm, parent, false);


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
        final int sound1ID = spHold1.load(getActivity(), R.raw.gradient1, 1);
        final int sound2ID = spHold2.load(getActivity(), R.raw.gradient2, 1);
        final int sound3ID = spHold3.load(getActivity(), R.raw.gradient3, 1);

        final int sound4ID = spNormal.load(getActivity(), R.raw.ethereal4, 1);
        final int sound5ID = spNormal.load(getActivity(), R.raw.gradient5, 1);
        final int sound6ID = spNormal.load(getActivity(), R.raw.gradient6, 1);
        final int sound7ID = spNormal.load(getActivity(), R.raw.gradient7, 1);
        final int sound8ID = spNormal.load(getActivity(), R.raw.gradient8, 1);
        final int sound9ID = spNormal.load(getActivity(), R.raw.gradient9, 1);
        final int sound10ID = spNormal.load(getActivity(), R.raw.gradient10, 1);
        final int sound11ID = spNormal.load(getActivity(), R.raw.gradient11, 1);
        final int sound12ID = spNormal.load(getActivity(), R.raw.gradient12, 1);


        // wire up the top button, which you hold down to play
        setUpHoldDownButton(soundButton1, sound1ID, spHold1);
        setUpHoldDownButton(soundButton2, sound2ID, spHold2);
        setUpHoldDownButton(soundButton3, sound3ID, spHold3);

        // wire up the rest of the button, which you touch to play
        setUpRegularButton(soundButton4, sound4ID, spNormal, R.raw.ethereal4);
        setUpRegularButton(soundButton5, sound5ID, spNormal, R.raw.gradient5);
        setUpRegularButton(soundButton6, sound6ID, spNormal, R.raw.gradient6);
        setUpRegularButton(soundButton7, sound7ID, spNormal, R.raw.gradient7);
        setUpRegularButton(soundButton8, sound8ID, spNormal, R.raw.gradient8);
        setUpRegularButton(soundButton9, sound9ID, spNormal, R.raw.gradient9);
        setUpRegularButton(soundButton10, sound10ID, spNormal, R.raw.gradient10);
        setUpRegularButton(soundButton11, sound11ID, spNormal, R.raw.gradient11);
        setUpRegularButton(soundButton12, sound12ID, spNormal, R.raw.gradient12);

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

        // Set the action bar to black
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

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
                        EDMActivity.handleLoopingQueues(resID);
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
}

package dif.clogic.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;
import dif.clogic.other.Accompaniment;
import dif.clogic.other.ChordReference;
import dif.clogic.other.DbOpenHelper;
import dif.clogic.other.Sound;
import dif.clogic.view.DrawView;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 10.
 * Time: 오후 10:44
 * To change this template use File | Settings | File Templates.
 */
public class TestActivity extends Activity {

    private DbOpenHelper mDbOpenHelper;
    MelodyView melodyView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbOpenHelper = new DbOpenHelper(TestActivity.this);
        melodyView = new MelodyView(this, getIntent().getStringArrayListExtra("recordData"));
        setContentView(melodyView);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                melodyView.saveFile();
                finish();
                break;
        }
        return true;
    }

    public class MelodyView extends DrawView {

        private int nowSound;
        private ArrayList<Sound> playEndSoundList;

        private ArrayList<String> recordList;

        public MelodyView(Context context, ArrayList<String> recordData) {
            super(context);

            recordList = recordData;

            playEndSoundList = new ArrayList<Sound>();

            beatSequence = ChordReference.beat2;
            beatSequenceIdx = 0;
            virtualPitch = 0;

            final int[][] colorPackage = ChordReference.redPackage;

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    //To change body of implemented methods use File | Settings | File Templates.
                    if(System.currentTimeMillis() - count < 120) {
                        try {
                            Thread.sleep(120 - (System.currentTimeMillis() - count));
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }

                    codeSequence = colorPackage[(beatSequenceIdx/beatSequence.length)%colorPackage.length];

                    if(beatSequenceIdx/beatSequence.length < 1) {
                        soundPool.play(soundFileTable.get(ChordReference.beatList[ChordReference.beatReady[beatSequenceIdx]]), 1, 1, 0, 0, 1);
                        beatSequenceIdx++;
                        count = System.currentTimeMillis();
                        return;
                    }

                    for(int i=0; i<playEndSoundList.size(); i++) {
                        Sound sound = playEndSoundList.get(i);

                        if(sound.volume > 0.0f) {
                            sound.volume -= 0.24f;
                            soundPool.setVolume(sound.streamId, sound.volume, sound.volume);
                        }
                    }

                    soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                    String raw = "";
                    if((beatSequenceIdx%2 == 0)) { // 이걸 바꾸어야한닷

                        for(int i=0; i<recordList.size(); i++) {
                            String accompaniment = recordList.get(i);
                            String[] chip = accompaniment.split(" ");

                            if(!chip[beatSequenceIdx/2 % chip.length].equals("R"))
                                soundPool.play(soundFileTable.get(chip[beatSequenceIdx/2 % chip.length]), 0.3f, 0.3f, 0, 0, 1);
                        }

                        /*if(isTouching == true) {
                            raw = ChordReference.melodyList[codeSequence[virtualPitch%codeSequence.length]];
                        }*/
                        if(raw.equals("")) {
                            raw = "R";
                        }
                        record += raw + " ";
                    }

                    count = System.currentTimeMillis();
                    beatSequenceIdx++;
                }
            };

            timer.schedule(timerTask, 120, 120);
            count = System.currentTimeMillis();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            if(beatSequenceIdx/beatSequence.length < 1)
                return false;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for(int i=0;i<codeSequence.length; i++) {
                        /*if(event.getY() >= (outSize.y / codeSequence.length * i) &&
                                (event.getY() < outSize.y / codeSequence.length * (i+1))) {
                            nowSound = soundPool.play(soundFileTable.get(ChordReference.melodyList2[codeSequence[i]]), 1, 1, 0, 0, 1);
                        }*/
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_UP:
                    //playEndSoundList.add(new Sound(nowSound));
                    return false;
            }
            return false;
        }

        public void saveFile() {
            SimpleDateFormat fomatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
            Date currentTime = new Date();
            String filename = fomatter.format(currentTime);

            try {
                mDbOpenHelper.open();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            String accompaniment = "";
            for(int i=0; i<recordList.size(); i++) {
                accompaniment += recordList.get(i);
                if(i < recordList.size()-1)
                    accompaniment += ",";
            }
            long result = mDbOpenHelper.insertMelodyColumn(filename, record, accompaniment);

            String fileMelodySequence = Accompaniment.convert(record);
            String strlist[] = fileMelodySequence.split(" ");

            // 멜로디는 새로만드러야됨...

            ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();

            MidiTrack tempoTrack = new MidiTrack();

            TimeSignature ts = new TimeSignature();
            ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

            Tempo t = new Tempo();
            t.setBpm(125);

            tempoTrack.insertEvent(ts);
            tempoTrack.insertEvent(t);

            tracks.add(tempoTrack);

            //////// for문으로 만들기
            for(int i=0; i<recordList.size(); i++) { // measure길이 재는거 추가하기. ((beatSequenceIdx/beatSequence.length >= 1) && beatSequenceIdx%8) == 1 measure

                MidiTrack noteTrack = new MidiTrack();
                int measure = beatSequenceIdx;
                measure = measure - 8;
                measure = measure + measure%8;
                measure = measure / 8;

                for(int idx=0; idx<measure; idx++) { // measure. 1 measure에 4박자
                    String[] chip = recordList.get(i).split(" ");

                    for(String str : chip) {
                        int channel = i+1;
                        int pitch = 0;
                        int velocity = 100;

                        switch(str.charAt(0)) {
                            case 'C':
                                pitch = 0;
                                break;
                            case 'D':
                                pitch = 2;
                                break;
                            case 'E':
                                pitch = 4;
                                break;
                            case 'F':
                                pitch = 5;
                                break;
                            case 'G':
                                pitch = 7;
                                break;
                            case 'A':
                                pitch = 9;
                                break;
                            case 'B':
                                pitch = 11;
                                break;
                            case 'R':
                                pitch = 0;
                                break;
                            default:
                                break;
                        }

                        if (str.length() > 1) {
                            if (str.charAt(1) == 'S') {
                                pitch++;
                            }
                            pitch = pitch + 12 * (str.charAt(2) - '0' + 1) - 4;
                        }
                        noteTrack.insertNote(channel, pitch, velocity, idx * 240, 240);
                    }
                }
                tracks.add(noteTrack);

                MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

                File output = new File(Environment.getExternalStorageDirectory() + File.separator + filename + ".mid");
                try {
                    midi.writeToFile(output);
                } catch (IOException e) {
                    System.err.println(e);
                }

                /*MediaPlayer mPlayer = new MediaPlayer();
                try {
                    String str = Environment.getExternalStorageDirectory().getAbsolutePath();
                    str += File.separator;
                    str += filename;
                    str += ".mid";
                    mPlayer.setDataSource(str);
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                mPlayer.start(); // 작동확인.
                */
            }
        }
    }
}
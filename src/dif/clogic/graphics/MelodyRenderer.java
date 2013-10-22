package dif.clogic.graphics;

import android.content.Context;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;
import dif.clogic.other.ChordReference;
import dif.clogic.other.DbOpenHelper;
import dif.clogic.other.Sound;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 19.
 * Time: 오후 2:34
 * To change this template use File | Settings | File Templates.
 */
public class MelodyRenderer extends GLRenderer {

    private Thread thread;
    private DbOpenHelper mDbOpenHelper;
    private float bpm = 60.0f;
    private float bpmTimer = 0.0f;

    private int[] beatSequence = ChordReference.beat2;
    private int beatSequenceIdx = 0;

    private int[] codeSequence = ChordReference.C;

    private SoundPool soundPool;
    private HashMap<String, Integer> soundFileTable;

    private ArrayList<Sound> playEndSoundList;
    private ArrayList<Sound> playSoundList;

    private String record;
    private ArrayList<String> recordList;

    public MelodyRenderer(Context context, Thread pThread, ArrayList<String> recordData) {
        super(context);
        thread = pThread;
        recordList = recordData;

        mDbOpenHelper = new DbOpenHelper(context);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        soundPool = new SoundPool(256, AudioManager.STREAM_MUSIC, 0);
        soundFileTable = new HashMap<String, Integer>();
        playSoundList = new ArrayList<Sound>();
        playEndSoundList = new ArrayList<Sound>();

        for(int i=0; i< ChordReference.accompanimentList.length; i++) {
            soundFileTable.put(ChordReference.accompanimentList[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.accompanimentList[i], "raw", "dif.clogic.druzic"), 0));
        }

        for(int i=0; i<ChordReference.beatList.length; i++) {
            soundFileTable.put(ChordReference.beatList[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.beatList[i], "raw", "dif.clogic.druzic"), 0));
        }

        for(int i=0; i<ChordReference.melodyList2.length; i++) {
            soundFileTable.put(ChordReference.melodyList2[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.melodyList2[i], "raw", "dif.clogic.druzic"), 0));
        }

        record = "";

        beatSequenceIdx = 0;
    }

    @Override
    public void Initialize(GL10 gl) {
        //To change body of implemented methods use File | Settings | File Templates.
        spriteBundle = new SpriteBundle();

        thread.start();
    }

    @Override
    public void onDestroy() {
        // 파일 저장 코드
        saveFile();

        mDbOpenHelper.close();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl, eglConfig);

        this.Initialize(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        super.onSurfaceChanged(gl, w, h);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        spriteBundle.draw();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF touchPoint = new PointF(event.getX(), this.windowHeight - event.getY());

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                for(int i=0; i<codeSequence.length; i++) { // code package 순서정리..
                    if(event.getY() >= (windowHeight / codeSequence.length * i) &&
                            (event.getY() < windowHeight / codeSequence.length * (i+1))) {
                        playSoundList.add(new Sound(ChordReference.melodyList2[codeSequence[codeSequence.length - 1 - i]]));
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:

                for(Sound sound : playSoundList) {
                    if(sound.isStart && !sound.isEnd) {
                        playEndSoundList.add(sound);
                    }
                }

                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void update(float dt) {
        spriteBundle.update(dt);

        if(bpmTimer >= (bpm/60.0f)/8) { // 반의 반박자마다 한번씩 들어감
            bpmTimer = 0.0f;

            codeSequence = ChordReference.redPackage[(beatSequenceIdx/beatSequence.length)%ChordReference.redPackage.length];

            soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

            if(beatSequenceIdx%2 == 0) { // 반박자마다 들어감
                // melody Sequence
                for(Sound sound : playSoundList) {
                    if(sound.isEnd)
                        continue;

                    sound.isStart = true;
                    if(!sound.isPlaying) {
                        sound.streamId = soundPool.play(soundFileTable.get(sound.refName), sound.volume, sound.volume, 0, 0, 1);
                        sound.isPlaying = true;
                    } else {
                        if(sound.beatTerm < 8) {
                            sound.beatTerm++;
                        }
                    }
                }

                ArrayList<Sound> removeList = new ArrayList<Sound>();
                for(Sound sound : playEndSoundList) {
                    sound.isPlaying = false;
                    sound.isEnd = true;

                    if(sound.volume > 0.0f) {
                        sound.volume -= 0.24f;
                        soundPool.setVolume(sound.streamId, sound.volume, sound.volume);
                    } else {
                        record += sound.refName.replace("_", "") + "_" + sound.beatTerm + " ";
                        soundPool.stop(sound.streamId);
                        removeList.add(sound);
                    }
                }
                playEndSoundList.removeAll(removeList);
                removeList.clear();

                if(playEndSoundList.isEmpty()) {
                    record += "R" + " ";
                }
            }
            beatSequenceIdx++;
        }
        bpmTimer += dt;

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

        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();

        MidiTrack tempoTrack = new MidiTrack();

        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

        Tempo t = new Tempo();
        t.setBpm(60);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        tracks.add(tempoTrack);
        // 아래는 Accompaniment 구현 후에.

    }
}

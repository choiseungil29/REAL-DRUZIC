package dif.clogic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.EditText;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;
import dif.clogic.graphics.GLRenderer;
import dif.clogic.graphics.SpriteBundle;
import dif.clogic.other.ChordReference;
import dif.clogic.other.DbOpenHelper;
import dif.clogic.other.Sound;
import dif.clogic.sprite.ScreenSprite;
import dif.clogic.texture.Texture;
import dif.clogic.texture.TextureCache;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 22.
 * Time: 오후 9:31
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentActivity extends Activity {

    private AccompanimentView view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view = new AccompanimentView(this);
        setContentView(view);
    }

    public void onPause() {
        super.onPause();
        view.onPause();
    }

    public void onResume() {
        super.onResume();
        view.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        view.onDestroy();
    }

    public class AccompanimentView extends GLSurfaceView {
        private AccompanimentRenderer renderer;
        private GLThread thread;

        public AccompanimentView(Context context) {
            super(context);

            thread = new GLThread();
            renderer = new AccompanimentRenderer(context, thread);

            setRenderer(renderer);
        }

        public void onDestroy() {
            thread.stopThread();
            renderer.onDestroy();
        }

        public boolean onTouchEvent(final MotionEvent event) {
            return renderer.onTouchEvent(event);
        }

        public class GLThread extends Thread {
            private boolean isRun = true;

            public GLThread() {
                isRun = true;
            }

            public void stopThread() {
                isRun = false;
            }

            public void run() {
                float frameRate = 1000.0f/60.0f;
                long time = System.currentTimeMillis();
                while(isRun) {
                    if((System.currentTimeMillis() - time) > frameRate) {
                        renderer.update((System.currentTimeMillis() - time)/1000.0f);
                        requestRender();
                        time = System.currentTimeMillis();
                    }
                }
            }
        }

        public class AccompanimentRenderer extends GLRenderer {

            private GLThread thread;
            private DbOpenHelper mDbOpenHelper;
            private float bpm = 30.0f;
            private float bpmTimer = 0.0f;

            private int[] beatSequence = ChordReference.beatReady;
            private int beatSequenceIdx = 0;

            private int[] codeSequence = ChordReference.C;

            private SoundPool soundPool;
            private HashMap<String, Integer> soundFileTable;

            private List<Sound> playEndSoundList;
            private List<Sound> bePlaySoundList;
            private List<Sound> bePlayingSoundList;

            private String record;

            private MotionEvent event = null;
            private boolean isTouching = false;
            private PointF touchPoint;

            private Handler handler;

            private ScreenSprite[] sprite = new ScreenSprite[10];

            public AccompanimentRenderer(Context context, GLThread pThread) {
                super(context);
                thread = pThread;

                mDbOpenHelper = new DbOpenHelper(context);
                try {
                    mDbOpenHelper.open();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                soundPool = new SoundPool(256, AudioManager.STREAM_MUSIC, 0);
                soundFileTable = new HashMap<String, Integer>();
                playEndSoundList = Collections.synchronizedList(new ArrayList<Sound>());
                bePlaySoundList = Collections.synchronizedList(new ArrayList<Sound>());
                bePlayingSoundList = Collections.synchronizedList(new ArrayList<Sound>());

                for(int i=0; i< ChordReference.accompanimentList.length; i++) {
                    soundFileTable.put(ChordReference.accompanimentList[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.accompanimentList[i], "raw", "dif.clogic.app"), 0));
                }

                for(int i=0; i<ChordReference.beatList.length; i++) {
                    soundFileTable.put(ChordReference.beatList[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.beatList[i], "raw", "dif.clogic.app"), 0));
                }

                for(int i=0; i<ChordReference.melodyList2.length; i++) {
                    soundFileTable.put(ChordReference.melodyList2[i], soundPool.load(context, context.getResources().getIdentifier(ChordReference.melodyList2[i], "raw", "dif.clogic.app"), 0));
                }

                record = "";

                beatSequenceIdx = 0;

                handler = new Handler() {
                    public void handleMessage(Message msg) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentActivity.this);
                        LayoutInflater inflater = AccompanimentActivity.this.getLayoutInflater();
                        final View v = inflater.inflate(R.layout.dialog_signin, null);
                        builder.setTitle("저장");
                        builder.setView(v)
                                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                        EditText editText = (EditText)v.findViewById(R.id.fileName);
                                        SimpleDateFormat fomatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
                                        Date currentTime = new Date();
                                        editText.setHint(fomatter.format(currentTime));
                                        String filename = editText.getText().toString();
                                        if(filename.equals(""))
                                            filename = fomatter.format(currentTime);
                                        saveFile(filename);
                                        Intent intent = new Intent((Activity)mContext, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("저장하지 않음", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                        Intent intent = new Intent((Activity)mContext, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                        builder.show();
                    }
                };
            }

            @Override
            public void Initialize(GL10 gl) {
                //To change body of implemented methods use File | Settings | File Templates.
                spriteBundle = new SpriteBundle();

                for(int i=0; i<11; i++) {
                    String filename = "touch_" + String.format("%02d", i+1);
                    TextureCache.getInstance().addTexture(filename, new Texture(gl, mContext, mContext.getResources().getIdentifier(filename, "drawable", mContext.getPackageName())));
                }

                TextureCache.getInstance().addTexture("screen_mint", new Texture(gl, mContext, mContext.getResources().getIdentifier("touchscreen_mint", "drawable", mContext.getPackageName())));
                TextureCache.getInstance().addTexture("screen_white", new Texture(gl, mContext, mContext.getResources().getIdentifier("touchscreen_white", "drawable", mContext.getPackageName())));

                for(int i=0; i<10; i++) {
                    if(i%2 == 0)
                        sprite[i] = new ScreenSprite(TextureCache.getInstance().getTexture("screen_mint"));
                    else
                        sprite[i] = new ScreenSprite(TextureCache.getInstance().getTexture("screen_white"));
                    sprite[i].setAnchorPoint(0.0f, 0.0f);
                    spriteBundle.addSprite(sprite[i]);
                }

                thread.start();
            }

            @Override
            public void onDestroy() {
                //To change body of implemented methods use File | Settings | File Templates.
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
                spriteBundle.draw(gl);
            }

            @Override
            public void update(float dt) {
                //To change body of implemented methods use File | Settings | File Templates.
                spriteBundle.update(dt);

                for(int i=0; i<10; i++) {
                    if(i < codeSequence.length) {
                        sprite[i].setIsVisible(true);
                        sprite[i].setScale(1.0f, (windowHeight / codeSequence.length) / 100.0f);
                        sprite[i].setPosition(0.0f, windowHeight / codeSequence.length * i);
                    } else {
                        sprite[i].setIsVisible(false);
                    }
                }

                if(beatSequenceIdx/beatSequence.length >= 1) {
                    if(event != null) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN) {
                            for(int i=0; i<codeSequence.length; i++) { // code package 순서정리..
                                if(touchPoint.y >= (windowHeight / codeSequence.length * i) &&
                                        (touchPoint.y < windowHeight / codeSequence.length * (i+1))) {
                                    if(!bePlaySoundList.isEmpty())
                                        bePlaySoundList.clear();
                                    if(bePlayingSoundList.isEmpty())
                                        bePlaySoundList.add(new Sound(ChordReference.melodyList2[codeSequence[i]]));
                                }
                            }
                        }

                        if(event.getAction() == MotionEvent.ACTION_UP) {
                            ArrayList<Sound> removeList = new ArrayList<Sound>();
                            for(Sound sound : bePlayingSoundList) {
                                if(!playEndSoundList.contains(sound)) {
                                    playEndSoundList.add(sound);
                                    removeList.add(sound);
                                }
                            }
                            bePlayingSoundList.removeAll(removeList);
                        }
                    }
                }

                if(bpmTimer >= 1.0f/16.0f) { // /16이 반의 반박자마다 들어가는 코드다.
                    bpmTimer = 0.0f;

                    soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                    if(beatSequenceIdx/beatSequence.length < 1) {
                        beatSequenceIdx++;
                        return;
                    } else {
                        beatSequence = ChordReference.beat1;
                    }

                    if(beatSequenceIdx/beatSequence.length >= 5) {
                        handler.sendEmptyMessage(0);
                        thread.stopThread();
                    }

                    codeSequence = ChordReference.redPackage[((beatSequenceIdx+1)/beatSequence.length-1)%ChordReference.redPackage.length];

                    if(beatSequenceIdx%2 == 0) { // 반박자마다 들어감
                        // melody Sequence
                        {
                            ArrayList<Sound> removeList = new ArrayList<Sound>();
                            for(Sound sound : bePlaySoundList) {
                                if(!bePlayingSoundList.contains(sound)) {
                                    sound.streamId = soundPool.play(soundFileTable.get(sound.refName), sound.volume, sound.volume, 0, 0, 1);
                                    bePlayingSoundList.add(sound);
                                    removeList.add(sound);
                                }
                            }
                            bePlaySoundList.removeAll(removeList);
                        }

                        {
                            ArrayList<Sound> removeList = new ArrayList<Sound>();
                            for(Sound sound : bePlayingSoundList) {
                                if(sound.beatTerm < 8) {
                                    sound.beatTerm++;
                                } else {
                                    if(!playEndSoundList.contains(sound)) {
                                        playEndSoundList.add(sound);
                                        removeList.add(sound);
                                    }
                                }
                            }
                            bePlayingSoundList.removeAll(removeList);
                        }

                        {
                            ArrayList<Sound> removeList = new ArrayList<Sound>();
                            for(Sound sound : playEndSoundList) {
                                if(sound.volume > 0.0f) {
                                    if(sound.volume >= 1.0f) {
                                        String[] str = record.split(" ");
                                        record = "";
                                        for(int i=0; i<str.length-sound.beatTerm; i++) {
                                            record += str[i] + " ";
                                        }
                                        record += sound.refName.replace("_", "") + "_" + sound.beatTerm + " ";
                                    }
                                    sound.volume -= 0.24f;
                                    soundPool.setVolume(sound.streamId, sound.volume, sound.volume);
                                } else {
                                    soundPool.stop(sound.streamId);
                                    removeList.add(sound);
                                }
                            }
                            playEndSoundList.removeAll(removeList);
                        }
                        record += "R" + " ";
                    }
                    beatSequenceIdx++;
                }
                bpmTimer += dt;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                this.event = event;
                touchPoint = new PointF(event.getX(), this.windowHeight - event.getY());

                if(beatSequenceIdx/beatSequence.length < 1)
                    return false;

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouching = true;

                        //TouchSprite sprite = new TouchSprite();
                        //sprite.setPosition(touchPoint.x, touchPoint.y);
                        //spriteBundle.addSprite(sprite);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouching = false;
                        break;
                    default:
                        break;
                }

                return false;
            }

            public void saveFile(String filename) {

                try {
                    mDbOpenHelper.open();
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                record = record.substring(0, record.length()-1);

                mDbOpenHelper.insertAccompanimentColumn(filename, record);
                String strlist[] = record.split(" ");

                MidiTrack tempoTrack = new MidiTrack();
                MidiTrack noteTrack = new MidiTrack();

                TimeSignature ts = new TimeSignature();
                ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

                Tempo t = new Tempo();
                t.setBpm(120);

                tempoTrack.insertEvent(ts);
                tempoTrack.insertEvent(t);

                int everyAllLength = 0;
                for (int idx = 0; idx < strlist.length; idx++) {

                    if(strlist[idx].equals("")) {
                        continue;
                    }

                    int channel = 0;
                    int pitch = 0;
                    int velocity = 100;

                    String str = strlist[idx];
                    switch (str.charAt(0)) {

                        case 'c':
                            pitch = 0;
                            break;
                        case 'd':
                            pitch = 2;
                            break;
                        case 'e':
                            pitch = 4;
                            break;
                        case 'f':
                            pitch = 5;
                            break;
                        case 'g':
                            pitch = 7;
                            break;
                        case 'a':
                            pitch = 9;
                            break;
                        case 'b':
                            pitch = 11;
                            break;
                        case 'R':
                            pitch = 0;
                        default:
                            break;
                    }

                    int length = 1; // if 'R', quater beat.
                    if (str.length() > 1) {
                        pitch = pitch + 12 * ((int)str.charAt(1) - '0');
                        length = (int)str.charAt(3) - '0';
                    }
                    length *= 2;

                    noteTrack.insertNote(channel, pitch, velocity, everyAllLength, length * 120); // 240이면 1박자
                    everyAllLength = everyAllLength + length * 120;
                }

                ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
                tracks.add(tempoTrack);
                tracks.add(noteTrack);

                MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

                File output = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename + ".mid");
                try {
                    midi.writeToFile(output);
                    //isEnd = true;
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }
}
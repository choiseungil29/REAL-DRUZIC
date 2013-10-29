package dif.clogic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import dif.clogic.sprite.TouchSprite;
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

            //thread.start();
            setRenderer(renderer);
        }

        public void onDestroy() {
            thread.stopThread();
            renderer.onDestroy();
        }

        public synchronized boolean onTouchEvent(final MotionEvent event) {
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

            public synchronized void run() {
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
            private float bpm = 75.0f;
            private float bpmTimer = 0.0f;

            private int[] beatSequence = ChordReference.beatReady;
            private int beatSequenceIdx = 0;

            private int[] codeSequence = ChordReference.C;

            private SoundPool soundPool;
            private HashMap<String, Integer> soundFileTable;

            private List<Sound> playEndSoundList;
            private List<Sound> playSoundList;

            private String record;

            private boolean isEnd = false;

            private GL10 gl;

            private Handler handler;

            private ScreenSprite[] sprite = new ScreenSprite[6];

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
                playSoundList = Collections.synchronizedList(new ArrayList<Sound>());

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
                                        ((Activity)mContext).finish();
                                    }
                                })
                                .setNegativeButton("저장하지 않음", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                        ((Activity)mContext).finish();
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

                for(int i=0; i<6; i++) {
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
                this.gl = gl;
                this.Initialize(gl);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                super.onSurfaceChanged(gl, w, h);
                this.gl = gl;
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                super.onDrawFrame(gl);
                spriteBundle.draw(gl);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                PointF touchPoint = new PointF(event.getX(), this.windowHeight - event.getY());

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        for(int i=0; i<codeSequence.length; i++) { // code package 순서정리..
                            if(event.getY() >= (windowHeight / codeSequence.length * i) &&
                                    (event.getY() < windowHeight / codeSequence.length * (i+1))) {
                                synchronized (playSoundList) {
                                    playSoundList.add(new Sound(ChordReference.melodyList2[codeSequence[codeSequence.length - 1 - i]]));
                                }
                            }
                        }

                        TouchSprite sprite = new TouchSprite();
                        sprite.setPosition(touchPoint.x, touchPoint.y);
                        spriteBundle.addSprite(sprite);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        for(Sound sound : playSoundList) {
                            if(sound.isStart && !sound.isEnd) {
                                synchronized (playEndSoundList) {
                                    playEndSoundList.add(sound);
                                }
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
                //To change body of implemented methods use File | Settings | File Templates.
                spriteBundle.update(dt);

                for(int i=0; i<6; i++) {
                    if(i < codeSequence.length) {
                        sprite[i].setIsVisible(true);
                        sprite[i].setScale(sprite[i].getScale().x, (windowHeight / codeSequence.length) / 100.0f);
                        sprite[i].setPosition(0.0f, windowHeight / codeSequence.length * i);
                    } else {
                        sprite[i].setIsVisible(false);
                    }
                }

                if(bpmTimer >= (60.0f/bpm)/8) { // 반의 반박자마다 한번씩 들어감
                    bpmTimer = 0.0f;

                    codeSequence = ChordReference.redPackage[(beatSequenceIdx/beatSequence.length)%ChordReference.redPackage.length];

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


                    if(beatSequenceIdx%2 == 0) { // 반박자마다 들어감
                        // melody Sequence
                        synchronized (playSoundList) {
                            for(Sound sound : playSoundList) {
                                if(sound.isEnd)
                                    continue;

                                sound.isStart = true;
                                if(!sound.isPlaying) {
                                    sound.streamId = soundPool.play(soundFileTable.get(sound.refName), sound.volume, sound.volume, 0, 0, 1);
                                    sound.isPlaying = true;
                                } else {
                                    if(sound.beatTerm < 4) {
                                        sound.beatTerm++;
                                    } else {
                                        playEndSoundList.add(sound);
                                    }
                                }
                            }
                        }

                        synchronized (playEndSoundList) {
                            ArrayList<Sound> removeList = new ArrayList<Sound>();
                            for(Sound sound : playEndSoundList) {
                                sound.isPlaying = false;
                                sound.isEnd = true;

                                if(sound.volume > 0.0f) {
                                    sound.volume -= 0.24f;
                                    soundPool.setVolume(sound.streamId, sound.volume, sound.volume);
                                } else {
                                    record += sound.refName.replace("_", "") + "_" + sound.beatTerm + " ";
                                    Log.i("TEST", "record : " + record);
                                    soundPool.stop(sound.streamId);
                                    removeList.add(sound);
                                }
                            }
                            playEndSoundList.removeAll(removeList);
                        }
                        if(playEndSoundList.isEmpty()) {
                            record += "R" + " ";
                        }
                    }
                    beatSequenceIdx++;
                }
                bpmTimer += dt;
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
                t.setBpm((int)bpm);

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
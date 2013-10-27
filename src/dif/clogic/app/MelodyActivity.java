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
 * Date: 13. 10. 21.
 * Time: 오전 11:07
 * To change this template use File | Settings | File Templates.
 */
public class MelodyActivity extends Activity {

    private MelodyView view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view = new MelodyView(this, getIntent().getStringArrayListExtra("recordData"));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return view.onKeyDown(keyCode, event);
    }

    public class MelodyView extends GLSurfaceView {

        private MelodyRenderer renderer;
        private GLThread thread;

        public MelodyView(Context context, ArrayList<String> recordData) {
            super(context);

            thread = new GLThread();
            renderer = new MelodyRenderer(context, thread, recordData);
            setRenderer(renderer);
        }

        public void onDestroy() {
            thread.stopThread();
            renderer.onDestroy();
        }

        public boolean onTouchEvent(final MotionEvent event) {
            return renderer.onTouchEvent(event);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return renderer.onKeyDown(keyCode, event);
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
    }


    public class MelodyRenderer extends GLRenderer {

        private MelodyView.GLThread thread;
        private DbOpenHelper mDbOpenHelper;
        private float bpm = 60.0f;
        private float bpmTimer = 0.0f;

        private int[] beatSequence = ChordReference.beat2;
        private int beatSequenceIdx = 0;

        private int[] codeSequence = ChordReference.C;

        private SoundPool soundPool;
        private HashMap<String, Integer> soundFileTable;

        private List<Sound> playEndSoundList;
        private List<Sound> playSoundList;

        private ArrayList<AccompanimentSound>[] accompanimentList;
        private ArrayList<AccompanimentSound> accompanimentEndList;
        private int[] accompanimentListIdx;

        private String record;
        private ArrayList<String> recordList;

        private Handler handler;

        public MelodyRenderer(Context context, MelodyView.GLThread pThread, ArrayList<String> recordData) {
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
            playSoundList = Collections.synchronizedList(new ArrayList<Sound>());
            playEndSoundList = Collections.synchronizedList(new ArrayList<Sound>());

            accompanimentListIdx = new int[recordList.size()];
            accompanimentList = new ArrayList[recordList.size()];
            accompanimentEndList = new ArrayList<AccompanimentSound>();

            for(int i=0; i<recordList.size(); i++) {
                accompanimentListIdx[i] = 0;
                accompanimentList[i] = new ArrayList<AccompanimentSound>();

                String[] record = recordList.get(i).split(" ");
                for(int idx=0; idx<record.length; idx++) {
                    String str = record[idx];
                    if(str.equals("R")) {
                        accompanimentList[i].add(new AccompanimentSound("R", 1));
                    } else {
                        accompanimentList[i].add(new AccompanimentSound(str.charAt(0) + "_" + (str.charAt(1)-'0'), str.charAt(3)-'0'));
                    }
                }
            }


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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MelodyActivity.this);
                    LayoutInflater inflater = MelodyActivity.this.getLayoutInflater();
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

            thread.start();
        }

        @Override
        public void onDestroy() {
            // 파일 저장 코드
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

                    synchronized (playSoundList) {
                        for(Sound sound : playSoundList) {
                            if(sound.isStart && !sound.isEnd) {
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

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    handler.sendEmptyMessage(0);
                    thread.stopThread();
            }
            return false;
        }

        @Override
        public void update(float dt) {
            spriteBundle.update(dt);

            if(bpmTimer >= (bpm/60.0f)/12) { // 반의 반박자마다 한번씩 들어감
                bpmTimer = 0.0f;

                codeSequence = ChordReference.redPackage[(beatSequenceIdx/beatSequence.length)%ChordReference.redPackage.length];

                soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                if(beatSequenceIdx%2 == 0) { // 반박자마다 들어감
                    // melody Sequence

                    for(int i=0; i<accompanimentList.length; i++) {
                        ArrayList<AccompanimentSound> arrayList = accompanimentList[i];
                        AccompanimentSound sound = arrayList.get(accompanimentListIdx[i]%arrayList.size());

                        if(sound.refName.equals("R")) {
                            accompanimentListIdx[i]++;
                        } else {
                            if(sound.beatTerm <= 0) {
                                sound.streamId = soundPool.play(soundFileTable.get(sound.refName), sound.volume, sound.volume, 0, 0, 1);
                                sound.beatTerm++;
                            } else if(sound.beatTerm < sound.originBeatTerm) {
                                sound.beatTerm++;
                            } else {
                                sound.beatTerm = 0;
                                accompanimentEndList.add(sound);
                                accompanimentListIdx[i]++;
                            }
                        }
                    }

                    ArrayList<AccompanimentSound> accompanimentRemoveList = new ArrayList<AccompanimentSound>();
                    for(AccompanimentSound sound : accompanimentEndList) {
                        if(sound.volume > 0.0f) {
                            sound.volume -= 0.2f;
                            soundPool.setVolume(sound.streamId, sound.volume, sound.volume);
                        } else {
                            soundPool.stop(sound.streamId);
                            accompanimentRemoveList.add(sound);
                        }
                    }
                    accompanimentEndList.removeAll(accompanimentRemoveList);

                    synchronized (playSoundList) {
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
                                soundPool.stop(sound.streamId);
                                removeList.add(sound);
                            }
                        }
                        playEndSoundList.removeAll(removeList);
                        removeList.clear();
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
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            record = record.substring(0, record.length()-1);

            String accompaniment = "";
            for(String str : recordList) {
                accompaniment += str;
                accompaniment = accompaniment + ", ";
            }

            if(!accompaniment.equals(""))
                accompaniment = accompaniment.substring(0, accompaniment.length()-2);
            mDbOpenHelper.insertMelodyColumn(filename, record, accompaniment); // recordList를 ,로 구분짓기
            String strlist[] = record.split(" ");

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

            int melodyLength = 0;
            MidiTrack noteTrack = new MidiTrack();
            for(int i=0; i<strlist.length; i++) {
                if(strlist[i].equals("")) {
                    continue;
                }

                int channel = 0;
                int pitch = 0;
                int velocity = 100;

                String str = strlist[i];
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

                noteTrack.insertNote(channel, pitch, velocity, melodyLength, length * 120); // 240이면 1박자
                melodyLength = melodyLength + length * 120;
            }

            tracks.add(noteTrack);

            for(int i=0; i<recordList.size(); i++) {
                MidiTrack accompanimentTrack = new MidiTrack();
                int measure = beatSequenceIdx;

                int idx = 0;
                int everyAllLength = 0;
                while(everyAllLength < melodyLength) {
                    String[] chip = recordList.get(i).split(" ");
                    String str = chip[idx%chip.length];
                    int channel = i+1;
                    int pitch = 0;
                    int velocity = 80;

                    switch(str.charAt(0)) {
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
                            break;
                        default:
                            break;
                    }

                    int length = 1; // if 'R', quater*2 beat.
                    if (str.length() > 1) {
                        pitch = pitch + 12 * ((int)str.charAt(1) - '0');
                        length = (int)str.charAt(3) - '0';
                    }
                    if(everyAllLength + length * 120 > melodyLength)
                        length = 960;

                    accompanimentTrack.insertNote(channel, pitch, velocity, everyAllLength, length * 120);
                    everyAllLength = everyAllLength + length * 120;
                    idx++;
                }
                tracks.add(accompanimentTrack);
            }

            MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
            File output = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename + ".mid");
            try {
                midi.writeToFile(output);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
}
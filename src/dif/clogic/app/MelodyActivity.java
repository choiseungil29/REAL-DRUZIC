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
import com.leff.midi.event.ChannelEvent;
import com.leff.midi.event.MidiEvent;
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

        view = new MelodyView(this, getIntent().getStringArrayListExtra("recordData"), getIntent().getStringArrayListExtra("filenameData"));
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

        public MelodyView(Context context, ArrayList<String> recordData, ArrayList<String> filenameData) {
            super(context);

            thread = new GLThread();
            renderer = new MelodyRenderer(context, thread, recordData , filenameData);
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
    }


    public class MelodyRenderer extends GLRenderer {

        private MelodyView.GLThread thread;
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

        private ArrayList<AccompanimentSound>[] accompanimentList;
        private int[] accompanimentListIdx;

        private String record;
        private ArrayList<String> recordList;
        private ArrayList<String> filenameList;
        private ArrayList<Integer> filenameIdList;

        private MotionEvent event = null;
        PointF touchPoint;

        private Handler handler;

        private ScreenSprite[] sprite = new ScreenSprite[10];

        public MelodyRenderer(Context context, MelodyView.GLThread pThread, ArrayList<String> recordData, ArrayList<String> filenameData) {
            super(context);
            thread = pThread;
            recordList = recordData;
            filenameList = filenameData;
            filenameIdList = new ArrayList<Integer>();

            mDbOpenHelper = new DbOpenHelper(context);
            try {
                mDbOpenHelper.open();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            soundPool = new SoundPool(256, AudioManager.STREAM_MUSIC, 0);
            soundFileTable = new HashMap<String, Integer>();
            bePlaySoundList = Collections.synchronizedList(new ArrayList<Sound>());
            bePlayingSoundList = Collections.synchronizedList(new ArrayList<Sound>());
            playEndSoundList = Collections.synchronizedList(new ArrayList<Sound>());

            accompanimentListIdx = new int[recordList.size()];
            accompanimentList = new ArrayList[recordList.size()];

            for(int i=0; i<recordList.size(); i++) {
                accompanimentListIdx[i] = 0;
                accompanimentList[i] = new ArrayList<AccompanimentSound>();

                String[] record = recordList.get(i).split(" ");
                for(int idx=0; idx<record.length; idx++) {
                    String str = record[idx];
                    if(str.equals("R")) {
                        accompanimentList[i].add(new AccompanimentSound("R", 1));
                    } else {
                        try {
                            accompanimentList[i].add(new AccompanimentSound(str.charAt(0) + "_" + (str.charAt(1)-'0'), str.charAt(2)-'0'));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

            for(String str : filenameList) {
                filenameIdList.add(soundPool.load(str, 0));
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
                                    saveFile(filename, fomatter.format(currentTime));
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
        public void update(float dt) {
            spriteBundle.update(dt);

            for(int i=0; i<10; i++) {
                if(i < codeSequence.length) {
                    sprite[i].setIsVisible(true);
                    sprite[i].setScale(sprite[i].getScale().x, (windowHeight / codeSequence.length) / 100.0f);
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

                    if(event.getAction() != MotionEvent.ACTION_DOWN) {
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

            if(bpmTimer >= 2.0f/16) { // 60.0f / bpm -> 1박자마다 들어가는 루프. /8이 붙으면 반의 반박자마다
                bpmTimer = 0.0f;

                soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                if(beatSequenceIdx/beatSequence.length < 1) {
                    beatSequenceIdx++;
                    return;
                } else {
                    beatSequence = ChordReference.beat1;
                }

                codeSequence = ChordReference.redPackage[((beatSequenceIdx+1)/beatSequence.length-1)%ChordReference.redPackage.length];

                if(beatSequenceIdx%2 == 0) { // 반박자마다 들어감
                    // melody Sequence

                    if((beatSequenceIdx-16)%64 == 0) {
                        // 음악파일 틀기
                        for(int i : filenameIdList) {
                            //soundPool.play(i, 0.7f, 0.7f, 0, 0, 1);
                        }
                    }

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
                    //TouchSprite sprite = new TouchSprite();
                    //sprite.setPosition(touchPoint.x, touchPoint.y);
                    //spriteBundle.addSprite(sprite);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
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

        public void saveFile(String filename, String birth) {

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
            t.setBpm(120);

            tempoTrack.insertEvent(ts);
            tempoTrack.insertEvent(t);

            tracks.add(tempoTrack);

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
                length *= 2;

                noteTrack.insertNote(channel, pitch, velocity, melodyLength, length * 120); // 120에 반박자

                melodyLength = melodyLength + length * 120;
            }
            tracks.add(noteTrack);

            // 아래는 Accompaniment 구현 후에.
            // 이 코드에 파일 불러와 MidiEvent(ProgramChange)만 입히면 됨.
            for(int i=0; i<recordList.size(); i++) {
                //MidiTrack accompanimentTrack = new MidiTrack();
                File file = new File(filenameList.get(i));

                MidiFile mFile = null;
                try {
                    mFile = new MidiFile(file);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if(mFile == null)
                    return;

                MidiTrack fileTrack = mFile.getTracks().get(1);

                String[] instruments = getResources().getStringArray(R.array.instruments);

                int channel = i+1;
                int pitch = 0;
                int velocity = 80;

                Iterator<MidiEvent> it = fileTrack.getEvents().iterator();
                while(it.hasNext()) {
                    ChannelEvent event = (ChannelEvent) it.next();
                    if(event.getType() == ChannelEvent.PROGRAM_CHANGE) {
                        event.setChannel(channel);
                        noteTrack.insertEvent(event);
                    }
                }

                int idx = 0;
                int everyAllLength = 0;
                while(everyAllLength < melodyLength) {
                    String[] chip = recordList.get(i).split(" ");
                    String str = chip[idx%chip.length];

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
                        if(str.length() <= 3) {
                            length = (int)str.charAt(2) - '0';
                        } else {
                            length = (int)str.charAt(3) - '0';
                        }
                    }
                    length *= 2;
                    if(everyAllLength + length * 120 > melodyLength)
                        length = 1920;

                    noteTrack.insertNote(channel, pitch, velocity, everyAllLength, length * 120); // 120은 반의반박자
                    everyAllLength = everyAllLength + length * 120;
                    idx++;
                }
                //tracks.add(accompanimentTrack);
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
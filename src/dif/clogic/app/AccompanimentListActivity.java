package dif.clogic.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.ChannelEvent;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.ProgramChange;
import dif.clogic.custom.CustomProgressBar;
import dif.clogic.other.Accompaniment;
import dif.clogic.other.AccompanimentAdapter;
import dif.clogic.other.DbOpenHelper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 3.
 * Time: 오후 6:53
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentListActivity extends Activity {

    private ListView listView;
    private ArrayList<Accompaniment> accompanimentList;
    private AccompanimentAdapter accompanimentAdapter;
    private DbOpenHelper mDbOpenHelper;
    private MediaPlayer mPlayer = null;

    private ImageButton playButton;
    private CustomProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accompanimentactivity);

        setTitle("반주 리스트");

        playButton = (ImageButton)findViewById(R.id.accompanimentPlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                if(mPlayer == null)
                    return;

                if(mPlayer.isPlaying()) {
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.play5));
                    mPlayer.pause();
                } else {
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause2));
                    mPlayer.start();
                }
            }
        });

        mPlayer = new MediaPlayer();

        TextView t = (TextView)findViewById(R.id.playingTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "nanumN.ttf");
        t.setTypeface(font);

        progress = (CustomProgressBar)findViewById(R.id.accompanimentProgress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                while(true) {
                    if(mPlayer == null)
                        continue;

                    if(mPlayer.isPlaying())
                        progress.setProgress(mPlayer.getCurrentPosition());
                }
            }
        }).start();


        accompanimentList = new ArrayList<Accompaniment>();
        accompanimentAdapter = new AccompanimentAdapter(this, R.layout.row, accompanimentList);

        mDbOpenHelper = new DbOpenHelper(AccompanimentListActivity.this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            findFolder();
        }

        listView = (ListView)findViewById(R.id.accompanimentListView);
        listView.setAdapter(accompanimentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int which, long l) {
                //To change body of implemented methods use File | Settings | File Templates.

                AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentListActivity.this);
                builder.setTitle("리스트");

                builder.setItems(R.array.data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //To change body of implemented methods use File | Settings | File Templates.

                        String[] data = getResources().getStringArray(R.array.data);

                        if (data[i].equals("음악 듣기")) {

                            if (mPlayer == null)
                                mPlayer = new MediaPlayer();

                            mPlayer.reset();
                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    //To change body of implemented methods use File | Settings | File Templates.
                                    progress.setProgress(0);
                                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.play5));
                                }
                            });

                            try {
                                String str = Environment.getExternalStorageDirectory().getAbsolutePath();
                                str += File.separator;
                                str += accompanimentList.get(which).Name;
                                str += ".mid";
                                mPlayer.setDataSource(str);
                                mPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            mPlayer.start();

                            playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause2));

                            progress.setProgress(0);
                            progress.setMax(mPlayer.getDuration());

                            TextView t = (TextView)findViewById(R.id.playingTitle);
                            t.setText(accompanimentList.get(which).Name);
                            Typeface font = Typeface.createFromAsset(getAssets(), "nanumN.ttf");
                            t.setTypeface(font);
                        }

                        if (data[i].equals("악기 설정")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentListActivity.this);
                            builder.setTitle("악기 설정");
                            builder.setItems(R.array.instruments, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //To change body of implemented methods use File | Settings | File Templates.
                                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + accompanimentList.get(which).Name + ".mid");

                                    MidiFile mFile = null;
                                    try {
                                        mFile = new MidiFile(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                    if(mFile == null)
                                        return;

                                    MidiTrack t = mFile.getTracks().get(1);

                                    String[] instruments = getResources().getStringArray(R.array.instruments);

                                    Iterator<MidiEvent> it = t.getEvents().iterator();
                                    ArrayList<MidiEvent> removeProgramChange = new ArrayList<MidiEvent>();
                                    while(it.hasNext()) {
                                        ChannelEvent event = (ChannelEvent) it.next();
                                        if(event.getType() == ChannelEvent.PROGRAM_CHANGE)
                                            removeProgramChange.add(event);
                                    }

                                    for(MidiEvent e : removeProgramChange) {
                                        t.removeEvent(e);
                                    }

                                    if(instruments[i].equals("어쿠스틱 그랜드 피아노")) {
                                        MidiEvent pc = new ProgramChange(0, 0, 0);
                                        t.insertEvent(pc);
                                    } else if (instruments[i].equals("일렉 피아노")) {
                                        MidiEvent pc = new ProgramChange(0, 0, 4);
                                        t.insertEvent(pc);
                                    } else if (instruments[i].equals("실로폰")) {
                                        MidiEvent pc = new ProgramChange(0, 0, 10);
                                        t.insertEvent(pc);
                                    }

                                    try {
                                        mFile.writeToFile(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                            });
                            builder.show();
                        }

                        if (data[i].equals("악보 보기")) {
                            String params = Accompaniment.convert(accompanimentList.get(which).originRecord);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://druzicofclogic.appspot.com/melody/view/" + params)); // CN5 같은 쿼리 보내주기~~
                            startActivity(intent);
                        }

                        if(data[i].equals("이름 바꾸기")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentListActivity.this);
                            LayoutInflater inflater = AccompanimentListActivity.this.getLayoutInflater();
                            final View v = inflater.inflate(R.layout.dialog_signin, null);
                            builder.setView(v)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //To change body of implemented methods use File | Settings | File Templates.
                                            String beforeText = accompanimentList.get(which).Name;

                                            EditText editText = (EditText)v.findViewById(R.id.fileName);
                                            String afterText = editText.getText().toString();

                                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + beforeText + ".mid");
                                            File to = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + afterText + ".mid");
                                            if(!to.exists())
                                                file.renameTo(to);

                                            accompanimentList.get(which).Name = afterText;

                                            mDbOpenHelper.updateAccompanimentColumn(accompanimentList.get(which).Id, accompanimentList.get(which).Name, Accompaniment.convert(accompanimentList.get(which).originRecord));
                                        }
                                    })
                                    .setNegativeButton("취소", null);
                            builder.show();

                        }

                        if(data[i].equals("삭제")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentListActivity.this)
                                    .setTitle("DRUZIC")
                                    .setMessage("정말로 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int ii) {

                                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + accompanimentList.get(which).Name + ".mid");
                                            file.delete();

                                            mDbOpenHelper.deleteAccompanimentColumn(accompanimentList.get(which).Id);
                                            accompanimentAdapter.remove(accompanimentAdapter.getItem(which));
                                            accompanimentAdapter.notifyDataSetChanged();
                                        }
                                    });
                            builder.setNegativeButton("취소", null);
                            builder.show();

                        }

                    }
                });
                builder.show();
            }
        });

        ActionBar ab = getActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_actionbar, null);
        TextView tv = (TextView)v.findViewById(R.id.actionBarTitle);
        tv.setText("반주");
        tv.setTypeface(font);

        ImageButton button = (ImageButton)v.findViewById(R.id.addBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                Intent intent = new Intent(AccompanimentListActivity.this, AccompanimentActivity.class);
                startActivity(intent);
            }
        });

        ab.setCustomView(v);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mPlayer.isPlaying())
            mPlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mPlayer.isPlaying())
            mPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            findFolder();
        } else {
        }
        accompanimentAdapter.notifyDataSetChanged();
    }

    private void findFolder() {
        accompanimentList.clear();
        Cursor cursor = mDbOpenHelper.getAllAccompanimentColumns();
        while(cursor.moveToNext()) {
            accompanimentList.add(new Accompaniment(cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("originrecord"))));
        }
    }
}

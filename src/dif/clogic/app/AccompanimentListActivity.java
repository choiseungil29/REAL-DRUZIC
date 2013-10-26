package dif.clogic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import dif.clogic.custom.CustomProgressBar;
import dif.clogic.other.Accompaniment;
import dif.clogic.other.AccompanimentAdapter;
import dif.clogic.other.DbOpenHelper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 3.
 * Time: 오후 6:53
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentListActivity extends Activity {

    private ListView listView;
    private Button addButton;
    private ArrayList<Accompaniment> accompanimentList;
    private DbOpenHelper mDbOpenHelper;
    private MediaPlayer mPlayer = null;

    //private SeekBar playerSeekBar;
    private Button playButton;
    private CustomProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accompanimentactivity);

        setTitle("반주 리스트");

        playButton = (Button)findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                if(mPlayer == null)
                    return;

                if(mPlayer.isPlaying()) {
                    playButton.setText("재생");
                    mPlayer.pause();
                } else {
                    playButton.setText("일시정지");
                    mPlayer.start();
                }
            }
        });

        progress = (CustomProgressBar)findViewById(R.id.accompanimentProgress);

        /*playerSeekBar = (SeekBar)findViewById(R.id.seekBar);
        playerSeekBar.setVisibility(ProgressBar.VISIBLE);
        playerSeekBar.setProgressDrawable(this.getResources().getDrawable(R.drawable.custom_seekbar));
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
                if (mPlayer != null)
                    mPlayer.seekTo(seekBar.getProgress());
            }
        });*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                while(true) {
                    if(mPlayer == null)
                        continue;

                    if(mPlayer.isPlaying())
                        progress.setProgress(mPlayer.getCurrentPosition());
                        //playerSeekBar.setProgress(mPlayer.getCurrentPosition());
                }
            }
        }).start();


        accompanimentList = new ArrayList<Accompaniment>();
        final AccompanimentAdapter accompanimentAdapter = new AccompanimentAdapter(this, R.layout.row, accompanimentList);

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

        addButton = (Button)findViewById(R.id.addAccompanimentBtn);
        addButton.setText("반주 그리기");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                // go to drawActivity!!
                Intent intent = new Intent(AccompanimentListActivity.this, AccompanimentActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView)findViewById(R.id.accompanimentListView);
        listView.setAdapter(accompanimentAdapter);
        listView.setDividerHeight(0);
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

                            if (mPlayer == null) {
                                mPlayer = new MediaPlayer();
                                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                        //playerSeekBar.setProgress(0);
                                        progress.setProgress(0);
                                    }
                                });
                            } else {
                                mPlayer.stop();
                                mPlayer.reset();
                            }

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

                            playButton.setText("일시정지");

                            /*playerSeekBar.setProgress(0);
                            playerSeekBar.setMax(mPlayer.getDuration());*/
                            progress.setProgress(0);
                            progress.setMax(mPlayer.getDuration());
                        }

                        if (data[i].equals("악보 보기")) {

                            //Cursor cursor = mDbOpenHelper.getMatchName("example");
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void findFolder() {
        Cursor cursor = mDbOpenHelper.getAllAccompanimentColumns();
        while(cursor.moveToNext()) {
            Accompaniment song = new Accompaniment(cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("originrecord")));

            accompanimentList.add(song);
        }
    }
}

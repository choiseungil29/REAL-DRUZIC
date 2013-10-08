package dif.clogic.druzic;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
    private ArrayList<Song> accompanimentList;
    private DbOpenHelper mDbOpenHelper;
    private MediaPlayer mPlayer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accompanimentactivity);

        setTitle("반주 리스트");

        accompanimentList = new ArrayList<Song>();
        final SongAdapter songAdapter = new SongAdapter(this, R.layout.row, accompanimentList);

        mDbOpenHelper = new DbOpenHelper(AccompanimentListActivity.this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            findFolder();
        } else {
        }

        addButton = (Button)findViewById(R.id.addAccompanimentBtn);
        addButton.setText("반주 그리기");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                // go to drawActivity!!
                Intent intent = new Intent(AccompanimentListActivity.this, AccompanimentActivity.class);
                intent.putExtra("isMelody", false);
                startActivity(intent);
            }
        });

        listView = (ListView)findViewById(R.id.accompanimentListView);
        listView.setAdapter(songAdapter);

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
                            if (mPlayer != null) {
                                mPlayer.reset();
                                mPlayer.release();
                                mPlayer = null;
                            }

                            mPlayer = new MediaPlayer();
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
                        }

                        if (data[i].equals("악보 보기")) {

                            //Cursor cursor = mDbOpenHelper.getMatchName("example");
                            String params = Song.convert(accompanimentList.get(which).originRecord);
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

                                            accompanimentList.get(which).Name = afterText;

                                            mDbOpenHelper.updateColumn(accompanimentList.get(which).Id, accompanimentList.get(which).Name, Song.convert(accompanimentList.get(which).originRecord), accompanimentList.get(which).IsMelody);
                                        }
                                    })
                                    .setNegativeButton("취소", null);
                            builder.show();

                        }

                        if(data[i].equals("삭제")) {

                            String alertTitle = getResources().getString(R.string.app_name);

                            AlertDialog.Builder builder = new AlertDialog.Builder(AccompanimentListActivity.this)
                                    .setTitle("DRUZIC")
                                    .setMessage("정말로 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            accompanimentList.remove(which);
                                            mDbOpenHelper.deleteColumn(accompanimentList.get(which).Id);
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

    private void findFolder() {
        Cursor cursor = mDbOpenHelper.getAllColumns();
        while(cursor.moveToNext()) {
            if(!(cursor.getInt(cursor.getColumnIndex("ismelody")) > 0))
                accompanimentList.add(new Song(
                        cursor.getInt(cursor.getColumnIndex("_id")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("originrecord")),
                        cursor.getInt(cursor.getColumnIndex("ismelody")) > 0));
        }
    }
}

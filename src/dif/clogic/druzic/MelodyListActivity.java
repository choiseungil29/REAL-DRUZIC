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
import dif.clogic.other.AccompanimentAdapter;
import dif.clogic.other.DbOpenHelper;
import dif.clogic.other.Accompaniment;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 4.
 * Time: 오후 10:16
 * To change this template use File | Settings | File Templates.
 */
public class MelodyListActivity extends Activity {

    private ArrayList<Accompaniment> melodyList;
    private MediaPlayer mPlayer = null;
    private DbOpenHelper mDbOpenHelper;
    private Button addButton;
    private ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.melodyactivity);

        setTitle("멜로디 리스트");

        melodyList = new ArrayList<Accompaniment>();
        final AccompanimentAdapter accompanimentAdapter = new AccompanimentAdapter(this, R.layout.row, melodyList);

        mDbOpenHelper = new DbOpenHelper(MelodyListActivity.this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            //findFolder();
        } else {
        }

        addButton = (Button)findViewById(R.id.addAccompanimentBtn);
        addButton.setText("멜로디 그리기");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                // go to drawActivity!!
                //Intent intent = new Intent(MelodyListActivity.this, AccompanimentActivity.class);
                Intent intent = new Intent(MelodyListActivity.this, CheckboxListViewActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView)findViewById(R.id.accompanimentListView);
        listView.setAdapter(accompanimentAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int which, long l) {
                //To change body of implemented methods use File | Settings | File Templates.

                AlertDialog.Builder builder = new AlertDialog.Builder(MelodyListActivity.this);
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
                                str += melodyList.get(which).Name;
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
                            String params = Accompaniment.convert(melodyList.get(which).originRecord);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://druzicofclogic.appspot.com/melody/view/" + params)); // CN5 같은 쿼리 보내주기~~
                            startActivity(intent);
                        }

                        if(data[i].equals("이름 바꾸기")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MelodyListActivity.this);
                            LayoutInflater inflater = MelodyListActivity.this.getLayoutInflater();
                            final View v = inflater.inflate(R.layout.dialog_signin, null);
                            builder.setView(v)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //To change body of implemented methods use File | Settings | File Templates.

                                            String beforeText = melodyList.get(which).Name;

                                            EditText editText = (EditText)v.findViewById(R.id.fileName);
                                            String afterText = editText.getText().toString();

                                            melodyList.get(which).Name = afterText;

                                            //mDbOpenHelper.updateMelodyColumn(melodyList.get(which).Id, melodyList.get(which).Name, Accompaniment.convert(melodyList.get(which).originRecord), melodyList.get(which).isMelody);
                                        }
                                    })
                                    .setNegativeButton("취소", null);
                            builder.show();

                        }

                        if(data[i].equals("삭제")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MelodyListActivity.this)
                                    .setTitle("DRUZIC")
                                    .setMessage("정말로 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //mDbOpenHelper.deleteColumn(melodyList.get(which).Id);
                                            melodyList.remove(which);
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
            /*if((cursor.getInt(cursor.getColumnIndex("ismelody")) > 0))
                melodyList
                        .add(new Accompaniment(
                                cursor.getInt(cursor.getColumnIndex("_id")),
                                cursor.getString(cursor.getColumnIndex("name")),
                                cursor.getString(cursor.getColumnIndex("originrecord")),
                                cursor.getInt(cursor.getColumnIndex("ismelody")) > 0));*/
        }
    }
}
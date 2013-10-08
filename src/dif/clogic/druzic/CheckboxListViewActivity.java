package dif.clogic.druzic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 6.
 * Time: 오전 4:22
 * To change this template use File | Settings | File Templates.
 */
public class CheckboxListViewActivity extends Activity {

    private ListView listView;
    private Button button;
    private ArrayList<Song> accompanimentList;
    private DbOpenHelper mDbOpenHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accompanimentactivity);

        setTitle("반주 설정하기");

        listView = (ListView)findViewById(R.id.accompanimentListView);
        button = (Button)findViewById(R.id.addAccompanimentBtn);

        accompanimentList = new ArrayList<Song>();

        mDbOpenHelper = new DbOpenHelper(CheckboxListViewActivity.this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Cursor cursor = mDbOpenHelper.getAllColumns();
        while(cursor.moveToNext()) {
            if(!(cursor.getInt(cursor.getColumnIndex("ismelody")) > 0))
                accompanimentList.add(new Song(
                        cursor.getInt(cursor.getColumnIndex("_id")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("originrecord")),
                        cursor.getInt(cursor.getColumnIndex("ismelody")) > 0));
        }

        ArrayList<String> array = new ArrayList<String>();
        array.add("안녕");

        final SongAdapter adapter = new SongAdapter(this, R.layout.mcrow, accompanimentList);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, array);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        button.setText("멜로디 그리기");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.

                ArrayList<String> selectedList = new ArrayList<String>();
                SparseBooleanArray array = listView.getCheckedItemPositions();
                long[] test = listView.getCheckedItemIds();
                for(int i=0; i<array.size(); i++) {
                    int position = array.keyAt(i);
                    if(array.valueAt(i))
                        selectedList.add(adapter.getItem(position).originRecord);
                }

                Intent intent = new Intent(CheckboxListViewActivity.this, MelodyActivity.class);
                startActivity(intent);

            }
        });
    }
}
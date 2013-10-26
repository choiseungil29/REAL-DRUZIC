package dif.clogic.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import dif.clogic.other.Accompaniment;
import dif.clogic.other.AccompanimentAdapter;
import dif.clogic.other.DbOpenHelper;

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
    private ArrayList<Accompaniment> accompanimentList;
    private DbOpenHelper mDbOpenHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accompanimentactivity);

        setTitle("반주 설정하기");

        listView = (ListView)findViewById(R.id.accompanimentListView);
        button = (Button)findViewById(R.id.addAccompanimentBtn);

        accompanimentList = new ArrayList<Accompaniment>();

        mDbOpenHelper = new DbOpenHelper(CheckboxListViewActivity.this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Cursor cursor = mDbOpenHelper.getAllAccompanimentColumns();
        while(cursor.moveToNext()) {
            accompanimentList.add(new Accompaniment(
                    cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("originrecord"))));
        }

        final AccompanimentAdapter adapter = new AccompanimentAdapter(this, R.layout.mcrow, accompanimentList);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                //To change body of implemented methods use File | Settings | File Templates.
                listView.setItemChecked(which, !listView.isItemChecked(which));
            }
        });

        button.setText("멜로디 그리기");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                ArrayList<String> selectedList = new ArrayList<String>();

                for(int i=0; i<accompanimentList.size(); i++) {
                    if(accompanimentList.get(i).checked) {
                        selectedList.add(adapter.getItem(i).originRecord);
                    }
                }

                Intent intent = new Intent(CheckboxListViewActivity.this, MelodyActivity.class);
                intent.putStringArrayListExtra("recordData", selectedList);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // selectedList 보내기
                startActivity(intent);

            }
        });
    }
}
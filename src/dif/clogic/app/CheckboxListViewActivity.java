package dif.clogic.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import dif.clogic.other.Accompaniment;
import dif.clogic.other.AccompanimentAdapter;
import dif.clogic.other.DbOpenHelper;

import java.io.File;
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
        //button = (Button)findViewById(R.id.addAccompanimentBtn);

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                //To change body of implemented methods use File | Settings | File Templates.
                listView.setItemChecked(which, !listView.isItemChecked(which));
            }
        });

        ActionBar ab = getActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_actionbar, null);
        TextView tv = (TextView)v.findViewById(R.id.actionBarTitle);
        tv.setText("반주 설정하기");
        Typeface font = Typeface.createFromAsset(getAssets(), "nanumN.ttf");
        tv.setTypeface(font);

        ImageButton button = (ImageButton)v.findViewById(R.id.addBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                ArrayList<String> selectedList = new ArrayList<String>();
                ArrayList<String> filenameList = new ArrayList<String>();

                for(int i=0; i<accompanimentList.size(); i++) {
                    if(accompanimentList.get(i).checked) {
                        selectedList.add(adapter.getItem(i).originRecord);
                        filenameList.add(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + adapter.getItem(i).Name + ".mid");
                    }
                }

                Intent intent = new Intent(CheckboxListViewActivity.this, MelodyActivity.class);
                intent.putStringArrayListExtra("recordData", selectedList);
                intent.putStringArrayListExtra("filenameData", filenameList);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        ab.setCustomView(v);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
    }
}
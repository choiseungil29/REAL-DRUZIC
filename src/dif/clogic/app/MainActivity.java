package dif.clogic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 7. 25.
 * Time: 오전 9:35
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        setTitle("DRUZIC - 음악을 그리다");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.main));

        listView = (ListView)findViewById(R.id.mainListView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //To change body of implemented methods use File | Settings | File Templates.

                if(adapter.getItem(i).equals("반주")) {
                    // 이거먼저
                    // 반주 리스트 띄워야징
                    Intent intent = new Intent(MainActivity.this, AccompanimentListActivity.class);
                    startActivity(intent);
                }
                if(adapter.getItem(i).equals("멜로디")) {
                    // 시팔.. 나중에
                    Intent intent = new Intent(MainActivity.this, MelodyListActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                String alertTitle = getResources().getString(R.string.app_name);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("DRUZIC")
                        .setMessage("종료하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
// TODO Auto-generated method stub
                                moveTaskToBack(true);
                                System.exit(0);
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();
        }
        return true;
    }
}
package dif.clogic.druzic;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import dif.clogic.view.MelodyView;

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
}
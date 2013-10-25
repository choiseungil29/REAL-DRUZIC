package dif.clogic.view;

import android.app.ActionBar;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;
import dif.clogic.other.ChordReference;
import dif.clogic.other.DbOpenHelper;

import java.util.HashMap;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 4.
 * Time: 오전 4:56
 * To change this template use File | Settings | File Templates.
 */
public class DrawView extends View {

    private DbOpenHelper mDbOpenHelper;

    protected int[] codeSequence;
    protected int virtualPitch;

    protected int[] beatSequence;
    protected int beatSequenceIdx;

    protected HashMap<String, int[][]> codeSetTable;
    protected HashMap<String, Integer> soundFileTable;

    protected SoundPool soundPool;

    protected String record;

    ////////////

    protected String color;

    protected Timer timer;

    protected long count;

    /** Need to track this so the dirty region can accommodate the stroke. **/

    public DrawView(Context context) {
        super(context);

        this.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));

        mDbOpenHelper = new DbOpenHelper(getContext());

        //this.color = color;
        this.color = "RED";

        record = "";

        codeSetTable = new HashMap<String, int[][]>();
        codeSetTable.put("RED", ChordReference.redPackage);
        codeSetTable.put("MINT", ChordReference.cyanPackgage);
        codeSetTable.put("ORANGE", ChordReference.yellowPackage);
        codeSetTable.put("BLUE", ChordReference.bluePackage);

        soundFileTable = new HashMap<String, Integer>();

        soundPool = new SoundPool(256, AudioManager.STREAM_MUSIC, 0);

        for(int i=0; i<ChordReference.melodyList.length; i++) {
            soundFileTable.put(ChordReference.melodyList[i], soundPool.load(context, getResources().getIdentifier(ChordReference.melodyList[i], "raw", "dif.clogic.app"), 0));
        }

        for(int i=0; i<ChordReference.beatList.length; i++) {
            soundFileTable.put(ChordReference.beatList[i], soundPool.load(context, getResources().getIdentifier(ChordReference.beatList[i], "raw", "dif.clogic.app"), 0));
        }

        for(int i=0; i<ChordReference.melodyList2.length; i++) {
            soundFileTable.put(ChordReference.melodyList2[i], soundPool.load(context, getResources().getIdentifier(ChordReference.melodyList2[i], "raw", "dif.clogic.app"), 0));
        }

        count = System.currentTimeMillis();
    }
}
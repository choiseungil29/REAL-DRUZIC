package dif.clogic.druzic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 28.
 * Time: 오후 3:06
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentActivity extends Activity {

    private DbOpenHelper mDbOpenHelper;
    private AccompanimentView accompanimentView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbOpenHelper = new DbOpenHelper(AccompanimentActivity.this);
        accompanimentView = new AccompanimentView(this);
        setContentView(accompanimentView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();

        if(accompanimentView.timer != null) {
            accompanimentView.timer.cancel();
            accompanimentView.timer.purge();
            accompanimentView.timer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:

                /*if(accompanimentView.isMelody()) {
                    new AlertDialog.Builder(AccompanimentActivity.this)
                            .setTitle("멜로디")
                            .setMessage("저장하고 종료하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    accompanimentView.saveFile(false);

                                    Intent intent = new Intent(AccompanimentActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            })
                            .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //To change body of implemented methods use File | Settings | File Templates.

                                    Intent intent = new Intent(AccompanimentActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    new AlertDialog.Builder(AccompanimentActivity.this)
                            .seTitle("반주")
                            .setMessage("저장하고 종료하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    soundView.saveFile();

                                    Intent intent = new Intent(AccompanimentActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            })
                            .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //To change body of implemented methods use File | Settings | File Templates.

                                    Intent intent = new Intent(AccompanimentActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }*/
        }
        return true;
    }

    public class AccompanimentView extends DrawView {

        private boolean isEnd = false;

        public AccompanimentView(Context context) {
            super(context);

            beatSequence = ChordReference.beatReady;
            beatSequenceIdx = 0;
            virtualPitch = 0;

            isEnd = false;

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(System.currentTimeMillis() - count < 120) {
                        try {
                            Thread.sleep(120 - (System.currentTimeMillis() - count));
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }

                    if(beatSequenceIdx/beatSequence.length >= 1) {
                        beatSequence = ChordReference.beat1;
                    }
                    if(beatSequenceIdx/beatSequence.length >= 5) {

                        isEnd = true;
                        saveFile(false);
                        Intent intent = new Intent(AccompanimentActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    codeSequence = codeSetTable.get(color)[(beatSequenceIdx/beatSequence.length)%codeSetTable.get(color).length];
                    soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                    String raw = "";
                    if((beatSequenceIdx%2 == 0)) {
                        if((beatSequenceIdx/beatSequence.length >= 1)) {
                            if(isTouching == true) {
                                if(isUp(pointStack)) {
                                    if(virtualPitch < (codeSequence.length - 1)) {
                                        virtualPitch++;
                                    }
                                }
                                else {
                                    if(virtualPitch > 0) {
                                        virtualPitch--;
                                    }
                                }
                                soundPool.play(soundFileTable.get(ChordReference.melodyList[codeSequence[virtualPitch % codeSequence.length]]), 1, 1, 0, 0, 1);
                                raw = ChordReference.melodyList[codeSequence[virtualPitch%codeSequence.length]];
                                raw.replaceAll("_", "");
                                raw = raw.toUpperCase();
                            }
                        } else {
                            raw = "R";
                        }
                        record += "&" + raw;
                    }

                    count = System.currentTimeMillis();
                    beatSequenceIdx++;

                    if(isEnd == true) {
                        if(timer != null) {
                            timer.cancel();
                            timer.purge();
                            timer = null;
                        }
                    }
                }
            };
            timer.schedule(timerTask, 120, 120);
            count = System.currentTimeMillis();
        }


    }
}


























package dif.clogic.druzic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 7.
 * Time: 오전 2:13
 * To change this template use File | Settings | File Templates.
 */
public class MelodyActivity extends Activity {

    private DbOpenHelper mDbOpenHelper;
    private MelodyView melodyView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbOpenHelper = new DbOpenHelper(MelodyActivity.this);
        melodyView = new MelodyView(this);
        setContentView(melodyView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();

        if(melodyView.timer != null) {
            melodyView.timer.cancel();
            melodyView.timer.purge();
            melodyView.timer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                new AlertDialog.Builder(MelodyActivity.this)
                        .setTitle("멜로디")
                        .setMessage("저장하고 종료하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                melodyView.saveFile(false);

                                Intent intent = new Intent(MelodyActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //To change body of implemented methods use File | Settings | File Templates.

                                Intent intent = new Intent(MelodyActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
        }

        return true;
    }

    public class MelodyView extends DrawView {

        private int melodySequenceRange = 0;

        public MelodyView(Context context) {
            super(context);

            beatSequence = ChordReference.beatReady;
            beatSequenceIdx = 0;
            virtualPitch = 0;

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    //To change body of implemented methods use File | Settings | File Templates.
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

                    codeSequence = codeSetTable.get(color)[(beatSequenceIdx/beatSequence.length)%codeSetTable.get(color).length];
                    soundPool.play(soundFileTable.get(ChordReference.beatList[beatSequence[beatSequenceIdx % beatSequence.length]]), 1, 1, 0, 0, 1);

                    String raw = "";
                    if((beatSequenceIdx%2 == 0)) {
                        if((beatSequenceIdx/beatSequence.length >= 1)) {
                            if(isTouching == true) {
                                if(isUp(pointStack)) {
                                    if((ChordReference.melodyList.length - 1) > (codeSequence[virtualPitch%codeSequence.length] + melodySequenceRange)) {
                                        melodySequenceRange++;
                                    }
                                    /*if(codeSequenceIdx < (codeSequence.length - 1)) {
                                        codeSequenceIdx++;
                                    }*/
                                }
                                else {
                                    if((codeSequence[virtualPitch%codeSequence.length] + melodySequenceRange) > 0) {
                                        melodySequenceRange--;
                                    }
                                    /*if(codeSequenceIdx > 0) {
                                        codeSequenceIdx--;
                                    }*/
                                }
                                soundPool.play(soundFileTable.get(ChordReference.melodyList[codeSequence[virtualPitch % codeSequence.length] + melodySequenceRange]), 1, 1, 0, 0, 1);
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
                }
            };

            timer.schedule(timerTask, 120, 120);
            count = System.currentTimeMillis();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for(int i=0;i<codeSequence.length-1; i++) {
                        if(event.getY() >= (outSize.y * i) &&
                                (event.getY() < outSize.y * (i+1))) {
                            virtualPitch = codeSequence[i];
                        }
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_UP:
                    melodySequenceRange = 0;
                    return false;
            }

            return false;

        }
    }
}
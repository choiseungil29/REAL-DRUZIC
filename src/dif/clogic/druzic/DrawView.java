package dif.clogic.druzic;

import android.content.Context;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    protected boolean isTouching;

    protected Stack<Point> pointStack;

    protected WindowManager wMgr;
    protected Point outSize;

    protected Timer timer;

    protected long count;

    /////

    private Paint paint = new Paint();
    private Path path = new Path();

    private Point point = new Point();

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();

    private static final float STROKE_WIDTH = 5f;

    private Bitmap bg;

    /** Need to track this so the dirty region can accommodate the stroke. **/
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    public DrawView(Context context) {
        super(context);

        mDbOpenHelper = new DbOpenHelper(getContext());

        //this.color = color;
        this.color = "RED";

        record = "";

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        outSize = new Point();
        wMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wMgr.getDefaultDisplay().getSize(outSize);

        codeSetTable = new HashMap<String, int[][]>();
        codeSetTable.put("RED", ChordReference.redPackage);
        codeSetTable.put("MINT", ChordReference.cyanPackgage);
        codeSetTable.put("ORANGE", ChordReference.yellowPackage);
        codeSetTable.put("BLUE", ChordReference.bluePackage);

        soundFileTable = new HashMap<String, Integer>();

        soundPool = new SoundPool(256, AudioManager.STREAM_MUSIC, 0);

        pointStack = new Stack<Point>();

        for(int i=0; i<ChordReference.melodyList.length; i++) {
            soundFileTable.put(ChordReference.melodyList[i], soundPool.load(context, getResources().getIdentifier(ChordReference.melodyList[i], "raw", "dif.clogic.druzic"), 0));
        }

        for(int i=0; i<ChordReference.beatList.length; i++) {
            soundFileTable.put(ChordReference.beatList[i], soundPool.load(context, getResources().getIdentifier(ChordReference.beatList[i], "raw", "dif.clogic.druzic"), 0));
        }

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);

        if(color.equals("RED")) {
            paint.setColor(Color.rgb(135, 47, 47));
        } else if(color.equals("MINT")) {
            paint.setColor(Color.rgb(18, 165, 143));
        } else if(color.equals("ORANGE")) {
            paint.setColor(Color.rgb(212, 123, 0));
        } else if(color.equals("BLUE")) {
            paint.setColor(Color.rgb(40, 138, 181));
        } else {
            paint.setColor(Color.BLACK);
        }

        count = System.currentTimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float eventX = event.getX();
        float eventY = event.getY();
        pointStack.push(new Point((int)event.getX(), (int)event.getY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;

                isTouching = true;

                return true;
            case MotionEvent.ACTION_MOVE:

                resetDirtyRect(eventX, eventY);

                // When the hardware tracks events faster than they are delivered, the
                // event will contain a history of those skipped points.
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }

                // After replaying history, connect the line to the touch point.
                path.lineTo(eventX, eventY);

                break;
            case MotionEvent.ACTION_UP:

                resetDirtyRect(eventX, eventY);

                // When the hardware tracks events faster than they are delivered, the
                // event will contain a history of those skipped points.
                historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }

                // After replaying history, connect the line to the touch point.
                path.lineTo(eventX, eventY);

                isTouching = false;
                break;
            default:
                return false;
        }

        invalidate(
                (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouchX = eventX;
        lastTouchY = eventY;

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bg, 0, 0, paint);
        canvas.drawPath(path, paint);
    }

    public boolean isUp(Stack<Point> pointStack) {
        Point n = null;
        Point p = null;
        try {
            n = pointStack.pop();
            if(!pointStack.isEmpty())
                p = pointStack.pop();
            else
                p = n;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(n.y < p.y)
            return true;

        return false;
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    public void saveFile(boolean isMelody) {
        SimpleDateFormat fomatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
        Date currentTime = new Date();
        String filename = fomatter.format(currentTime);

        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String fileMelodySequence = Song.convert(record);
        long result = mDbOpenHelper.insertColumn(filename, record, isMelody);

        String strlist[] = fileMelodySequence.split("&");

        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack = new MidiTrack();

        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

        Tempo t = new Tempo();
        t.setBpm(125);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        for (int idx = 0; idx < strlist.length; idx++) {

            if(strlist[idx].equals("")) {
                continue;
            }

            int channel = 0;
            int pitch = 0;
            int velocity = 100;

            String str = strlist[idx];
            switch (str.charAt(0)) {

                case 'C':
                    pitch = 0;
                    break;
                case 'D':
                    pitch = 2;
                    break;
                case 'E':
                    pitch = 4;
                    break;
                case 'F':
                    pitch = 5;
                    break;
                case 'G':
                    pitch = 7;
                    break;
                case 'A':
                    pitch = 9;
                    break;
                case 'B':
                    pitch = 11;
                    break;
                case 'R':
                    pitch = 0;
                default:
                    break;
            }

            if (str.length() > 1) {
                if (str.charAt(1) == 'S') {
                    pitch++;
                }
                pitch = pitch + 12 * (str.charAt(2) - '0' + 1) - 4;
            }

            noteTrack.insertNote(channel, pitch, velocity, idx * 240, 240);
        }

        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

        File output = new File(Environment.getExternalStorageDirectory() + File.separator + filename + ".mid");
        System.out.println(output.getAbsolutePath());
        try {
            midi.writeToFile(output);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
package dif.clogic.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import dif.clogic.graphics.MelodyRenderer;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 19.
 * Time: 오후 2:48
 * To change this template use File | Settings | File Templates.
 */
public class MelodyView extends GLSurfaceView {

    private MelodyRenderer renderer;
    private GLThread thread;

    public MelodyView(Context context, ArrayList<String> recordData) {
        super(context);

        thread = new GLThread();
        renderer = new MelodyRenderer(context, thread, recordData);
        setRenderer(renderer);
    }

    public void onDestroy() {
        thread.stopThread();
        renderer.onDestroy();
    }

    public boolean onTouchEvent(final MotionEvent event) {
        return renderer.onTouchEvent(event);
    }

    public class GLThread extends Thread {
        private boolean isRun = true;

        public GLThread() {
            isRun = true;
        }

        public void stopThread() {
            isRun = false;
        }

        public synchronized void run() {
            float frameRate = 1000.0f/60.0f;
            long time = System.currentTimeMillis();
            while(isRun) {
                if((System.currentTimeMillis() - time) > frameRate) {
                    renderer.update((System.currentTimeMillis() - time)/1000.0f);
                    requestRender();
                    time = System.currentTimeMillis();
                }
            }
        }
    }
}

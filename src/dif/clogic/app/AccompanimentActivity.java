package dif.clogic.app;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import dif.clogic.graphics.AccompanimentRenderer;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 22.
 * Time: 오후 9:31
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentActivity extends Activity {

    private AccompanimentView view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view = new AccompanimentView(this);
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

    public class AccompanimentView extends GLSurfaceView {
        private AccompanimentRenderer renderer;
        private GLThread thread;

        public AccompanimentView(Context context) {
            super(context);

            thread = new GLThread();
            renderer = new AccompanimentRenderer(context, thread);

            //thread.start();
            setRenderer(renderer);
        }

        public void onDestroy() {
            thread.stopThread();
            renderer.onDestroy();
        }

        public synchronized boolean onTouchEvent(final MotionEvent event) {
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
}
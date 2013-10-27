package dif.clogic.graphics;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 19.
 * Time: 오후 2:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class GLRenderer implements GLSurfaceView.Renderer {

    protected Context mContext;
    protected SpriteBundle spriteBundle;

    protected float windowWidth;
    protected float windowHeight;

    public GLRenderer(Context context) {
        mContext = context;
    }

    abstract public void Initialize(GL10 gl);
    abstract public void onDestroy();
    abstract public boolean onTouchEvent(MotionEvent event);
    abstract public void update(float dt);

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        //To change body of implemented methods use File | Settings | File Templates.

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0, 0, 1, 0);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        //To change body of implemented methods use File | Settings | File Templates.

        windowWidth = w;
        windowHeight = h;

        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, w, 0, h);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //To change body of implemented methods use File | Settings | File Templates.
        // Draw
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
    }

    public PointF getWindowSize() {
        return new PointF(windowWidth, windowHeight);
    }
}
package dif.clogic.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 18.
 * Time: 오전 10:13
 * To change this template use File | Settings | File Templates.
 */
public class Texture {

    private int[] texture;
    private Bitmap bitmap;

    private final BitmapFactory.Options sBitmapOptions;

    private GL10 gl;

    public Texture(GL10 pGL, Context context, int resId) {
        gl = pGL;
        texture = new int[1];
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        sBitmapOptions = new BitmapFactory.Options();
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        this.LoadTexture();
    }

    public boolean LoadTexture() {
        if(gl == null) {
            return false;
        }

        int error;

        gl.glGenTextures(1, texture, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        error = gl.glGetError();
        if(error != GL10.GL_NO_ERROR) {
            return false;
            // have error
        }

        return true;
    }

    public int[] getTexture() {
        return texture;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}

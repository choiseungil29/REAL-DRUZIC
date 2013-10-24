package dif.clogic.graphics;

import android.graphics.PointF;
import android.graphics.RectF;
import dif.clogic.texture.Texture;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 18.
 * Time: 오후 6:35
 * To change this template use File | Settings | File Templates.
 */
abstract public class Sprite {
    private Texture originTexture;
    private Texture texture;

    private float width;
    private float height;

    private float x;
    private float y;

    private float scaleX;
    private float scaleY;

    private float anchorX;
    private float anchorY;

    private boolean isEnable;
    private boolean isVisible;

    private float[] textureMapping;
    private FloatBuffer textureBuffer;
    private float[] vertices;

    private RectF viewRect;

    private Animation animation;

    public Sprite(Texture tex) {
        texture = tex;
        originTexture = texture;

        width = texture.getBitmap().getWidth();
        height = texture.getBitmap().getHeight();

        x = 360;
        y = 0;

        anchorX = 0.5f;
        anchorY = 0.5f;

        scaleX = 1.0f;
        scaleY = 1.0f;

        isEnable = true;
        isVisible = true;

        viewRect = new RectF(0.0f, 1.0f, 1.0f, 0.0f);
        setViewRect(viewRect);

        textureBuffer = this.getFloatBufferFromFloatArray(textureMapping);

        animation = null;
    }

    public void draw(GL10 gl) {

        float[] dummyVertices = new float[] {
                vertices[0] - anchorX, vertices[1] - anchorY, vertices[2],
                vertices[3] - anchorX, vertices[4] - anchorY, vertices[5],
                vertices[6] - anchorX, vertices[7] - anchorY, vertices[8],
                vertices[9] - anchorX, vertices[10] - anchorY, vertices[11]
        };
        FloatBuffer vertexBuffer = this.getFloatBufferFromFloatArray(dummyVertices);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, this.texture.getTexture()[0]);

        gl.glLoadIdentity();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(width, height, 0);
        gl.glScalef(scaleX, scaleY, 0);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Set the face rotation
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public void update(float dt) {
        if(animation != null) {
            animation.update(dt);
            if(!animation.isEnd()) {
                texture = animation.getNowFrameTexture();
            } else {
                texture = originTexture;
            }
        }
    }

    public void setViewRect(RectF rect) {
        vertices = new float[] {
                rect.left, rect.bottom, 0.0f,
                rect.right, rect.bottom, 0.0f,
                rect.left, rect.top, 0.0f,
                rect.right, rect.top, 0.0f
        };

        textureMapping = new float[] {
                rect.left, rect.top,
                rect.right, rect.top,
                rect.left, rect.bottom,
                rect.right, rect.bottom
        };
        textureBuffer = this.getFloatBufferFromFloatArray(textureMapping);
    }

    public void setAnimation(Animation pAnimation) {
        animation = pAnimation;
    }

    public Animation getAnimation() {
        return animation;
    }

    public RectF getViewRect() {
        return viewRect;
    }

    public void setPosition(float pX, float pY) {
        x = pX;
        y = pY;
    }

    public PointF getPosition() {
        return new PointF(x, y);
    }

    public void setIsVisible(boolean b) {
        isVisible = b;
    }

    public boolean getIsVisible() {
        return isVisible;
    }

    public void setIsEnable(boolean b) {
        isEnable = b;
    }

    public boolean getIsEnable() {
        return isEnable;
    }

    public void setAnchorPoint(float pX, float pY) {
        anchorX = pX;
        anchorY = pY;
    }

    public PointF getAnchorPoint() {
        return new PointF(anchorX, anchorY);
    }

    public void setScale(float pX, float pY) {
        scaleX = pX;
        scaleY = pY;
    }

    public PointF getScale() {
        return new PointF(scaleX, scaleY);
    }

    FloatBuffer getFloatBufferFromFloatArray(float[] array) {
        ByteBuffer temp = ByteBuffer.allocateDirect(array.length * 4);
        temp.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = temp.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }
}

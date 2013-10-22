package dif.clogic.sprite;

import android.content.Context;
import dif.clogic.graphics.Sprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 19.
 * Time: 오후 8:33
 * To change this template use File | Settings | File Templates.
 */
public class WallCircle extends Sprite {

    private boolean isTouching = false;
    private boolean isScaleAnimate = false;
    private float scaleAnimateRate = 0.0f;

    public WallCircle(GL10 pGl, Context context, int resId) {
        super(pGl, context, resId);
    }

    @Override
    public void update(float dt) {
        //To change body of implemented methods use File | Settings | File Templates.

        if(isScaleAnimate) {
            this.setScale(this.getScale().x - scaleAnimateRate * dt * 2, this.getScale().y - scaleAnimateRate * dt * 2);
        }
    }

    public void setIsTouching(boolean b) {
        isTouching = b;
    }

    public boolean getIsTouching() {
        return isTouching;
    }

    public void startAnimate(float rate) {
        isScaleAnimate = true;
        scaleAnimateRate = rate;
    }

    public void endAnimate() {
        isScaleAnimate = false;
        scaleAnimateRate = 0.0f;
    }

    public boolean isAnimate() {
        return isScaleAnimate;
    }
}

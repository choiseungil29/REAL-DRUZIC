package dif.clogic.sprite;

import android.content.Context;
import dif.clogic.graphics.Sprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 19.
 * Time: 오후 6:58
 * To change this template use File | Settings | File Templates.
 */
public class Circle extends Sprite {
    public Circle(GL10 pGl, Context context, int resId) {
        super(pGl, context, resId);
    }

    @Override
    public void update(float dt) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

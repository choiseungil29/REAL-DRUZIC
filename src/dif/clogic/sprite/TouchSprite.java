package dif.clogic.sprite;

import dif.clogic.graphics.Animation;
import dif.clogic.graphics.Sprite;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 24.
 * Time: 오후 2:36
 * To change this template use File | Settings | File Templates.
 */
public class TouchSprite extends Sprite {

    public TouchSprite() {
        //super(TextureCache.getInstance().getTexture("touch_01"));

        Animation animation = new Animation();
        /*for(int i=0; i<11; i++) {
            animation.addFrameWithTexture(TextureCache.getInstance().getTexture("touch_" + String.format("%0d", i+1)));
        }*/
        animation.setRepeat(false);
        animation.setRate(1.0f);

        this.setAnimation(animation);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if(this.getAnimation().isEnd()) {
            this.setIsEnable(false);
        }
    }
}

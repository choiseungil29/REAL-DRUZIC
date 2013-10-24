package dif.clogic.sprite;

import dif.clogic.graphics.Animation;
import dif.clogic.graphics.Sprite;
import dif.clogic.texture.TextureCache;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 24.
 * Time: 오후 2:36
 * To change this template use File | Settings | File Templates.
 */
public class TouchSprite extends Sprite {

    public TouchSprite() {
        super(TextureCache.getInstance().getTexture("touch_01"));
        Animation animation = new Animation();
        for(int i=0; i<11; i++) {
            if(TextureCache.getInstance().isInstance("touch_" + String.format("%02d", i+1)))
                animation.addFrameWithTexture(TextureCache.getInstance().getTexture("touch_" + String.format("%02d", i+1)));
        }
        animation.setRepeat(false);
        animation.setRate(0.4f);

        this.setAnimation(animation);

        this.setScale(2.0f, 2.0f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if(this.getAnimation().isEnd()) {
            this.setIsEnable(false);
        }
    }
}

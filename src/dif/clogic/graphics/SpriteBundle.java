package dif.clogic.graphics;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 18.
 * Time: 오후 8:03
 * To change this template use File | Settings | File Templates.
 */
public class SpriteBundle {

    private ArrayList<Sprite> spriteList;

    public SpriteBundle() {
        spriteList = new ArrayList<Sprite>();
    }

    public void draw() {
        for(Sprite sprite: spriteList) {
            if(sprite.getIsEnable() && sprite.getIsVisible()) {
                sprite.draw();
            }
        }
    }

    public void update(float dt) {
        for(Sprite sprite : spriteList) {
            if(sprite.getIsEnable()) {
                sprite.update(dt);
            }
        }
    }

    public void addSprite(Sprite sprite) {
        if(!spriteList.contains(sprite))
            spriteList.add(sprite);
    }

    public void removeSprite(Sprite sprite) {
        if(spriteList.contains(sprite))
            spriteList.remove(sprite);
    }
}

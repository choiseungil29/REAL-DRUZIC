package dif.clogic.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 18.
 * Time: 오후 8:03
 * To change this template use File | Settings | File Templates.
 */
public class SpriteBundle {

    private List<Sprite> spriteList;

    public SpriteBundle() {
        spriteList = Collections.synchronizedList(new ArrayList<Sprite>());
    }

    public void draw() {
        synchronized (spriteList) {
            for(Sprite sprite: spriteList) {
                if(sprite.getIsEnable() && sprite.getIsVisible()) {
                    sprite.draw();
                }
            }
        }
    }

    public void update(float dt) {
        synchronized (spriteList) {
            for(Sprite sprite : spriteList) {
                if(sprite.getIsEnable()) {
                    sprite.update(dt);
                }
            }
        }
    }

    public void addSprite(Sprite pSprite) {
        synchronized (spriteList) {
            Sprite sprite = pSprite;
            spriteList.add(sprite);
        }
    }

    public void removeSprite(Sprite sprite) {
        synchronized (spriteList) {
            if(spriteList.contains(sprite))
                spriteList.remove(sprite);
        }
    }
}

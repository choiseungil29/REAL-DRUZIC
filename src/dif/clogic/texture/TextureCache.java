package dif.clogic.texture;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 24.
 * Time: 오후 4:33
 * To change this template use File | Settings | File Templates.
 */
public class TextureCache {

    private static HashMap<String, Texture> textureTable;
    private static TextureCache instance;

    private TextureCache() {
        textureTable = new HashMap<String, Texture>();
    }

    public void addTexture(String name, Texture texture) {
        if(!textureTable.containsValue(texture))
            textureTable.put(name, texture);
    }

    public Texture getTexture(String name) {
        return textureTable.get(name);
    }

    public boolean isInstance(String name) {
        return textureTable.containsKey(name);
    }

    public static TextureCache getInstance() {
        if(instance == null)
            instance = new TextureCache();
        return instance;
    }
}

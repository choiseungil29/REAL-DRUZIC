package dif.clogic.graphics;

import dif.clogic.texture.Texture;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 24.
 * Time: 오후 1:38
 * To change this template use File | Settings | File Templates.
 */
public class Animation {

    private ArrayList<AnimationFrame> frames;
    private AnimationFrame nowFrame;
    private boolean repeat;
    private float rate;

    public Animation() {
        repeat = false;
        frames = new ArrayList<AnimationFrame>();
        nowFrame = null;
        rate = 0.0f;
    }

    public void addFrameWithTexture(Texture tex) {
        frames.add(new AnimationFrame(tex));
    }

    public void setRepeat(boolean b) {
        repeat = b;
    }

    public boolean getRepeat() {
        return repeat;
    }

    public void setRate(float animationRate) {
        rate = animationRate;
        for(AnimationFrame frame : frames) {
            frame.originRate = rate/frames.size();
            frame.rate = frame.originRate;
        }
    }

    public float getRate() {
        return rate;
    }

    public void update(float dt) {
        if(frames.size() <= 0)
            return;

        if(nowFrame == null) {
            nowFrame = frames.get(0);
        }

        if(!repeat) {
            if(frames.indexOf(nowFrame) >= (frames.size()-1)) {
                return;
            }
        }

        nowFrame.rate -= dt;
        if(nowFrame.rate <= 0.0f) {
            nowFrame.rate = nowFrame.originRate;
            nowFrame = frames.get((frames.indexOf(nowFrame)+1)%frames.size());
        }
    }

    public Texture getNowFrameTexture() {
        return nowFrame.texture;
    }

    public boolean isEnd() {
        if(!repeat) {
            if(frames.indexOf(nowFrame) >= (frames.size()-1)) {
                return true;
            }
        }
        return false;
    }

    public class AnimationFrame {
        private Texture texture;
        private float originRate;
        private float rate;

        public AnimationFrame(Texture tex) {
            texture = tex;
            rate = 0.0f;
            originRate = 0.0f;
        }
    }

}
package dif.clogic.other;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 13.
 * Time: 오후 5:17
 * To change this template use File | Settings | File Templates.
 */
public class Sound {
    public int streamId;
    public float volume;
    public String refName;
    public boolean isPlaying;
    public boolean isStart;
    public boolean isEnd;
    public int beatTerm;

    public Sound(String refName) {
        this.volume = 1.0f;
        this.isPlaying = false;
        this.isStart = false;
        this.isEnd = false;
        this.beatTerm = 0;
        this.refName = refName;
    }
}

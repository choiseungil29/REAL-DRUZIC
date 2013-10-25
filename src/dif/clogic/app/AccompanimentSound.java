package dif.clogic.app;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 23.
 * Time: 오후 12:47
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentSound { //just for using Melody.
    public String refName;
    public int originBeatTerm;
    public int beatTerm;
    public int streamId;
    public float volume;

    public AccompanimentSound(String refName, int beatTerm) { // 잘 조합해서 넣음
        this.refName = refName;
        this.volume = 0.75f;
        this.beatTerm = 0;
        this.originBeatTerm = beatTerm;
    }
}

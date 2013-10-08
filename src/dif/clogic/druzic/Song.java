package dif.clogic.druzic;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 25.
 * Time: 오전 5:30
 * To change this template use File | Settings | File Templates.
 */
public class Song {
    public int Id;
    public String Name;
    public String originRecord;
    public boolean IsMelody;

    public Song(int id, String name, String melodySequence, boolean isMelody) {
        Id = id;
        Name = name;
        originRecord = melodySequence;
        IsMelody = isMelody;
    }

    static String convert(String str) {
        String convert = str.replaceAll(" ", "&");
        convert = convert.replaceAll("_", "");
        convert = convert.substring(1);

        return convert;
    }
}

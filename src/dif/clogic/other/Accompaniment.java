package dif.clogic.other;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 25.
 * Time: 오전 5:30
 * To change this template use File | Settings | File Templates.
 */
public class Accompaniment {
    public int Id;
    public String Name;
    public String originRecord;
    public boolean checked;

    public Accompaniment(int id, String name, String melodySequence) {
        Id = id;
        Name = name;
        originRecord = melodySequence;
        checked = false;
    }

    public static String convert(String str) {
        String convert = str;
        convert = convert.substring(0, convert.length()-1);
        convert = convert.replaceAll("_", "");
        convert.toUpperCase();

        return convert;
    }
}

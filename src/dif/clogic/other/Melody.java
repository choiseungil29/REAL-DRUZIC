package dif.clogic.other;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 15.
 * Time: 오후 7:14
 * To change this template use File | Settings | File Templates.
 */
public class Melody {
    public int Id;
    public String Name;
    public String originRecord;
    public ArrayList<Accompaniment> accompanimentRecordList;

    public Melody(int id, String name, String record, ArrayList<Accompaniment> accompanimentList) {
        Id = id;
        Name = name;
        originRecord = record;
        accompanimentRecordList = accompanimentList;
    }
}

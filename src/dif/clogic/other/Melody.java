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
    public String birth;
    public ArrayList<String> accompanimentRecordList;

    public Melody(int id, String name, String record, String accompanimentList) {
        Id = id;
        Name = name;
        originRecord = record;
        accompanimentRecordList = new ArrayList<String>();
        String[] str = accompanimentList.split(", ");
        if(str[0].equals("")) {
        } else {
            for(String chip : str) {
                accompanimentRecordList.add(chip);
            }
        }
    }

    static public String StringListToString(ArrayList<String> list) {
        String builder = "";
        for(String str : list) {
            builder += str;
            builder += ", ";
        }
        return builder;
    }
}

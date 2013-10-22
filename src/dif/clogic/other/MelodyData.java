package dif.clogic.other;

import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 15.
 * Time: 오후 8:14
 * To change this template use File | Settings | File Templates.
 */
public class MelodyData {

    public static final class CreateDB implements BaseColumns {

        public static final String NAME = "name";
        public static final String ORIGINRECORD = "originrecord";
        public static final String ACCOMPANIMENTLIST = "accompanimentlist";
        public static final String _TABLENAME = "melodydata";
        public static final String _CREATE =
                "create table " + _TABLENAME + " ("
                        + _ID + " integer primary key autoincrement, "
                        + NAME + " text not null , "
                        + ACCOMPANIMENTLIST + " text not null , "
                        + ORIGINRECORD + " text not null );";
    }
}

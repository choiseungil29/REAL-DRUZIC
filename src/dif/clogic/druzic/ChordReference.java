package dif.clogic.druzic;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 23.
 * Time: 오후 8:17
 * To change this template use File | Settings | File Templates.
 */
public class ChordReference {

    public static final String[] melodyList = { "cn_5", "cs_5", "dn_5", "ds_5", "en_5", "fn_5", "fs_5", "gn_5", "gs_5", "an_5", "as_5", "bn_5", // 0~11
            "cn_6", "cs_6", "dn_6", "ds_6", "en_6", "fn_6", "fs_6", "gn_6", "gs_6", "an_6", "as_6", "bn_6", // 12~23
            "cn_7", "cs_7", "dn_7", "ds_7", "en_7", "fn_7", "fs_7", "gn_7", "gs_7", "an_7", "as_7", "bn_7", // 24~35
            "cn_8"};

    public static final String[] beatList = { "bg_1", "bg_2", "bg_space" };

    public static final int[] C = { 0, 4, 7, 12, 16, 19, 24, 28, 31, 36 };
    public static final int[] CM = { 0, 3, 7, 12, 15, 19, 24, 27, 31, 36 };
    public static final int[] D = { 2, 6, 9, 14, 18, 21, 26, 30, 33 };
    public static final int[] DM = { 2, 5, 9, 14, 17, 21, 26, 29, 33 };
    public static final int[] E = { 4, 8, 11, 16, 20, 23, 28, 32, 35 };
    public static final int[] EM = { 4, 7, 11, 16, 19, 23, 28, 31, 35 };
    public static final int[] F = { 0, 5, 9, 12, 17, 21, 24, 29, 33, 36 };
    public static final int[] FM = {0, 5, 8, 12, 17, 20, 24, 29, 32, 36 };
    public static final int[] G = { 2, 7, 11, 14, 19, 23, 26, 31, 35 };
    public static final int[] GM = { 2, 7, 10, 14, 19, 22, 26, 31, 34 };
    public static final int[] A = { 1, 4, 9, 13, 16, 21, 25, 28, 33 };
    public static final int[] AM = { 0, 4, 9, 12, 16, 21, 24, 28, 33, 36 };
    public static final int[] B = { 3, 6, 11, 15, 18, 23, 27, 30, 35 };
    public static final int[] BM = { 2, 6, 11, 14, 18, 23, 26, 30, 35 };

    public static final int[][] bluePackage = {C, EM, F, G};
    public static final int[][] redPackage = {C, AM, F, G};
    public static final int[][] cyanPackgage = {C, G, F, G};
    public static final int[][] yellowPackage = {F, G, EM, AM};

    public static final int[] beatReady = {1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2};
    public static final int[] beat1 = {0, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2};
    public static final int[] beat2 = {0, 2, 2, 2, 1, 2, 0, 2, 2, 2, 0, 2, 1, 2, 2, 2};
    public static final int[] beat3 = {0, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 2, 1, 2, 2, 1};
    public static final int[] beat4 = {0, 2, 2, 2, 1, 2, 2, 0, 2, 0, 0, 2, 1, 2, 2, 2};
}

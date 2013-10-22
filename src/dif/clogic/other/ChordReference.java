package dif.clogic.other;

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

    public static final String[] melodyList2 = {
            "c_4", "d_4", "e_4", "f_4", "g_4", "a_4", "b_4",
            "c_5", "d_5", "e_5", "f_5", "g_5", "a_5", "b_5",
            "c_6"
    };

    public static final String[] accompanimentList = {
            "e_2", "f_2", "g_2", "a_2", "b_2", // why not exist 'C1, D1'?
            "c_3", "d_3", "e_3", "f_3", "g_3", "a_3", "b_3",
            "c_4", "d_4", "e_4", "f_4", "g_4", "a_4", "b_4",
            "c_5"
    };

    public static final double[] testList = {
            16.35, 17.32, 18.35, 19.45, 20.60, 21.83, 23.12, 24.50, 25.96, 27.50, 29.14, 30.87,
            32.70, 34.65, 36.71, 38.89, 41.20, 43.65, 46.25, 49.00, 51.91, 55.00, 58.27, 61.74,
            65.41, 69.30, 73.42, 77.78, 82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54, 123.47,
            130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65, 220.00, 233.08, 246.94,
            261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99, 392.00, 415.30, 440.00, 466.16, 493.88,
            523.25, 554.37, 587.33, 622.25, 659.26, 698.46, 739.99, 783.99, 830.61, 880.00, 932.33, 987.77,
            1046.50, 1108.73, 1174.66, 1244.51, 1318.51, 1396.91, 1479.98, 1567.98, 1661.22, 1760.00, 1864.66, 1975.53,
            2093.00, 2217.46, 2349.32, 2489.02, 2637.02, 2793.83, 2959.96, 3135.96, 3322.44, 3520.00, 3729.31, 3951.07,
            4186.01, 4434.92, 4698.64, 4978.03
    }; // length 100

    public static final String[] beatList = { "bg_1", "bg_2", "bg_space" };

    /*public static final int[] C = { 0, 4, 7, 12, 16, 19, 24, 28, 31, 36 };
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
    public static final int[] BM = { 2, 6, 11, 14, 18, 23, 26, 30, 35 };*/

    /*public static final int[] C = { 0, 4, 7, 12, 16, 19, 24, 28, 31, 36, 40, 43, 48, 52, 55, 60, 64, 67, 72, 76, 79, 84, 88, 91, 96 };
    public static final int[] CM = { 0, 3, 7, 12, 15, 19, 24, 27, 31, 36, 39, 43, 48, 51, 55, 60, 63, 67, 72, 75, 79, 84, 87, 91, 96 };
    public static final int[] D = { 2, 6, 9, 14, 18, 21, 26, 30, 33, 38, 42, 45, 50, 54, 57, 62, 66, 69, 74, 78, 81, 86, 90, 93, 98 };
    public static final int[] DM = { 2, 5, 9, 14, 17, 21, 26, 29, 33, 38, 41, 45, 50, 53, 57, 62, 65, 69, 74, 77, 81, 86, 89, 93, 98 };
    public static final int[] E = { 4, 8, 11, 16, 20, 23, 28, 32, 35, 40, 44, 47, 52, 56, 59, 64, 68, 71, 76, 80, 83, 88, 92, 95 };
    public static final int[] EM = { 4, 7, 11, 16, 19, 23, 28, 31, 35, 40, 43, 47, 52, 55, 59, 64, 67, 71, 76, 79, 83, 88, 91, 95 };
    public static final int[] F = { 0, 5, 9, 12, 17, 21, 24, 29, 33, 36, 41, 45, 48, 53, 57, 60, 65, 69, 72, 77, 81, 84, 89, 93, 96 };
    public static final int[] FM = {0, 5, 8, 12, 17, 20, 24, 29, 32, 36, 41, 44, 48, 53, 56, 60, 65, 68, 72, 77, 80, 84, 89, 92, 96 };
    public static final int[] G = { 2, 7, 11, 14, 19, 23, 26, 31, 35, 38, 43, 47, 50, 55, 59, 62, 67, 71, 74, 79, 83, 86, 91, 95, 98 };
    public static final int[] GM = { 2, 7, 10, 14, 19, 22, 26, 31, 34, 38, 43, 46, 50, 55, 58, 62, 67, 70, 74, 79, 82, 86, 91, 94, 98 };
    public static final int[] A = { 1, 4, 9, 13, 16, 21, 25, 28, 33, 37, 40, 45, 49, 52, 57, 61, 64, 69, 73, 76, 81, 85, 88, 93, 97 };
    public static final int[] AM = { 0, 4, 9, 12, 16, 21, 24, 28, 33, 36, 40, 45, 48, 52, 57, 60, 64, 69, 72, 76, 81, 84, 88, 93, 96 };
    public static final int[] B = { 3, 6, 11, 15, 18, 23, 27, 30, 35, 39, 42, 47, 51, 54, 59, 63, 66, 71, 75, 78, 83, 87, 90, 95, 99 };
    public static final int[] BM = { 2, 6, 11, 14, 18, 23, 26, 30, 35, 38, 42, 47, 50, 54, 59, 62, 66, 71, 74, 78, 83, 87, 91, 96, 99 };*/

    public static final int[] C = { 0, 4, 7, 12 };
    public static final int[] CM = { 0, 3, 7, 12 };
    public static final int[] D = { 2, 6, 9, 14 };
    public static final int[] DM = { 2, 5, 9, 14 };
    public static final int[] E = { 4, 8, 11 };
    public static final int[] EM = { 4, 7, 11 };
    public static final int[] F = { 0, 5, 9, 12 };
    public static final int[] FM = {0, 5, 8, 12 };
    public static final int[] G = { 2, 7, 11, 14 };
    public static final int[] GM = { 2, 7, 10, 14 };
    public static final int[] A = { 1, 4, 9, 13 };
    public static final int[] AM = { 0, 4, 9, 12 };
    public static final int[] B = { 3, 6, 11 };
    public static final int[] BM = { 2, 6, 11, 14 };

    public static final int[][] bluePackage = {C, EM, F, G};
    public static final int[][] redPackage = {C, AM, F, G};
    public static final int[][] cyanPackgage = {C, G, F, G};
    public static final int[][] yellowPackage = {F, G, EM, AM};

    public static final int[] beatReady = {1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2};
    public static final int[] beat1 = {0, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2};
    public static final int[] beat2 = {0, 2, 2, 2, 1, 2, 0, 2, 2, 2, 0, 2, 1, 2, 2, 2};
    public static final int[] beat3 = {0, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 2, 1, 2, 2, 1};
    public static final int[] beat4 = {0, 2, 2, 2, 1, 2, 2, 0, 2, 0, 0, 2, 1, 2, 2, 2};
}

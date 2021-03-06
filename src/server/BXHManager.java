package server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import real.ClanManager;
import real.ItemData;

/**
 *
 * @author ghost
 */
public class BXHManager {

    public static class Entry {

        int index;
        String name;
        long[] nXH;
    }

    public static final ArrayList<Entry> bangXH[] = new ArrayList[6];
    public static final Timer t = new Timer();

    public static void ini() {
        Calendar cl = GregorianCalendar.getInstance();
        Date d = new Date();
        cl.setTime(d);
        cl.set(Calendar.HOUR_OF_DAY, 0);
        cl.set(Calendar.MINUTE, 0);
        cl.set(Calendar.SECOND, 0);
        cl.add(Calendar.DATE, 0);
        t.schedule(new TimerTask() {
            public void run() {
                init();
            }
        }, cl.getTime(), 1000*60*10);
    }

    public static void init() {
        for (int i = 0; i < bangXH.length; i++) {
            bangXH[i] = new ArrayList<>();
        }
        System.out.println("load BXH");
        for (int i = 0; i < bangXH.length; i++) {
            initBXH(i);
        }
    }

    public static void initBXH(int type) {
        ResultSet red;
        bangXH[type].clear();
        ArrayList<Entry> bxh = bangXH[type];
        switch (type) {
            case 0:
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`yen`,`level` FROM `ninja` WHERE (`yen` > 0) ORDER BY `yen` DESC LIMIT 10;");
                    while (red.next()) {
                        String name = red.getString("name");
                        int coin = red.getInt("yen");
                        int level = red.getInt("level");
                        Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = coin;
                        bXHE.nXH[1] = level;
                        bxh.add(bXHE);
                        i++;
                    }
                    red.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`exp`,`level` FROM `ninja` WHERE (`exp` > 0) ORDER BY `exp` DESC LIMIT 10;");
                    while (red.next()) {
                        String name = red.getString("name");
                        long exp = red.getLong("exp");
                        int level = red.getInt("level");
                        Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = exp;
                        bXHE.nXH[1] = level;
                        bxh.add(bXHE);
                        i++;
                    }
                    red.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`level` FROM `clan` WHERE (`level` > 0) ORDER BY `level` DESC LIMIT 10;");
                    while (red.next()) {
                        String name = red.getString("name");
                        int level = red.getInt("level");
                        Entry bXHE = new Entry();
                        bXHE.nXH = new long[1];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = level;
                        bxh.add(bXHE);
                        i++;
                    }
                    red.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`bagCaveMax`,`itemIDCaveMax` FROM `ninja` WHERE (`bagCaveMax` > 0) ORDER BY `bagCaveMax` DESC LIMIT 10;");
                    while (red.next()) {
                        String name = red.getString("name");
                        int cave = red.getInt("bagCaveMax");
                        short id = red.getShort("itemIDCaveMax");
                        Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = cave;
                        bXHE.nXH[1] = id;
                        bxh.add(bXHE);
                        i++;
                    }
                    red.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`sk` FROM `ninja` WHERE (`sk` > 0) ORDER BY `sk` DESC LIMIT 10;");
                    while (red.next()) {
                        String name = red.getString("name");
                        int sk = red.getInt("sk");
                        Entry bXHE = new Entry();
                        bXHE.nXH = new long[1];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = sk;
                        bxh.add(bXHE);
                        i++;
                    }
                    red.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 5: {
                try {
                    int i = 1;
                    red = SQLManager.stat.executeQuery("SELECT `name`,`pointCT`,`typeCT` FROM `ninja` WHERE (`pointCT` > 0) ORDER BY `pointCT` DESC LIMIT 15;");
                    while (red.next()) {
                        final String name = red.getString("name");
                        final int pointCT = red.getInt("pointCT");
                        final int typeCT = red.getInt("typeCT");
                        final Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = pointCT;
                        bXHE.nXH[1] = typeCT;
                        bxh.add(bXHE);
                        ++i;
                    }
                    red.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }    
        }
    }

    public static final Entry[] getBangXH(int type) {
        ArrayList<Entry> bxh = bangXH[type];
        Entry[] bxhA = new Entry[bxh.size()];
        for (int i = 0; i < bxhA.length; i++) {
            bxhA[i] = bxh.get(i);
        }
        return bxhA;
    }

    public static String getStringBXH(int type) {
        String str = "";
        switch (type) {
            case 0:
                if (bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                } else {
                    for (Entry bxh : bangXH[type]) {
                        str += bxh.index + ". " + bxh.name + ": " + util.getFormatNumber(bxh.nXH[0]) + " y??n - c???p: " + bxh.nXH[1] + "\n";
                    }
                    break;
                }
                break;
            case 1:
                if (bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                } else {
                    for (Entry bxh : bangXH[type]) {
                        str += bxh.index + ". " + bxh.name + ": " + util.getFormatNumber(bxh.nXH[0]) + " kinh nghi???m - c???p: " + bxh.nXH[1] + "\n";
                    }
                }
                break;
            case 2:
                if (bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                } else {
                    for (Entry bxh : bangXH[type]) {
                        ClanManager clan = ClanManager.getClanName(bxh.name);
                        if (clan != null) {
                            str += bxh.index + ". Gia t???c " + bxh.name + " tr??nh ????? c???p " + bxh.nXH[0] + " do " + clan.getmain_name() + " l??m t???c tr?????ng, th??nh vi??n " + clan.members.size() + "/" + clan.getMemMax() + "\n";
                        } else {
                            str += bxh.index + ". Gia t???c " + bxh.name + " tr??nh ????? c???p " + bxh.nXH[0] + " ???? b??? gi???i t??n\n";
                        }
                    }
                }
                break;
            case 3:
                if (bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                } else {
                    for (Entry bxh : bangXH[type]) {
                        str += bxh.index + ". " + bxh.name + " nh???n ???????c " + util.getFormatNumber(bxh.nXH[0]) + " " + ItemData.ItemDataId((int) bxh.nXH[1]).name + "\n";
                    }
                }
                break;
            case 4:
                if (bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                } else {
                    for (Entry bxh : bangXH[type]) {
                        str += bxh.index + ". " + bxh.name + " ??ang c?? " + util.getFormatNumber(bxh.nXH[0]) + " ??i???m\n";
                    }
                }
                break;
            case 5: {
                if (BXHManager.bangXH[type].isEmpty()) {
                    str = "Ch??a c?? th??ng tin";
                    break;
                }
                for (final Entry bxh : BXHManager.bangXH[type]) {
                    str = str + bxh.index + ". " + bxh.name + ": " + util.getFormatNumber(bxh.nXH[0]) + " ??i???m " + (bxh.nXH[1] == 4 ? "(B???ch)" :"(H???c)") + "\nDanh hi???u: " + (bxh.nXH[0] < 4000 ? (bxh.nXH[0] < 1500 ? (bxh.nXH[0] < 600 ? (bxh.nXH[0] < 200 ? "H???c Gi???" : "H??? Nh???n") : "Trung Nh???n") : "Th?????ng Nh???n") : "Nh???n Gi???") + "\n";
                }
                break;
            }    
        }
        return str;
    }

}

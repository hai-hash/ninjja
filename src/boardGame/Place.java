package boardGame;

import io.Message;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import real.Body;
import real.Effect;
import real.Item;
import real.ItemData;
import real.Level;
import real.Option;
import real.Map;
import real.Mob;
import real.Char;
import real.Npc;
import real.Party;
import real.Player;
import real.Skill;
import real.SkillData;
import real.SkillTemplates;
import real.Vgo;
import server.GameCanvas;
import server.GameScr;
import server.Manager;
import server.Server;
import server.Service;
import server.util;
import real.Cave;
import server.BXHManager;
import server.SQLManager;

/**
 *
 * @author Văn Tú
 */
public class Place {

    Map ma = Manager.getMapid(22);
    public Player p;
    public Map map;
    protected byte id;
    public byte numplayers = 0;
    private int numTA = 0;
    private int numTL = 0;
    protected int numMobDie = 0;
    public final ArrayList<Player> players = new ArrayList<>();
    public final ArrayList<Mob> mobs = new ArrayList<>();
    private final ArrayList<ItemMap> itemMap = new ArrayList<>();
    Server server = Server.getInstance();
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);

    public Place(Map map, byte id) {
        this.map = map;
        this.id = id;
    }

    public void sendMessage(Message m) {
        try {
            for (int i = players.size() - 1; i >= 0; i--) {
                players.get(i).conn.sendMessage(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMyMessage(Player p, Message m) {
        for (int i = players.size() - 1; i >= 0; i--) {
            if (p.id != players.get(i).id) {
                players.get(i).conn.sendMessage(m);
            }
        }
    }

    public Mob getMob(int id) {
        for (short i = 0; i < mobs.size(); i++) {
            if (mobs.get(i).id == id) {
                return mobs.get(i);
            }
        }
        return null;
    }

    public ArrayList getArryListParty() {
        synchronized (this) {
            ArrayList<Party> partys = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (p.c.get().party != null) {
                    boolean co = true;
                    for (int j = 0; j < partys.size(); j++) {
                        if (p.c.get().party.id == partys.get(j).id) {
                            co = false;
                            break;
                        }
                    }
                    if (co) {
                        partys.add(p.c.get().party);
                    }
                }
            }
            return partys;
        }
    }

    public Char getNinja(int id) {
        synchronized (this) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).c.id == id) {
                    return players.get(i).c;
                }
            }
            return null;
        }
    }

    private short getItemMapNotId() {
        short itemmapid = 0;
        while (true) {
            boolean isset = false;
            for (int i = itemMap.size() - 1; i >= 0; i--) {
                if (itemMap.get(i).itemMapId == itemmapid) {
                    isset = true;
                }
            }
            if (!isset) {
                return itemmapid;
            }
            itemmapid++;
        }
    }

    public void leave(Player p) {
        synchronized (this) {
            if (map.cave != null && map.cave.ninjas.contains(p.c)) {
                map.cave.ninjas.remove(p.c);
            }
            if (players.contains(p)) {
                players.remove(p);
                removeMessage(p.c.id);
                removeMessage(p.c.clone.id);
                numplayers--;
            }
        }
    }

    public void changerTypePK(Player p, Message m) throws IOException {
        if (p.c.isNhanban) {
            p.sendAddchatYellow("Bạn đang trong chế độ thứ thân không thể dùng được chức năng này");
            return;
        }
        byte pk = m.reader().readByte();
        m.cleanup();
        if (p.c.pk > 14) {
            p.sendAddchatYellow("Điểm hiếu chiến quá cao không thể thay đổi chế độ pk");
            return;
        }
        if (map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) {
            p.sendAddchatYellow("Không thể thay đổi chế độ PK");
            return;
        }
        if (pk < 0 || pk > 3) {
            return;
        }
        p.c.typepk = pk;
        m = new Message(-30);
        m.writer().writeByte(-92);
        m.writer().writeInt(p.c.id);
        m.writer().writeByte(pk);
        sendMessage(m);
        m.cleanup();
    }

    public void sendCoat(Body b, Player pdo) {
        try {
            if (b.ItemBody[12] == null) {
                return;
            }
            Message m = new Message(-30);
            m.writer().writeByte((72 - 128));
            m.writer().writeInt(b.id);
            m.writer().writeInt(b.hp);
            m.writer().writeInt(b.getMaxHP());
            m.writer().writeShort(b.ItemBody[12].id);
            m.writer().flush();
            pdo.conn.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGlove(Body b, Player pdo) {
        try {
            if (b.ItemBody[13] == null) {
                return;
            }
            Message m = new Message(-30);
            m.writer().writeByte((73 - 128));
            m.writer().writeInt(b.id);
            m.writer().writeInt(b.hp);
            m.writer().writeInt(b.getMaxHP());
            m.writer().writeShort(b.ItemBody[13].id);
            m.writer().flush();
            pdo.conn.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMounts(Body b, Player pdo) {
        try {
            Message m = new Message(-30);
            m.writer().writeByte(-54);
            m.writer().writeInt(b.id);//id ninja
            for (byte i = 0; i < 5; i++) {
                Item item = b.ItemMounts[i];
                if (item != null) {
                    m.writer().writeShort(item.id);
                    m.writer().writeByte(item.upgrade);//cap
                    m.writer().writeLong(item.expires);//het han
                    m.writer().writeByte(item.sys);//thuoc tinh
                    m.writer().writeByte(item.options.size());//lent option
                    for (Option Option : item.options) {
                        m.writer().writeByte(Option.id);
                        m.writer().writeInt(Option.param);
                    }
                } else {
                    m.writer().writeShort(-1);
                }
            }
            m.writer().flush();
            pdo.conn.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Chat(Player p, Message m) throws IOException {
        String chat = m.reader().readUTF();
        if (chat.equals("gamingbaotri")) {
            server.stop();
            return;
        }
        m.cleanup();
        m = new Message(-23);
        m.writer().writeInt(p.c.get().id);
        m.writer().writeUTF(chat);
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    public void EnterMap0(Char n) {
        n.clone.x = n.x = map.template.x0;
        n.clone.y = n.y = map.template.y0;
        n.mapid = map.id;
        try {
            Enter(n.p);
        } catch (IOException e) {
        }
    }

    public void Enter(Player p) throws IOException {
        synchronized (this) {
            players.add(p);
            p.c.place = this;
            numplayers++;
            p.c.mobAtk = -1;
            p.c.eff5buff = System.currentTimeMillis() + 5000L;
            if (map.cave != null) {
                map.cave.ninjas.add(p.c);
            }
            if (map.timeMap != -1) {
                p.setTimeMap((int) (map.cave.time - System.currentTimeMillis()) / 1000);
            }
            Message m = new Message(57);
            m.writer().flush();
            p.conn.sendMessage(m);
            m = new Message(-18);
            m.writer().writeByte(map.id);//map id
            m.writer().writeByte(map.template.tileID);//tile id
            m.writer().writeByte(map.template.bgID);//bg id
            m.writer().writeByte(map.template.typeMap);//type map
            m.writer().writeUTF(map.template.name);//name map
            m.writer().writeByte(id);//zone
            m.writer().writeShort(p.c.get().x);//X
            m.writer().writeShort(p.c.get().y); // Y
            m.writer().writeByte(map.template.vgo.length);// vgo
            for (byte i = 0; i < map.template.vgo.length; i++) {
                m.writer().writeShort(map.template.vgo[i].minX);//x
                m.writer().writeShort(map.template.vgo[i].minY);//y
                m.writer().writeShort(map.template.vgo[i].maxX);//xnext
                m.writer().writeShort(map.template.vgo[i].maxY);//ynext
            }
            m.writer().writeByte(mobs.size());// mob
            for (short i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                m.writer().writeBoolean(mob.isDisable);//isDisable
                m.writer().writeBoolean(mob.isDontMove);//isDontMove
                m.writer().writeBoolean(mob.isFire);//isFire
                m.writer().writeBoolean(mob.isIce);//isIce
                m.writer().writeBoolean(mob.isWind);//isWind
                m.writer().writeByte(mob.templates.id);//id templates
                m.writer().writeByte(mob.sys);//sys
                m.writer().writeInt(mob.hp);//hp
                m.writer().writeByte(mob.level);//level
                m.writer().writeInt(mob.hpmax);//hp max
                m.writer().writeShort(mob.x);//x
                m.writer().writeShort(mob.y);//y
                m.writer().writeByte(mob.status);//status
                m.writer().writeByte(mob.lvboss);//level boss
                m.writer().writeBoolean(mob.isboss);//isBosss
            }
            m.writer().writeByte(0); // 
            for (int i = 0; i < 0; i++) {
                m.writer().writeUTF("khúc gỗ");//name
                m.writer().writeShort(1945);//x
                m.writer().writeShort(240);//y
            }
            m.writer().writeByte(map.template.npc.length);//numb npc
            for (Npc npc : map.template.npc) {
                m.writer().writeByte(npc.type); //type
                m.writer().writeShort(npc.x); //x
                m.writer().writeShort(npc.y); //y
                m.writer().writeByte(npc.id); //id
            }
            m.writer().writeByte(itemMap.size());// item map
            for (int i = 0; i < itemMap.size(); i++) {
                ItemMap im = itemMap.get(i);
                m.writer().writeShort(im.itemMapId); //item map id
                m.writer().writeShort(im.item.id); //id item
                m.writer().writeShort(im.x); //x
                m.writer().writeShort(im.y); //y
            }
            m.writer().writeUTF(map.template.name);//name zone
            m.writer().writeByte(0);// item
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
            //Send Info team to me
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                if (player.id != p.id) {
                    sendCharInfo(player, p);
                    sendCoat(player.c.get(), p);
                    sendGlove(player.c.get(), p);
                }
                if (!player.c.isNhanban && !player.c.clone.isDie) {
                    Service.sendclonechar(player, p);
                }
                sendMounts(player.c.get(), p);
            }
            //Send Info do team
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                if (player.id != p.id) {
                    sendCharInfo(p, player);
                    sendCoat(p.c.get(), player);
                    sendGlove(p.c.get(), player);
                    if (!player.c.isNhanban && p.c.timeRemoveClone > System.currentTimeMillis()) {
                        Service.sendclonechar(p, player);
                    }
                }
                sendMounts(p.c.get(), player);
            }
            if (p.c.level == 1) {
                p.updateExp(Level.getMaxExp(99));
                p.upluongMessage(2000000000L);
                p.c.upxuMessage(2000000000L);
                p.c.upyenMessage(2000000000L);
                p.c.addItemBag(false, ItemData.itemDefault(194));
            }
            if (util.compare_Day(Date.from(Instant.now()), p.c.newlogin)) {
                p.c.pointCave = 0;
                p.c.nCave = 1;
                p.c.useCave = 2;
                p.c.ddClan = false;
                p.c.newlogin = Date.from(Instant.now());
            }
        }
    }

    public void VGo(Player p, Message m) throws IOException {
        m.cleanup();
        for (byte i = 0; i < map.template.vgo.length; i++) {
            Vgo vg = map.template.vgo[i];
            if (p.c.get().x + 100 >= vg.minX && p.c.get().x <= vg.maxX + 100 && p.c.get().y + 100 >= vg.minY && p.c.get().y <= vg.maxY + 100) {
                leave(p);
                int mapid;
                if (map.id == 138) {
                    mapid = new int[]{134, 135, 136, 137}[util.nextInt(4)];
                } else {
                    mapid = vg.mapid;
                }
                Map ma = Manager.getMapid(mapid);
                if (map.cave != null) {
                    for (byte j = 0; j < map.cave.map.length; j++) {
                        if (map.cave.map[j].id == mapid) {
                            ma = map.cave.map[j];
                        }
                    }
                }
                for (byte j = 0; j < ma.template.vgo.length; j++) {
                    Vgo vg2 = ma.template.vgo[j];
                    if (vg2.mapid == map.id) {
                        p.c.get().x = (short) (vg2.goX);
                        p.c.get().y = (short) (vg2.goY);
                    }
                }
                byte errornext = -1;
                for (byte n = 0; n < p.c.get().ItemMounts.length; n++) {
                    if (p.c.get().ItemMounts[n] != null && p.c.get().ItemMounts[n].isExpires && p.c.get().ItemMounts[n].expires < System.currentTimeMillis()) {
                        errornext = 1;
                    }
                }
                if (map.cave != null && map.getXHD() < 9 && map.cave.map.length > map.cave.level && map.cave.map[map.cave.level].id < mapid) {
                    errornext = 2;
                }
                if (errornext == -1) {
                    for (byte j = 0; j < ma.area.length; j++) {
                        if (ma.area[j].numplayers < ma.template.maxplayers) {
                            if (map.id == 138) {
                                ma.area[j].EnterMap0(p.c);
                            } else {
                                p.c.mapid = mapid;
                                p.c.x = vg.goX;
                                p.c.y = vg.goY;
                                p.c.clone.x = p.c.x;
                                p.c.clone.y = p.c.y;
                                ma.area[j].Enter(p);
                            }
                            return;
                        }
                        if (j == ma.area.length - 1) {
                            errornext = 0;
                        }
                    }
                }
                Enter(p);
                switch (errornext) {
                    case 0:
                        p.conn.sendMessageLog("Bản đồ quá tải.");
                        return;
                    case 1:
                        p.conn.sendMessageLog("Trang bị thú cưới đã hết hạn. Vui lòng tháo ra để di chuển");
                        return;
                    case 2:
                        p.conn.sendMessageLog("Cửa " + ma.template.name + " vẫn chưa mở");
                        return;
                }
            }
        }
    }

    public void moveMessage(Player p, Message m) throws IOException {
        short x, y, xold, yold;
        if (p.c.get().getEffId(18) != null || p.c.get().getEffId(6) != null || p.c.get().getEffId(7) != null) {
            return;
        }
        xold = p.c.get().x;
        yold = p.c.get().y;
        x = m.reader().readShort();
        y = m.reader().readShort();
        p.c.x = x;
        p.c.y = y;
        if (p.c.isNhanban) {
            p.c.clone.x = x;
            p.c.clone.y = y;
        }
        m.cleanup();
        move(p.c.get().id, p.c.get().x, p.c.get().y);
        /*     m = new Message(-23);
       m.writer().writeInt(p.nj.id);
        m.writer().writeUTF("X="+p.nj.x+"\nY="+p.nj.y);
        m.writer().flush();
       sendMessage(m);
       m.cleanup();*/
    }

    public void move(int id, short x, short y) {
        try {
            Message m = new Message(1);
            m.writer().writeInt(id);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().flush();
            sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeItemMapMessage(short itemmapid) throws IOException {
        Message m = new Message(-15);
        m.writer().writeShort(itemmapid);
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    public synchronized void pickItem(Player p, Message m) throws IOException {
        if (m.reader().available() == 0) {
            return;
        }
        short itemmapid = m.reader().readShort();
        m.cleanup();
        for (short i = 0; i < itemMap.size(); i++) {
            if (itemMap.get(i).itemMapId == itemmapid) {
                ItemMap itemmap = itemMap.get(i);
                Item item = itemmap.item;
                ItemData data = ItemData.ItemDataId(item.id);
                if (itemmap.master != -1 && itemmap.master != p.c.id) {
                    p.sendAddchatYellow("Vật phẩm của người khác.");
                    return;
                } else if (Math.abs(itemmap.x - p.c.get().x) > 50 || Math.abs(itemmap.y - p.c.get().y) > 30) {
                    p.sendAddchatYellow("Khoảng cách quá xa.");
                    return;
                } else if (data.type == 19 || p.c.getBagNull() > 0 || (p.c.getIndexBagid(item.id, item.isLock) != -1 && data.isUpToUp)) {
                    itemMap.remove(i);
                    m = new Message(-13);
                    m.writer().writeShort(itemmap.itemMapId);
                    m.writer().writeInt(p.c.get().id);
                    m.writer().flush();
                    sendMyMessage(p, m);
                    m.cleanup();
                    m = new Message(-14);
                    m.writer().writeShort(itemmap.itemMapId);
                    if (ItemData.ItemDataId(item.id).type == 19) {
                        p.c.upyen(item.quantity);
                        m.writer().writeShort(item.quantity);
                    }
                    m.writer().flush();
                    p.conn.sendMessage(m);
                    m.cleanup();
                    if (ItemData.ItemDataId(item.id).type != 19) {
                        p.c.addItemBag(true, itemmap.item);
                    }
                    break;
                } else {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống.");
                }
            }
        }
    }

    public void leaveItemBackground(Player p, Message m) throws IOException {
        byte index = m.reader().readByte();
        m.cleanup();
        Item itembag = p.c.getIndexBag(index);
        if (itembag == null || itembag.isLock) {
            return;
        }
        if (itemMap.size() > 100) {
            removeItemMapMessage(itemMap.remove(0).itemMapId);
        }
        short itemmapid = getItemMapNotId();
        ItemMap item = new ItemMap();
        item.x = p.c.get().x;
        item.y = p.c.get().y;
        item.itemMapId = itemmapid;
        item.item = itembag;
        itemMap.add(item);
        p.c.ItemBag[index] = null;
        m = new Message(-6);
        m.writer().writeInt(p.c.get().id);
        m.writer().writeShort(item.itemMapId);
        m.writer().writeShort(item.item.id);
        m.writer().writeShort(item.x);
        m.writer().writeShort(item.y);
        m.writer().flush();
        sendMyMessage(p, m);
        m.cleanup();
        m = new Message(-12);
        m.writer().writeByte(index);
        m.writer().writeShort(item.itemMapId);
        m.writer().writeShort(item.x);
        m.writer().writeShort(item.y);
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    public void refreshMob(int mobid) {
        try {
            synchronized (this) {
                Mob mob = getMob(mobid);
                mob.ClearFight();
                mob.sys = (byte) util.nextInt(1, 3);
                if (map.cave == null && mob.lvboss != 3 && !mob.isboss) {
                    if (mob.lvboss > 0) {
                        mob.lvboss = 0;
                    }
                    if (mob.level >= 10 && 1 > util.nextInt(100) && numTA < 2 && numTL < 1) {
                        mob.lvboss = util.nextInt(1, 2);
                    }
                }
                if (map.cave != null && map.cave.finsh > 0 && map.getXHD() == 6) {
                    int hpup = mob.templates.hp * ((10 * map.cave.finsh) + 100) / 100;
                    mob.hp = mob.hpmax = hpup;
                } else {
                    mob.hp = mob.hpmax = mob.templates.hp;
                }
                if (mob.lvboss == 3) {
                    mob.hp = mob.hpmax *= 200;
                } else if (mob.lvboss == 2) {
                    numTL++;
                    mob.hp = mob.hpmax *= 100;
                } else if (mob.lvboss == 1) {
                    numTA++;
                    mob.hp = mob.hpmax *= 10;
                }
                mob.status = 5;
                mob.isDie = false;
                mob.timeRefresh = 0;
                Message m = new Message(-5);
                m.writer().writeByte(mob.id);
                m.writer().writeByte(mob.sys);
                m.writer().writeByte(mob.lvboss);
                m.writer().writeInt(mob.hpmax);
                m.writer().flush();
                sendMessage(m);
                m.cleanup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attachedMob(int dame, int mobid, boolean fatal) throws IOException {
        Message m = new Message(-1);
        m.writer().writeByte(mobid);
        Mob mob = getMob(mobid);
        m.writer().writeInt(mob.hp);
        m.writer().writeInt(dame);
        m.writer().writeBoolean(fatal);//flag
        m.writer().writeByte(mob.lvboss);
        m.writer().writeInt(mob.hpmax);
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    private void MobStartDie(int dame, int mobid, boolean fatal) throws IOException {
        Mob mob = getMob(mobid);
        Message m = new Message(-4);
        m.writer().writeByte(mobid);
        m.writer().writeInt(dame);
        m.writer().writeBoolean(fatal);//flag
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    private void sendXYPlayer(Player p) throws IOException {
        Message m = new Message(52);
        m.writer().writeShort(p.c.get().x);
        m.writer().writeShort(p.c.get().y);
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    private void setXYPlayers(short x, short y, Player p1, Player p2) throws IOException {
        p1.c.get().x = p2.c.get().x = x;
        p1.c.get().y = p2.c.get().y = y;
        Message m = new Message(64);
        m.writer().writeInt(p1.c.get().id);
        m.writer().writeShort(p1.c.get().x);
        m.writer().writeShort(p1.c.get().y);
        m.writer().writeInt(p2.c.get().id);
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    public void removeMessage(int id) {
        try {
            Message m = new Message(2);
            m.writer().writeInt(id);
            m.writer().flush();
            sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCharInfo(Player p, Player p2) {
        try {
            Message m = new Message(3);
            m.writer().writeInt(p.c.get().id);//id ninja
            m.writer().writeUTF(p.c.clan.clanName);//clan name
            if (!p.c.clan.clanName.isEmpty()) {
                m.writer().writeByte(p.c.clan.typeclan);//type clan
            }
            m.writer().writeBoolean(false);//isInvisible
            m.writer().writeByte(p.c.get().typepk);// type pk
            m.writer().writeByte(p.c.get().nclass);// class
            m.writer().writeByte(p.c.gender);// gender
            m.writer().writeShort(p.c.get().partHead());//head
            m.writer().writeUTF(p.c.name);//name
            m.writer().writeInt(p.c.get().hp);//hp
            m.writer().writeInt(p.c.get().getMaxHP());//hp max
            m.writer().writeByte(p.c.get().level);//level
            m.writer().writeShort(p.c.get().Weapon());//vu khi
            m.writer().writeShort(p.c.get().Body());// body
            m.writer().writeShort(p.c.get().Leg());//leg
            m.writer().writeByte(-1);//mob
            m.writer().writeShort(p.c.get().x);// X
            m.writer().writeShort(p.c.get().y);// Y
            m.writer().writeShort(p.c.get().eff5buffHP());//eff5BuffHp
            m.writer().writeShort(p.c.get().eff5buffMP());//eff5BuffMP
            m.writer().writeByte(0);
            m.writer().writeBoolean(p.c.isHuman); // human
            m.writer().writeBoolean(p.c.isNhanban); // nhan ban
            m.writer().writeShort(p.c.get().partHead());
            m.writer().writeShort(p.c.get().Weapon());
            m.writer().writeShort(p.c.get().Body());
            m.writer().writeShort(p.c.get().Leg());
            m.writer().flush();
            p2.conn.sendMessage(m);
            m.cleanup();
            if (p.c.get().mobMe != null) {
                m = new Message(-30);
                m.writer().writeByte(-68);
                m.writer().writeInt(p.c.get().id);
                m.writer().writeByte(p.c.get().mobMe.templates.id);
                m.writer().writeByte(p.c.get().mobMe.isboss ? 1 : 0);
                m.writer().flush();
                p2.conn.sendMessage(m);
                m.cleanup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void FightMob2(Player p, Message m) throws IOException {
        int mobId = m.reader().readByte();
        m.cleanup();
        Mob mob = getMob(mobId);
        if (p.c.get().ItemBody[1] == null || mob == null || mob.isDie) {
            return;
        }
        Skill skill = p.c.get().getSkill(p.c.get().CSkill);
        if (skill == null) {
            return;
        }
        SkillTemplates data = SkillData.Templates(skill.id, skill.point);
        if (skill.coolDown > System.currentTimeMillis() || Math.abs(p.c.get().x - mob.x) > data.dx || Math.abs(p.c.get().y - mob.y) > data.dy || p.c.get().mp < data.manaUse) {
            return;
        }
        p.c.get().upMP(-data.manaUse);
        skill.coolDown = System.currentTimeMillis() + data.coolDown;
        Mob[] arMob = new Mob[10];
        arMob[0] = mob;
        byte n = 1;
        for (Mob mob2 : mobs) {
            if (mob2.isDie || mob.id == mob2.id || Math.abs(mob2.x - mob2.x) > data.dx || Math.abs(mob2.y - mob2.y) > data.dy) {
                continue;
            }
            if (data.maxFight > n) {
                arMob[n] = mob2;
                n++;
            } else {
                break;
            }
        }
        m = new Message(60);
        m.writer().writeInt(p.c.get().id);
        m.writer().writeByte(p.c.get().CSkill);
        for (byte i = 0; i < arMob.length; i++) {
            if (arMob[i] != null) {
                m.writer().writeByte(arMob[i].id);
            }
        }
        m.writer().flush();
        sendMyMessage(p, m);
        m.cleanup();
        long xpup = 0;
        for (byte i = 0; i < arMob.length; i++) {
            if (arMob[i] == null) {
                continue;
            }
            Mob mob3 = arMob[i];
            int dame = util.nextInt(p.c.get().dameMin(), p.c.get().dameMax());
            int oldhp = mob3.hp;
            if (dame <= 0) {
                dame = 1;
            }
            int fatal = p.c.get().Fatal();
            boolean isfatal = fatal > util.nextInt(1, 1000);
            if (isfatal) {
                dame *= 2;
            }
            xpup += mob3.xpup + dame;
            mob3.updateHP(-dame);
            attachedMob((oldhp - mob3.hp), mob3.id, isfatal);

        }
        p.updateExp(xpup);
    }

    public void selectUIZone(Player p, Message m) throws IOException {
        byte zoneid = m.reader().readByte();
        byte index = m.reader().readByte();
        m.cleanup();
        if (zoneid == id) {
            return;
        }
        Item item = null;
        try {
            item = p.c.ItemBag[index];
        } catch (Exception e) {
        }
        boolean isalpha = false;
        for (byte i = 0; i < map.template.npc.length; i++) {
            Npc npc = map.template.npc[i];
            if (npc.id == 13 && Math.abs(npc.x - p.c.get().x) < 50 && Math.abs(npc.y - p.c.get().y) < 50) {
                isalpha = true;
                break;
            }
        }
        if ((item != null && (item.id == 35 || item.id == 37)) || (isalpha)) {
            if (zoneid >= 0 && zoneid < map.area.length) {
                if (map.area[zoneid].numplayers < map.template.maxplayers) {
                    leave(p);
                    map.area[zoneid].Enter(p);
                    p.endLoad(true);
                    if (item != null && item.id != 37) {
                        p.c.removeItemBag(index);
                    }
                } else {
                    p.sendAddchatYellow("Khu vực này đã đầy.");
                    p.endLoad(true);
                }
            }
        }
        m = new Message(57);
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    public void openUIZone(Player p) throws IOException {
        boolean isalpha = false;
        for (byte i = 0; i < map.template.npc.length; i++) {
            Npc npc = map.template.npc[i];
            if (npc.id == 13 && Math.abs(npc.x - p.c.get().x) < 50 && Math.abs(npc.y - p.c.get().y) < 50) {
                isalpha = true;
                break;
            }
        }
        if (p.c.quantityItemyTotal(37) > 0 || p.c.quantityItemyTotal(35) > 0 || isalpha) {
            Message m = new Message(36);
            m.writer().writeByte(map.area.length);//so khu
            for (byte j = 0; j < map.area.length; j++) {
                m.writer().writeByte(map.area[j].numplayers);//map.area[i].numplayers);//so nguoi
                m.writer().writeByte(map.area[j].getArryListParty().size());//grups
            }
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
        } else {
            p.c.get().upDie();
        }
    }

    public void chatNPC(Player p, Short idnpc, String chat) throws IOException {
        Message m = new Message(38);
        m.writer().writeShort(idnpc);//npcid
        m.writer().writeUTF(chat);//chat
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    public void selectMenuNpc(Player p, Message m) throws IOException {
        chatNPC(p, (short) m.reader().readByte(), m.reader().readByte() + "");
    }

    private ItemMap LeaveItem(short id, short x, short y) throws IOException {
        if (itemMap.size() > 100) {
            removeItemMapMessage(itemMap.remove(0).itemMapId);
        }
        Item item;
        ItemData data = ItemData.ItemDataId(id);
        if (data.type < 10) {
            if (data.type == 1) {
                item = ItemData.itemDefault(id);
                item.sys = GameScr.SysClass(data.nclass);
            } else {
                byte sys = (byte) util.nextInt(1, 3);
                item = ItemData.itemDefault(id, sys);
            }
        } else {
            item = ItemData.itemDefault(id);
        }
        ItemMap im = new ItemMap();
        im.itemMapId = getItemMapNotId();
        im.x = x;
        im.y = y;
        im.item = item;
        itemMap.add(im);
        Message m = new Message(6);
        m.writer().writeShort(im.itemMapId);
        m.writer().writeShort(item.id);
        m.writer().writeShort(im.x);
        m.writer().writeShort(im.y);
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
        return im;
    }

    public void PlayerAttack(Mob[] arrmob, Body b, int type) {
        for (int j = 0; j < this.players.size(); j++) {
            Service.PlayerAttack(this.players.get(j), arrmob, b);
        }
        Message m;
        long xpup = 0;
        for (byte i = 0; i < arrmob.length; i++) {
            Mob mob = arrmob[i];
            int dame = util.nextInt(b.dameMin(), b.dameMax());
            if (map.cave == null && mob.isboss && b.level - mob.level > 30) {
                dame = 0;
            }
            int fatal = b.Fatal();
            if (fatal > 800) {
                fatal = 800;
            }
            boolean flag = fatal > util.nextInt(1, 1000);
            if (flag) {
                dame *= 2;
                dame = dame * (100 + b.percentFantalDame()) / 100;
                dame += b.FantalDame();
            }
            if (dame <= 0) {
                dame = 1;
            }
            if (mob.isFire) {
                dame *= 2;
            }
            if (b.c.isNhanban) {
                dame = dame * b.c.clone.percendame / 100;
            }
            if (dame > 0) {
                mob.Fight(b.c.p.conn.id, dame);
                mob.updateHP(-dame);
            }

            int xpnew = dame / 30 * b.level;
            if (b.getEffType((byte) 18) != null) {
                xpnew *= b.getEffType((byte) 18).param;
            }
            if (mob.lvboss == 1) {
                xpnew *= 2;
            } else if (mob.lvboss == 2) {
                xpnew *= 3;
            } else if (mob.lvboss == 3) {
                xpnew /= 2;
            }
            if (map.LangCo()) {
                xpnew = xpnew * 150 / 100;
            } else if (map.VDMQ()) {
                xpnew = xpnew * 150 / 100;
            }
            if (b.level > 99) {
                xpnew /= 4;
            }
            if (map.cave != null || (b.level > 1 && Math.abs(b.level - b.level) <= 10)) {
                xpup += xpnew;
            }
            for (int j = 0; j < this.players.size(); j++) {
                Service.Mobstart(this.players.get(j), mob.id, mob.hp, dame, flag, mob.lvboss, mob.hpmax);
            }
            if (!mob.isDie) {
                if (b.percentFire2() >= util.nextInt(1, 100)) {
                    FireMobMessage(mob.id, 0);
                }
                if (b.percentFire4() >= util.nextInt(1, 100)) {
                    FireMobMessage(mob.id, 1);
                }
                if (b.percentIce1_5() >= util.nextInt(1, 100)) {
                    IceMobMessage(mob.id, 0);
                }
                if (b.percentWind1() >= util.nextInt(1, 100)) {
                    WindMobMessage(mob.id, 0);
                }
                if (b.percentWind2() >= util.nextInt(1, 100)) {
                    WindMobMessage(mob.id, 1);
                }
            }
        }
    }

    public void FightMob(Player p, Message m) throws IOException {
        if (p.c.get().CSkill == -1 && p.c.get().skill.size() > 0) {
            p.c.get().CSkill = p.c.get().skill.get(0).id;
        }
        Skill skill = p.c.get().getSkill(p.c.get().CSkill);
        if (skill == null) {
            return;
        }
        int mobId = m.reader().readUnsignedByte();
        synchronized (this) {
            Mob mob = getMob(mobId);
            Mob[] arMob = new Mob[10];
            arMob[0] = mob;
            if (mob == null || mob.isDie) {
                return;
            }
            if (p.c.get().ItemBody[1] == null) {
                p.sendAddchatYellow("Vũ khí không thích hợp");
                return;
            }
            p.removeEffect(15);
            p.removeEffect(16);
            SkillTemplates data = SkillData.Templates(skill.id, skill.point);
            if (p.c.get().mp < data.manaUse) {
                p.getMp();
                return;
            }
            if (skill.coolDown > System.currentTimeMillis() || Math.abs(p.c.get().x - mob.x) > 150 || Math.abs(p.c.get().y - mob.y) > 150) {
                return;
            }
            skill.coolDown = System.currentTimeMillis() + data.coolDown;
            p.c.mobAtk = mob.id;
            p.c.get().upMP(-data.manaUse);
            if (skill.id == 42) {
                p.c.get().x = mob.x;
                p.c.get().y = mob.y;
                this.sendXYPlayer(p);
            }
            if (skill.id == 40) {
                this.DisableMobMessage(p, mob.id, 0);
            }
            if (skill.id == 24) {
                this.DontMoveMobMessage(p, mob.id, 0);
            }
            int size = m.reader().available();
            byte n = 1;
            for (int i = 0; i < size; i++) {
                Mob mob2 = getMob(m.reader().readUnsignedByte());
                if (mob2.isDie || mob.id == mob2.id)// || Math.abs(mob.x - mob2.x) > data.dx || Math.abs(mob.y - mob2.y) > data.dy)
                {
                    continue;
                }
                if (data.maxFight > n) {
                    arMob[n] = mob2;
                    n++;
                } else {
                    break;
                }
            }
            m.cleanup();
            for (int j = 0; j < this.players.size(); j++) {
                Service.PlayerAttack(this.players.get(j), arMob, p.c.get());

                if (p.c.isHuman && !p.c.clone.isDie) {
                    Service.PlayerAttack(this.players.get(j), arMob, p.c.clone);
                    if (p.c.mobAtk != -1) {
                        for (byte k = 0; k < arMob.length; ++k) {
                            if (arMob[k] != null) {
                                int dame = util.nextInt(p.c.clone.dameMin(), p.c.clone.dameMax()) * p.c.clone.percendame / 100;
                                arMob[k].updateHP(-dame);
                                this.attachedMob(dame, arMob[k].id, false);
                            }
                        }
                    }
                }
            }
            long xpup = 0;
            for (byte i = 0; i < arMob.length; i++) {
                if (arMob[i] == null) {
                    continue;
                }
                Mob mob3 = arMob[i];
                int dame = util.nextInt(p.c.get().dameMin(), p.c.get().dameMax());
                if (map.cave == null && mob3.isboss && p.c.get().level - mob3.level > 30) {
                    dame = 0;
                }
                int oldhp = mob3.hp;
                int fatal = p.c.get().Fatal();
                if (fatal > 800) {
                    fatal = 800;
                }
                boolean isfatal = fatal > util.nextInt(1, 1000);
                if (isfatal) {
                    dame *= 2;
                    dame = dame * (100 + p.c.get().percentFantalDame()) / 100;
                    dame += p.c.get().FantalDame();
                }
                if (dame <= 0) {
                    dame = 1;
                }
                if (mob3.isFire) {
                    dame *= 2;
                }
                if (p.c.isNhanban) {
                    dame = dame * p.c.clone.percendame / 100;
                }
                int xpnew = dame / 15 * p.c.get().level;
                if (p.c.get().getEffType((byte) 18) != null) {
                    xpnew *= p.c.get().getEffType((byte) 18).param;
                }
                if (mob3.lvboss == 1) {
                    xpnew *= 2;
                } else if (mob3.lvboss == 2) {
                    xpnew *= 3;
                } else if (mob3.lvboss == 3) {
                    xpnew /= 2;
                }
                if (map.LangCo()) {
                    xpnew = xpnew * 150 / 100;
                } else if (map.VDMQ()) {
                    xpnew = xpnew * 150 / 100;
                }
                if (p.c.get().level > 99) {
                    xpnew /= 7;
                }
                if (map.cave != null || (mob3.level > 1 && Math.abs(mob3.level - p.c.get().level) <= 10)) {
                    xpup += xpnew;
                }
                mob3.updateHP(-dame);
                if (dame > 0) {
                    mob3.Fight(p.conn.id, dame);
                }
                if (!mob3.isFire) {
                    if (p.c.get().percentFire2() >= util.nextInt(1, 100)) {
                        FireMobMessage(mob3.id, 0);
                    }
                    if (p.c.get().percentFire4() >= util.nextInt(1, 100)) {
                        FireMobMessage(mob3.id, 1);
                    }
                }
                if (!mob3.isIce) {
                    if (p.c.get().percentIce1_5() >= util.nextInt(1, 100)) {
                        IceMobMessage(mob3.id, 0);
                    }
                }
                if (!mob3.isWind) {
                    if (p.c.get().percentWind1() >= util.nextInt(1, 100)) {
                        WindMobMessage(mob3.id, 0);
                    }
                    if (p.c.get().percentWind2() >= util.nextInt(1, 100)) {
                        WindMobMessage(mob3.id, 1);
                    }
                }
                if (mob3.isDie) {
                    MobStartDie((oldhp - mob3.hp), mob3.id, isfatal);
                } else {
                    attachedMob((oldhp - mob3.hp), mob3.id, isfatal);
                }
//                if (mob3.isDie && (mob3.templates.id == 98 || mob3.templates.id == 99) && map.id >= 98 && map.id <= 104){
//                        server.manager.chatKTG("Long trụ bị " + p.c.name + " đánh sập, mang về lợi thế cho đồng minh");
//                        p.c.pointCT = p.c.pointCT + 250;
//                        map.war.updatePoint(p.c,250);
//                        if (p.c.typeCT == 4){
//                            map.war.pointWhite += 250;
//                        }else {
//                            map.war.pointBlack += 250;
//                        }
//                    }
                if (mob3.isDie && mob3.level > 1) {
                    this.numMobDie++;
                    if (map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) {
                        if (mob3.level == 49) {
                            p.upExpClan(100);
                        } else {
                            p.upExpClan(util.nextInt(5, 10));
                        }
                    }
                    if (map.cave != null) {
                        map.cave.updatePoint(1);
                    }
                    if (this.map.war != null && map.id >= 98 && map.id <= 104) {
                            if(mob3.isboss) {
                                map.war.updatePoint(p.c,300);
                                if (p.c.typeCT == 4){
                                    map.war.pointWhite += 300;
                                }else {
                                    map.war.pointBlack += 300;
                                }
                            } else if(mob3.lvboss == 2) {
                                map.war.updatePoint(p.c,50);
                                if (p.c.typeCT == 4){
                                    map.war.pointWhite += 50;
                                }else {
                                    map.war.pointBlack += 50;
                                }
                            }else if(mob3.lvboss == 1) {
                                map.war.updatePoint(p.c,10);
                                if (p.c.typeCT == 4){
                                    map.war.pointWhite += 10;
                                }else {
                                    map.war.pointBlack += 10;
                                }
                            } else {
                                map.war.updatePoint(p.c,1);
                                if (p.c.typeCT == 4){
                                    map.war.pointWhite += 1;
                                }else {
                                    map.war.pointBlack += 1;
                                }
                            }
                        }
                    int master = mob3.sortNinjaFight();
                    if (mob3.lvboss == 1) {
                        numTA--;
                        p.c.upyenMessage(100000);
                    } else if (mob3.lvboss == 2) {
                        numTL--;
                        p.c.upyenMessage(500000);
                    }
                    ItemMap im;
                    short[] arid;
                    short[] aridsk = null;
                    if (map.LangCo()) {
                        int a = util.nextInt(500);
                        if (a == 11) {
                            p.upluongMessage(1);
                        }
                        arid = new short[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 12, 12, 12, 12, 778, 778, -1, -1, -1, -1, -1, -1, 778, -1, -1, -1, -1, 778, -1, -1, -1, 12, 12, 12, 12, 12, 12, -1, -1, 778, -1, -1, -1, 455, -1, -1, -1, -1, -1, -1, 778, -1, -1, -1, -1, -1, 456, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 454, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 439, 440, 441, 442, 486, 487, 488, 489, 573, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 574, 575, 576, 577, 578};
                    } else if (this.map.VDMQ()) {
                        int a = util.nextInt(500);
                        if (a == 11) {
                            p.upluongMessage(1);
                        }
                        arid = new short[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 12, 12, 12, 12, 12, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 778, -1, -1, -1, 12, 12, 12, 12, 12, 12, 12, 573, -1, -1, -1, 778, -1, -1, -1, -1, 574, -1, -1, -1, 575, -1, -1, -1, 778, -1, -1, 576, -1, -1, 577, -1, -1, 578, -1, -1, 439, -1, -1, 440, -1, -1, 441, -1, -1, -1, -1, -1, 442, -1, -1, -1, -1, -1, -1, 486, -1, -1, 487, -1, -1, 778, -1, -1, -1, -1, -1, -1, 488, -1, -1, -1, -1, -1, -1, 489, -1, -1, -1, -1, -1, -1, 455, -1, -1, -1, -1, -1, -1, 456, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 455
                        };
                    } else {
                        arid = new short[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 7, 12, 12, 12, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 12, 12};
                    }
                    int per = 100;
                    switch (server.manager.event) {
                        case 1:
                            per = 5;
                            aridsk = new short[]{607, 608, 609, 610, 617};
                            break;
                        case 2:
                            per = 3;
                            aridsk = new short[]{292, 293, 294, 295, 296, 297};
                            break;
                    }
                    if (aridsk != null && util.nextInt(per) == 0) {
                        arid = aridsk;
                    }
                    if (map.VDMQ() && p.c.get().level >= 100 && util.nextInt(100) <= 15) {
                        if (mob3.lvboss == 1 || mob3.lvboss == 2) {
                            arid = new short[]{545};
                        }
                    }
                    int lent = util.nextInt(arid.length);
                    if ((map.LangCo() || Math.abs(mob3.level - p.c.get().level) <= 10) && arid[lent] != -1) {
                        im = LeaveItem(arid[lent], mob3.x, mob3.y);
                        int quantity = 1;
                        if (im.item.id == 12) {
                            quantity = util.nextInt(10000, 30000);
                        }
                        if (im.item.id == 455 || im.item.id == 456) {
                            im.item.isExpires = true;
                            im.item.expires = util.TimeDay(7);
                        } else if (im.item.id == 545) {
                            im.item.isExpires = true;
                            im.item.expires = util.TimeDay(1);
                        }
                        im.item.quantity = quantity;
                        im.master = master;
                    }
                    if (mob3.isboss) {
                        if (map.cave == null) {
                            Manager.chatKTG(p.c.name + " đã tiêu diệt " + mob3.templates.name);
                        }
                        if (map.VDMQ()) {
                            im = LeaveItem((short) 547, (short) util.nextInt(mob3.x - 30, mob3.x + 30), mob3.y);
                            im.master = master;
                        }
                        int l = mob3.templates.arrIdItem.length;
                        if (l > 1) {
                            for (int j = 0; j < mob3.templates.arrIdItem[0]; j++) {
                                lent = util.nextInt(1, l - 1);
                                short idi = mob3.templates.arrIdItem[lent];
                                if (idi == -1) {
                                    continue;
                                }
                                im = LeaveItem(idi, (short) util.nextInt(mob3.x - 30, mob3.x + 30), mob3.y);
                                if (im.item.id == 12) {
                                    im.item.quantity = util.nextInt(10000, 30000);
                                }
                                im.master = master;
                            }
                        }
                        if (this.map.cave != null) {
                                this.map.cave.updatePoint(this.mobs.size());
                                for (short k2 = 0; k2 < this.mobs.size(); ++k2) {
                                    this.mobs.get(k2).updateHP(-this.mobs.get(k2).hpmax);
                                    this.mobs.get(k2).isRefresh = false;
                                    for (short h = 0; h < this.players.size(); ++h) {
                                        Service.setHPMob(this.players.get(h).c, this.mobs.get(k2).id, 0);
                                    }
                                }
                                final Cave cave = this.map.cave;
                                ++cave.level;
                            }
                    }
                    if (this.map.cave != null && this.map.getXHD() < 9) {
                            mob3.isRefresh = false;
                            if (this.mobs.size() == this.numMobDie) {
                                if (this.map.getXHD() == 5) {
                                    if (this.map.id == 105) {
                                        this.map.cave.openMap();
                                        this.map.cave.openMap();
                                        this.map.cave.openMap();
                                    }
                                    else if (this.map.id == 106 || this.map.id == 107 || this.map.id == 108) {
                                        final Cave cave2 = this.map.cave;
                                        ++cave2.finsh;
                                        if (this.map.cave.finsh >= 3) {
                                            this.map.cave.openMap();
                                        }
                                    }
                                    else {
                                        this.map.cave.openMap();
                                    }
                                }
                                else if (this.map.getXHD() == 6 && this.map.id == 116) {
                                    if (this.map.cave.finsh == 0) {
                                        this.map.cave.openMap();
                                    }
                                    else {
                                        final Cave cave3 = this.map.cave;
                                        ++cave3.finsh;
                                    }
                                    this.numMobDie = 0;
                                    for (short l2 = 0; l2 < this.mobs.size(); ++l2) {
                                        this.refreshMob(l2);
                                    }
                                }
                                else {
                                    this.map.cave.openMap();
                                }
                            }
                        }

                } else {
                }
            }
            if (xpup > 0) {
                if (map.cave != null) {
                    map.cave.updateXP(xpup);
                } else {
                    if (p.c.isNhanban) {
                        xpup /= 4;
                    }
                    p.updateExp(xpup);
                    xpup /= 5;
                    if (p.c.get().party != null) {
                        for (int i = 0; i < players.size(); i++) {
                            Player p2 = players.get(i);
                            if (p2.c.id != p.c.id) {
                                if (p2.c.party == p.c.party && Math.abs(p2.c.level - p.c.level) <= 10) {
                                    p2.updateExp(xpup / 15 * 100);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void FightNinja(Player p, Message m) throws IOException {

        if ((p.c.typeSolo != 1 && (map.id == 22 || map.id == 17 || map.id == 10 || map.id == 27 || map.id == 32 || map.id == 38 || map.id == 43 || map.id == 48 || map.id == 72 || map.id == 138 || map.id == 1)) || p.c.getEffId(6) != null || p.c.getEffId(7) != null || p.c.getEffId(14) != null) {
            return;
        }
        int idP = m.reader().readInt();
        m.cleanup();
        Char c = getNinja(idP);
        if ((p.c.typepk == 4 && c.typepk == 5 || (p.c.typepk == 5 && c.typepk == 4)) && ((p.c.clan.clanName != "") && (c.clan.clanName != "") && (p.c.clan.clanName.equals(c.clan.clanName)))) {
            return;
        } else if (p.c.ItemBody[1] != null && c != null && ((p.c.typepk == 1 && c.typepk == 1) || p.c.typepk == 3 || c.typepk == 3 || (p.c.typeSolo == 1 && c.typeSolo == 1 || (p.c.typepk == 4 && c.typepk == 5 || (p.c.typepk == 5 && c.typepk == 4))))) {
            if (p.c.CSkill == -1 && p.c.skill.size() > 0) {
                p.c.CSkill = p.c.skill.get(0).id;
            }
            Skill skill = p.c.getSkill(p.c.CSkill);
            if (skill == null || c.isDie) {
                return;
            }
            final Char[] arNinja = new Char[10];
            arNinja[0] = c;
            SkillTemplates data = SkillData.Templates(skill.id, skill.point);
            if (skill.coolDown > System.currentTimeMillis() || Math.abs(p.c.x - c.x) > data.dx || Math.abs(p.c.y - c.y) > data.dy || p.c.mp < data.manaUse) {
                return;
            }
            p.c.upMP(-data.manaUse);
            if (skill.id == 42) {
                    this.setXYPlayers(c.get().x, c.get().y, p, c.p);
                    c.p.setEffect(18, 0, 5000, 0);
                }

            skill.coolDown = System.currentTimeMillis() + data.coolDown;
            ArrayList<Char> spread = new ArrayList<>();
            spread.add(c);
            byte n = 1;
            try {
                    while (true) {
                        final int idn = m.reader().readInt();
                        final Char nj2 = this.getNinja(idn);
                        if (nj2 != null && !nj2.isDie && nj2.getEffId(15) == null && c.get().id != p.c.get().id && nj2.id != p.c.get().id && Math.abs(c.get().x - nj2.x) <= data.dx) {
                            if (Math.abs(c.get().y - nj2.y) > data.dy) {
                                continue;
                            }
                            if (data.maxFight <= n) {
                                break;
                            }
                            if (nj2.typepk == 3 || p.c.get().typepk == 3 || (p.c.get().typepk == 1 && nj2.typepk == 1) || (p.c.get().typepk == 4 && nj2.typepk == 4) || (p.c.get().typepk == 5 && nj2.typepk == 5) || (p.c.typeSolo == 1 && c.typeSolo == 1)) {
                                arNinja[n] = nj2;
                            }
                            ++n;
                        }
                    }
                }
            catch (IOException ex) {}
            m = new Message(61);
            m.writer().writeInt(p.c.id);
            m.writer().writeByte(skill.id);
            for (byte i = 0; i < spread.size(); i++) {
                m.writer().writeInt(spread.get(i).id);
            }
            m.writer().flush();
            sendMyMessage(p, m);
            m.cleanup();

            for (Char c2 : spread) {
                if (c2.id != p.c.id) {
                    int dame = util.nextInt(p.c.dameMin(), p.c.dameMax()) / 7;
                    int oldhp = c2.hp;
                    dame -= c2.dameDown();// Giam sat thuong
                    if (dame <= 0) {
                        dame = 1;
                    }
                    int fatal = p.c.get().Fatal();
                        if (fatal > 800) {
                            fatal = 800;
                        }
                        final boolean isfatal = fatal > util.nextInt(1, 1000);
                        if (isfatal || c2.getEffId(5) != null) {
                            dame *= 2;
                            dame = dame * (100 + p.c.get().percentFantalDame()) / 100;
                            dame += p.c.get().FantalDame();
                        }
                    int miss = c2.Miss() - p.c.Exactly();
                    if (util.nextInt(25000) < miss) {
                        dame = 0;
                    }
                    c2.upHP(-dame);

                    if (isfatal || c2.getEffId(5) != null) {
                            this.attached(c2.hp - oldhp, c2.id);
                        }
                        else {
                            this.attached(oldhp - c2.hp, c2.id);
                        }
                    if (c2.getPramSkill(69) > util.nextInt(0,100)) {
                            p.setEffect(6, 0, c2.getPramSkill(70), 0);
                        }
                    switch (skill.id) {
                            case 9:
                            case 61:
                            case 73:
                            case 79:
                            case 18:
                            case 62:
                            case 78:
                            case 83:
                                if (p.c.percentFire2() > util.nextInt(0,100)) {
                                    c2.p.setEffect(5, 0, 2000, 0);
                                    Service.PlayerAddEfect(p, c2, c2.getEffId(5));            
                                }
                                break;
                            case 7:
                            case 16:
                                if (p.c.percentFire4() > util.nextInt(0,100)) {
                                    c2.p.setEffect(5, 0, 4000, 0);
                                    Service.PlayerAddEfect(p, c2, c2.getEffId(5));            
                                }
                                break;
                            case 27:
                            case 63:
                            case 75: 
                            case 81:
                            case 36:
                            case 64:
                            case 76:
                            case 82:
                                if (p.c.percentIce1_5() > util.nextInt(0,100)) {
                                    c2.p.setEffect(6, 0, 1500, 0);
                                    Service.PlayerAddEfect(p, c2, c2.getEffId(6));   
                                }
                                break;
                            case 45:
                            case 65:
                            case 74:
                            case 80:
                            case 54:
                            case 66:
                            case 77:
                            case 84:
                                if (p.c.percentWind1() > 0) {
                                    c2.p.setEffect(7, 0, 1000, 0);
                                    Service.PlayerAddEfect(p, c2, c2.getEffId(7));   
                                }
                                break;
                            case 43:
                                if (p.c.percentWind2() > util.nextInt(0,100)) {
                                    c2.p.setEffect(7, 0, 2000, 0);
                                    Service.PlayerAddEfect(p, c2, c2.getEffId(7));   
                                }
                                break;
                            case 42:
                                c2.p.setEffect(18, 0, 5000, 0);
                                Service.PlayerAddEfect(p, c2, c2.getEffId(18));
                                break;
                            case 24:
                                c2.p.setEffect(18, 0, 1000*p.c.getPramSkill(55), 0);
                                Service.PlayerAddEfect(p, c2, c2.getEffId(18));
                                break;
                        }

                    if (c2.isDie) {
                        if ((c2.typepk == 4 || c2.typepk == 5) && (map.id == 111)){
                                p.sendAddchatYellow("Phe " + p.c.name + " đã giành chiến thắng nhận được " + p.c.xuTestDun + " xu.");
                                c2.p.sendAddchatYellow("Phe " + p.c.name + " đã giành chiến thắng nhận được " + p.c.xuTestDun + " xu.");
                                p.c.upxuMessage((long)p.c.xuTestDun);
                                c2.upxuMessage((long)-p.c.xuTestDun);
                                p.c.timeTestDun = System.currentTimeMillis() + 10000L;
                                c2.timeTestDun = System.currentTimeMillis() + 10000L;
                                p.setTimeMap((int) ((p.c.timeTestDun - System.currentTimeMillis())/1000));
                                c2.p.setTimeMap((int) ((c2.timeTestDun - System.currentTimeMillis())/1000));
                            }
                        if ((c2.typeCT == 4 || c2.typeCT == 5) && (map.id >= 98 && map.id <= 104)){
                                p.sendAddchatYellow("Bạn đánh trọng thương " + c2.name);
                                c2.p.sendAddchatYellow("Bạn bị " + p.c.name + " đánh trọng thương");
                                map.war.updatePoint(p.c,10);
                                if (p.c.typeCT == 4) {
                                    map.war.pointWhite = map.war.pointWhite + 10;
                                    System.out.println(map.war.pointWhite);
                                } else if (p.c.typeCT == 5){
                                    map.war.pointBlack = map.war.pointBlack + 10;
                                    System.out.println(map.war.pointBlack);
                                }
                            }
//                        if (map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) {
//                            p.upluongMessage(util.nextInt(5));
//                            p.c.upxuMessage(util.nextInt(15000));
//                            c2.upxuMessage(util.nextInt(-80000, -40000));
//                        }
                        c2.p.closeTrade();
                        c2.type = 14;
                        waitDie(c2.p);
                        myDie(c2.p);
                    }
                }

            }
            if (p.c.isHuman && !p.c.clone.isDie) {
                m = new Message(61);
                    m.writer().writeInt(p.c.clone.id);
                    m.writer().writeByte(p.c.clone.CSkill);
                    for (byte i = 0; i < arNinja.length; ++i) {
                        final Char nj3 = arNinja[i];
                        if (nj3 != null) {
                            m.writer().writeInt(nj3.id);
                        }
                    }
                    m.writer().flush();
                    p.conn.sendMessage(m);
                    this.sendMyMessage(p, m);
                    m.cleanup();
                    for (Char c2 : spread) {
                        if (c2.id != p.c.id) {
                            int dame = util.nextInt(p.c.dameMin(), p.c.dameMax()) / 7;
                            int oldhp = c2.hp;
                            dame -= c2.dameDown();// Giam sat thuong
                            if (dame <= 0) {
                                dame = 1;
                            }
                            int fatal = p.c.get().Fatal();
                                if (fatal > 800) {
                                    fatal = 800;
                                }
                                final boolean isfatal = fatal > util.nextInt(1, 1000);
                                if (isfatal || c2.getEffId(5) != null) {
                                    dame *= 2;
                                    dame = dame * (100 + p.c.get().percentFantalDame()) / 100;
                                    dame += p.c.get().FantalDame();
                                }
                            int miss = c2.Miss() - p.c.Exactly();
                            if (util.nextInt(25000) < miss) {
                                dame = 0;
                            }
                            c2.upHP(-dame);

                            if (isfatal || c2.getEffId(5) != null) {
                                    this.attached(c2.hp - oldhp, c2.id);
                                }
                                else {
                                    this.attached(oldhp - c2.hp, c2.id);
                                }
                            if (c2.getPramSkill(69) > util.nextInt(0,100)) {
                                    p.setEffect(6, 0, c2.getPramSkill(70), 0);
                                }
                            switch (skill.id) {
                                    case 9:
                                    case 61:
                                    case 73:
                                    case 79:
                                    case 18:
                                    case 62:
                                    case 78:
                                    case 83:
                                        if (p.c.percentFire2() > util.nextInt(0,100)) {
                                            c2.p.setEffect(5, 0, 2000, 0);
                                            Service.PlayerAddEfect(p, c2, c2.getEffId(5));            
                                        }
                                        break;
                                    case 7:
                                    case 16:
                                        if (p.c.percentFire4() > util.nextInt(0,100)) {
                                            c2.p.setEffect(5, 0, 4000, 0);
                                            Service.PlayerAddEfect(p, c2, c2.getEffId(5));            
                                        }
                                        break;
                                    case 27:
                                    case 63:
                                    case 75: 
                                    case 81:
                                    case 36:
                                    case 64:
                                    case 76:
                                    case 82:
                                        if (p.c.percentIce1_5() > util.nextInt(0,100)) {
                                            c2.p.setEffect(6, 0, 1500, 0);
                                            Service.PlayerAddEfect(p, c2, c2.getEffId(6));   
                                        }
                                        break;
                                    case 45:
                                    case 65:
                                    case 74:
                                    case 80:
                                    case 54:
                                    case 66:
                                    case 77:
                                    case 84:
                                        if (p.c.percentWind1() > 0) {
                                            c2.p.setEffect(7, 0, 1000, 0);
                                            Service.PlayerAddEfect(p, c2, c2.getEffId(7));   
                                        }
                                        break;
                                    case 43:
                                        if (p.c.percentWind2() > util.nextInt(0,100)) {
                                            c2.p.setEffect(7, 0, 2000, 0);
                                            Service.PlayerAddEfect(p, c2, c2.getEffId(7));   
                                        }
                                        break;
                                    case 42:
                                        c2.p.setEffect(18, 0, 5000, 0);
                                        Service.PlayerAddEfect(p, c2, c2.getEffId(18));
                                        break;
                                    case 24:
                                        c2.p.setEffect(18, 0, 1000*p.c.getPramSkill(55), 0);
                                        Service.PlayerAddEfect(p, c2, c2.getEffId(18));
                                        break;
                                }

                            if (c2.isDie) {
                                if ((c2.typepk == 4 || c2.typepk == 5) && (map.id == 111)){
                                        p.sendAddchatYellow("Phe " + p.c.name + " đã giành chiến thắng nhận được " + p.c.xuTestDun + " xu.");
                                        c2.p.sendAddchatYellow("Phe " + p.c.name + " đã giành chiến thắng nhận được " + p.c.xuTestDun + " xu.");
                                        p.c.upxuMessage((long)p.c.xuTestDun);
                                        c2.upxuMessage((long)-p.c.xuTestDun);
                                        p.c.timeTestDun = System.currentTimeMillis() + 10000L;
                                        c2.timeTestDun = System.currentTimeMillis() + 10000L;
                                        p.setTimeMap((int) ((p.c.timeTestDun - System.currentTimeMillis())/1000));
                                        c2.p.setTimeMap((int) ((c2.timeTestDun - System.currentTimeMillis())/1000));
                                    }
                                if ((c2.typeCT == 4 || c2.typeCT == 5) && (map.id >= 98 && map.id <= 104)){
                                        p.sendAddchatYellow("Bạn đánh trọng thương " + c2.name);
                                        c2.p.sendAddchatYellow("Bạn bị " + p.c.name + " đánh trọng thương");
                                        map.war.updatePoint(p.c,10);
                                        if (p.c.typeCT == 4) {
                                            map.war.pointWhite = map.war.pointWhite + 10;
                                            System.out.println(map.war.pointWhite);
                                        } else if (p.c.typeCT == 5){
                                            map.war.pointBlack = map.war.pointBlack + 10;
                                            System.out.println(map.war.pointBlack);
                                        }
                                    }
        //                        if (map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) {
        //                            p.upluongMessage(util.nextInt(5));
        //                            p.c.upxuMessage(util.nextInt(15000));
        //                            c2.upxuMessage(util.nextInt(-80000, -40000));
        //                        }
                                c2.p.closeTrade();
                                c2.type = 14;
                                waitDie(c2.p);
                                myDie(c2.p);
                            }
                        }

                    }
            }
        }
    }

    private void myDie(Player p) throws IOException {
        if (p.c.exp > Level.getMaxExp(p.c.level - 1)) {
            Message m = new Message(-11);
            m.writer().writeByte(p.c.typepk);
            m.writer().writeShort(p.c.x);
            m.writer().writeShort(p.c.y);
            m.writer().writeLong(p.c.exp);
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
        } else {
            p.c.exp = Level.getMaxExp(p.c.level - 1);
            Message m = new Message(72);
            m.writer().writeByte(p.c.typepk);
            m.writer().writeShort(p.c.x);
            m.writer().writeShort(p.c.y);
            m.writer().writeLong(p.c.expdown);
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
        }
    }

    private void waitDie(Player p) throws IOException {
        Message m = new Message(0);
        m.writer().writeInt(p.c.id);
        m.writer().writeByte(p.c.typepk);
        m.writer().writeShort(p.c.x);
        m.writer().writeShort(p.c.y);
        m.writer().flush();
        sendMyMessage(p, m);
        m.cleanup();
    }

    public void wakeUpDieReturn(Player p) throws IOException, InterruptedException {
        if (!p.c.isDie || map.LangCo()) {
            return;
        }
        if (p.luong < 5) {
            p.conn.sendMessageLog("Bạn không có đủ 5 lượng!");
            return;
        }
        p.sendAddchatYellow("Vui lòng đợi");
        Thread.sleep(5000);
        p.c.get().isDie = false;
        p.luongMessage(-5);
        p.c.get().hp = p.c.get().getMaxHP();
        p.c.get().mp = p.c.get().getMaxMP();
        p.liveFromDead();
    }

    public void sendDie(Char c) throws IOException {
        if (c.get().exp > Level.getMaxExp(c.get().level)) {
            Message m = new Message(-11);
            m.writer().writeByte(c.get().pk);
            m.writer().writeShort(c.get().x);
            m.writer().writeShort(c.get().y);
            m.writer().writeLong(c.get().exp);
            m.writer().flush();
            c.p.conn.sendMessage(m);
            m.cleanup();
        } else {
            c.get().exp = Level.getMaxExp(c.get().level);
            Message m = new Message(72);
            m.writer().writeByte(c.get().pk);
            m.writer().writeShort(c.get().x);
            m.writer().writeShort(c.get().y);
            m.writer().writeLong(c.get().expdown);
            m.writer().flush();
            c.p.conn.sendMessage(m);
            m.cleanup();
        }
        Message m = new Message(0);
        m.writer().writeInt(c.get().id);
        m.writer().writeByte(c.get().pk);
        m.writer().writeShort(c.get().x);
        m.writer().writeShort(c.get().y);
        m.writer().flush();
        sendMyMessage(c.p, m);
        m.cleanup();
    }

    public void DieReturn(Player p) throws IOException {
        leave(p);
        p.c.get().isDie = false;
        Map ma;
        if (map.cave != null) {
            ma = map.cave.map[0];
        } else {
            ma = Manager.getMapid(p.c.mapLTD);
        }
        if ((map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) && p.c.typepk == 4) {
            ma = Manager.getMapid(98);
        }
        if ((map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) && p.c.typepk == 5) {
            ma = Manager.getMapid(104);
        }
        for (Place area : ma.area) {
            if (area.numplayers < ma.template.maxplayers) {
                area.EnterMap0(p.c);
                p.c.get().hp = p.c.get().getMaxHP();
                p.c.get().mp = p.c.get().getMaxMP();
                Message m = new Message(-30);
                m.writer().writeByte(-123);
                m.writer().writeInt(p.c.xu);
                m.writer().writeInt(p.c.yen);
                m.writer().writeInt(p.luong);
                m.writer().writeInt(p.c.get().getMaxHP());
                m.writer().writeInt(p.c.get().getMaxMP());
                m.writer().writeByte(0);
                m.writer().flush();
                p.conn.sendMessage(m);
                m.cleanup();
                m = new Message(57);
                m.writer().flush();
                p.conn.sendMessage(m);
                m.cleanup();
                return;
            }
        }
    }

    private void attached(int dame, int nid) throws IOException {
        Char n = getNinja(nid);
        Message m = new Message(62);
        m.writer().writeInt(nid);
        m.writer().writeInt(n.hp);//hp
        m.writer().writeInt(dame);//dame
        m.writer().writeInt(n.mp);// mp
        m.writer().writeInt(0);// dame2
        m.writer().flush();
        sendMessage(m);
        m.cleanup();
    }

    private void FireMobMessage(int mobid, int type) {
        try {
            Mob mob = getMob(mobid);
            switch (type) {
                case -1:
                    mob.isFire = false;
                    break;
                case 0:
                    mob.isFire = true;
                    mob.timeFire = System.currentTimeMillis() + 1000L;
                    break;
                case 1:
                    mob.isFire = true;
                    mob.timeFire = System.currentTimeMillis() + 1500L;
                    break;
                default:
                    break;
            }
            Message m = new Message(89);
            m.writer().writeByte(mobid);
            m.writer().writeBoolean(mob.isFire);
            m.writer().flush();
            sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void IceMobMessage(int mobid, int type) {
        try {
            Mob mob = getMob(mobid);
            switch (type) {
                case -1:
                    mob.isIce = false;
                    break;
                case 0:
                    mob.isIce = true;
                    mob.timeIce = System.currentTimeMillis() + 1500L;
                    break;
                case 1:
                    mob.isIce = true;
                    mob.timeIce = System.currentTimeMillis() + 3000L;
                    break;
                default:
                    break;
            }
            Message m = new Message(90);
            m.writer().writeByte(mobid);
            m.writer().writeBoolean(mob.isIce);
            m.writer().flush();
            sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void WindMobMessage(int mobid, int type) {
        try {
            Mob mob = getMob(mobid);
            switch (type) {
                case -1:
                    mob.isWind = false;
                    break;
                case 0:
                    mob.isWind = true;
                    mob.timeWind = System.currentTimeMillis() + 1000L;
                    break;
                case 1:
                    mob.isWind = true;
                    mob.timeWind = System.currentTimeMillis() + 1500L;
                    break;
                default:
                    break;
            }
            Message m = new Message(91);
            m.writer().writeByte(mobid);
            m.writer().writeBoolean(mob.isWind);
            m.writer().flush();
            sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMobAttached(int mobid) {
        synchronized (this) {
            try {
                Mob mob = getMob(mobid);
                if (mob.isIce || mob.isWind || mob.isDisable) {
                    return;
                }
                long tFight = System.currentTimeMillis() + 1200L;
                if (mob.isboss) {
                    tFight = System.currentTimeMillis() + 400L;
                }
                mob.timeFight = tFight;
                for (short i = 0; i < players.size(); i++) {
                    Player player = players.get(i);
                    if (player.c.get().isDie || player.c.get().getEffId(15) != null || player.c.get().getEffId(16) != null) {
                        continue;
                    }
                    short dx = 80;
                    short dy = 2;
                    if (mob.templates.type > 3) {
                        dy = 80;
                    }
                    if (mob.isboss) {
                        dx = 150;
                    }
                    if (mob.isFight(player.conn.id)) {
                        dx = 200;
                        dy = 160;
                    }
                    if (Math.abs(player.c.get().x - mob.x) < dx && Math.abs(player.c.get().y - mob.y) < dy) {
                        int dame = mob.level * mob.level / 4;
                        if (map.cave != null && map.cave.finsh > 0 && map.getXHD() == 6) {
                            int dup = dame * ((10 * map.cave.finsh) + 100) / 100;
                            dame = dup;
                        }
                        if (mob.lvboss == 1) {
                            dame *= 2;
                        } else if (mob.lvboss == 2) {
                            dame *= 3;
                        } else if (mob.lvboss == 3) {
                            dame *= 4;
                        }
                        if (mob.isboss) {
                            dame *= 8;
                        }
                        if (mob.sys == 1) {
                            dame -= player.c.get().ResFire();
                        } else if (mob.sys == 2) {
                            dame -= player.c.get().ResIce();
                        } else if (mob.sys == 3) {
                            dame -= player.c.get().ResWind();
                        }
                        dame -= player.c.get().dameDown();
                        dame = util.nextInt((dame * 90 / 100), dame);
                        if (dame <= 0) {
                            dame = 1;
                        }
                        int miss;
                        if (mob.isboss) {
                            miss = player.c.get().Miss() / 3;
                        } else {
                            miss = player.c.get().Miss();
                        }
                        if (miss > util.nextInt(10000)) {
                            dame = 0;
                        }
                        int mpdown = 0;
                        if (player.c.get().hp * 100 / player.c.get().getMaxHP() > 10) {
                            Effect eff = player.c.get().getEffId(10);
                            if (eff != null) {
                                int mpold = player.c.get().mp;
                                player.c.get().upMP(-(dame * eff.param / 100));
                                dame -= mpdown = (mpold - player.c.get().mp);
                            }
                        }
                        player.c.get().upHP(-dame);
                        MobAtkMessage(mob.id, player.c, dame, mpdown, (short) -1, (byte) -1, (byte) -1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void MobAtkMessage(int mobid, Char n, int dame, int mpdown, short idskill_atk, byte typeatk, byte typetool) throws IOException {
        Message m = new Message(-3);
        m.writer().writeByte(mobid);
        m.writer().writeInt(dame);//-Hp;
        m.writer().writeInt(mpdown);//-mp
        m.writer().writeShort(idskill_atk);//idSkill_atk
        m.writer().writeByte(typeatk);//type atk
        m.writer().writeByte(typetool);//type tool
        m.writer().flush();
        n.p.conn.sendMessage(m);
        m.cleanup();
        m = new Message(-2);
        m.writer().writeByte(mobid);
        m.writer().writeInt(n.id);//id ninja
        m.writer().writeInt(dame);//-Hp;
        m.writer().writeInt(mpdown);//-mp
        m.writer().writeShort(idskill_atk);//idSkill_atk
        m.writer().writeByte(typeatk);//type atk
        m.writer().writeByte(typetool);//type tool
        m.writer().flush();
        sendMyMessage(n.p, m);
        if (n.isDie && !map.LangCo()) {
            sendDie(n);
        }
    }

    private void loadMobMeAtk(Char n) {
        n.mobMe.timeFight = System.currentTimeMillis() + 1000L;
        try {
            if (n.mobAtk != -1 && n.mobMe.templates.id >= 211 && n.mobMe.templates.id <= 217) {
                Mob mob = getMob(n.mobAtk);
                if (!mob.isDie) {
                    int dame = n.dameMax() * 20 / 100;
                    MobMeAtkMessage(n, mob.id, dame, (short) 40, (byte) 1, (byte) 1, (byte) 0);
                    mob.updateHP(-dame);
                    attachedMob(dame, mob.id, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MobMeAtkMessage(Char n, int idatk, int dame, short idskill_atk, byte typeatk, byte typetool, byte type) throws IOException {
        Message m = new Message(87);
        m.writer().writeInt(n.id);
        m.writer().writeByte(idatk);
        m.writer().writeShort(idskill_atk);//idSkill_atk
        m.writer().writeByte(typeatk);//type atk
        m.writer().writeByte(typetool);//type tool
        m.writer().writeByte(type);//type
        if (type == 1) {
            m.writer().writeInt(idatk);//char atk
        }
        m.writer().flush();
        n.p.conn.sendMessage(m);
        m.cleanup();
    }

    public void openFindParty(Player p) {
        try {
            ArrayList<Party> partys = this.getArryListParty();
            Message m = new Message(-30);
            m.writer().writeByte(-77);
            for (int i = 0; i < partys.size(); i++) {
                Char n = partys.get(i).getNinja(partys.get(i).master);
                m.writer().writeByte(n.nclass);
                m.writer().writeByte(n.level);
                m.writer().writeUTF(n.name);
                m.writer().writeByte(partys.get(i).ninjas.size());
            }
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        synchronized (this) {
            try {
                Calendar cal = GregorianCalendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                int sec = cal.get(Calendar.SECOND);
                for (int k = 0; k < this.players.size(); k++) {
                    final Player pl = this.players.get(k);
                    if (pl != null && min % 1 == 0 && sec == 0) {
                        pl.flush();
                        if (pl.c != null) {
                            pl.c.flush();
                            if (pl.c.clone != null) {
                                pl.c.clone.flush();
                            }
                        }
                        System.err.println("Update :" + pl.c.name);
                    }
//                    Thread.sleep(1000);
                }
                //quai vat
                for (int i = mobs.size() - 1; i >= 0; i--) {
                    Mob mob = mobs.get(i);
                    if (mob.timeRefresh > 0 && System.currentTimeMillis() >= mob.timeRefresh && mob.isRefresh) {
                        refreshMob(mob.id);
                    }
                    if (mob.isDisable && System.currentTimeMillis() >= mob.timeDisable) {
                        this.DisableMobMessage(null, mob.id, -1);
                    }
                    if (mob.isDontMove && System.currentTimeMillis() >= mob.timeDontMove) {
                        this.DontMoveMobMessage(null, mob.id, -1);
                    }
                    if (mob.isFire && System.currentTimeMillis() >= mob.timeFire) {
                        FireMobMessage(mob.id, -1);
                    }
                    if (mob.isIce && System.currentTimeMillis() >= mob.timeIce) {
                        IceMobMessage(mob.id, -1);
                    }
                    if (mob.isWind && System.currentTimeMillis() >= mob.timeWind) {
                        WindMobMessage(mob.id, -1);
                    }
                    if (!mob.isDie && mob.status != 0 && mob.level != 1 && System.currentTimeMillis() >= mob.timeFight) {
                        loadMobAttached(mob.id);
                    }
                }
                for (int i = players.size() - 1; i >= 0; i--) {
                    Player p = players.get(i);
                    //effect
                    for (int j = p.c.get().veff.size() - 1; j >= 0; j--) {
                        Effect eff = p.c.get().veff.get(j);
                        if (System.currentTimeMillis() >= eff.timeRemove) {
                            p.removeEffect(eff.template.id);
                            j--;
                        } else if (eff.template.type == 0 || eff.template.type == 12) {
                            p.c.get().upHP(eff.param);
                            p.c.get().upMP(eff.param);
                        } else if (eff.template.type == 4 || eff.template.type == 17) {
                            p.c.get().upHP(eff.param);
                        } else if (eff.template.type == 13) {
                            p.c.get().upHP(-(p.c.get().getMaxHP() * 3 / 100));
                            if (p.c.get().isDie) {
                                p.c.get().upDie();
                            }
                        }
                    }
                    //eff5buff
                    if (p.c.eff5buffHP() > 0 || p.c.get().eff5buffMP() > 0) {
                        if (p.c.eff5buff <= System.currentTimeMillis()) {
                            p.c.eff5buff = System.currentTimeMillis() + 5000L;
                            p.c.get().upHP(p.c.get().eff5buffHP());
                            p.c.get().upMP(p.c.get().eff5buffMP());
                        }
                    }
                    // cai gi ke di
                    if (System.currentTimeMillis() > p.c.delayEffect) {
                        if (p.c.get().fullTL() >= 7) {
                            byte tl = 0;
                            switch (GameScr.SysClass(p.c.nclass)) {
                                case 1:
                                    tl = 9;
                                    break;
                                case 2:
                                    tl = 3;
                                    break;
                                case 3:
                                    tl = 6;
                                    break;
                            }
                            if (p.c.fullTL() >= 9) {
                                tl += 1;
                            }
                            if (p.c.fullTL() >= 7) {
                                tl += 0;
                            }
                            for (int j = 0; j < players.size(); j++) {
                                GameCanvas.addEffect(players.get(j).conn, (byte) 0, p.c.get().id, tl, 1, 1, false);
                            }
                        }
                        p.c.delayEffect = System.currentTimeMillis() + 5000;
                        int dem = 0;
                        int dem1 = 0;
                        for (byte j = 0; j < p.c.get().ItemBody.length; j++) {
                            Item item = p.c.get().ItemBody[j];
                            if (item != null && item.id == 694) {
                                for (int k = 0; k < players.size(); k++) {
                                    GameCanvas.addEffect(players.get(k).conn, (byte) 0, p.c.id, (byte) 21, 1, 1, false);
                                }
                            }
                            if (item != null && item.id == 583) {
                                for (int k = 0; k < players.size(); k++) {
                                    GameCanvas.addEffect(players.get(k).conn, (byte) 0, p.c.id, (byte) 25, 1, 1, false);
                                }
                            }
                            if (item != null && item.id == 781) {
                                for (int k = 0; k < players.size(); k++) {
                                    GameCanvas.addEffect(players.get(k).conn, (byte) 0, p.c.id, (byte) 24, 1, 1, false);
                                }
                            }
                            if (item != null && item.id == 720 || item != null && item.id == 718 || item != null && item.id == 719) {
                                dem += 1;
                            }
                            if (item != null && item.id == 722 || item != null && item.id == 721 || item != null && item.id == 723) {
                                dem1 += 1;
                            }
                            if (dem == 3) {
                                for (int k = 0; k < players.size(); k++) {
                                    GameCanvas.addEffect(players.get(k).conn, (byte) 0, p.c.id, (byte) 1, 0, 0, false);
                                }
                            }
                            if (dem1 == 3) {
                                for (int k = 0; k < players.size(); k++) {
                                    GameCanvas.addEffect(players.get(k).conn, (byte) 0, p.c.id, (byte) 2, 0, 0, false);
                                }
                            }
                        }
                        if (((hour == 13 || hour == 21 || hour == 23) && (map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104)) || ((map.id == 98 || map.id == 99 || map.id == 100 || map.id == 101 || map.id == 102 || map.id == 103 || map.id == 104) && ((p.c.xu < 100000) || (p.c.typepk == 0)))) {
                            for (byte k = 0; k < ma.area.length; k++) {
                                if (ma.area[k].numplayers < ma.template.maxplayers) {
                                    p.c.place.leave(p);
                                    ma.area[k].EnterMap0(p.c);
                                    break;
                                }
                            }
                            p.c.changePk(p.c, (byte) 0);
                            p.conn.sendMessageLog("Hết giờ rồi hoặc dưới 100k xu. Cút ra ngoài!");
                        }
                    }
                    //pet attack
//                    if (p.c.get().mobMe != null && p.c.get().mobMe.timeFight <= System.currentTimeMillis()) {
//                        loadMobMeAtk(p.c);
//                    }
                    //xoa item tui  het han cua ng choi
                    for (byte j = 0; j < p.c.ItemBag.length; j++) {
                        if (p.c.isHuman) {
                            Item item = p.c.ItemBag[j];
                            if (item == null || !item.isExpires) {
                                continue;
                            }
                            if (System.currentTimeMillis() >= item.expires) {
                                p.c.removeItemBag(j, item.quantity);
                            }
                        }
                    }
                    //xoa item tra bi  het han cua ng choi
                    for (byte j = 0; j < p.c.get().ItemBody.length; j++) {
                        if (p.c.isHuman) {
                            Item item = p.c.get().ItemBody[j];
                            if (item == null || !item.isExpires) {
                                continue;
                            }
                            if (System.currentTimeMillis() >= item.expires) {
                                p.c.removeItemBody(j);
                            }
                        }
                    }
                    //xoa item ruong het han cua ng choi
                    for (byte j = 0; j < p.c.ItemBox.length; j++) {
                        if (p.c.isHuman) {
                            Item item = p.c.ItemBox[j];
                            if (item == null || !item.isExpires) {
                                continue;
                            }
                            if (System.currentTimeMillis() >= item.expires) {
                                p.c.removeItemBox(j);
                            }
                        }
                    }
                    if (map.LangCo() && (p.c.isDie || p.c.expdown > 0)) {
                        DieReturn(p);
                    }
                    if (System.currentTimeMillis() > p.c.deleyRequestClan) {
                        p.c.requestclan = -1;
                    }
                    if (p.c.clone != null && !p.c.clone.isDie && (Math.abs(p.c.x - p.c.clone.x) > 80 || Math.abs(p.c.y - p.c.clone.y) > 30)) {
                        p.c.clone.move((short) util.nextInt(p.c.x - 35, p.c.x + 35), p.c.y);
                    }
                    if (!p.c.clone.isDie && System.currentTimeMillis() > p.c.timeRemoveClone) {
                        p.c.clone.off();
                    }
                    if (p.c.get().isDie && p.c.isNhanban) {
                        p.exitNhanBan(true);
                    }
                    if (map.id == 110 && p.c.cTestDun != null && sec % 5 == 0) {
                        if (p.c.xuTestDun == p.c.cTestDun.xuTestDun && p.c.xuTestDun != 0 && p.c.cTestDun.xuTestDun != 0) {
                            Map ma = Manager.getMapid(111);
                            for (Place area : ma.area) {
                                if (area.numplayers < ma.template.maxplayers) {
                                    p.c.place.leave(p);
                                    area.EnterMap0(p.c);
                                    p.c.cTestDun.place.leave(p.c.cTestDun.p);
                                    area.EnterMap0(p.c.cTestDun);
                                    p.sendAddchatYellow("Trận đấu bắt đầu.");
                                    p.c.cTestDun.p.sendAddchatYellow("Trận đấu bắt đầu.");
                                    p.c.changePk(p.c, (byte)4);
                                    p.c.cTestDun.changePk(p.c.cTestDun, (byte)5);
                                    server.manager.chatKTG(p.c.name + "(" + p.c.level + ") đang thách đấu với " + p.c.cTestDun.name + "(" + p.c.cTestDun.level + ") " + p.c.xuTestDun + " xu ở lôi đài.");
                                    p.setEffect(14, 0, 30000, 0);
                                    p.c.timeTestDun = 600000L + System.currentTimeMillis();
                                    p.c.cTestDun.timeTestDun = 600000L + System.currentTimeMillis();
                                    p.c.cTestDun.p.setEffect(14, 0, 30000, 0);
                                    p.setTimeMap((int) ((p.c.timeTestDun - System.currentTimeMillis())/1000));
                                    p.c.cTestDun.p.setTimeMap((int) ((p.c.cTestDun.timeTestDun - System.currentTimeMillis())/1000));
                                    return;
                                }
                            }
                        }
                    }
                    if ((this.map.id == 110 || this.map.id == 111) && p.c.cTestDun != null && System.currentTimeMillis() > p.c.timeTestDun) {
                        Map ma = Manager.getMapid(p.c.mapLTD);
                        for (Place area : ma.area) {
                            if (area.numplayers < ma.template.maxplayers) {
                                p.c.place.leave(p);
                                area.EnterMap0(p.c);
                                p.c.changePk(p.c, (byte)0);
                                p.liveFromDead();
                                p.removeEffect(14);
                                p.c.cTestDun.p.removeEffect(14);
                                p.c.cTestDun.xuTestDun = 0;
                                p.c.cTestDun = null;
                                p.c.xuTestDun = 0;
                                return;
                            }
                        }
                    }
                }
                //xoa item map
                for (int i = 0; i < itemMap.size(); i++) {
                    ItemMap itm = itemMap.get(i);
                    if (System.currentTimeMillis() >= itm.removedelay) {
                        removeItemMapMessage(itm.itemMapId);
                        itemMap.remove(i);
                        i--;
                    } else if ((itm.removedelay - System.currentTimeMillis()) < 70000L && itm.master != -1) {
                        itm.master = -1;
                    }
                }
                if (map.cave != null && System.currentTimeMillis() > map.cave.time) {
                    map.cave.rest();
                }
                if (map.cave != null && map.cave.level == map.cave.map.length) {
                    map.cave.finsh();
                }
                for (int k = 0; k < this.players.size(); ++k) {
                    final Player p = this.players.get(k);
                    if (map.id >= 98 && map.id <= 104 && (hour == 11 || hour == 21) && (min == 0) && sec == 0) {
                        Map ma = Manager.getMapid(p.c.mapLTD);
                        for (Place area : ma.area) {
                            if (area.numplayers < ma.template.maxplayers) {
                                p.c.place.leave(p);
                                area.EnterMap0(p.c);
                                p.sendAddchatYellow("Chiến trường đã kết thúc. Nhận thưởng tại NPC Rikudou.");
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
    }

    public synchronized void sendToMap(final Message ms) throws IOException {
        for (final Player pl : players) {
            if (pl != null) {
                pl.conn.sendMessage(ms);
            }
        }
    }
    
    private void DisableMobMessage(final Player p , final int mobid, final int type) {
        try {
            final Mob mob = this.getMob(mobid);
            switch (type) {
                case -1: {
                    mob.isDisable = false;
                    break;
                }
                case 0: {
                    mob.isDisable = true;
                    mob.timeDisable = System.currentTimeMillis() + 1000*p.c.getPramSkill(48);
                    break;
                }
            }
            final Message m = new Message(85);
            m.writer().writeByte(mobid);
            m.writer().writeBoolean(mob.isDisable);
            m.writer().flush();
            this.sendMessage(m);
            m.cleanup();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void DontMoveMobMessage(final Player p , final int mobid, final int type) {
        try {
            final Mob mob = this.getMob(mobid);
            switch (type) {
                case -1: {
                    mob.isDontMove = false;
                    break;
                }
                case 0: {
                    mob.isDontMove = true;
                    mob.timeDontMove = System.currentTimeMillis() + 1000*p.c.getPramSkill(55);
                    break;
                }
            }
            final Message m = new Message(86);
            m.writer().writeByte(mobid);
            m.writer().writeBoolean(mob.isDontMove);
            m.writer().flush();
            this.sendMessage(m);
            m.cleanup();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String result(Char c) throws SQLException {
        String win = null;
        int pointWhite = 0;
        ResultSet red = SQLManager.stat.executeQuery("SELECT `pointCT` FROM `ninja` WHERE typeCT=4;");
        while (red.next()) {
            int a = red.getInt("pointCT");
            pointWhite = pointWhite + a;
        }
        int pointBlack = 0;
        ResultSet red2 = SQLManager.stat.executeQuery("SELECT `pointCT` FROM `ninja` WHERE typeCT=5;");
        while (red2.next()) {
            int b = red2.getInt("pointCT");
            pointBlack = pointBlack + b;
        }
        if (pointWhite == pointBlack) {
            win = "Hai phe hoà nhau";
        } else if (pointWhite > pointBlack) {
            win = "Bạch Giả giành chiến thắng";
            map.war.win = 4;
        } else {
            win = "Hắc Giả giành chiến thắng";
            map.war.win = 5;
        }
        String result = "Tích luỹ: " + c.pointCT + " điểm " + (c.typeCT == map.war.win ? "(thưởng)\n" : "\n") + win + "\nBạch Giả: " + pointWhite + " điểm\nHắc Giả: " + pointBlack + " điểm\n" + BXHManager.getStringBXH(5);
        return result;
    }
}

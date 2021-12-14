package server;

import boardGame.Place;
import io.Message;
import java.io.IOException;
import java.util.Calendar;
import real.Cave;
import real.ClanManager;
import real.Item;
import real.ItemData;
import real.Map;
import real.Player;

/**
 *
 * @author Văn Tú
 */
public class MenuController {

    Server server = Server.getInstance();

    public void sendMenu(Player p, Message m) throws IOException {
        byte b1 = m.reader().readByte();//idnpc
        byte b2 = m.reader().readByte();//class1
        byte b3 = m.reader().readByte();//class2
        m.cleanup();
        switch (p.typemenu) {
            //menu npc Katana
            case 0:
                if (p.c.mapid == 110) {
                        switch (b2) {
                            case 0: {
                                Map ma = Manager.getMapid(p.c.mapLTD);
                                for (Place area : ma.area) {
                                    if (area.numplayers < ma.template.maxplayers) {
                                        p.c.place.leave(p);
                                        area.EnterMap0(p.c);
                                        p.sendAddchatYellow("Trận đấu bị hủy vì bạn đã khiếp sợ bỏ chạy");
                                        p.c.cTestDun.place.leave(p.c.cTestDun.p);
                                        area.EnterMap0(p.c.cTestDun);
                                        p.c.cTestDun.p.sendAddchatYellow("Trận đấu bị hủy vì đối phương đã khiếp sợ bỏ chạy");
                                        p.setTimeMap(0);
                                        p.c.cTestDun.p.setTimeMap(0);
                                        p.c.cTestDun.xuTestDun = 0;
                                        p.c.cTestDun = null;
                                        p.c.xuTestDun = 0;
                                        return;
                                    }
                                }
                                break;
                            }
                            case 1: {
                                this.sendWrite(p, (short)48, "Nhập xu cược");
                                break;
                            }
                            case 2: {
                                p.c.place.chatNPC(p, (short)b1, "Lo mà quánh lộn đi đừng quan tâm tới ta.");
                                break;
                            }
                        }
                        break;
                    }
                if (b2 == 0) {
                    p.requestItem(2);
                } else if (b2 == 1) {
                    if (b3 == 0) {
                        if (!p.c.clan.clanName.isEmpty()) {
                            p.c.place.chatNPC(p, (short) b1, "Hiện tại con đã có gia tộc không thể thành lập thêm được nữa.");
                        } else if (p.luong < 300000) {
                            p.c.place.chatNPC(p, (short) b1, "Để thành lập gia tộc con cần phải cóc đủ 300.000 lượng trong người.");
                        } else {
                            this.sendWrite(p, (short) 50, "Tên gia tộc");
                        }
                    }
                } else if (b2 == 2) {
                    if (p.c.isNhanban) {
                        p.conn.sendMessageLog("Chức năng này không dành cho phân thân");
                        return;
                    }
                    if (b3 == 0) {
                        Service.evaluateCave(p.c);
                    } else {
                        Cave cave = null;
                        if (p.c.caveID != -1) {
                            if (Cave.caves.containsKey(p.c.caveID)) {
                                cave = Cave.caves.get(p.c.caveID);
                                p.c.place.leave(p);
                                cave.map[0].area[0].EnterMap0(p.c);
                            }
                        } else if (p.c.party != null) {
                            if (p.c.party.cave == null && p.c.party.master != p.c.id) {
                                p.conn.sendMessageLog("Chỉ có nhóm trưởng mới được phép mở cửa hang động");
                                return;
                            }
                        }
                        if (cave == null) {
                            if (p.c.nCave <= 0) {
                                p.c.place.chatNPC(p, (short) b1, "Số lần vào hang động cảu con hôm nay đã hết hãy quay lại vào ngày mai.");
                                return;
                            }
                            if (b3 == 1) {
                                if (p.c.level < 30 || p.c.level > 39) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                }
                                if (p.c.party != null) {
                                    synchronized (p.c.party.ninjas) {
                                        for (byte i = 0; i < p.c.party.ninjas.size(); i++) {
                                            if (p.c.party.ninjas.get(i).level < 30 || p.c.party.ninjas.get(i).level > 39) {
                                                p.conn.sendMessageLog("Thành viên trong nhóm trình độ không phù hợp");
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (p.c.party != null) {
                                    if (p.c.party.cave == null) {
                                        cave = new Cave(3);
                                        p.c.party.openCave(cave, p.c.name);
                                    } else {
                                        cave = p.c.party.cave;
                                    }
                                } else {
                                    cave = new Cave(3);
                                }
                                p.c.caveID = cave.caveID;
                            }
                            if (b3 == 2) {
                                if (p.c.level < 40 || p.c.level > 49) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                }
                                if (p.c.party != null) {
                                    synchronized (p.c.party) {
                                        for (byte i = 0; i < p.c.party.ninjas.size(); i++) {
                                            if (p.c.party.ninjas.get(i).level < 40 || p.c.party.ninjas.get(i).level > 49) {
                                                p.conn.sendMessageLog("Thành viên trong nhóm trình độ không phù hợp");
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (p.c.party != null) {
                                    if (p.c.party.cave == null) {
                                        cave = new Cave(4);
                                        p.c.party.openCave(cave, p.c.name);
                                    } else {
                                        cave = p.c.party.cave;
                                    }
                                } else {
                                    cave = new Cave(4);
                                }
                                p.c.caveID = cave.caveID;
                            }
                            if (b3 == 3) {
                                if (p.c.level < 50 || p.c.level > 59) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                }
                                if (p.c.party != null) {
                                    synchronized (p.c.party.ninjas) {
                                        for (byte i = 0; i < p.c.party.ninjas.size(); i++) {
                                            if (p.c.party.ninjas.get(i).level < 50 || p.c.party.ninjas.get(i).level > 59) {
                                                p.conn.sendMessageLog("Thành viên trong nhóm trình độ không phù hợp");
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (p.c.party != null) {
                                    if (p.c.party.cave == null) {
                                        cave = new Cave(5);
                                        p.c.party.openCave(cave, p.c.name);
                                    } else {
                                        cave = p.c.party.cave;
                                    }
                                } else {
                                    cave = new Cave(5);
                                }
                                p.c.caveID = cave.caveID;
                            }
                            if (b3 == 4) {
                                if (p.c.level < 60 || p.c.level > 69) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                } else if (p.c.party != null && p.c.party.ninjas.size() > 1) {
                                    p.conn.sendMessageLog("Hoạt động lần này chỉ được phép một mình");
                                    return;
                                }
                                cave = new Cave(6);
                                p.c.caveID = cave.caveID;
                            }
                            if (b3 == 5) {
                                if (p.c.level < 70 || p.c.level > 89) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                }
                                if (p.c.party != null) {
                                    synchronized (p.c.party.ninjas) {
                                        for (byte i = 0; i < p.c.party.ninjas.size(); i++) {
                                            if (p.c.party.ninjas.get(i).level < 70 || p.c.party.ninjas.get(i).level > 89) {
                                                p.conn.sendMessageLog("Thành viên trong nhóm trình độ không phù hợp");
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (p.c.party != null) {
                                    if (p.c.party.cave == null) {
                                        cave = new Cave(7);
                                        p.c.party.openCave(cave, p.c.name);
                                    } else {
                                        cave = p.c.party.cave;
                                    }
                                } else {
                                    cave = new Cave(7);
                                }
                                p.c.caveID = cave.caveID;
                            }
                            if (b3 == 6) {
                                if (p.c.level < 90 || p.c.level > 130) {
                                    p.conn.sendMessageLog("Trình độ không phù hợp");
                                    return;
                                }
                                if (p.c.party != null) {
                                    synchronized (p.c.party.ninjas) {
                                        for (byte i = 0; i < p.c.party.ninjas.size(); i++) {
                                            if (p.c.party.ninjas.get(i).level < 90 || p.c.party.ninjas.get(i).level > 131) {
                                                p.conn.sendMessageLog("Thành viên trong nhóm trình độ không phù hợp");
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (p.c.party != null) {
                                    if (p.c.party.cave == null) {
                                        cave = new Cave(9);
                                        p.c.party.openCave(cave, p.c.name);
                                    } else {
                                        cave = p.c.party.cave;
                                    }
                                } else {
                                    cave = new Cave(9);
                                }
                                p.c.caveID = cave.caveID;
                            }
                            if (cave != null) {
                                p.c.nCave--;
                                p.c.pointCave = 0;
                                p.c.place.leave(p);
                                cave.map[0].area[0].EnterMap0(p.c);
                            }
                        }
                        p.setPointPB(p.c.pointCave);
                    }
                } else if (b2 == 3) {
                    switch (b3) {
                            case 0: {
                                this.sendWrite(p, (short)49, "Nhập tên đối thủ");
                                break;
                            }
                        }
                        break;
                }
                break;
            //menu npc Furoya
            case 16: {
                switch (b2) {
                    case 0: {
                        try {
                            if (p.s.c.typeSolo != 0) {
                                p.solo.start();
                            }
                        } catch (Exception e) {
                            p.c.place.chatNPC(p, (short) b1, "Kêu thằng kia qua bấm bắt đầu.");;
                        }
                        break;
                    }
                }
                break;
            }
            case 1:
                if (b2 == 0) {
                    if (b3 == 0) {
                        p.requestItem(21 - p.c.gender);
                    } else if (b3 == 1) {
                        p.requestItem(23 - p.c.gender);
                    } else if (b3 == 2) {
                        p.requestItem(25 - p.c.gender);
                    } else if (b3 == 3) {
                        p.requestItem(27 - p.c.gender);
                    } else if (b3 == 4) {
                        p.requestItem(29 - p.c.gender);
                    }
                }
                break;
            //menu npc Ameji
            case 2:
                if (b2 == 0) {
                    if (b3 == 0) {
                        p.requestItem(16);
                    } else if (b3 == 1) {
                        p.requestItem(17);
                    } else if (b3 == 2) {
                        p.requestItem(18);
                    } else if (b3 == 3) {
                        p.requestItem(19);
                    }
                }
                break;
            //menu npc Kiriko
            case 3:
                if (b2 == 0) {
                    p.requestItem(7);
                } else if (b2 == 1) {
                    p.requestItem(6);
                }
                break;
            //menu npc Tabemono
            case 4:
                switch (b2) {
                    case 0:
                        p.requestItem(9);
                        break;
                    case 1:
                        p.requestItem(8);
                        break;
                }
                break;
            //menu npc Kamakura
            case 5:
                switch (b2) {
                    case 0:
                        p.requestItem(4);
                        break;
                    case 1:
                        p.c.mapLTD = p.c.place.map.id;
                        p.c.place.chatNPC(p, (short) b1, "Lưu tọa độ thành công, khi kiệt sức con sẽ được khiêng về đây");
                        break;
                    case 2:
                        if (b3 == 0) {
                            if (p.c.isNhanban) {
                                p.conn.sendMessageLog("Chức năng này không dành cho phân thân");
                                return;
                            }
                            if (p.c.level < 60) {
                                p.conn.sendMessageLog("Chức năng yêu cầu trình độ 60");
                                return;
                            }
                            Map ma = server.manager.getMapid(139);
                            for (Place area : ma.area) {
                                if (area.numplayers < ma.template.maxplayers) {
                                    p.c.place.leave(p);
                                    area.EnterMap0(p.c);
                                    return;
                                }
                            }
                        }
                        break;
                }
                break;
            //menu npc Kenshinto
            case 6:
                switch (b2) {
                    case 0:
                        if (b3 == 0) {
                            p.requestItem(10);
                        } else if (b3 == 1) {
                            p.requestItem(31);
                        }
                        break;
                    case 1:
                        if (b3 == 0) {
                            p.requestItem(12);
                        } else if (b3 == 1) {
                            p.requestItem(11);
                        }
                        break;
                    case 2:
                        p.requestItem(13);
                        break;
                    case 3:
                        p.requestItem(33);
                        break;
                    case 4:
                        p.requestItem(46);
                        break;
                    case 5:
                        p.requestItem(47);
                        break;
                    case 6:
                        p.requestItem(49);
                        break;
                    case 7:
                        p.requestItem(50);
                        break;
                }
                break;
            //menu noc Umayaki
            case 7:
                if (b2 == 0) {
                } else if (b2 > 0 && b2 <= Map.arrLang.length) {
                    Map ma = Manager.getMapid(Map.arrLang[b2 - 1]);
                    for (Place area : ma.area) {
                        if (area.numplayers < ma.template.maxplayers) {
                            p.c.place.leave(p);
                            area.EnterMap0(p.c);
                            return;
                        }
                    }
                }
                break;
            //menu noc Umayaki
            case 8:
                if (b2 >= 0 && b2 < Map.arrTruong.length) {
                    Map ma = Manager.getMapid(Map.arrTruong[b2]);
                    for (Place area : ma.area) {
                        if (area.numplayers < ma.template.maxplayers) {
                            p.c.place.leave(p);
                            area.EnterMap0(p.c);
                            return;
                        }
                    }
                } else {

                }
                break;
            //menu npc cô toyotomi
            case 9:
                if (b2 == 0) {
                    if (b3 == 0) {
                        server.manager.sendTB(p, "Top đại gia yên", BXHManager.getStringBXH(0));
                    } else if (b3 == 1) {
                        server.manager.sendTB(p, "Top cao thủ", BXHManager.getStringBXH(1));
                    } else if (b3 == 2) {
                        server.manager.sendTB(p, "Top gia tộc", BXHManager.getStringBXH(2));
                    } else if (b3 == 3) {
                        server.manager.sendTB(p, "Top hang động", BXHManager.getStringBXH(3));
                    }
                }
                if (b2 == 1) {
                    if (p.c.get().nclass > 0) {
                        p.c.place.chatNPC(p, (short) b1, "Con đã vào lớp từ trước rồi mà");
                    } else if (p.c.get().ItemBody[1] != null) {
                        p.c.place.chatNPC(p, (short) b1, "Con cần tháo vũ khí ra để đến đây nhập học nhé");
                    } else if (p.c.getBagNull() < 3) {
                        p.c.place.chatNPC(p, (short) b1, "Hành trang phải có đủ 2 ô để nhận đồ con nhé");
                    } else {
                        p.c.addItemBag(false, ItemData.itemDefault(420));
                        if (b3 == 0) {
                            p.Admission((byte) 1);
                        } else if (b3 == 1) {
                            p.Admission((byte) 2);
                        }
                        p.c.place.chatNPC(p, (short) b1, "Hãy chăm chỉ quay tay để lên cấp con nhé");
                    }
                } else if (b2 == 2) {
                    if (p.c.get().nclass != 1 && p.c.get().nclass != 2) {
                        p.c.place.chatNPC(p, (short) b1, "Con không phải học sinh trường này nên không thể tẩy điểm ở đây");
                    } else {
                        if (b3 == 0) {
                            p.restPpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm tiềm năng, hãy sử dụng tốt điểm tiềm năng nhé");
                        } else if (b3 == 1) {
                            p.restSpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm kĩ năng, hãy sử dụng tốt điểm kĩ năng nhé");
                        }
                    }
                }
                break;
            //menu npc cô Ookamesama
            case 10:
                if (b2 == 0) {
                    if (b3 == 0) {
                        server.manager.sendTB(p, "Top đại gia yên", BXHManager.getStringBXH(0));
                    } else if (b3 == 1) {
                        server.manager.sendTB(p, "Top cao thủ", BXHManager.getStringBXH(1));
                    } else if (b3 == 2) {
                        server.manager.sendTB(p, "Top gia tộc", BXHManager.getStringBXH(2));
                    } else if (b3 == 3) {
                        server.manager.sendTB(p, "Top hang động", BXHManager.getStringBXH(3));
                    }
                }
                if (b2 == 1) {
                    if (p.c.get().nclass > 0) {
                        p.c.place.chatNPC(p, (short) b1, "Con đã vào lớp từ trước rồi mà");
                    } else if (p.c.get().ItemBody[1] != null) {
                        p.c.place.chatNPC(p, (short) b1, "Con cần tháo vũ khí ra để đến đây nhập học nhé");
                    } else if (p.c.getBagNull() < 3) {
                        p.c.place.chatNPC(p, (short) b1, "Hành trang phải có đủ 2 ô để nhận đồ con nhé");
                    } else {
                        p.c.addItemBag(false, ItemData.itemDefault(421));
                        if (b3 == 0) {
                            p.Admission((byte) 3);
                        } else if (b3 == 1) {
                            p.Admission((byte) 4);
                        }
                        p.c.place.chatNPC(p, (short) 9, "Hãy chăm chỉ quay tay để lên cấp con nhé");
                    }
                } else if (b2 == 2) {
                    if (p.c.get().nclass != 3 && p.c.get().nclass != 4) {
                        p.c.place.chatNPC(p, (short) b1, "Con không phải học sinh trường này nên không thể tẩy điểm ở đây");
                    } else {
                        if (b3 == 0) {
                            p.restPpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm tiềm năng, hãy sử dụng tốt điểm tiềm năng nhé");
                        } else if (b3 == 1) {
                            p.restSpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm kĩ năng, hãy sử dụng tốt điểm kĩ năng nhé");
                        }
                    }
                }
                break;
            //menu npc thầy Kazeto
            case 11:
                if (b2 == 0) {
                    if (b3 == 0) {
                        server.manager.sendTB(p, "Top đại gia yên", BXHManager.getStringBXH(0));
                    } else if (b3 == 1) {
                        server.manager.sendTB(p, "Top cao thủ", BXHManager.getStringBXH(1));
                    } else if (b3 == 2) {
                        server.manager.sendTB(p, "Top gia tộc", BXHManager.getStringBXH(2));
                    } else if (b3 == 3) {
                        server.manager.sendTB(p, "Top hang động", BXHManager.getStringBXH(3));
                    }
                }
                if (b2 == 1) {
                    if (p.c.get().nclass > 0) {
                        p.c.place.chatNPC(p, (short) b1, "Con đã vào lớp từ trước rồi mà");
                    } else if (p.c.get().ItemBody[1] != null) {
                        p.c.place.chatNPC(p, (short) b1, "Con cần tháo vũ khí ra để đến đây nhập học nhé");
                    } else if (p.c.getBagNull() < 3) {
                        p.c.place.chatNPC(p, (short) b1, "Hành trang phải có đủ 2 ô để nhận đồ con nhé");
                    } else {
                        p.c.addItemBag(false, ItemData.itemDefault(422));
                        if (b3 == 0) {
                            p.Admission((byte) 5);
                        } else if (b3 == 1) {
                            p.Admission((byte) 6);
                        }
                        p.c.place.chatNPC(p, (short) b1, "Hãy chăm chỉ quay tay để lên cấp con nhé");
                    }
                } else if (b2 == 2) {
                    if (p.c.get().nclass != 5 && p.c.get().nclass != 6) {
                        p.c.place.chatNPC(p, (short) b1, "Con không phải học sinh trường này nên không thể tẩy điểm ở đây");
                    } else {
                        if (b3 == 0) {
                            p.restPpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm tiềm năng, hãy sử dụng tốt điểm tiềm năng nhé");
                        } else if (b3 == 1) {
                            p.restSpoint();
                            p.c.place.chatNPC(p, (short) b1, "Ta đã giúp con tẩy điểm kĩ năng, hãy sử dụng tốt điểm kĩ năng nhé");
                        }
                    }
                }
                break;
            //menu npc Tajima
            case 12:
                if (b2 == 0) {
//                    if (p.nj.denbu == 2) {
//                        p.nj.place.chatNPC(p, (short) b1, "Con đã nhận đền bù từ ad Đức rồi nha");
//                    } else {
//                        if (p.nj.getBagNull() < 1) {
//                            p.nj.place.chatNPC(p, (short) b1, "Hành trang không đủ 51 chỗ trống");
//                        } else {
//                            p.nj.place.chatNPC(p, (short) b1, "Xin lỗi bạn vì bảo trì hơi lâu");
//                            p.nj.denbu = 2;
//                            p.upluongMessage(100000);
//                            Item it = new Item();
//                            it.id = 384;
//                            it.quantity = 15;
//                            it.isLock = true;
//                            p.nj.addItemBag(true, it);
////                            for (byte i = 0; i < 50; i++) {
////                                it = new Item();
////                                it.id = 454;
////                                it.isLock = true;
////                                p.nj.addItemBag(true, it);
////                            }
//                        }
//                    }
                    if (p.luong < 20000) {
                        p.c.place.chatNPC(p, (short) b1, "Cần 20k lượng để mở");
                        break;
                    } else if (p.c.maxluggage > 120) {
                        p.c.place.chatNPC(p, (short) b1, "126 ô rương là full rồi con à");
                        break;
                    } else {
                        p.upluongMessage(-20000);
                        p.c.maxluggage += 6;
                        p.conn.sendMessageLog("Bạn đã mở thêm 6 ô rương. Tổng rương hiện tại của bạn là " + p.c.maxluggage);
                        break;
                    }
                } else if (b2 == 4) {
                    if (p.c.timeRemoveClone > System.currentTimeMillis()) {
                        p.toNhanBan();
                    }
                } else if (b2 == 5) {
                    if (!p.c.clone.isDie && p.c.timeRemoveClone > System.currentTimeMillis()) {
                        p.exitNhanBan(false);
                    }
                } else {
                    p.c.place.chatNPC(p, (short) b1, "Con đang thực hiện nhiệm vụ kiên trì diệt ác, hãy chọn Menu/Nhiệm vụ để biết mình đang làm đến đâu");
                }
                break;
            //Menu npc Kirin
            case 19:
                if (b2 == 0) {
                    if (p.c.exptype == 0) {
                        p.c.exptype = 1;
                        p.c.place.chatNPC(p, (short) b1, "Đã tắt không nhận kinh nghiệm");
                    } else {
                        p.c.exptype = 0;
                        p.c.place.chatNPC(p, (short) b1, "Đã bật không nhận kinh nghiệm");
                    }
                } else if (b2 == 1) {
                    p.passold = "";
                    this.sendWrite(p, (short) 51, "Nhập mật khẩu cũ");
                }
                break;
            //menu npc Guriin
            case 22: {
                if (b2 != 0) {
                    break;
                }
                if (p.c.clan.clanName.isEmpty()) {
                    p.c.place.chatNPC(p, (short) b1, "Con cần phải có gia tộc thì mới có thể điểm danh được nhé");
                    break;
                }
                if (p.c.ddClan) {
                    p.c.place.chatNPC(p, (short) b1, "Hôm nay con đã điểm danh rồi nhé, hãy quay lại đây vào ngày mai");
                    break;
                }
                p.c.ddClan = true;
                final ClanManager clan = ClanManager.getClanName(p.c.clan.clanName);
                if (clan == null) {
                    p.c.place.chatNPC(p, (short) b1, "Gia tộc lỗi");
                    return;
                }
                p.upExpClan(util.nextInt(1, 10 + clan.level));
                p.upluongMessage(50 * clan.level);
                p.c.upyenMessage(500000 * clan.level);
                p.c.place.chatNPC(p, (short) b1, "Điểm danh mỗi ngày sẽ nhận được các phần quà giá trị");
                break;
            }
            // Chiến trường
            case 25: {
                    Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    if ((b2 == 2) && (hour == 10 || hour == 20)) {
                        switch (b3) {
                            case 0: {
                                if (p.c.typeCT == 5) {
                                    p.c.place.chatNPC(p, (short)b1, "Mi đang ở phe hắc giả, không thể đổi phe");
                                    return;
                                }
                                p.c.changePk(p.c, (byte) 4);
                                Map ma = Manager.getMapid(98);
                                for (Place area : ma.area) {
                                    if (area.numplayers < ma.template.maxplayers) {
                                        p.c.place.leave(p);
                                        area.EnterMap0(p.c);
                                        return;
                                    }
                                }
                                break;
                            }
                            case 1: {
                                if (p.c.typeCT == 4) {
                                    p.c.place.chatNPC(p, (short)b1, "Mi đang ở phe bạch giả, không thể đổi phe");
                                    return;
                                }
                                p.c.changePk(p.c, (byte) 5);
                                Map ma = Manager.getMapid(104);
                                for (Place area : ma.area) {
                                    if (area.numplayers < ma.template.maxplayers) {
                                        p.c.place.leave(p);
                                        area.EnterMap0(p.c);
                                        return;
                                    }
                                }
                                break;
                            }
                            case 2: {
                                Service.rewardCT(p.c);
                                break;
                            }
                        }
                        break;
                    } else {
                        if (b2 == 2) {
                            switch (b3) {
                                case 2: {
                                    Service.rewardCT(p.c);
                                    break;
                                }
                            }
                        }
                        p.c.place.chatNPC(p, (short) b1, "Chưa đến giờ chiến trường.");
                        break;
                    }
                }
            //Menu npc Goosho
            case 26:
                if (b2 == 0) {
                    p.requestItem(14);
                    break;
                } else if (b2 == 1) {
                    p.requestItem(15);
                    break;
                } else if (b2 == 2) {
                    p.requestItem(32);
                } else if (b2 == 3) {
                    p.requestItem(34);
                }
                break;
            //Menu npc Rakkii
            case 30:
                switch (b2) {
                    case 0:
                        p.requestItem(38);
                        break;
                    case 2:
                        if (b3 == 0) {
                            server.manager.rotationluck[0].luckMessage(p);
                        } else if (b3 == 2) {
                            server.manager.sendTB(p, "Vòng xoay vip", "Tham gia đi xem luật lm gì");
                        }
                        break;
                    case 3:
                        if (b3 == 0) {
                            server.manager.rotationluck[1].luckMessage(p);
                        } else if (b3 == 2) {
                            server.manager.sendTB(p, "Vòng xoay thường", "Tham gia đi xem luật lm gì");
                        }
                        break;
                }
                break;
            //menu npc Kagai
            case 32:
                switch (b2) {
                    case 4:
                        if (b3 == 1) {
                            p.requestItem(44);
                        } else if (b3 == 2) {
                            p.requestItem(45);
                        }
                        break;
                }
                break;
            //menu npc Tiên nữ
            case 33: {
                if (p.typemenu != 33) {
                    break;
                }
                switch (this.server.manager.event) {
                    case 1: {
                        switch (b2) {
                            case 0: {
                                if (p.c.quantityItemyTotal(609) < 1 || p.c.quantityItemyTotal(610) < 1 || p.luong < 20 || p.c.xu < 20000 || p.c.yen < 100000) {
                                    p.c.place.chatNPC(p, (short) b1, "Hành trang của con không có đủ nguyên liệu hoặc không đủ tiền");
                                    break;
                                }
                                if (p.c.getBagNull() == 0) {
                                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                                    break;
                                }
                                p.upluongMessage(-20);
                                p.c.upxuMessage(-20000);
                                p.c.upyenMessage(-100000);
                                final Item it = ItemData.itemDefault(611);
                                p.c.addItemBag(true, it);
                                p.c.removeItemBags(609, 1);
                                p.c.removeItemBags(610, 1);
                                break;
                            }
                            case 1: {
                                if (p.c.quantityItemyTotal(607) < 1 || p.c.quantityItemyTotal(608) < 1 || p.c.quantityItemyTotal(617) < 1 || p.luong < 50 || p.c.xu < 50000 || p.c.yen < 100000) {
                                    p.c.place.chatNPC(p, (short) b1, "Hành trang của con không có đủ nguyên liệu hoặc không đủ tiền");
                                    break;
                                }
                                if (p.c.getBagNull() == 0) {
                                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                                    break;
                                }
                                p.upluongMessage(-50);
                                p.c.upxuMessage(-50000);
                                p.c.upyenMessage(-100000);
                                final Item it = ItemData.itemDefault(612);
                                p.c.addItemBag(true, it);
                                p.c.removeItemBags(607, 1);
                                p.c.removeItemBags(608, 1);
                                p.c.removeItemBags(617, 1);
                                break;
                            }
                            case 2: {
                                if (p.c.quantityItemyTotal(611) < 1 || p.c.quantityItemyTotal(612) < 1 || p.c.quantityItemyTotal(661) < 1 || p.luong < 10 || p.c.xu < 10000 || p.c.yen < 10000) {
                                    p.c.place.chatNPC(p, (short) b1, "Hành trang của con không có đủ nguyên liệu hoặc không đủ tiền");
                                    break;
                                }
                                if (p.c.getBagNull() == 0) {
                                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                                    break;
                                }
                                p.upluongMessage(-10);
                                p.c.upxuMessage(-10000);
                                p.c.upyenMessage(-10000);
                                final Item it = ItemData.itemDefault(606);
                                p.c.addItemBag(true, it);
                                p.c.removeItemBags(611, 1);
                                p.c.removeItemBags(612, 1);
                                p.c.removeItemBags(661, 1);
                                break;
                            }
                            case 3: {
                                server.manager.sendTB(p, "Top sự kiện", BXHManager.getStringBXH(4));
                                break;
                            }
                            case 4: {
                                server.manager.sendTB(p, "Hướng dẫn sự kiện", "1) Kẹo Táo\n - Trong thời gian diễn ra sự kiện tiêu diệt quái các bạn sẽ có cơ hội nhận được Quả Táo và Mật Ong.\n - Mang 2 vật phẩm này tới gặp NPC Tiên nữ sẽ làm được Kẹo Táo với chi phí 20 lượng + 20k xu + 100k yên.\n2) Hộp Ma Quỷ\n - Trong thời gian diễn ra sự kiện tiêu diệt quái các bạn sẽ có cơ hội nhận được Xương Thú, Tàn linh và Ma vật.\n - Các bạn có thể tới gặp NPC Tiên nữ để làm Hộp ma quỷ với các nguyên liệu Xương Thú, Tàn Linh và Ma vật với chi phí 50 Lượng + 50k xu + 100k yên.\n3) Thu thập linh hồn\n - Trong thời gian diễn ra sự kiện. Sau khi hạ gục 1 người trong người trong map chiến trường sẽ thu thập được truy tung lệnh. Sử dụng 1 Truy tung lệnh + 1 Kẹo táo + 1 Hộp ma quái để đổi lấy bí ma tại NPC tiên nữ với chi phí 10 Lượng + 10k xu + 10k yên.\n  Sử dụng bí ma sẽ nhận được phần thưởng kinh nghiệm và một phần quà may mắn.");
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        p.c.place.chatNPC(p, (short) b1, "Hi\u1ec7n t\u1ea1i ch\u01b0a c\u00f3 s\u1ef1 ki\u1ec7n di\u1ec5n ra");
                        break;
                    }
                }
                break;
            }

            case 35: {
                if (p.typemenu != 35) {
                    break;
                }
                switch (b2) {
                    case 0: {
                        p.typemenu = ((b2 == 0) ? 157 : 158);
                        doMenuArray(p, new String[]{"Chơi bằng xu", "Chơi bằng lượng"});
                        break;
                    }
                    case 1: {
                        server.manager.sendTB(p, "Luật chơi", "Để Tham gia bạn cần có đủ lượng hoặc xu theo tùy chọn của bạn.\n - Khi tham gia bạn có 2 quyền lựa chọn chẵn hoặc lẻ. Tỉ lệ trứng thưởng sẽ là 50:50. Nếu may mắn bạn sẽ ngay lập tức nhận được 190% số lượng or lượng đã đặt cược. Nếu không bạn sẽ mất toàn bộ số lượng đã đặt cược.\n - Chúc bạn may mắn");
                        break;
                    }
                }
                break;
            }
            case 157: {
                switch (b2) {
                    case 0: {
                        p.typemenu = ((b2 == 0) ? 159 : 160);
                        doMenuArray(p, new String[]{"10 Triệu xu", "50 Triệu xu", "100 Triệu xu"});
                        break;
                    }
                    case 1: {
                        p.typemenu = ((b2 == 1) ? 161 : 162);
                        doMenuArray(p, new String[]{"10k Lượng", "50k Lượng", "100k Lượng"});
                        break;
                    }
                }
                break;
            }
            case 159: {
                switch (b2) {
                    case 0: {
                        p.typemenu = ((b2 == 0) ? 163 : 164);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                    case 1: {
                        p.typemenu = ((b2 == 1) ? 165 : 166);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                    case 2: {
                        p.typemenu = ((b2 == 2) ? 167 : 168);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                }
                break;
            }
            case 163: {
                switch (b2) {
                    case 0: {
                        if (p.c.xu > 10000000) {
                            p.c.upxuMessage(-10000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(19000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.c.xu > 10000000) {
                            p.c.upxuMessage(-10000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(19000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            case 165: {
                switch (b2) {
                    case 0: {
                        if (p.c.xu > 50000000) {
                            p.c.upxuMessage(-50000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(95000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.c.xu > 50000000) {
                            p.c.upxuMessage(-50000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(95000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            case 167: {
                switch (b2) {
                    case 0: {
                        if (p.c.xu > 100000000) {
                            p.c.upxuMessage(-100000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(190000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.c.xu > 100000000) {
                            p.c.upxuMessage(-100000000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.c.upxuMessage(190000000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt xu");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có xu mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            case 161: {
                switch (b2) {
                    case 0: {
                        p.typemenu = ((b2 == 0) ? 168 : 169);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                    case 1: {
                        p.typemenu = ((b2 == 1) ? 170 : 171);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                    case 2: {
                        p.typemenu = ((b2 == 2) ? 172 : 173);
                        doMenuArray(p, new String[]{"Chẵn", "Lẻ"});
                        break;
                    }
                }
                break;
            }
            case 168: {
                switch (b2) {
                    case 0: {
                        if (p.luong > 10000) {
                            p.upluongMessage(-10000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.upluongMessage(19000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa chẵn HỐT 20k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                                server.manager.chatKTG("NOEL vừa húp của con nghiện " + p.c.name + " 10k lượng NGON VÃI LỒN ");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.luong > 10000) {
                            p.upluongMessage(-10000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.upluongMessage(19000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa lẻ HỐT 20k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                                server.manager.chatKTG("NOEL vừa húp của con nghiện " + p.c.name + " 10k lượng NGON VÃI LỒN ");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            case 170: {
                switch (b2) {
                    case 0: {
                        if (p.luong > 50000) {
                            p.upluongMessage(-50000);
                            int x = util.nextInt(3);
                            if (x == 1) {
                                p.upluongMessage(95000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa chẵn HỐT 100k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                                server.manager.chatKTG("NOEL vừa húp của con nghiện " + p.c.name + " 50k lượng NGON VÃI LỒN ");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.luong > 50000) {
                            p.upluongMessage(-50000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.upluongMessage(95000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa lẻ HỐT 100k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                                server.manager.chatKTG("NOEL vừa húp của con nghiện " + p.c.name + " 50k lượng NGON VÃI LỒN ");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            case 172: {
                switch (b2) {
                    case 0: {
                        if (p.luong > 100000) {
                            p.upluongMessage(-100000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.upluongMessage(190000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa chẵn HỐT 200k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                    case 1: {
                        if (p.luong > 100000) {
                            p.upluongMessage(-100000);
                            int x = util.nextInt(2);
                            if (x == 1) {
                                p.upluongMessage(190000);
                                p.c.place.chatNPC(p, (short) b1, "Hốt lượng");
                                server.manager.chatKTG("Con nghiện " + p.c.name + " vừa đặt cửa lẻ HỐT 200k lượng của NOEL Hên Vờ Lờ ");
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Còn gì nữa đâu mà khóc với sầu");
                                server.manager.chatKTG("NOEL vừa húp của con nghiện " + p.c.name + " 100k lượng NGON VÃI LỒN ");
                            }
                        } else {
                            p.c.place.chatNPC(p, (short) b1, "Không có lượng mà đòi chơi");
                            break;
                        }
                        break;
                    }
                }
                break;
            }
            // npc vua hung
            case 36: {
                p.typemenu = ((b2 == 0) ? 155 : 156);
                doMenuArray(p, new String[]{"Đổi lượng", "Đổi ngọc", "Nâng mắt", "Đổi đồ Jirai", "Đổi đồ Jumito", "Đổi bí kíp", "Nâng áo choàng", "Đổi vũ khí"});
                break;
            }
            case 155: {
                switch (b2) {
                    case 0: {
                        p.typemenu = ((b2 == 0) ? 107 : 108);
                        doMenuArray(p, new String[]{"Đổi 15k lượng ra 300 triệu yên", "Đổi 100 triêu yên ra 10 triệu xu", "Đổi 300 triệu xu ra 5k lượng"});
                        break;
                    }
                    case 1: {
                        p.typemenu = ((b2 == 1) ? 109 : 110);
                        doMenuArray(p, new String[]{"Đổi huyền tinh ngọc", "Đổi huyết ngọc", "Đổi lam tinh ngọc", "Đổi lục ngọc", "Nâng ngọc"});
                        break;
                    }
                    case 2: {
                        p.typemenu = ((b2 == 2) ? 111 : 112);
                        doMenuArray(p, new String[]{"Nâng mắt 1", "Nâng mắt 2", "Nâng mắt 3", "Nâng mắt 4", "Nâng mắt 5", "Nâng mắt 6", "Nâng mắt 7", "Nâng mắt 8", "Nâng mắt 9", "Nâng mắt 10"});
                        break;
                    }
                    case 3: {
                        p.typemenu = ((b2 == 3) ? 113 : 114);
                        doMenuArray(p, new String[]{"Đổi giày Jirai", "Đổi phù Jirai", "Đổi quần Jirai", "Đổi bội Jirai", "Đổi găng Jirai", "Đổi nhẫn Jirai", "Đổi áo Jirai", "Đổi dây chuyền Jirai", "Đổi nón Jirai", "Đổi mặt nạ Jirai"});
                        break;
                    }
                    case 4: {
                        p.typemenu = ((b2 == 4) ? 115 : 116);
                        doMenuArray(p, new String[]{"Đổi giày Jumito", "Đổi phù Jumito", "Đổi quần Jumito", "Đổi bội Jumito", "Đổi găng Jumito", "Đổi nhẫn Jumito", "Đổi áo Jumito", "Đổi dây chuyền Jumito", "Đổi nón Jumito", "Đổi mặt nạ Jumito"});
                        break;
                    }
                    case 5: {
                        p.typemenu = ((b2 == 5) ? 117 : 118);
                        doMenuArray(p, new String[]{"Bí kíp kiếm", "Bí kíp Tiêu", "Bí kíp Đao", "Bí kíp Quạt", "Bí kíp Kunia", "Bí kíp Cung"});
                        break;
                    }
                    case 6: {
                        p.typemenu = ((b2 == 6) ? 122 : 123);
                        doMenuArray(p, new String[]{"Nâng cấp 1", "Nâng cấp 2", "Nâng cấp 3", "Nâng cấp 4", "Nâng cấp 5", "Nâng cấp 6", "Nâng cấp 7", "Nâng cấp 8", "Nâng cấp 9", "Nâng cấp 10", "Nâng cấp 11", "Nâng cấp 12", "Nâng cấp 13", "Nâng cấp 14", "Nâng cấp 15", "Nâng cấp 16"});
                        break;
                    }
                    case 7: {
                        p.typemenu = ((b2 == 7) ? 124 : 125);
                        doMenuArray(p, new String[]{"Vũ khí vip", "Vũ khí siêu vip", "Vũ khí siêu cấp vip", "Vũ khí siêu cấp vip Pro"});
                        break;
                    }

                }
                break;
            }
            case 107: {
                switch (b2) {
                    case 0: {
                        if (p.luong > 15000) {
                            p.upluongMessage(-15000L);
                            p.c.upyenMessage(300000000);
                            return;
                        }
                        p.c.place.chatNPC(p, (short) b1, "Có lượng đâu mà đổi");
                        break;
                    }
                    case 1: {
                        if (p.c.yen < 100000000) {
                            p.c.place.chatNPC(p, (short) b1, "Kiếm thêm yên rồi đổi");
                            return;
                        } else {
                            p.c.upyenMessage(-100000000);
                            p.c.upxuMessage(10000000);
                            break;
                        }
                    }
                    case 2: {
                        if (p.c.xu < 300000000) {
                            p.c.place.chatNPC(p, (short) b1, "Kiếm thêm xu rồi đổi");
                            return;
                        } else {
                            p.c.upxuMessage(-300000000);
                            p.upluongMessage(5000);
                            break;
                        }
                    }
                }
                break;
            }
            case 109: {
                switch (b2) {
                    case 0: {
                        if (p.c.quantityItemyTotal(648) < 10) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 10 Huy chương chiến công đồng");
                            break;
                        } else if (p.luong < 500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 500 lượng");
                            break;
                        } else {

                            final Item itemup = ItemData.itemDefault(652);
                            p.upluongMessage(-500);
                            itemup.upgrade = 1;
                            p.c.removeItemBags(648, 10);
                            p.c.addItemBag(false, itemup);
                            break;

                        }
                    }
                    case 1: {
                        if (p.c.quantityItemyTotal(649) < 10) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 10 Huy chương chiến công bạc");
                            break;
                        } else if (p.luong < 500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 500 lượng");
                            break;
                        } else {
                            final Item itemup = ItemData.itemDefault(653);
                            p.upluongMessage(-500);
                            itemup.upgrade = 1;
                            p.c.removeItemBags(649, 10);
                            p.c.addItemBag(false, itemup);
                            break;
                        }
                    }
                    case 2: {
                        if (p.c.quantityItemyTotal(650) < 10) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 10 Huy chương chiến công vàng");
                            break;
                        } else if (p.luong < 500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 500 lượng");
                            break;
                        } else {
                            final Item itemup = ItemData.itemDefault(654);
                            p.upluongMessage(-500);
                            itemup.upgrade = 1;
                            p.c.removeItemBags(650, 10);
                            p.c.addItemBag(false, itemup);
                            break;
                        }
                    }
                    case 3: {
                        if (p.c.quantityItemyTotal(651) < 10) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 10 Huy chương chiến công bạch kim");
                            break;
                        } else if (p.luong < 500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 500 lượng");
                            break;
                        } else {
                            final Item itemup = ItemData.itemDefault(655);
                            p.upluongMessage(-500);
                            itemup.upgrade = 1;
                            p.c.removeItemBags(651, 10);
                            p.c.addItemBag(false, itemup);
                            break;
                        }
                    }
                }
                break;
            }
            case 111: {
//                p.c.place.chatNPC(p, (short) b1, "Tạm bảo trì chức năng này");
//               break;
                switch (b2) {
                    case 0: {
                        if (p.c.quantityItemyTotal(695) < 100) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 100 đá danh vọng 1");
                            break;
                        } else if (p.luong < 3000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-3000);
                                p.c.removeItemBags(695, 100);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 1 rồi");
                                final Item itemup = ItemData.itemDefault(685);
                                itemup.upgrade = 1;
                                p.upluongMessage(-3000);
                                p.c.removeItemBags(695, 100);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 1: {
                        if (p.c.quantityItemyTotal(695) < 500 || p.c.quantityItemyTotal(685) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 500 đá danh vọng 1 và mắt 1");
                            break;
                        } else if (p.luong < 3500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3500 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-3500);
                                p.c.removeItemBags(695, 500);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 2 rồi");
                                final Item itemup = ItemData.itemDefault(686);
                                itemup.upgrade = 2;
                                p.upluongMessage(-3500);
                                p.c.removeItemBags(695, 500);
                                p.c.removeItemBags(685, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 2: {
                        if (p.c.quantityItemyTotal(695) < 1000 || p.c.quantityItemyTotal(686) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 1000 đá danh vọng 1 và mắt 2");
                            break;
                        } else if (p.luong < 4000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 4000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-4000);
                                p.c.removeItemBags(695, 1000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 3 rồi");
                                final Item itemup = ItemData.itemDefault(687);
                                itemup.upgrade = 3;
                                p.upluongMessage(-4000);
                                p.c.removeItemBags(695, 1000);
                                p.c.removeItemBags(686, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 3: {
                        if (p.c.quantityItemyTotal(695) < 1500 || p.c.quantityItemyTotal(687) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 1500 đá danh vọng 1 và mắt 3");
                            break;
                        } else if (p.luong < 4500) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 4500 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-4500);
                                p.c.removeItemBags(695, 1500);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 4 rồi");
                               final Item itemup = ItemData.itemDefault(688);
                                itemup.upgrade = 4;
                                p.upluongMessage(-4500);
                                p.c.removeItemBags(695, 1500);
                                p.c.removeItemBags(687, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 4: {
                        if (p.c.quantityItemyTotal(695) < 2000 || p.c.quantityItemyTotal(688) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 2000 đá danh vọng 1 và mắt 4");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 2000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 5 rồi");
                                server.manager.chatKTG("Trùm " + p.c.name + " đã đổi thành công mắt 5 KHÁ GIÀU ");
                                final Item itemup = ItemData.itemDefault(689);
                                itemup.upgrade = 5;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 2000);
                                p.c.removeItemBags(688, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 5: {
                        if (p.c.quantityItemyTotal(695) < 2500 || p.c.quantityItemyTotal(689) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 2500 đá danh vọng 1 và mắt 5");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 2500);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 6 rồi");
                                final Item itemup = ItemData.itemDefault(690);
                                itemup.upgrade = 6;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(689, 1);
                                p.c.removeItemBags(695, 2500);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 6: {
                        if (p.c.quantityItemyTotal(695) < 3000 || p.c.quantityItemyTotal(690) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3000 đá danh vọng 1 và mắt 6");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 7 rồi");
                                final Item itemup = ItemData.itemDefault(691);
                                itemup.upgrade = 7;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(690, 1);
                                p.c.removeItemBags(695, 3000);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 7: {
                        if (p.c.quantityItemyTotal(695) < 3000 || p.c.quantityItemyTotal(691) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3000 đá danh vọng 1 và mắt 7");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 8 rồi");
                                final Item itemup = ItemData.itemDefault(692);
                                itemup.upgrade = 8;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                p.c.removeItemBags(691, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 8: {
                        if (p.c.quantityItemyTotal(695) < 3000 || p.c.quantityItemyTotal(692) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3000 đá danh vọng 1 và mắt 8");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 9 rồi");
                                final Item itemup = ItemData.itemDefault(693);
                                itemup.upgrade = 9;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                p.c.removeItemBags(692, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 9: {
                        if (p.c.quantityItemyTotal(695) < 3000 || p.c.quantityItemyTotal(693) < 1) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 3000 đá danh vọng 1 và mắt 9");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mắt 10 rồi");
                                server.manager.chatKTG("Trùm " + p.c.name + " đã đổi thành công mắt 10 GIÀU VÃI LỒN ");
                                final Item itemup = ItemData.itemDefault(694);
                                itemup.upgrade = 10;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(695, 3000);
                                p.c.removeItemBags(693, 1);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case 113: {
 //               p.c.place.chatNPC(p, (short) b1, "Tạm bảo trì chức năng này");
   //             break;
                switch (b2) {
                    case 0: {
                        if (p.c.quantityItemyTotal(737) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh giày Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(737, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có giày Jirai rồi");
                                final Item itemup = ItemData.itemDefault(748, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(737, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 1: {
                        if (p.c.quantityItemyTotal(740) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh phù Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(740, 150);
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có phù Jirai rồi");
                                final Item itemup = ItemData.itemDefault(750, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(740, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 2: {
                        if (p.c.quantityItemyTotal(736) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh quần Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(736, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có quần Jirai rồi");
                                final Item itemup = ItemData.itemDefault(713, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(736, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 3: {
                        if (p.c.quantityItemyTotal(739) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh ngọc bội Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(739, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có bội Jirai rồi");
                                final Item itemup = ItemData.itemDefault(751, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(739, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 4: {
                        if (p.c.quantityItemyTotal(734) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh găng Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(734, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có găng Jirai rồi");
                                final Item itemup = ItemData.itemDefault(747, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(734, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 5: {
                        if (p.c.quantityItemyTotal(741) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh nhẫn Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(741, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có nhẫn Jirai rồi");
                                final Item itemup = ItemData.itemDefault(749, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(741, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 6: {
                        if (p.c.quantityItemyTotal(735) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh áo Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(735, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có áo Jirai rồi");
                                final Item itemup = ItemData.itemDefault(712, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(735, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 7: {
                        if (p.c.quantityItemyTotal(738) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh dây chuyền Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(738, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có dây chuyền Jirai rồi");
                                final Item itemup = ItemData.itemDefault(752, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(738, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 8: {
                        if (p.c.quantityItemyTotal(733) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh nón Jirai");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(733, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có nón Jirai rồi");
                                final Item itemup = ItemData.itemDefault(746, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(733, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 9: {
                        if (p.c.quantityItemyTotal(684) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 cỏ bốn lá");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(684, 150);
                                break;
                            } else {
                                 p.c.place.chatNPC(p, (short) b1, "Ngon. Có mặt nạ Jirai rồi");
                                final Item itemup = ItemData.itemDefault(711, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(684, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case 115: {
 //               p.c.place.chatNPC(p, (short) b1, "Tạm bảo trì chức năng này");
   //             break;
                switch (b2) {
                    case 0: {
                        if (p.c.quantityItemyTotal(764) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh giày Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(764, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có giày Jumito rồi");
                                final Item itemup = ItemData.itemDefault(755, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(764, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 1: {
                        if (p.c.quantityItemyTotal(767) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh phù Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(767, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có phù Jumito rồi");
                                final Item itemup = ItemData.itemDefault(757, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(767, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 2: {
                        if (p.c.quantityItemyTotal(763) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh quần Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(763, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có quần Jumito rồi");
                                final Item itemup = ItemData.itemDefault(716, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(763, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 3: {
                        if (p.c.quantityItemyTotal(766) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh ngọc bội Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(766, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có bội Jumito rồi");
                                final Item itemup = ItemData.itemDefault(758, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(766, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 4: {
                        if (p.c.quantityItemyTotal(761) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh găng Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(761, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có găng Jumito rồi");
                                final Item itemup = ItemData.itemDefault(754, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(761, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 5: {
                        if (p.c.quantityItemyTotal(768) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh nhẫn Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(768, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có nhẫn Jumito rồi");
                                final Item itemup = ItemData.itemDefault(756, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(768, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 6: {
                        if (p.c.quantityItemyTotal(762) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh áo Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(762, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có áo Jumito rồi");
                                final Item itemup = ItemData.itemDefault(715, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(762, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 7: {
                        if (p.c.quantityItemyTotal(765) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh dây chuyền Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(765, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có dây chuyền Jumito rồi");
                                final Item itemup = ItemData.itemDefault(759, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(765, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 8: {
                        if (p.c.quantityItemyTotal(760) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 mảnh nón Jumito");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(760, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có nón Jumito rồi");
                                final Item itemup = ItemData.itemDefault(753, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(760, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                    case 9: {
                        if (p.c.quantityItemyTotal(684) < 150) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 150 cỏ bốn lá");
                            break;
                        } else if (p.luong < 5000) {
                            p.c.place.chatNPC(p, (short) b1, "Cần 5000 lượng");
                            break;
                        } else {
                            int tl = util.nextInt(3);
                            if (tl != 1) {
                                p.c.place.chatNPC(p, (short) b1, "Đen vl. Xịt rồi");
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(684, 150);
                                break;
                            } else {
                                p.c.place.chatNPC(p, (short) b1, "Ngon. Có mặt nạ Jumito rồi");
                                final Item itemup = ItemData.itemDefault(714, (byte) util.nextInt(1, 3));
                                itemup.upgrade = 16;
                                p.upluongMessage(-5000);
                                p.c.removeItemBags(684, 150);
                                p.c.addItemBag(false, itemup);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case 92:
                p.typemenu = ((b2 == 0) ? 93 : 94);
                doMenuArray(p, new String[]{"Thông tin", "Luật chơi"});
                break;
            case 93:
                if (b2 == 0) {
                    server.manager.rotationluck[0].luckMessage(p);
                } else if (b2 == 1) {
                    server.manager.sendTB(p, "Vòng xoay vip", "Tham gia đi xem luật lm gì");
                }
                break;
            case 94:
                if (b2 == 0) {
                    server.manager.rotationluck[1].luckMessage(p);
                } else if (b2 == 1) {
                    server.manager.sendTB(p, "Vòng xoay thường", "Tham gia đi xem luật lm gì");
                }
                break;
            case 95:
                break;
            case 120:
                if (b2 > 0 && b2 < 7) {
                    p.Admission(b2);
                }
                break;
            default:
                p.c.place.chatNPC(p, (short) b1, "Chức năng này đang cập nhật");
                break;

        }
        util.Debug("byte1 " + b1 + " byte2 " + b2 + " byte3 " + b3);
    }

    public void openUINpc(Player p, Message m) throws IOException {
        short idnpc = m.reader().readShort();//idnpc
        m.cleanup();
        p.c.typemenu = 0;
        p.typemenu = idnpc;
        if (idnpc == 0 && p.c.mapid == 110) {
            this.doMenuArray(p, new String[]{"Rời khỏi nơi này", "Đặt cược", "Nói chuyện"});
            return;
        }
        if (idnpc == 33) {
            switch (server.manager.event) {
                case 1:
                    doMenuArray(p, new String[]{"Làm Kẹo Táo", "Làm Hộp Ma Quái", "Làm Bí Ma", "Xếp Hạng", "Hướng dẫn"});
                    return;
                case 2:
                    doMenuArray(p, new String[]{"Hộp bánh thường", "Hộp bánh vip", "Bánh thập cẩm", "Bánh Dẻo", "Đậu xanh", "Bánh pía"});
                    return;
            }
        }
        if (idnpc == 35) {
            p.c.typemenu = 1;
            this.doMenuArray(p, new String[]{"Chẵn lẻ", "Luật chơi"});
            return;
        }
        if (idnpc
                == 16) {
            p.c.typemenu = 1;
            this.doMenuArray(p, new String[]{"Bắt đầu"});
            return;

        }
        m = new Message(40);
        if (idnpc == 12) {
            m.writer().writeUTF("Mở thêm 6 ô rương");
        }

        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    public void doMenuArray(Player p, String[] menu) throws IOException {
        Message m = new Message(63);
        for (byte i = 0; i < menu.length; i++) {
            m.writer().writeUTF(menu[i]);//menu
        }
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
    }

    public void sendWrite(Player p, short type, String title) {
        try {
            Message m = new Message(92);
            m.writer().writeUTF(title);
            m.writer().writeShort(type);
            m.writer().flush();
            p.conn.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }
}

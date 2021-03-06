package real;

import boardGame.Place;
import io.Message;
import java.io.IOException;
import static real.ItemData.ItemDataId;
import static real.ItemData.isTypeBody;
import static real.ItemData.isTypeMounts;
import server.GameScr;
import server.Manager;
import server.Server;
import server.util;

/**
 *
 * @author Ghost
 */
public class useItem {

    static Server server = Server.getInstance();
    static final int[] arrOp = new int[]{6, 7, 10, 67, 68, 69, 70, 71, 72, 73, 74};
    static final int[] arrParam = new int[]{50, 50, 10, 5, 10, 10, 5, 5, 5, 100, 50};
    private static final byte[] arrOpenBag = new byte[]{0, 6, 6, 12};

    public static void uesItem(Player p, Item item, byte index) throws IOException {
        if (ItemDataId(item.id).level > p.c.get().level) {
            return;
        }
        Map map;
        ItemData data = ItemDataId(item.id);
        if (data.gender != 2 && data.gender != p.c.gender) {
            return;
        }
        if (data.type == 26) {
            p.sendAddchatYellow("Vật phẩm liên quan đến nâng cấp, hãy gặp Kenshinto trong làng để sử dụng.");
            return;
        } else if ((p.c.get().nclass == 0 && item.id == 547) || (data.nclass > 0 && ((p.c.get().nclass != 0  && data.nclass != p.c.get().nclass) || (p.c.get().nclass == 0 && data.nclass != 1)))) {
            p.sendAddchatYellow("Môn phái không phù hợp");
            return;
        } else if (p.c.isNhanban && item.id == 547) {
            p.sendAddchatYellow("Chức năng này không thể sử dụng cho phân thân");
            return;
        } else if (isTypeBody(item.id)) {
            item.isLock = true;
            Item itemb = p.c.get().ItemBody[data.type];
            p.c.ItemBag[index] = itemb;
            p.c.get().ItemBody[data.type] = item;
            if (data.type == 10) {
                p.mobMeMessage(0, (byte) 0);
            }
            if (itemb != null && itemb.id == 569) {
                p.removeEffect(36);
            }
            switch (item.id) {
                case 246:
                    p.mobMeMessage(70, (byte) 0);
                    break;
                case 419:
                    p.mobMeMessage(122, (byte) 0);
                    break;
                case 568:
                    p.setEffect(38, 0, (int) (item.expires - System.currentTimeMillis()), 0);
                    p.mobMeMessage(205, (byte) 0);
                    break;
                case 569:
                    p.setEffect(36, 0, (int) (item.expires - System.currentTimeMillis()), p.c.get().getPramItem(99));
                    p.mobMeMessage(206, (byte) 0);
                    break;
                case 570:
                    p.setEffect(37, 0, (int) (item.expires - System.currentTimeMillis()), 0);
                    p.mobMeMessage(207, (byte) 0);
                    break;
                case 571:
                    p.setEffect(39, 0, (int) (item.expires - System.currentTimeMillis()), 0);
                    p.mobMeMessage(208, (byte) 0);
                    break;
                case 583:
                    p.mobMeMessage(211, (byte) 1);
                    break;
                case 584:
                    p.mobMeMessage(212, (byte) 1);
                    break;
                case 585:
                    p.mobMeMessage(213, (byte) 1);
                    break;
                case 586:
                    p.mobMeMessage(214, (byte) 1);
                    break;
                case 587:
                    p.mobMeMessage(215, (byte) 1);
                    break;
                case 588:
                    p.mobMeMessage(216, (byte) 1);
                    break;
                case 589:
                    p.mobMeMessage(217, (byte) 1);
                    break;
                case 742:
                    p.mobMeMessage(229, (byte) 1);
                    break;
                case 781:
                    p.mobMeMessage(235, (byte) 1);
                    break;
                default:
                    break;
            }

        } else if (isTypeMounts(item.id)) {
            byte idM = (byte) (data.type - 29);
            Item itemM = p.c.get().ItemMounts[idM];
            if (idM == 4) {
                if (p.c.get().ItemMounts[0] != null || p.c.get().ItemMounts[1] != null || p.c.get().ItemMounts[2] != null || p.c.get().ItemMounts[3] != null) {
                    p.conn.sendMessageLog("Bạn cần phải tháo trang bị thú cưới đang sử dụng");
                    return;
                }
                if (!item.isLock) {
                    for (byte i = 0; i < 4; i++) {
                        int op = -1;
                        while (true) {
                            op = util.nextInt(arrOp.length);
                            for (Option option : item.options) {
                                if (arrOp[op] == option.id) {
                                    op = -1;
                                    break;
                                }
                            }
                            if (op != -1) {
                                break;
                            }
                        }
                        int idOp = arrOp[op];
                        int par = arrParam[op];
                        if (item.isExpires) {
                            par *= 10;
                        }
                        Option option = new Option(idOp, par);
                        item.options.add(option);
                    }
                }
            } else if (p.c.get().ItemMounts[4] == null) {
                p.conn.sendMessageLog("Bạn cần có thú cưới để sử dụng");
                return;
            }
            item.isLock = true;
            p.c.ItemBag[index] = itemM;
            p.c.get().ItemMounts[idM] = item;
        }
        if (data.skill > 0) {
            byte skill = data.skill;
            if (item.id == 547) {
                skill += p.c.get().nclass;
            }
            p.openBookSkill(index, skill);
            return;
        }
        byte numbagnull = p.c.getBagNull();
        Item itemup;
        switch (item.id) {
            case 13:
                if (p.buffHP(25)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 14:
                if (p.buffHP(90)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 15:
                if (p.buffHP(230)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 16:
                if (p.buffHP(400)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 17:
                if (p.buffHP(650)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 565:
                if (p.buffHP(1500)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 18:
                if (p.buffMP(150)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 19:
                if (p.buffMP(500)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 20:
                if (p.buffMP(1000)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 21:
                if (p.buffMP(2000)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 22:
                if (p.buffMP(3500)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 566:
                if (p.buffMP(5000)) {
                    p.c.removeItemBag(index, 1);
                }
                return;
            case 23:
                if (p.dungThucan((byte) 0, 3, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 24:
                if (p.dungThucan((byte) 1, 20, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 25:
                if (p.dungThucan((byte) 2, 30, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 26:
                if (p.dungThucan((byte) 3, 40, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 27:
                if (p.dungThucan((byte) 4, 50, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 29:
                if (p.dungThucan((byte) 28, 60, 60 * 30)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 30:
                if (p.dungThucan((byte) 28, 60, 60 * 60 * 24 * 3)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 34:
            case 36:
                map = Manager.getMapid(p.c.mapLTD);
                if (map != null) {
                    for (byte i = 0; i < map.area.length; i++) {
                        if (map.area[i].numplayers < map.template.maxplayers) {
                            p.c.place.leave(p);
                            map.area[i].EnterMap0(p.c);
                            if (item.id == 34) {
                                p.c.removeItemBag(index, 1);
                            }
                            return;
                        }
                    }
                }
                break;
            case 257:
                if (p.c.get().pk > 0) {
                    p.c.get().pk -= 5;
                    if (p.c.get().pk < 0) {
                        p.c.get().pk = 0;
                    }
                    p.sendAddchatYellow("Điểm hiếu chiến của bạn còn lại là " + p.c.get().pk);
                    p.c.removeItemBag(index, 1);
                } else {
                    p.sendAddchatYellow("Bạn không có điểm hiếu chiến");
                }
                break;
            case 409:
                if (p.dungThucan((byte) 30, 75, 60 * 60 * 24)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 410:
                if (p.dungThucan((byte) 31, 90, 60 * 60 * 24)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 215:
            case 229:
            case 283: {
                byte level = (byte) ((item.id != 215) ? (item.id != 229) ? 3 : 2 : 1);
                if (level > p.c.levelBag + 1) {
                    p.sendAddchatYellow("Cần mở Túi vải cấp " + (p.c.levelBag + 1) + " mới có thể mở được túi vải này");
                    return;
                }
                if (p.c.levelBag >= level) {
                    p.sendAddchatYellow("Bạn đã mở túi vải này rồi");
                    return;
                }
                p.c.levelBag = level;
                p.c.maxluggage += arrOpenBag[level];
                Item[] bag = new Item[p.c.maxluggage];
                for (short i = 0; i < p.c.ItemBag.length; i++) {
                    bag[i] = p.c.ItemBag[i];
                }
                p.c.ItemBag = bag;
                p.c.ItemBag[index] = null;
                p.openBagLevel(index);
            }
            break;
            case 272: {
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                if (util.nextInt(2) == 0) {
                    int num = util.nextInt(1000, 1500);
                    p.c.upyenMessage(num);
                    p.sendAddchatYellow("Bạn nhận được " + num + " yên");
                } else {
                    short[] arId = new short[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 242, 275, 276, 277, 278, 280, 284, 285};
                    short idI = arId[util.nextInt(arId.length)];
                    ItemData data2 = ItemData.ItemDataId(idI);
                    if (data2.type < 10) {
                        if (data2.type == 1) {
                            itemup = ItemData.itemDefault(idI);
                            itemup.sys = GameScr.SysClass(data2.nclass);
                        } else {
                            byte sys = (byte) util.nextInt(1, 3);
                            itemup = ItemData.itemDefault(idI, sys);
                        }
                    } else {
                        itemup = ItemData.itemDefault(idI);
                    }
                    itemup.isLock = item.isLock;
                    for (Option Option : itemup.options) {
                        int idOp = Option.id;
                        Option.param = util.nextInt(item.getOptionShopMin(idOp, Option.param), Option.param);
                    }
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
            }
            break;
            case 248: {
                Effect eff = p.c.get().getEffId(22);
                if (eff != null) {
                    long time = eff.timeRemove + (1000 * 60 * 60 * 5);
                    p.setEffect(22, 0, (int) (time - System.currentTimeMillis()), 2);
                } else {
                    p.setEffect(22, 0, (1000 * 60 * 60 * 5), 2);
                }
                p.c.removeItemBag(index, 1);
            }
            break;
            case 249:
                if (p.dungThucan((byte) 4, 40, 60 * 60 * 24 * 3)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 250:
                if (p.dungThucan((byte) 28, 50, 60 * 60 * 24 * 3)) {
                    p.c.removeItemBag(index, 1);
                }
                break;
            case 275: {
                p.setEffect(24, 0, (1000 * 60 * 10), 500);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 276: {
                p.setEffect(25, 0, (1000 * 60 * 10), 500);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 277: {
                p.setEffect(26, 0, (1000 * 60 * 10), 100);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 278: {
                p.setEffect(27, 0, (1000 * 60 * 10), 1000);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 280:
                if (p.c.useCave == 0) {
                    p.conn.sendMessageLog("Số lần dùng Lệnh bài hạng động trong ngày hôm nay đã hết");
                    return;
                }
                p.c.nCave++;
                p.c.useCave--;
                p.sendAddchatYellow("Số lần đi hang động của bạn trong ngày hôm nay tăng lên là " + p.c.useCave + " lần");
                p.c.removeItemBag(index, 1);
                break;
            case 282: {
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                if (util.nextInt(3) == 0) {
                    int num = util.nextInt(1000, 3000);
                    p.c.upyenMessage(num);
                    p.sendAddchatYellow("Bạn nhận được " + num + " yên");
                } else {
                    final short[] arId = {695, 695, 695, 695, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 11, 242, 275, 276, 277, 278, 280, 280, 280, 283, 284, 285, 436, 437};
                    final short idI = arId[util.nextInt(arId.length)];
                    ItemData data2 = ItemData.ItemDataId(idI);
                    if (data2.type < 10) {
                        if (data2.type == 1) {
                            itemup = ItemData.itemDefault(idI);
                            itemup.sys = GameScr.SysClass(data2.nclass);
                        } else {
                            byte sys = (byte) util.nextInt(1, 3);
                            itemup = ItemData.itemDefault(idI, sys);
                        }
                    } else {
                        itemup = ItemData.itemDefault(idI);
                    }
                    itemup.isLock = item.isLock;
                    for (Option Option : itemup.options) {
                        int idOp = Option.id;
                        Option.param = util.nextInt(item.getOptionShopMin(idOp, Option.param), Option.param);
                    }
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
            }
            break;
            case 298:
            case 299:
            case 300:
            case 301:
                if (server.manager.event != 2) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                p.updateExp(500000);
                p.c.removeItemBag(index, 1);
                break;
            case 302: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.updateExp(200000);
                if (util.nextInt(12) != 0) {
                    p.updateExp(3000000L);
                } else {
                    short[] arId = new short[]{695, 695, 695, 695, 695, 695, 695, 695, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 9, 275, 276, 277, 278, 289, 340, 340, 383, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 443, 433, 485, 485, 524, 524, 549, 550, 551, 568, 569, 570, 571, 577};
                    short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
                break;
            }
            case 303: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.updateExp(500000);
                if (util.nextInt(15) != 0) {
                    p.updateExp(4000000L);
                } else {
                    short[] arId = new short[]{695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 10, 275, 276, 277, 278, 289, 340, 340, 383, 397, 398, 399, 337, 338, 400, 401, 402, 541, 542, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 438, 443, 443, 485, 485, 524, 524, 539, 540, 454, 436, 436, 436, 437, 437, 437, 438, 385, 549, 550, 551, 568, 570, 571, 577, 648, 648, 648, 648, 648, 649, 649, 649, 649, 649, 650, 650, 650, 650, 650, 651, 651, 651, 651, 651};
                    short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
                break;
            }
            case 383:
            case 384:
            case 385: {
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                if (p.c.get().nclass == 0) {
                    p.conn.sendMessageLog("Hãy nhập học để mở vật phẩm.");
                    return;
                }
                int idI;
                byte sys = -1;
                if (util.nextInt(2) == 0) {
                    if (p.c.gender == 0) {
                        if (p.c.get().level < 50 && item.id != 384 && item.id != 385) {
                            idI = new short[]{171, 161, 151, 141, 131}[util.nextInt(5)];
                        } else if (p.c.get().level < 60 && item.id != 385) {
                            idI = new short[]{137, 163, 153, 143, 133}[util.nextInt(5)];
                        } else if (p.c.get().level < 70) {
                            idI = new short[]{330, 329, 328, 327, 326}[util.nextInt(5)];
                        } else {
                            idI = new short[]{368, 367, 366, 365, 364}[util.nextInt(5)];
                        }
                    } else {
                        if (p.c.get().level < 50 && item.id != 384 && item.id != 385) {
                            idI = new short[]{170, 160, 102, 140, 130}[util.nextInt(5)];
                        } else if (p.c.get().level < 60 && item.id != 385) {
                            idI = new short[]{172, 162, 103, 142, 132}[util.nextInt(5)];
                        } else if (p.c.get().level < 70) {
                            idI = new short[]{325, 323, 333, 319, 317}[util.nextInt(5)];
                        } else {
                            idI = new short[]{363, 361, 359, 357, 355}[util.nextInt(5)];
                        }
                    }
                } else {
                    if (util.nextInt(2) == 1) {
                        if (p.c.get().nclass == 1 || p.c.get().nclass == 2) {
                            sys = 1;
                        } else if (p.c.get().nclass == 3 || p.c.get().nclass == 4) {
                            sys = 2;
                        } else if (p.c.get().nclass == 5 || p.c.get().nclass == 6) {
                            sys = 3;
                        }
                        if (p.c.get().level < 50 && item.id != 384 && item.id != 385) {
                            idI = new short[]{97, 117, 102, 112, 107, 122}[p.c.get().nclass - 1];
                        } else if (p.c.get().level < 60 && item.id != 385) {
                            idI = new short[]{98, 118, 103, 113, 108, 123}[p.c.get().nclass - 1];
                        } else if (p.c.get().level < 70) {
                            idI = new short[]{331, 332, 333, 334, 335, 336}[p.c.get().nclass - 1];
                        } else {
                            idI = new short[]{369, 370, 371, 372, 373, 374}[p.c.get().nclass - 1];
                        }
                    } else {
                        if (p.c.get().level < 50 && item.id != 384 && item.id != 385) {
                            idI = new short[]{192, 187, 182, 177}[util.nextInt(4)];
                        } else if (p.c.get().level < 60 && item.id != 385) {
                            idI = new short[]{193, 188, 183, 178}[util.nextInt(4)];
                        } else if (p.c.get().level < 70) {
                            idI = new short[]{324, 322, 320, 318}[util.nextInt(4)];
                        } else {
                            idI = new short[]{362, 360, 358, 356}[util.nextInt(4)];
                        }
                    }
                }
                if (sys < 0) {
                    sys = (byte) util.nextInt(1, 3);
                    itemup = ItemData.itemDefault(idI, sys);
                } else {
                    itemup = ItemData.itemDefault(idI);
                }
                itemup.sys = sys;
                byte nextup = 12;
                if (item.id == 384) {
                    nextup = 14;
                } else if (item.id == 385) {
                    nextup = 16;
                }
                itemup.isLock = item.isLock;
                itemup.upgradeNext(nextup);
                p.c.addItemBag(true, itemup);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 252: {
                if (p.c.limitKyNangSo < 3) {
                    p.c.limitKyNangSo++;
                    p.c.spoint += 1;
                    p.loadSkill();
                    p.c.removeItemBag(index, 1);
                    p.sendAddchatYellow("Bạn nhận được 1 điểm kỹ năng.");
                }
                else {
                    p.sendAddchatYellow("Bạn chỉ được học 3 lần.");
                }
                break;
            }
            case 253: {
                if (p.c.limitTiemNangSo < 8) {
                    p.c.limitTiemNangSo++;
                    p.c.ppoint += 10;
                    p.loadPpoint();
                    p.c.removeItemBag(index, 1);
                    p.sendAddchatYellow("Bạn nhận được 10 điểm tiềm năng.");
                }
                else {
                    p.sendAddchatYellow("Bạn chỉ được học 8 lần.");
                }
                break;
            }
            case 308: {
                if (p.c.banhPhongLoi < 10) {
                    p.c.banhPhongLoi++;
                    p.c.spoint += 1;
                    p.loadSkill();
                    p.c.removeItemBag(index, 1);
                    p.sendAddchatYellow("Bạn nhận được 1 điểm kỹ năng.");
                }
                else {
                    p.sendAddchatYellow("Bạn chỉ được học 10 lần.");
                }
                break;
            }
            case 309: {
                if (p.c.banhBangHoa < 10) {
                    p.c.banhBangHoa++;
                    p.c.ppoint += 10;
                    p.loadPpoint();
                    p.c.removeItemBag(index, 1);
                    p.sendAddchatYellow("Bạn nhận được 10 điểm tiềm năng.");
                }
                else {
                    p.sendAddchatYellow("Bạn chỉ được học 10 lần.");
                }
                break;
            }
            case 434: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.updateExp(1500000);
                if (util.nextInt(12) != 0) {
                    p.updateExp(3000000L);
                } else {
                    final short[] arId = {695, 695, 695, 695, 695, 695, 695, 695, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 9, 275, 276, 277, 278, 289, 340, 340, 383, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 443, 433, 485, 485, 524, 524, 549, 550, 551, 568, 569, 570, 571, 577};
                    final short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    
                    // Khi mở ra ngọc sẽ tự động thành ngọc 1. Thêm ID ngọc vào list id ở trên là đc
                    if (itemup.id >= 652 && itemup.id <= 655) {
                        itemup.upgrade = 1;
                    }
                    
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
            }
            break;
            case 435: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.updateExp(3000000);
                if (util.nextInt(12) != 0) {
                    p.updateExp(5000000L);
                } else {
                    final short[] arId = {695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 695, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 10, 275, 276, 277, 278, 289, 340, 340, 383, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 438, 443, 443, 485, 485, 524, 524, 539, 540, 454, 436, 436, 436, 437, 437, 437, 438, 385, 549, 550, 551, 568, 570, 571, 577, 648, 648, 648, 648, 648, 649, 649, 649, 649, 649, 650, 650, 650, 650, 650, 651, 651, 651, 651, 651};
                    final short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
            }
            break;
            case 611: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.c.sk += 1;
                if (util.nextInt(15) != 0) {
                    p.updateExp(1500000L);
                } else {
                    short[] arId = new short[]{695, 695, 695, 695, 695, 695, 695, 695, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 9, 275, 276, 277, 278, 289, 340, 340, 383, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 443, 433, 485, 485, 524, 524, 549, 550, 551, 568, 569, 570, 571, 577};
                    short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
                break;
            }
            case 612: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.c.sk += 3;
                if (util.nextInt(15) != 0) {
                    p.updateExp(3000000L);
                } else {
                    short[] arId = new short[]{5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 275, 276, 277, 278, 289, 340, 340, 383, 384, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 436, 436, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 438, 443, 443, 485, 485, 524, 524, 539, 540, 454, 436, 436, 436, 437, 437, 437, 438, 549, 550, 551, 568, 570, 571, 577, 648, 648, 649, 649, 650, 650, 651, 651, 695, 695};
                    short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
                break;
            }
            case 606: {
                if (server.manager.event != 1) {
                    p.sendAddchatYellow("Sự kiện này đã kết thúc không còn sử dụng được vật phẩm này nữa");
                    return;
                }
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                p.c.sk += 5;
                if (util.nextInt(15) != 0) {
                    p.updateExp(5000000L);
                } else {
                    short[] arId = new short[]{5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 275, 276, 277, 278, 289, 340, 340, 383, 384, 385, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 419, 436, 436, 437, 437, 438, 438, 443, 443, 485, 524, 539, 540, 454, 436, 436, 437, 437, 438, 549, 550, 551, 568, 570, 571, 577, 648, 648, 649, 649, 650, 650, 651, 651, 695, 695};
                    short idI = arId[util.nextInt(arId.length)];
                    itemup = ItemData.itemDefault(idI);
                    itemup.isLock = item.isLock;
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
                break;
            }
            case 436:
            case 437:
            case 438: {
                ClanManager clan = ClanManager.getClanName(p.c.clan.clanName);
                if (clan != null && clan.getMem(p.c.name) != null) {
                    if (item.id == 436) {
                        if (clan.level < 1) {
                            p.sendAddchatYellow("Yêu cầu gia tộc phải đạt cấp 5");
                            return;
                        } else {
                            p.upExpClan(util.nextInt(100, 200));
                            p.c.removeItemBag(index, 1);
                            return;
                        }
                    } else if (item.id == 437) {
                        if (clan.level < 10) {
                            p.sendAddchatYellow("Yêu cầu gia tộc phải đạt cấp 10");
                            return;
                        } else {
                            p.upExpClan(util.nextInt(300, 800));
                            p.c.removeItemBag(index, 1);
                            return;
                        }
                    } else if (item.id == 438) {
                        if (clan.level < 15) {
                            p.sendAddchatYellow("Yêu cầu gia tộc phải đạt cấp 15");
                            return;
                        } else {
                            p.upExpClan(util.nextInt(1000, 2000));
                            p.c.removeItemBag(index, 1);
                            return;
                        }
                    }
                } else {
                    p.sendAddchatYellow("Cần có gia tộc để sử dụng");
                    return;
                }
            }
            break;
            case 454: {
                if (p.updateSysMounts()) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 490: {
                if (p.c.isNhanban) {
                    p.conn.sendMessageLog("Chức năng này không dành cho phân thân");
                    return;
                }
                p.c.place.leave(p);
                map = server.maps[138];
                map.area[0].EnterMap0(p.c);
                p.endLoad(true);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 537: {
                p.setEffect(41, 0, (1000 * 60 * 60), 3);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 538: {
                p.setEffect(40, 0, (1000 * 60 * 60), 3);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 539: {
                p.setEffect(32, 0, (1000 * 60 * 60), 3);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 540: {
                p.setEffect(33, 0, (1000 * 60 * 60), 4);
                p.c.removeItemBag(index, 1);
            }
            break;
            case 444: {
                if (p.updateXpMounts(200, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 449: {
                if (p.updateXpMounts(5, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 450: {
                if (p.updateXpMounts(7, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 451: {
                if (p.updateXpMounts(14, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 452: {
                if (p.updateXpMounts(20, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 453: {
                if (p.updateXpMounts(25, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 573: {
                if (p.updateXpMounts(200, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 574: {
                if (p.updateXpMounts(400, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 575: {
                if (p.updateXpMounts(600, (byte) 0)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 576: {
                if (p.updateXpMounts(100, (byte) 1)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 577: {
                if (p.updateXpMounts(250, (byte) 1)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 578: {
                if (p.updateXpMounts(500, (byte) 1)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 778: {
                if (p.updateXpMounts(util.nextInt(20, 50), (byte) 2)) {
                    p.c.removeItemBag(index, 1);
                }
            }
            break;
            case 647: {
                if (numbagnull == 0) {
                    p.conn.sendMessageLog("Hành trang không đủ chỗ trống");
                    return;
                }
                if (util.nextInt(2) == 0) {
                    int num = util.nextInt(1000, 5000);
                    p.c.upyenMessage(num);
                    p.sendAddchatYellow("Bạn nhận được " + num + " yên");
                } else {
                    short[] arId = new short[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 11, 280, 280, 280, 436, 437, 539, 540, 618, 619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631, 632, 633, 634, 635, 636, 637, 776, 777};
                    short idI = arId[util.nextInt(arId.length)];
                    ItemData data2 = ItemData.ItemDataId(idI);
                    if (data2.type < 10) {
                        if (data2.type == 1) {
                            itemup = ItemData.itemDefault(idI);
                            itemup.sys = GameScr.SysClass(data2.nclass);
                        } else {
                            byte sys = (byte) util.nextInt(1, 3);
                            itemup = ItemData.itemDefault(idI, sys);
                        }
                    } else {
                        itemup = ItemData.itemDefault(idI);
                    }
                    itemup.isLock = item.isLock;
                    for (Option Option : itemup.options) {
                        int idOp = Option.id;
                        Option.param = util.nextInt(item.getOptionShopMin(idOp, Option.param), Option.param);
                    }
                    p.c.addItemBag(true, itemup);
                }
                p.c.removeItemBag(index, 1);
            }
        }
        Message m = new Message(11);
        m.writer().writeByte(index);//vi tri item
        m.writer().writeByte(p.c.get().speed());//toc do
        m.writer().writeInt(p.c.get().getMaxHP());//hp max
        m.writer().writeInt(p.c.get().getMaxMP());//mp max
        m.writer().writeShort(p.c.get().eff5buffHP());//eff5BuffHp
        m.writer().writeShort(p.c.get().eff5buffMP());//eff5BuffMp
        m.writer().flush();
        p.conn.sendMessage(m);
        m.cleanup();
        if (isTypeMounts(item.id)) {
            for (Player player : p.c.place.players) {
                p.c.place.sendMounts(p.c.get(), player);
            }
        }
    }

    public static void useItemChangeMap(Player p, Message m) {
        try {
            byte indexUI = m.reader().readByte();
            byte indexMenu = m.reader().readByte();
            m.cleanup();
            Item item = p.c.ItemBag[indexUI];
            if (item != null && (item.id == 37 || item.id == 35)) {
                if (item.id != 37) {
                    p.c.removeItemBag(indexUI);
                }
                if (indexMenu == 0 || indexMenu == 1 || indexMenu == 2) {
                    Map ma = Manager.getMapid(Map.arrTruong[indexMenu]);
                    for (Place area : ma.area) {
                        if (area.numplayers < ma.template.maxplayers) {
                            p.c.place.leave(p);
                            area.EnterMap0(p.c);
                            return;
                        }
                    }
                }
                if (indexMenu == 3 || indexMenu == 4 || indexMenu == 5 || indexMenu == 6 || indexMenu == 7 || indexMenu == 8 || indexMenu == 9) {
                    Map ma = Manager.getMapid(Map.arrLang[indexMenu - 3]);
                    for (Place area : ma.area) {
                        if (area.numplayers < ma.template.maxplayers) {
                            p.c.place.leave(p);
                            area.EnterMap0(p.c);
                            return;
                        }
                    }
                }

            }
        } catch (IOException e) {
        }
        p.c.get().upDie();
    }
}

package boardGame;

import real.Item;

/**
 *
 * @author Văn Tú
 */
public class ItemMap {

    public short x;
    public short y;
    public short itemMapId;
    public long removedelay = 80000L + System.currentTimeMillis();
    public int master = -1;
    public Item item;
}

package server;

import io.Message;
import io.Session;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import real.ClanManager;
import real.Map;
import real.MapTemplate;
import real.Player;
import real.PlayerManager;
import real.RealController;

/**
 *
 * @author Văn Tú
 */
public class Server {

    protected static Server instance;
    protected static ServerSocket listenSocket = null;
    public static boolean isrun = false;
    private final Object LOCK;
    private static final int DELAY = 1000;
    public Player player = null;
    File f = new File("res/" + java.time.LocalDate.now() + ".txt");
    FileWriter fw;

    public Server() {
        try {
            this.fw = new FileWriter(f);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.LOCK = new Object();
        this.isrun = false;
    }

    public Manager manager;
    public MenuController menu;
    public ServerController controllerManager;
    public Controller serverMessageHandler;

    public Map[] maps;
    public static final Object LOCK_MYSQL = new Object();

    private static final int[] hoursRefreshBoss = new int[]{0, 3, 6, 9, 12, 15, 18, 20, 22};
    private static final boolean[] isRefreshBoss = new boolean[]{false, false, false, false, false, false, false, false, false};
    private static final short[] mapBossVDMQ = new short[]{141, 142, 143};
    private static final short[] mapBoss45 = new short[]{14, 15, 16, 34, 35, 52, 68};
    private static final short[] mapBoss55 = new short[]{44, 67};
    private static final short[] mapBoss65 = new short[]{24, 41, 45, 59};
    private static final short[] mapBoss75 = new short[]{18, 36, 54};
    private static final short[] mapBossTG = new short[]{2, 23};

    private static boolean running = true;

    public static Thread run = new Thread(new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (running) {
                    synchronized (ClanManager.entrys) {
                        for (int i = ClanManager.entrys.size() - 1; i >= 0; i--) {
                            ClanManager clan = ClanManager.entrys.get(i);
                            if (util.compare_Week(Date.from(Instant.now()), util.getDate(clan.week))) {
                                clan.payfeesClan();
                            }
                        }
                    }
                    Calendar rightNow = Calendar.getInstance();
                    int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    for (int i = 0; i < hoursRefreshBoss.length; i++) {
                        if (hoursRefreshBoss[i] == hour) {
                            if (!isRefreshBoss[i]) {
                                String textchat = "Thần thú đã suất hiện tại";
                                for (byte j = 0; j < util.nextInt(1, 1); j++) {
                                    Map map = Manager.getMapid(mapBoss75[util.nextInt(mapBoss75.length)]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(15, 29));
                                        textchat += " " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                for (byte j = 0; j < util.nextInt(1, 2); j++) {
                                    Map map = Manager.getMapid(mapBoss65[util.nextInt(mapBoss65.length)]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(15, 30));
                                        textchat += ", " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                for (byte j = 0; j < util.nextInt(1, 2); j++) {
                                    Map map = Manager.getMapid(mapBoss55[util.nextInt(mapBoss55.length)]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(15, 30));
                                        textchat += ", " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                for (byte j = 0; j < util.nextInt(1, 2); j++) {
                                    Map map = Manager.getMapid(mapBoss45[util.nextInt(mapBoss45.length)]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(15, 30));
                                        textchat += ", " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                for (byte j = 0; j < mapBossTG.length; j++) {
                                    Map map = Manager.getMapid(mapBossTG[util.nextInt(mapBossTG.length)]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(10, 19));
                                        textchat += ", " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                for (byte j = 0; j < mapBossVDMQ.length; j++) {
                                    Map map = Manager.getMapid(mapBossVDMQ[j]);
                                    if (map != null) {
                                        map.refreshBoss(util.nextInt(15, 30));
                                        textchat += ", " + map.template.name;
                                        isRefreshBoss[i] = true;
                                    }
                                }
                                Manager.chatKTG(textchat);
                            }
                        } else {
                            isRefreshBoss[i] = false;
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }));

    private void init() {
        manager = new Manager();
        menu = new MenuController();
        controllerManager = new RealController();
        serverMessageHandler = new Controller();
    }

    public static Server getInstance() {
        if (Server.instance == null) {
            Server.instance = new Server();
            Server.instance.init();
            BXHManager.ini();
            run.start();
        }
        return Server.instance;
    }

    public static void main(String[] args) {
        isrun = true;
        getInstance().run();
    }

    public void run() {
        maps = new Map[MapTemplate.arrTemplate.length];
        for (short i = 0; i < maps.length; i++) {
            maps[i] = new Map(i, null);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Shutdown Server!");
                Server.isrun = false;
                stop();
            }
        }));
        Server.listenSocket = null;
        try {
            Server.listenSocket = new ServerSocket(manager.post);
            Server.isrun = true;
            System.out.println("Listen " + manager.post);
            while (Server.isrun) {
                final Socket clientSocket = Server.listenSocket.accept();
                Session conn = new Session(clientSocket, serverMessageHandler);
                InetSocketAddress socketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
//                String clientIpAddress = socketAddress.getAddress().getHostAddress();
                System.err.println(socketAddress);
                fw.write(socketAddress.toString() + "\n");
                PlayerManager.getInstance().put(conn);
                if (PlayerManager.getInstance().conns_size() > 3000) {
                    stop();
                }
                conn.start();
//                if (player != null) {
//                    conn.disconnect();
//                }
                System.out.println("Accept socket size :" + PlayerManager.getInstance().conns_size());
//                Thread.sleep(200);  
            }
        } catch (BindException bindEx) {
            System.exit(0);
        } catch (IOException genEx) {
            genEx.printStackTrace();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (Server.listenSocket != null) {
                Server.listenSocket.close();
            }
            System.out.println("Close server socket");
        } catch (Exception ioEx) {
        }
    }

    public void stop() {
        if (isrun) {
            isrun = false;
            try {
                fw.close();
                Server.isrun = false;
                Server.listenSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //Kick all Player game
            PlayerManager.getInstance().Clear();
            ClanManager.close();
            manager.close();
            manager = null;
            PlayerManager.getInstance().close();
            menu = null;
            controllerManager = null;
            serverMessageHandler = null;
            SQLManager.close();
            System.gc();
        }
    }
}

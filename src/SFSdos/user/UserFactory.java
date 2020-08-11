package SFSdos.user;

import SFSdos.Config;
import SFSdos.Main;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.concurrent.ThreadLocalRandom;

public class UserFactory implements Runnable {
    public LinkedHashSet users;
    private Main main;
    private RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
            .build();

    public UserFactory (Main main) {
        this.main = main;
        this.users = new LinkedHashSet(Config.maxConnections);
    }


    @Override
    public void run() {
        System.out.println("UserFactory thread started");
        while (true) {
            while (users.size() < Config.maxConnections) {
                String u = generateRandomString(ThreadLocalRandom.current().nextInt(5, 8 + 1));
                String p = generateRandomString(ThreadLocalRandom.current().nextInt(4, 8 + 1));
                String e = generateRandomString(ThreadLocalRandom.current().nextInt(3,7 + 1)) + "@gmail.com";

                UserObject user = null;
                if (Config.initializeProxies) {
                    Object[] keys = main.proxyMap.keySet().toArray();
                    Object[] values = main.proxyMap.values().toArray();
                    Integer index = main.random.nextInt(main.proxyMap.size());

                    String ip = (String) keys[index];
                    String port = (String) values[index];

                    user = new UserObject(u, p, e, main, new HttpHost(ip, Integer.parseInt(port)));
                } else
                    user = new UserObject(u, p, e, main);

                System.out.format("[%s] Generating user with password %s and email %s\n", u, p, e);
                if (user.create()) {
                    System.out.format("[%s] User generation success\n", u);
                    users.add(user);
                    try {
                        Files.write(Paths.get(Config.listUsers), (String.format("%s:%s", user.getName(), user.getPass())  + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        System.out.format("[%s] User credentials saved to %ss\n", u, Config.listUsers);
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateRandomString(int length) {
        return randomStringGenerator.generate(length);
    }
}

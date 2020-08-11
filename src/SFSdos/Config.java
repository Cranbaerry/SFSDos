package SFSdos;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Config {
    public static String
            remoteAddress, remotePort,
            proxyAddress, proxyPort,
            listUsers,
            listProxy,
            linkRegister,
            linkLogin,
            configFile,
            headerSignup,
            headerLogin,
            remoteHost,

            customPacket1, customPacket2, customPacket3,
                    customPacket4, customPacket5;
    public static int
            maxConnections;

    public static boolean
            initializeUsers,
            initializeConnections,
            initializeProxies;

    public static void load() {
        try {
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter configuration file: ");
            Config.configFile = reader.nextLine();
            reader.close();

            InputStream stream = new FileInputStream( "configs/" + configFile);
            Properties prop = new Properties();
            prop.load(stream);

            remoteAddress = prop.getProperty("remote.address");
            remotePort = prop.getProperty("remote.port");
            remoteHost = prop.getProperty("remote.host");
            proxyAddress = prop.getProperty("proxy.address").isEmpty() ? null : prop.getProperty("proxy.address");
            proxyPort = prop.getProperty("proxy.port");
            listUsers = prop.getProperty("factory.users.list");
            listProxy = prop.getProperty("factory.proxies.list");
            initializeUsers = Boolean.parseBoolean(prop.getProperty("factory.users"));
            initializeConnections = Boolean.parseBoolean(prop.getProperty("factory.connections"));
            initializeProxies = Boolean.parseBoolean(prop.getProperty("factory.proxies"));
            maxConnections = Integer.parseInt(prop.getProperty("factory.threads"));
            customPacket1 = prop.getProperty("packets.1");
            customPacket2 = prop.getProperty("packets.2");
            customPacket3 = prop.getProperty("packets.3");
            customPacket4 = prop.getProperty("packets.4");
            customPacket5 = prop.getProperty("packets.5");
            linkLogin = prop.getProperty("link.login");
            linkRegister = prop.getProperty("link.register");
            headerLogin = prop.getProperty("link.login.referrer");
            headerSignup = prop.getProperty("link.register.referrer");
        } catch (FileNotFoundException ex) {
            Config.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setup() {
        try {
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter remote address: ");
            Config.remoteAddress = reader.nextLine();

            System.out.println("Enter remote port: ");
            Config.remotePort = reader.nextLine();

            System.out.println("Enter remote host: ");
            Config.remoteHost = reader.nextLine();

            System.out.println("Enter number of threads/connections: ");
            Config.maxConnections = Integer.parseInt(reader.nextLine());

            //System.out.println("Enter characters list filename: ");
            //Config.listUsers = reader.nextLine();
            Config.listUsers = "users.txt";
            Config.listProxy = "proxies.txt";

            System.out.println("Do you want to use SOCK 5 Proxy? [Y/N] ");
            switch (reader.nextLine().toLowerCase()) {
                case "yes":
                case "y":
                    System.out.println("Enter proxy address: ");
                    Config.proxyAddress = reader.nextLine();

                    System.out.println("Enter proxy port: ");
                    Config.proxyPort = reader.nextLine();
                    break;
                default:
                case "no":
                case "n":
                    Config.proxyAddress = null;
                    Config.proxyPort = "0000";
                    break;
            }

            System.out.println("Do you want to use HTTP Proxy? [Y/N] ");
            switch (reader.nextLine().toLowerCase()) {
                case "yes":
                case "y":
                    Config.initializeProxies = true;
                    break;
                default:
                case "no":
                case "n":
                    Config.initializeProxies = false;
                    break;
            }


            System.out.println("Do you want to enable connection factory thread? [Y/N] ");
            switch (reader.nextLine().toLowerCase()) {
                case "yes":
                case "y":
                    Config.initializeConnections = true;
                    break;
                default:
                case "no":
                case "n":
                    Config.initializeConnections = false;
                    break;
            }

            System.out.println("Do you want to enable character creator factory thread? [Y/N] ");
            switch (reader.nextLine().toLowerCase()) {
                case "yes":
                case "y":
                    Config.initializeUsers = true;

                    System.out.println("Enter login link: ");
                    Config.linkLogin = reader.nextLine();

                    System.out.println("Enter register link: ");
                    Config.linkRegister = reader.nextLine();

                    System.out.println("Enter login referrer: ");
                    Config.headerLogin = reader.nextLine();

                    System.out.println("Enter register referrer: ");
                    Config.headerSignup = reader.nextLine();

                    break;
                default:
                case "no":
                case "n":
                    Config.initializeUsers = false;
                    break;
            }

            System.out.println("Enter custom packet 1: [Leave empty if none]");
            Config.customPacket1 = reader.nextLine();

            System.out.println("Enter custom packet 2: [Leave empty if none]");
            Config.customPacket2 = reader.nextLine();

            System.out.println("Enter custom packet 3: [Leave empty if none]");
            Config.customPacket3 = reader.nextLine();

            System.out.println("Enter custom packet 4: [Leave empty if none]");
            Config.customPacket4 = reader.nextLine();

            System.out.println("Enter custom packet 5: [Leave empty if none]");
            Config.customPacket5 = reader.nextLine();

            OutputStream stream = new FileOutputStream("configs/" + configFile);
            Properties prop = new Properties();
            prop.setProperty("remote.address", Config.remoteAddress);
            prop.setProperty("remote.port", Config.remotePort);
            prop.setProperty("remote.host", Config.remoteHost);
            prop.setProperty("proxy.address", Config.proxyAddress);
            prop.setProperty("proxy.port", Config.proxyPort);
            prop.setProperty("factory.connections", Boolean.toString(Config.initializeConnections));
            prop.setProperty("factory.users", Boolean.toString(Config.initializeUsers));
            prop.setProperty("factory.users.list", Config.listUsers);
            prop.setProperty("factory.proxies", Boolean.toString(Config.initializeProxies));
            prop.setProperty("factory.proxies.list", Config.listProxy);
            prop.setProperty("factory.threads", Integer.toString(Config.maxConnections));
            prop.setProperty("packets.1", Config.customPacket1);
            prop.setProperty("packets.2", Config.customPacket2);
            prop.setProperty("packets.3", Config.customPacket3);
            prop.setProperty("packets.4", Config.customPacket4);
            prop.setProperty("packets.5", Config.customPacket5);
            prop.setProperty("link.login", Config.linkLogin);
            prop.setProperty("link.register", Config.linkRegister);
            prop.setProperty("link.login.referrer", Config.headerLogin);
            prop.setProperty("link.register.referrer", Config.headerSignup);
            prop.store(stream, Config.configFile);
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
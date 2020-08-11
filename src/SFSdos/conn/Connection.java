package SFSdos.conn;
import SFSdos.Config;
import SFSdos.user.UserFactory;
import SFSdos.user.UserObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.concurrent.*;

public class Connection implements Runnable {
    private DataOutputStream out;
    private UserObject user;
    private ConnectionFactory connFactory;

    public Connection(UserObject user, ConnectionFactory connFactory) {
        this.user = user;
        this.user.use();
        this.connFactory = connFactory;
    }

    private void write(String msg) throws IOException {
        if (msg.isEmpty())
            return;

        String strOut = msg + '\000';
        strOut = strOut.replace("CHAR_NAME", user.getName());
        out.write(strOut.getBytes());

        //System.out.format("[%s] Sent: %s \n", user.getName(), strOut);
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        SocketAddress addr = null;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Callable<Boolean> task = () -> {
            System.out.format("[%s] Rechecking token status..\n", user.getName());
            String token = user.fetchToken();
            if (token == null) {
                user.remove();
                System.out.format("[%s] Could not validate token, user removed\n", user.getName());
                return true;
            }

            return false;
        };

        ScheduledFuture<?> future = executor.schedule(task, 10, TimeUnit.MINUTES);

        if (Config.proxyAddress != null) {
            addr = new InetSocketAddress(Config.proxyAddress, Integer.parseInt(Config.proxyPort));
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
            socket = new Socket(proxy);
        }

        try {
            connFactory.connections.add(this);
            socket.setSoTimeout(500);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);

            InetSocketAddress dest = new InetSocketAddress(Config.remoteAddress, Integer.parseInt(Config.remotePort));
            /* if (Config.proxyAddress == null)
                System.out.format("[%s] Connecting to %s\n", user.getName(), dest);
            else
                System.out.format("[%s] Connecting to %s through proxy  %s\n", user.getName(), dest, addr); */

            socket.connect(dest);
            //System.out.format("[%s] Socket established\n", user.getName());

            out = new DataOutputStream(socket.getOutputStream());
            System.out.format("[%s] Logging in with token: %s\n", user.getName(), user.getToken());

            long respndingTimer = System.currentTimeMillis();
            while (!socket.isClosed() && socket.isConnected() && (System.currentTimeMillis() - respndingTimer) < 5000) {
                if (future.isDone() && !(Boolean) future.get())
                    break;

                write("<msg t='sys'><body action='verChk' r='0'><ver v='166' /></body></msg>");
                write("<msg t='sys'><body action='login' r='0'><login z='zone_master'><nick><![CDATA[N7B5W8W1Y5B1R5VWVZ~"+ user.getName() +"]]></nick><pword><![CDATA["+ user.getToken() +"]]></pword></login></body></msg>");
                write("%xt%zm%firstJoin%1%");

                write(Config.customPacket1);
                write(Config.customPacket2);
                write(Config.customPacket3);
                write(Config.customPacket4);
                write(Config.customPacket5);

                respndingTimer = System.currentTimeMillis();
            }
            executor.shutdown();
        } catch (SocketException e) {
            //System.out.format("[%s] Socket exception occured\n", user.getName());
        } catch (SocketTimeoutException e) {
            //System.out.format("[%s] Socket timed out\n", user.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
            user.release();
            connFactory.connections.remove(this);
            //System.out.format("[%s] Disconnected from socket\n", user.getName());
            Thread.currentThread().interrupt();
        }
    }
}

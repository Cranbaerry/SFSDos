package SFSdos.user;

import SFSdos.Config;
import SFSdos.Main;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserObject {
    private Main main;

    private String user;
    private String pass;
    private String mail;
    private String token;
    private String sessId;
    private Boolean used;
    private long timer;

    private HttpClient httpClient;
    private HttpHost httpTarget;
    private HttpHost httpProxy;
    private boolean useProxy;
    private RequestConfig httpConfig;
    private HttpClientBuilder httpBuilder;

    private CookieStore httpCookieStore;

    public UserObject(String user, String pass, String mail, Main main, HttpHost proxy) {
        this.main = main;
        this.user = user;
        this.pass = pass;
        this.mail = mail;
        this.used = false;
        this.token = null;
        this.sessId = null;
        this.useProxy = true;
        this.httpProxy = proxy;

        httpCookieStore = new BasicCookieStore();
        httpBuilder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).setProxy((httpProxy));
        httpClient = httpBuilder.build();
        httpTarget = new HttpHost(Config.remoteHost);
    }

    public UserObject(String user, String pass, String mail, Main main) {
        this.main = main;
        this.user = user;
        this.pass = pass;
        this.mail = mail;
        this.used = false;
        this.token = null;
        this.sessId = null;
        this.useProxy = false;
        this.httpProxy = null;

        httpCookieStore = new BasicCookieStore();
        httpBuilder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore);
        httpClient = httpBuilder.build();
        httpTarget = new HttpHost(Config.remoteHost);
    }

    public void remove() {
        main.userFactory.users.remove(this);
    }

    public String getName() {
        return user;
    }

    public String getPass() {
        return pass;
    }
    public String getToken() {
        return token;
    }


    private String get(HttpGet request) {
        String out = null;
        try {
            request.setConfig(httpConfig);

            System.out.format("[%s] Executing request %s %s\n", user, request, useProxy ? "via proxy " + httpProxy : "");
            HttpResponse response = httpClient.execute(httpTarget, request);
            out = EntityUtils.toString(response.getEntity());

            /* List cookies = httpCookieStore.getCookies();
            for (Object obj : cookies) {
                Cookie cookie = (Cookie) obj;
                System.out.println(">  " + cookie.getName() + " : " + cookie.getValue());
                if (cookie.getName().contains("PHPSESSID")) {
                    System.out.format("[%s] Session id retrieved: %s\n", user, cookie.getValue());
                    this.sessId = cookie.getValue();
                    break;
                }
            } */

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(out);
        } catch (IOException e) {
            System.out.format("[%s] IOException: %s\n", user, e.getMessage());
        }

        request.releaseConnection();
        return out;
    }

    private String post(HttpPost request) {
        String out = "";
        try {
            request.setConfig(httpConfig);
            request.setHeader("X-Requested-With", "ShockwaveFlash/31.0.0.153");
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
            request.setHeader("DNT", "1");
            //request.setHeader("Referer", "http://www.silveraq.net/gamefiles/newuser/securelanding.swf");
            //request.setHeader("Origin", "http://www.silveraq.net");
            request.setHeader("Host", Config.remoteHost);
            //request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            //request.setHeader("Cookie", "PHPSESSID=" + this.sessId);
            //request.setHeader("Accept", "*/*");
            //request.setHeader("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
            //request.setHeader("Accept-Encoding", "gzip, deflate");

            System.out.format("[%s] Executing request %s %s\n", user, request, useProxy ? "via proxy " + httpProxy : "");
            HttpResponse response = httpClient.execute(httpTarget, request);
            out = EntityUtils.toString(response.getEntity());

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(out);

            if (out.contains("Attention Required! | Cloudflare") && Config.initializeProxies) {
                request.releaseConnection();
                getNewProxy();
                return post(request);
            }
        } catch (IOException e) {
            System.out.format("[%s] IOException: %s\n", user, e.getMessage());
            if (Config.initializeProxies && (e.getMessage() == null || e.getMessage().contains("failed to respond"))) {
                request.releaseConnection();
                getNewProxy();
                return post(request);
            }
        }

        request.releaseConnection();
        return out;
    }

    private void getNewProxy() {
        Object[] keys = main.proxyMap.keySet().toArray();
        Object[] values = main.proxyMap.values().toArray();
        Integer index = main.random.nextInt(main.proxyMap.size());

        String ip = (String) keys[index];
        String port = (String) values[index];

        httpProxy = new HttpHost(ip, Integer.parseInt(port));
        httpBuilder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).setProxy((httpProxy));
        httpClient = httpBuilder.build();

        System.out.format("[%s] Retrying with new proxy %s\n", user, httpProxy);
    }

    public boolean create() {
        List<NameValuePair> params = new ArrayList<>(2);

        params.add(new BasicNameValuePair("intColorHair", "6180663"));
        params.add(new BasicNameValuePair("strDOB", "12/2/2005"));
        params.add(new BasicNameValuePair("intColorSkin", "15388042"));
        params.add(new BasicNameValuePair("yonghuming", this.user));
        params.add(new BasicNameValuePair("strGender", "M"));
        params.add(new BasicNameValuePair("HairID", "52"));
        params.add(new BasicNameValuePair("mima", this.pass));
        params.add(new BasicNameValuePair("strEmail", this.mail));
        params.add(new BasicNameValuePair("intColorEye", "91294"));
        params.add(new BasicNameValuePair("ClassID", "2"));
        params.add(new BasicNameValuePair("intAge", "13"));

        HttpPost request = new HttpPost(Config.linkRegister);
        try {
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.format("[%s] UnsupportedEncodingException: %s\n", user, e.getMessage());
        }

        String result = post(request);
        if (result.contains("Success") || result.contains("Registrado Correctamente")) {
            System.out.format("[%s] User created successfully\n", user);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.format("[%s] InterruptedException: %s\n", user, e.getMessage());
            }

            token = fetchToken();
            if (token != null)
                return true;
        }

        return false;
    }

    public String fetchToken() {
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("yonghuming", this.user));
        params.add(new BasicNameValuePair("mima", this.pass));
        params.add(new BasicNameValuePair("strUsername", this.user));
        params.add(new BasicNameValuePair("strPassword", this.pass));
        params.add(new BasicNameValuePair("user", this.user));
        params.add(new BasicNameValuePair("pass", this.pass));

        HttpPost request = new HttpPost(Config.linkLogin);
        try {
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String response = post(request);
        if (!response.isEmpty()) {
            Pattern pattern = Pattern.compile("sToken=\"(.*?)\" ");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                System.out.format("[%s] Token retrieved: %s\n", user, matcher.group(1));
                return matcher.group(1);
            } else
                System.out.format("[%s] Invalid response: %s\n", user, response);
        }

        return null;
    }

    public void setToken(String in) {
        token = in;
    }

    public long getTimer() {
        return timer;
    }
    public void use() {
        used = true;
        timer = System.currentTimeMillis();
    }

    public void release() {
        used = false;
    }

    public boolean isInUse() {
        return used;
    }
}

/**
 * Created by Julia on 08.01.2015.
 */

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

public class LoginHttpClient {

    private String cookies;
    private static HttpClient client = HttpClientBuilder.create().setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE).build();
    private final String USER_AGENT = "Chrome/39.0.2171.95";

    public static void main(String[] args) throws Exception {

        String url = "http://moneta.kvartplata.info/login";
        String moneta = "http://moneta.kvartplata.info/";

        File resultFile = new File("result.txt");
        if(!resultFile.exists()){
            resultFile.createNewFile();
        }
        PrintWriter out = new PrintWriter(resultFile.getAbsoluteFile());

        try{
            CookieHandler.setDefault(new CookieManager());

            LoginHttpClient http = new LoginHttpClient();

            String page = http.GetPageContent(url);

            File file = new File( "logins.txt" );

            BufferedReader br = new BufferedReader (
                    new InputStreamReader(
                            new FileInputStream( file ), "UTF-8"
                    )
            );
            String login = br.readLine();
            String pass = br.readLine();
            String filePath = br.readLine();

            br.close();

            List<NameValuePair> postParams =
                    http.getFormParams(page, login, pass);

            http.sendPost(url, postParams);

            String result = http.GetPageContent(moneta);
            System.out.println(result);

            new PutFileHttpClient("http://moneta.kvartplata.info/download/charge", filePath, moneta, client);

            try {
                out.println("ok");
            } finally {
                out.close();
            }

            System.out.println("Done");
        }catch(Exception e) {
            System.out.println(e.getMessage());
            try {
                out.println("failed");
            } finally {
                out.close();
            }
        }
    }

    private void sendPost(String url, List<NameValuePair> postParams)
            throws Exception {

        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Host", "moneta.kvartplata.info");
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        post.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
        post.setHeader("Cookie", getCookies());
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", "http://moneta.kvartplata.info/login");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        post.setEntity(new UrlEncodedFormEntity(postParams));

        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        // System.out.println(result.toString());

    }

    private String GetPageContent(String url) throws Exception {

        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        // set cookies
        setCookies(response.getFirstHeader("Set-Cookie") == null ? "" :
                response.getFirstHeader("Set-Cookie").toString());

        return result.toString();

    }

    public List<NameValuePair> getFormParams(
            String html, String username, String password)
            throws UnsupportedEncodingException {

        System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        // Google form id
        Elements loginform = doc.getElementsByAttributeValue("name", "loginform");
        Elements inputElements = loginform.get(0).getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("f[login]"))
                value = username;
            else if (key.equals("f[password]"))
                value = password;

            paramList.add(new BasicNameValuePair(key, value));

        }

        return paramList;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }


}


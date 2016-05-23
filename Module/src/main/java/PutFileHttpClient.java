/**
 * Created by Julia on 08.01.2015.
 */

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PutFileHttpClient {

    public PutFileHttpClient(String fileName, String filePath, String url, HttpClient client) throws IOException{
        String file2 = getFileName("http://moneta.kvartplata.info/download/charge", filePath, client);
        String[] spl =  file2.split("\\.");

        String fullFilePath = filePath + "\\" + spl[0] + "\\";

        String zipFile = zipIt(spl[0], fullFilePath);

        postFile(url, client, zipFile);

    }

    @SuppressWarnings("deprecation")
    public void postFile(String url, HttpClient client, String filePath)
            throws IOException {

        HttpPost httppost = new HttpPost(url);

        FileBody bin = new FileBody(new File(filePath));
        StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("f[file]", bin)
                .addTextBody("f[type]", "charge")
                .addTextBody("f[go]", "Загрузить")
                .build();


        httppost.setEntity(reqEntity);

        System.out.println("executing request " + httppost.getRequestLine());
        HttpResponse response = client.execute(httppost);
        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            System.out.println("Response content length: " + resEntity.getContentLength());
        }
        EntityUtils.consume(resEntity);

    }

    public String getFileName(String fileURL, String saveDir, HttpClient client)
            throws IOException {

        HttpGet post = new HttpGet(fileURL);
        HttpResponse response = client.execute(post);
        String disposition = response.getFirstHeader("Content-Disposition").getValue();
        String fileName = null;
        int index = disposition.indexOf("filename=");
        if (index > 0) {
            fileName = disposition.substring(index + 10,
                    disposition.length() - 1);
        }

        return fileName;
    }

    public String zipIt(String file, String filePath){

        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(filePath + file + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze= new ZipEntry(file + ".xls");
            zos.putNextEntry(ze);
            FileInputStream in = new FileInputStream(filePath + file + ".xls");

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
            zos.closeEntry();

            //remember close it
            zos.close();

            System.out.println("Done");
            return filePath + file + ".zip";
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return filePath + file + ".zip";
    }
}


/**
 * Created by Julia on 08.01.2015.
 */

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GetFileHttpClient {

    String fileURL;
    String saveDir;
    String unzipFilePath;
    HttpClient client;

    public GetFileHttpClient(String fileURL, String saveDir, HttpClient client) throws IOException{
        this.fileURL = fileURL;
        this.saveDir = saveDir;
        this.client = client;
    }

    public String start() throws Exception{
        String file2 = downloadFile(fileURL, saveDir, client);
        String[] spl =  file2.split("\\.");
        unZipIt( saveDir + "\\" + file2,  saveDir + "\\" + spl[0]);
        return unzipFilePath;
    }
    public String downloadFile(String fileURL, String saveDir, HttpClient client)
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

	      /*System.out.println("Content-Type = " + contentType);
	      System.out.println("Content-Disposition = " + disposition);
	      System.out.println("Content-Length = " + contentLength);
	      System.out.println("fileName = " + fileName);*/

        // opens input stream from the HTTP connection
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();

        String saveFilePath = saveDir + File.separator + fileName;

        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(saveFilePath);

        int bytesRead = -1;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        //System.out.println("File downloaded");
        //System.out.println(fileName);
        return fileName;
    }

    public void unZipIt(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                String filePath = newFile.getAbsoluteFile().toString();

                if (filePath.endsWith(".xls")){
                    unzipFilePath = filePath;
                }
                //System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            //System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}


package utils.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartFormData {

    public static final class FileDto {
        private String fileName;
        private String mimeType;
        private byte[] bytes;

        public FileDto(String fileName, String mimeType, byte[] bytes) {
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.bytes = bytes;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public byte[] getBytes() {
            return this.bytes;
        }
    }

    static protected String generateBoundary() {
        final String randomString = "wbkuypnnawsgvb"; // TODO:
        return "---*#" + randomString + "#";
    }

    public static WebRequest.HttpResult post(String urlString, HashMap<String, String> headers, List<FileDto> multipartFiles) {
        final byte[] CRLFBytes = "\r\n".getBytes(StandardCharsets.UTF_8);
        final String boundary = generateBoundary();
        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());

            int contentLength = 0;
            for (FileDto file : multipartFiles) {

                final String firstBoundary = "--" + boundary + "\r\n";
                outputStream.write(firstBoundary.getBytes(StandardCharsets.UTF_8));
                contentLength += firstBoundary.getBytes(StandardCharsets.UTF_8).length;

                String formDataHeader = "Content-Disposition: form-data; name=\"file\"; filename=" // FIXME: name=fileだとMastodonにしか使えないので直す
                        + "\"" + file.getFileName() + "\"" + "\r\n"
                        + "Content-Type: " + file.getMimeType() + "\r\n";

                byte[] headerBytes = formDataHeader.getBytes(StandardCharsets.UTF_8);

                outputStream.write(headerBytes);
                contentLength += headerBytes.length;

                outputStream.write(CRLFBytes);
                contentLength += CRLFBytes.length;

                outputStream.write(file.getBytes());
                contentLength += file.getBytes().length;

                outputStream.write(CRLFBytes);
                contentLength += CRLFBytes.length;

            }

            final String endBoundary = "--" + boundary + "--" + "\r\n";
            outputStream.write(endBoundary.getBytes(StandardCharsets.UTF_8));
            contentLength += endBoundary.getBytes(StandardCharsets.UTF_8).length;

            outputStream.flush();
            outputStream.close();

            int status = connection.getResponseCode();
            System.out.println("HTTP status:" + status);

            if (status == HttpURLConnection.HTTP_OK) {

                in = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(in));

                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                return new WebRequest.HttpResult("ok", output.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new WebRequest.HttpResult("ng", "");
    }
}

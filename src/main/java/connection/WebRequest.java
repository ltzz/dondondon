package connection;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WebRequest {


    public static String requestPOST(String URLStr, String parameterString) {
        return requestPOST(URLStr, new HashMap<>(), parameterString);
    }

    public static String requestPOST(String URLStr, HashMap<String, String> headers, String parameterString) {
        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        try {
            //接続するURLを指定する
            URL url = new URL(URLStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            printWriter.print(parameterString);
            printWriter.close();
            connection.connect();

            // POSTデータ送信処理

            out = connection.getOutputStream();
            out.write("POST DATA".getBytes(StandardCharsets.UTF_8));
            out.flush();

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
                return output.toString();
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
        return "";
    }

    public static String requestGET(String URLStr, HashMap<String, String> headers) {
        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        try {
            //接続するURLを指定する
            URL url = new URL(URLStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("GET");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.connect();

            int status = connection.getResponseCode();
            System.out.println("HTTP status:" + status);

            if (status == HttpURLConnection.HTTP_OK) {

                in = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                return output.toString();
            }
        } catch (UnknownHostException e) {
            System.err.println("ホストとの接続に失敗");
        } catch (Exception e) {
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
        return "";
    }
}


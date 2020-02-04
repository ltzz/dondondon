import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

public class WebRequest {

    final String CLIENT_NAME = "test_client";
    final String RegisterURL = Akan.MASTODON_HOST + "/api/v1/apps";
    final String TokenURL = Akan.MASTODON_HOST + "/oauth/token";

    String getAuthorizeURL(String host){
        try {
            return host
                + "/oauth/authorize?"
                + "client_id=" + Akan.CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob"
                + "&scope=read"
            ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    String getAccessToken(){

        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        BrowserLauncher.launch(getAuthorizeURL(Akan.MASTODON_HOST));

        TextInputDialog dialog = new TextInputDialog("コードを入れてください");
        Optional<String> msg = dialog.showAndWait();
        if(msg.isPresent()) {//データがあるか？
            System.out.println("入力文字列：" + msg.get());
        }
        else {
            return "";
        }

        // TODO: validation
        String authCode = msg.get();

        try {
            //接続するURLを指定する
            URL url = new URL(TokenURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);


            String parameterString = new String("client_id=" + Akan.CLIENT_ID
                    + "&client_secret=" + Akan.CLIENT_SECRET
                    + "&grant_type=authorization_code&code=" + authCode
                    + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob");
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(parameterString);
            printWriter.close();

            connection.connect();

            // POSTデータ送信処理

            out = connection.getOutputStream();
            out.write("POST DATA".getBytes("UTF-8"));
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
                System.out.println(output.toString());

                // TODO: ここでJSON解釈してtoken返す
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

    void registerClient() {

        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        try {
            //接続するURLを指定する
            URL url = new URL(RegisterURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String parameterString = new String("client_name=" + CLIENT_NAME + "&redirect_uris=urn:ietf:wg:oauth:2.0:oob&scopes-read"); // TODO: "scopes=read write follow"
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(parameterString);
            printWriter.close();

            connection.connect();

            // POSTデータ送信処理

            out = connection.getOutputStream();
            out.write("POST DATA".getBytes("UTF-8"));
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
                System.out.println(output.toString());
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
    }

    String getTimeline() {
        String token = Akan.TOKEN;
        String url = Akan.MASTODON_HOST + "/api/v1/timelines/home";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + token);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }

    static String requestGET(String URLStr, HashMap<String,String> headers){
        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        OutputStream out = null;

        try {
            //接続するURLを指定する
            URL url = new URL(URLStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            for(var entry: headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.connect();

            int status = connection.getResponseCode();
            System.out.println("HTTP status:" + status);

            if (status == HttpURLConnection.HTTP_OK) {

                in = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                System.out.println(output.toString());
                return output.toString();
            }
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

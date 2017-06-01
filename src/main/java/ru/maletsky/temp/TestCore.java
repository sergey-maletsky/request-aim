package ru.maletsky.temp;

import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cybertron on 18.05.17.
 */
public class TestCore {
    private static final String coreHost = "80.82.94.194",
            coreProtocol = "http://",
            corePort = ":8888",
            coreServerUrl = coreProtocol + coreHost + corePort;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String CORE_FOLDER = "/home/cybertron/Work/fls/fns_project/testing/core/";
    private static final String XML_FOLDER = CORE_FOLDER + "xml/";
    private static final String LOG_FOLDER = CORE_FOLDER + "log/";
    private static final String DOT_XML = ".xml";
    private static final String DOT_TXT = ".txt";
    private static String fileName = LOG_FOLDER + "log" + DOT_TXT;

    public static void main(String[] args) throws JSONException, InterruptedException, IOException {
        //getFarmOut();
        postFarmIn("200-result");
        //postFarmIn("311_01");
        //postFarmIn("312_01");
        //postFarmIn("313_01");
        //postFarmIn("411_01");
        //postFarmIn("413_01");
        //postFarmIn("414_01");
        //postFarmIn("511_01");
        //postFarmIn("511_02");
        //postFarmIn("511_03");
        //postFarmIn("511_04");
    }

    private static String getFarmOut() throws IOException {
        String response = "";
        final String resource = "/farm/out";

        try {
            response = getGETResponse(resource);
        } catch (Exception e) {
            println(fileName, "Internal error\n\n");
        }

        return response;
    }

    private static String postFarmIn(String xmlName) throws IOException {
        String response = "";
        final String resource = "/farm/in";
        fileName = LOG_FOLDER + xmlName + DOT_TXT;

        try {
            HttpRequest httpRequest = HttpRequest.post(coreServerUrl + resource);
            httpRequest.part("xml_file", new File(XML_FOLDER + xmlName + DOT_XML));
            int status = httpRequest.code();
            if (status == 200) {
                println(fileName, httpRequest.body());
            }

/*            JSONObject data = new JSONObject();
            data.put("op_date", "2017-04-18T16:42:13.3855742+03:00");
            data.put("first_name", "Elon");
            data.put("middle_name", "Reeve");
            data.put("last_name", "Mask");
            data.put("inn", "1123456782");

            response = getPOSTResponse(resource, data);*/
        } catch (Exception e) {
            println(fileName, "Internal error\n\n");
        }

        return response;
    }

    private static String getStringValue(String jsonResponse, String key) throws JSONException {
        String value = "";
        if (!jsonResponse.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            value = jsonObject.getString(key);
        }
        return value;
    }

    private static int getIntValue(String jsonResponse, String key) throws JSONException {
        int value = 0;
        if (!jsonResponse.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            value = jsonObject.getInt(key);
        }

        return value;
    }

    private static String getGETResponse(String resource) throws IOException {
        HttpURLConnection connection = getConnection(GET, resource);

        return getResponse(connection, resource, new String(), GET);
    }

    private static String getPOSTResponse(String resource, JSONObject data) throws IOException {
        HttpURLConnection connection = getConnection(POST, resource);

        OutputStream os = connection.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        return getResponse(connection, resource, data.toString(), POST);
    }

    private static HttpURLConnection getConnection(String method, String resource) throws IOException {
        URL url = new URL(coreServerUrl + resource);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod(method);

        if (method.equals(POST)) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
        }

        return connection;
    }

    private static String getResponse(HttpURLConnection connection, String resource, String data, String method) throws IOException {
        StringBuffer response = new StringBuffer();
        long elapsedTime = 0;

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss:SSS");

        println(fileName, "Date: " + dateFormat.format(date));
        println(fileName, "REQUEST");
        println(fileName, connection.getRequestMethod() + " " + resource + " HTTP/1.1");
        if (method.equals(POST)) {
            println(fileName, "BODY");
            println(fileName, data);
        }

        long startTime = System.currentTimeMillis();
        connection.connect();
        println(fileName, "RESPONSE");
        try {
            println(fileName, "Code: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            if (connection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                elapsedTime = System.currentTimeMillis() - startTime;
                println(fileName, "Elapsed time: " + elapsedTime + "ms");

                String inputLine;
                response = new StringBuffer();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }

                bufferedReader.close();

                String jsonResponseMessage = response.toString();
                if (jsonResponseMessage.contains("\\u") && jsonResponseMessage.contains("errorDescription")) {
                    jsonResponseMessage = getStringValue(jsonResponseMessage, "errorDescription");
                    jsonResponseMessage = "{\"errorDescription\": " + jsonResponseMessage + "}";
                }

                println(fileName, "Response message: " + jsonResponseMessage + "\n\n");
            } else {
                println(fileName, "\n\n");
            }
        } catch (IOException e) {
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= 5000) {
                println(fileName, "Request timeout: " + elapsedTime + "ms\n\n");
            } else {
                println(fileName, "Internal error\n\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private static void writeln(String fileName, String text) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.println(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(String fileName, String text) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.print(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(String fileName, String newText) throws IOException {
        exists(fileName);
        StringBuilder sb = new StringBuilder();
        String oldFile = read(fileName);
        sb.append(oldFile);
        sb.append(newText);
        write(fileName, sb.toString());
    }

    private static void println(String fileName, String newText) throws IOException {
        exists(fileName);
        StringBuilder sb = new StringBuilder();
        String oldFile = read(fileName);
        sb.append(oldFile);
        sb.append(newText);
        writeln(fileName, sb.toString());
    }

    private static String read(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        exists(fileName);
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    private static void exists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }
}

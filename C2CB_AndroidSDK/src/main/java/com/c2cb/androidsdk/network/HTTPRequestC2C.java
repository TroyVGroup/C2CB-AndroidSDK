package com.c2cb.androidsdk.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HTTPRequestC2C extends AsyncTask<String, Void, String> {
    public String requestURL = "";
    public HashMap<String, String> postDataParams;
    public HashMap<String, String> headers;
    public HTTPCallback delegate = null;//Call back interface
    public int res_code = 0;
    String data;
    private Gson gson = new Gson();
    private Class clazz;
    private Uri imageUri;
    private Activity activity;
    Method methd;

    public <T> HTTPRequestC2C(String requestURL, String data, Method methd, HashMap<String, String> headers, Class<T> clazz, HTTPCallback asyncResponse) {
        this.delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.postDataParams = postDataParams;
        this.headers = headers;
        this.data = data;
        this.methd = methd;
        this.clazz = clazz;
        this.requestURL = NetworkManager.BASE_URL + requestURL;
    }

    public <T> HTTPRequestC2C(Boolean auth, String requestURL, String data, Method methd, HashMap<String, String> headers, Class<T> clazz, HTTPCallback asyncResponse) {
        this.delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.postDataParams = postDataParams;
        this.headers = headers;
        this.data = data;
        this.methd = methd;
        this.clazz = clazz;
        if (auth) {
            this.requestURL = NetworkManager.BASE_URL + requestURL;
        } else {
            this.requestURL = requestURL;
        }
    }

    public <T> HTTPRequestC2C(String requestURL, Method methd, HashMap<String, String> headers, Class<T> clazz, Uri imageUri, Activity activity,  String data, HTTPCallback asyncResponse) {
        this.delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.headers = headers;
        this.methd = methd;
        this.clazz = clazz;
        this.imageUri = imageUri;
        this.activity = activity;
        this.data = data;
        this.requestURL = NetworkManager.BASE_URL + requestURL;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            switch (methd) {
                case GET:
                    return performGet(requestURL, headers);
                case POST:
                    return performPostCall(requestURL, data, headers);
                case IMAGE_UPLOAD:
                    return uploadImageToServer(requestURL, imageUri, headers, activity);
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(result);
        if (res_code == HttpURLConnection.HTTP_OK) {
            Log.d("result response",result);
            delegate.processFinish(gson.fromJson(result, clazz));
        } else {
            delegate.processFailed(res_code, result);
        }
    }

    private String performGet(String requestURL, HashMap<String, String> headers) throws Exception {
        URL obj = new URL(requestURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        for (String value : headers.keySet()) {
            headers.get(value);
            con.setRequestProperty(value, headers.get(value));
        }

        int responseCode = con.getResponseCode();
        res_code = responseCode;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public String performPostCall(String requestURL, String data, HashMap<String, String> headers) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            for (String value : headers.keySet()) {
                headers.get(value);
                conn.setRequestProperty(value, headers.get(value));
            }
            OutputStream os = conn.getOutputStream();
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);

            os.close();
            int responseCode = conn.getResponseCode();
            res_code = responseCode;
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else if (responseCode == 500) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }


    private String uploadImageToServer(String requestURL, Uri imageUri, HashMap<String, String> headers, Activity activity) throws IOException {

        // URL of the server
        URL url = new URL(requestURL);

        // Open the connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set request method to POST
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // Set timeouts
        conn.setConnectTimeout(100000);  // 10 seconds for connection timeout
        conn.setReadTimeout(150000);     // 15 seconds for read timeout

        // Set the content type (multipart/form-data in this case)
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");
        for (String value : headers.keySet()) {
            headers.get(value);
            conn.setRequestProperty(value, headers.get(value));
        }
        // Prepare output stream to send the file
        OutputStream os = conn.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        // Add file part to the request
        addFilePart(dos, "image", imageUri, activity);


        dos.flush();
        dos.close();

        // Get the response
        int responseCode = conn.getResponseCode();
        res_code = responseCode;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = new BufferedInputStream(conn.getInputStream());
            String response = readStream(is);
            System.out.println("Response: " + response);
            return response;
        } else {
            System.out.println("Error: " + responseCode);
        }


        return "";
    }


    private void addFilePart(DataOutputStream dos, String paramName, Uri fileUri, Activity activity) throws IOException {
        // Get content resolver to access the file
        ContentResolver contentResolver = activity.getContentResolver();

        // Open an input stream to read the file (image)
        InputStream inputStream = contentResolver.openInputStream(fileUri);
        String fileName = getFileName(fileUri, activity); // Extract the file name from the URI

        // Buffer to hold file data
        byte[] buffer = new byte[4096];
        int bytesRead;

        // Write multipart headers for the file
        dos.writeBytes("--boundary\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n");
        dos.writeBytes("Content-Type: " + contentResolver.getType(fileUri) + "\r\n");
        dos.writeBytes("\r\n");

        // Write file content to the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }

        // End the part
        dos.writeBytes("\r\n");
        dos.writeBytes("--boundary--\r\n");

        inputStream.close();
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri, Activity activity) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }


}
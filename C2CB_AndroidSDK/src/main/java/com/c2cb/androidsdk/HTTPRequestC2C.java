package com.c2cb.androidsdk;

import android.os.AsyncTask;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HTTPRequestC2C extends AsyncTask<String, Void, String>{
    public String requestURL="";
    public HashMap<String, String> postDataParams;
    public HashMap<String, String> headers;
    public HTTPCallback delegate = null;//Call back interface
    public int res_code=0;
    String data;
    private Gson gson = new Gson();
    private Class clazz ;
    Method methd;
    public <T> HTTPRequestC2C(String requestURL, String data, Method methd, HashMap<String, String> headers, Class<T> clazz, HTTPCallback asyncResponse){
        this.delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.postDataParams=postDataParams;
        this.headers = headers;
        this.data = data;
        this.methd = methd;
        this.clazz = clazz;
        this.requestURL = C2CConstants.BASE_URL +requestURL;
    }
    public <T> HTTPRequestC2C(Boolean auth,String requestURL, String data, Method methd, HashMap<String, String> headers, Class<T> clazz, HTTPCallback asyncResponse){
        this.delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.postDataParams=postDataParams;
        this.headers = headers;
        this.data = data;
        this.methd = methd;
        this.clazz = clazz;
        if (auth){
            this.requestURL = C2CConstants.BASE_URL +requestURL;
        }else {
            this.requestURL = requestURL;
        }
    }

    @Override

    protected String doInBackground(String... params) {

        try {

            switch (methd){
                case GET:
                   return performGet(requestURL,headers);
                case POST:
                   return performPostCall(requestURL,data,headers);

            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
//        return performPostCall(requestURL,postDataParams,headers);
    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(result);
        if(res_code== HttpURLConnection.HTTP_OK){
//            gson.fromJson(result, clazz);
            delegate.processFinish(gson.fromJson(result,clazz));
        }else{
            delegate.processFailed(res_code, result);
        }
    }

    private String performGet(String requestURL, HashMap<String, String> headers) throws Exception {

        URL obj = new URL(requestURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        for(String value: headers.keySet()){
            headers.get(value);
            con.setRequestProperty(value, headers.get(value));
        }

        int responseCode = con.getResponseCode();
        res_code=responseCode;
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

    public String  performPostCall(String requestURL, String data, HashMap<String, String> headers) {
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
            for(String value: headers.keySet()){
                headers.get(value);
                conn.setRequestProperty(value, headers.get(value));
            }
                OutputStream os = conn.getOutputStream();
                byte[] input = data.getBytes("utf-8");
                os.write(input, 0, input.length);

            os.close();
            int responseCode=conn.getResponseCode();
            res_code=responseCode;
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }else if (responseCode == 500) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
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
}
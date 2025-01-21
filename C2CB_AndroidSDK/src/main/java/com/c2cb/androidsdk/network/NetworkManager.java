package com.c2cb.androidsdk.network;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.c2cb.androidsdk.C2CConstants;
import com.c2cb.androidsdk.pojo.C2CAddress;
import com.c2cb.androidsdk.pojo.CallPojo;
import com.c2cb.androidsdk.pojo.ImageUploadResponse;
import com.c2cb.androidsdk.pojo.Modes;
import com.c2cb.androidsdk.pojo.SuccessC2C;
import com.c2cb.androidsdk.pojo.TokenPojo;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager {

//    public static final String BASE_URL = "https://apis.vgroupinc.com/test_c2c_p82";
    public static final String BASE_URL = "https://dev-api-t10.vgroupinc.com/dev_c2c_p82";
//    public static final String BASE_IMAGE_URL = "https://0017-103-127-185-218.ngrok-free.app";
//public static final String BASE_URL = "https://apis.contexttocall.com/c2c"; // Prod
    public static final String android_version = "1.0";
    public void getModes(final NetworkEventListener listener, String channelId, String c2cPackage, ImageView call_icon, ImageView msg_icon, ImageView email_icon) {
        String url = C2CConstants.CHANNEL_MODES + channelId;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        headers.put("android_version", android_version);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, Method.GET.toString(), Method.GET, headers,Modes.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                Modes response = (Modes) obj;

                if (response.status == 200 && !response.channel.status.equalsIgnoreCase("inactive") && TextUtils.isEmpty(response.getChannel().versionerror)) {
                    call_icon.setVisibility(response.channel.callstats.enable ? View.VISIBLE : View.GONE);
                    msg_icon.setVisibility(response.channel.smsstats.enable ? View.VISIBLE : View.GONE);
                    email_icon.setVisibility(response.channel.emailstats.enable ? View.VISIBLE : View.GONE);
                    if (response.channel.callstats.enable){
                        new ImageLoadTask(channelId+"/connect.png" , call_icon).execute();
                    }
                    if (response.channel.smsstats.enable){
                        new ImageLoadTask(channelId+"/sms.png", msg_icon).execute();
                    }
                    if (response.channel.emailstats.enable){
                        new ImageLoadTask(channelId+"/email.png", email_icon).execute();
                    }
                } else {
                    call_icon.setVisibility(View.GONE);
                    msg_icon.setVisibility(View.GONE);
                    email_icon.setVisibility(View.GONE);
                }
                listener.OnSuccess(obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }


    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String key;
        private ImageView imageView;

        public ImageLoadTask(String key, ImageView imageView) {
            this.key = key;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(BASE_URL+C2CConstants.IMAGES+key);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public void getDeviceIP(final NetworkEventListener listener) {
        String url = C2CConstants.GEOCODE;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "prj_live_sk_569b6f639edde6120a26f703511c61aaecd3f7ef");
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(false,url, Method.GET.toString(), Method.GET, headers, C2CAddress.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess(obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }
    public void sendEmail(final NetworkEventListener listener, String data, String c2cPackage,String latLong) {
        String url = C2CConstants.SEND_EMAIL;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("c2c-latlong", latLong);
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, data, Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }

            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }
    public void sendSMS(final NetworkEventListener listener, String data, String c2cPackage, String latLong) {

        String url = C2CConstants.SEND_SMS;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("c2c-latlong", latLong);
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, data, Method.POST, headers, SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }

            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }
    public void verifyEmailOTP(final NetworkEventListener listener, String data, String c2cPackage) {

        String url = C2CConstants.VERIFY_EMAIL_OTP;
        HashMap<String, String> headers = new HashMap<>();

        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, data, Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }

            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }

    public void verifyMobileOTP(final NetworkEventListener listener, String data, String c2cPackage) {

        String url = C2CConstants.VERIFY_SMS_OTP;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");

        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, data, Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }

    public void getOTPForEmail(final NetworkEventListener listener, String channelId, String emailID, String c2cPackage) {

        String url = C2CConstants.EMAIL_OTP;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("channelid", channelId);
        data.put("email", emailID);

        JSONObject obj = new JSONObject(data);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, obj.toString(), Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }

    public void initiateCall(final NetworkEventListener listener, String data, Context context, String c2cPackage, String latLong) {

        String url = C2CConstants.INITIATE_CALL;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("c2c-latlong", latLong);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, data, Method.POST, headers, CallPojo.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                String callAuth = ((CallPojo) obj).callauth.id;
                getToken(listener, callAuth, c2cPackage, latLong);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }

    public void getToken(final NetworkEventListener listener, String authID,  String c2cPackage, String latLong) {

        String url = C2CConstants.GENERATE_TOKEN;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("c2c-latlong", latLong);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");

        HashMap<String, String> data = new HashMap<>();
        data.put("authId", authID);
        JSONObject obj = new JSONObject(data);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, obj.toString(), Method.POST, headers, TokenPojo.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((TokenPojo) obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }

    public void getOTPForSMS(final NetworkEventListener listener, String channelId, String code, String number, String c2cPackage) {

        String url = C2CConstants.SMS_OTP;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("channelid", channelId);
        data.put("countrycode", code);
        data.put("number", number);
        JSONObject obj = new JSONObject(data);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, obj.toString(), Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();
    }
    public void uploadImageToServer(NetworkEventListener listener, Uri imageUri, Activity activity, String c2cPackage, String channelID,String imageName,String imageFolder) {
        String url = C2CConstants.UPLOAD_IMAGES+ "?channelId=" + channelID + (TextUtils.isEmpty(imageFolder) ?"": "&imageFolder="+imageFolder+"&imageName="+imageName);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);

        Map<String, String> data = new HashMap<>();
        data.put("channelid", channelID);

        JSONObject obj = new JSONObject(data);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, Method.IMAGE_UPLOAD, headers, ImageUploadResponse.class, imageUri,activity,obj.toString(),new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((ImageUploadResponse) obj);
            }

            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();

    }


    public void deleteImage(NetworkEventListener listener, String channelId, String imageFolder, String imageName, String c2cPackage) {

        String url = C2CConstants.DELETE_IMAGE;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("request-package", c2cPackage);
        headers.put("Content-Type","application/json");
        headers.put("Accept", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("channelId", channelId);
        data.put("imageFolder", imageFolder);
        data.put("imageName", imageName);

        JSONObject obj = new JSONObject(data);
        HTTPRequestC2C requestHttp = new HTTPRequestC2C(url, obj.toString(), Method.POST, headers,SuccessC2C.class, new HTTPCallback() {
            @Override
            public void processFinish(Object obj) {
                listener.OnSuccess((SuccessC2C) obj);
            }
            @Override
            public void processFailed(int responseCode, String output) {
                Log.e("Response Failed", Integer.toString(responseCode) + " - " + output);
            }
        });
        requestHttp.execute();

    }

}

package com.raidzero.teamcitydownloader.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.QueryUtility;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.net.UnknownHostException;


/**
 * Created by posborn on 6/24/14.
 */
public class TeamCityTask extends AsyncTask<String, WebResponse, Void> {

    private static final String tag = "TeamCityTask";

    private QueryUtility queryUtility;
    OnWebRequestCompleteListener onWebRequestCompleteListener;

    public interface OnWebRequestCompleteListener {
        public void onWebRequestComplete(WebResponse response);
    }

    private WebResponse webResponse = null;
    private HttpGet httpGet;
    private String requestUrl;

/*
    public TeamCityTask(Activity activity, Fragment fragment) {
        onWebRequestCompleteListener = (OnWebRequestCompleteListener) fragment;
    }
*/
    public TeamCityTask(QueryUtility queryUtility) {
        this.queryUtility = queryUtility;
        Debug.Log(tag, "setting queryUtility as listener... null? " + (queryUtility == null));
        onWebRequestCompleteListener = (OnWebRequestCompleteListener) this.queryUtility;
    }

    public void setListener(Context context) {
        onWebRequestCompleteListener = (OnWebRequestCompleteListener) context;
    }
    public void setHttpGet(HttpGet httpGet, String requestUrl) {
        this.httpGet = httpGet;
        this.requestUrl = requestUrl;
    }

    @Override
    public void onCancelled() {
        Debug.Log(tag, "task cancelled: " + requestUrl);
        super.onCancelled();
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {

        String responseString = null;
        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpGet httpGet = this.httpGet;
            Debug.Log(tag, "doInBackground(" + requestUrl +")");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(baos);
                baos.close();
                responseString = baos.toString();
            } else {
                Debug.Log(tag, "response not ok: " + statusLine.getStatusCode());
            }

            webResponse = new WebResponse(statusLine, requestUrl, responseString);

        } catch (Exception e) {
            Debug.Log(tag, "error: ", e);
            webResponse = getErrorResponse(e);
        }

        Debug.Log(tag, "responseString: " + responseString);
        if (this.onWebRequestCompleteListener != null) {
            Debug.Log(tag, "task firing onWebRequestComplete()");
            this.onWebRequestCompleteListener.onWebRequestComplete(webResponse);
        }

        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    private WebResponse getErrorResponse(Exception e) {
        WebResponse rtn = new WebResponse(
                new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return 0;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "The server failed to respond";
                    }
                }, null, null);

        rtn.setReasonPhrase(e.getMessage());

        return rtn;
    }
}

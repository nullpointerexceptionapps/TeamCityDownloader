package com.raidzero.teamcitydownloader.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by posborn on 12/1/14.
 */
public class QueryBuildIdsTask extends AsyncTask<ArrayList<String>, HashMap<String, Integer>, Void> {
    private static final String tag = "QueryBuildIdsTask";
    private OnQueryBuildIdsCompleteListener mListener;

    private WebResponse webResponse = null;
    private TeamCityServer mServer;

    public interface OnQueryBuildIdsCompleteListener {
        void onQueryComplete(HashMap<String, Integer> values);
    }

    public QueryBuildIdsTask(Context context) {
        mListener = (OnQueryBuildIdsCompleteListener) context;
        AppHelper helper = (AppHelper) context.getApplicationContext();
        mServer = helper.getTcServer();
    }

    @Override
    protected Void doInBackground(ArrayList<String>... params) {
        HashMap<String, Integer> results = new HashMap<String, Integer>();

        HttpClient httpClient = new DefaultHttpClient();
        String serverRoot = mServer.getServerAddress();
        boolean useGuest = mServer.useGuest();

        for (String url : params[0]) {
            String authStr = useGuest ? "guestAuth" : "httpAuth";

            // only get ID's of successul builds and from any branch
            String requestUrl = serverRoot + url + "/builds/?locator=branch:default:any,status:success";

            HttpGet httpGet = new HttpGet(requestUrl);
            String responseString = null;
            int newBuildId = -1;
            if (!useGuest) {
                String credentials = mServer.getAuthStr();
                if (credentials != null) {
                    String username = credentials.split(":")[0];
                    String password = credentials.split(":")[1];
                    httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), "UTF-8", false));
                }
            }

            Debug.Log(tag, "doInBackground(" + url + ")");

            try {
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

                // parse response to get the most recent build id
                TeamCityXmlParser parser = new TeamCityXmlParser(responseString);

                NodeList rootNode = parser.getNodes("builds");
                Element buildNode = (Element) rootNode.item(0);

                Element lastBuild = (Element) buildNode.getFirstChild();
                newBuildId = Integer.valueOf(parser.getAttribute(lastBuild, "id"));

            } catch (NoHttpResponseException e) {
                Debug.Log(tag, "Server failed to respond");
                webResponse = getErrorResponse();
            } catch (HttpHostConnectException e) {
                webResponse = getErrorResponse();
            } catch (Exception e) {
                Debug.Log(tag, "error: ", e);
            }

            if (newBuildId > -1) {
                results.put(url, newBuildId);
            }
        }

        mListener.onQueryComplete(results);
        return null;
    }

    private WebResponse getErrorResponse() {
        webResponse = new WebResponse(
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

        return webResponse;
    }
}

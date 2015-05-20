package com.raidzero.teamcitydownloader.data;

import android.content.Context;

import com.raidzero.teamcitydownloader.R;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

/**
 * Created by posborn on 6/24/14.
 */
public class WebResponse {
    private String responseDocument;
    private StatusLine status;
    private String requestUrl;

    public WebResponse(StatusLine statusLine, String requestUrl, String response) {
        this.status = statusLine;
        this.requestUrl = requestUrl;
        this.responseDocument = response;
    }

    public int getStatusCode() {
        return this.status.getStatusCode();
    }

    public String getStatusReason(Context context) {
        // find the matching item in the web_response array
        String[] reasons = context.getResources().getStringArray(R.array.web_responses);

        for (String reason : reasons) {
            if (reason.startsWith(String.valueOf(status.getStatusCode()))) {
                return reason;
            }
        }

        return status.getReasonPhrase();
    }

    public void setReasonPhrase(final String reason) {
        final ProtocolVersion protoVersion = status.getProtocolVersion();
        final int statusCode = status.getStatusCode();

        status = new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return protoVersion;
            }

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getReasonPhrase() {
                return reason;
            }
        };
    }

    public String getResponseDocument() {
        return this.responseDocument;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }
}

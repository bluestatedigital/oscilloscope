/**
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluestatedigital.oscilloscope.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet for proxying an event stream.  Required to retrieve a remote Hystrix stream due to CORS issues.
 *
 * Also required when using Dropwizard due to a lack of proper, asynchronous streaming responses when following
 * the Dropwizard Jersey/resource approach.
 */
public class ProxyStreamServlet extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger(ProxyStreamServlet.class);

    public ProxyStreamServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Make sure the caller gave us a target to stream from.
        String target = request.getParameter("target");
        if (target == null) {
            response.setStatus(400);
            response.getWriter().print("required parameter 'target' not specified e.g. 1.2.3.4:7979");
            return;
        }

        target = target.trim();

        // Build a well-formed URL to go and retrieve.  We'll just assume HTTP here if they
        // didn't specify otherwise.  Probably a safe bet.
        StringBuilder targetUrlBuilder = new StringBuilder();
        if (!target.startsWith("http")) {
            targetUrlBuilder.append("http://");
        }
        targetUrlBuilder.append(target);

        String targetUrl = targetUrlBuilder.toString();

        // Now call out to the target and see what the deal is.
        HttpGet httpGetRequest = new HttpGet(targetUrl);
        HttpClient client = ProxyConnectionManager.getHttpClient();
        HttpResponse httpResponse = client.execute(httpGetRequest);

        // Set up our streams.
        InputStream is = httpResponse.getEntity().getContent();
        OutputStream os = response.getOutputStream();

        // Copy over all of the headers from the target response to our response.
        for (Header header : httpResponse.getAllHeaders()) {
            if (!HttpHeaders.TRANSFER_ENCODING.equals(header.getName())) {
                response.addHeader(header.getName(), header.getValue());
            }
        }

        // Mirror our target response's status code.
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        response.setStatus(statusCode);

        // Now shuttle all of the data from our target response to our response until we run
        // out of data or hit an exception.
        try {
            try {
                copyStream(is, os);
            } catch (Exception e) {
                if (e.getClass().getSimpleName().equalsIgnoreCase("ClientAbortException")) {
                } else {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
        } finally {
            // Abort our request first, and then close the stream.  If you do it in the reverse order,
            // apparently it can hang.
            try {
                httpGetRequest.abort();
            } catch (Exception e) {
            }

            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Copies from an input stream to an output stream in a chunked fashion.
     *
     * Runs until the input stream has been fully consumed or an exception is encountered.
     * @param is the input stream to read from
     * @param os the output stream to write to
     * @throws IOException
     */
    protected void copyStream(InputStream is, OutputStream os) throws IOException {
        int b;
        byte[] buf = new byte[4096];
        while ((b = is.read(buf, 0, buf.length)) != -1) {
            os.write(buf, 0, b);
            os.flush();
        }
    }

    private static class ProxyConnectionManager {
        private final static PoolingClientConnectionManager threadSafeConnectionManager = new PoolingClientConnectionManager();
        private final static HttpClient httpClient = new DefaultHttpClient(threadSafeConnectionManager);

        static {
            HttpParams httpParams = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            threadSafeConnectionManager.setDefaultMaxPerRoute(100);
            threadSafeConnectionManager.setMaxTotal(100);
        }

        private static HttpClient getHttpClient() {
            return httpClient;
        }
    }
}
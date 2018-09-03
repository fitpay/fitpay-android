package com.fitpay.android.utils;

/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fitpay.android.api.ApiManager;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import static okhttp3.internal.platform.Platform.INFO;

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a {@linkplain
 * OkHttpClient#networkInterceptors() network interceptor}. <p> The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
public final class HttpLogging implements Interceptor {
    public static final Charset UTF8 = Charset.forName("UTF-8");


    private static String TEST_NAME = null;
    public static void setTestName(String value){
        TEST_NAME = value;
        //FakeInterceptor.callList.clear();
        KeysManager.clear();
        ApiManager.clean();
    }

    public static String getTestName(){
        return TEST_NAME;
    }

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String message);

        /**
         * A {@link HttpLogging.Logger} defaults output appropriate for the current platform.
         */
        HttpLogging.Logger DEFAULT = new HttpLogging.Logger() {
            @Override
            public void log(String message) {
                Platform.get().log(INFO, message, null);
            }
        };
    }

    public HttpLogging() {
        this(HttpLogging.Logger.DEFAULT);
    }

    public HttpLogging(HttpLogging.Logger logger) {
        this.logger = logger;
    }

    private final HttpLogging.Logger logger;

    private volatile HttpLogging.Level level = Level.BODY;

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLogging setLevel(HttpLogging.Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public HttpLogging.Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpLogging.Level level = this.level;

        Request request = chain.request();
        if (level == HttpLogging.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == HttpLogging.Level.BODY;
        boolean logHeaders = logBody || level == HttpLogging.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        String requestStartMessage = "--> "
                + request.method()
                + ' ' + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.log(requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
//                    logger.log("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
//                    logger.log("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
//                    logger.log(name + ": " + headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
//                logger.log("--> END " + request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
//                logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

//                logger.log("");
                if (isPlaintext(buffer)) {
//                    logger.log(buffer.readString(charset));
//                    logger.log("--> END " + request.method()
//                            + " (" + requestBody.contentLength() + "-byte body)");
                } else {
//                    logger.log("--> END " + request.method() + " (binary "
//                            + requestBody.contentLength() + "-byte body omitted)");
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
//            logger.log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
//        logger.log("<-- "
//                + response.code()
//                + (response.message().isEmpty() ? "" : ' ' + response.message())
//                + ' ' + response.request().url()
//                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
//                logger.log(headers.name(i) + ": " + headers.value(i));
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
//                logger.log("<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
//                logger.log("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    GzipSource gzippedResponseBody = null;
                    try {
                        gzippedResponseBody = new GzipSource(buffer.clone());
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    } finally {
                        if (gzippedResponseBody != null) {
                            gzippedResponseBody.close();
                        }
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
//                    logger.log("");
//                    logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
//                    logger.log("");
                    String str = buffer.clone().readString(charset);
                    logger.log(str);



                    /*save to file*/
                    String url = request.url().host() + request.url().encodedPath();
                    String path = "d:\\fitpay\\" + TEST_NAME + "\\" + url.replace("https://", "").replace("http://", "").replace("/", "\\");

                    if(!path.endsWith(".json")) {
                        path = path + "\\" + request.method().toLowerCase() + "_" + getFileName(chain);
                    }

                    File file = new File((path));
                    file.getParentFile().mkdirs();

                    final String fileName = file.getName();
                    File[] files = file.getParentFile().listFiles((d, name) -> name.startsWith(fileName));

                    Integer endIndex = null;
                    if(files != null && files.length > 0){
                        String name = files[files.length - 1].getAbsolutePath();
                        String end = ".json_";
                        int index = name.lastIndexOf(end);
                        if(index > 0){
                            endIndex = Integer.valueOf(name.substring(index + end.length()));
                        } else {
                            endIndex = 0;
                        }
                    }
                    if(endIndex != null){


                        //CHECK FILE CONTENT
                        BufferedReader reader = new BufferedReader( new FileReader( file ) );
                        String fileStr = reader.lines().collect(Collectors.joining(System.lineSeparator()));

                        str = str.replace("\n ","").replace("\n","").replace("\r","");
                        fileStr = fileStr.replace("\n ","").replace("\n","").replace("\r","");

                        if(str.equals(fileStr)){
                            return response;
                        }




                        path = path + "_" + ++endIndex;
                        file = new File(path);
                    }

                    FileWriter fw = new FileWriter(file);
                    fw.write(str);
                    fw.flush();
                    fw.close();
                }

                if (gzippedLength != null) {
//                    logger.log("<-- END HTTP (" + buffer.size() + "-byte, "
//                            + gzippedLength + "-gzipped-byte body)");
                } else {
//                    logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
                }
            }
        }

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }

    private String getFileName(Chain chain) {
        String fileName = chain.request().url().pathSegments().get(chain.request().url().pathSegments().size() - 1);
        if(!fileName.isEmpty()){
            if(!fileName.endsWith(".json")){
                fileName = fileName.concat(".json");
            }
        } else {
            fileName = "index" + ".json";
        }
        return fileName;
    }
}

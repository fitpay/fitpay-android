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

import com.fitpay.android.TestConfig;
import com.fitpay.android.api.ApiManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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
    private static final String FILE_EXTENSION = ".json";

    private static String TEST_NAME = null;

    public static void setTestName(String value) {
        TEST_NAME = value;
        //FakeInterceptor.callList.clear();
        KeysManager.clear();
        ApiManager.clean();
    }

    public static String getTestName() {
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
        HttpLogging.Logger DEFAULT = message -> Platform.get().log(INFO, message, null);
    }

    public HttpLogging() {
        this(HttpLogging.Logger.DEFAULT);
    }

    public HttpLogging(HttpLogging.Logger logger) {
        this.logger = logger;
    }

    private final HttpLogging.Logger logger;

    private volatile HttpLogging.Level level = Level.BODY;

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

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            throw e;
        }

        ResponseBody responseBody = response.body();
        if (logHeaders) {
            Headers headers = response.headers();
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

            MockResponseData responseData = new MockResponseData();
            responseData.body = buffer.clone().readString(charset);
            responseData.code = response.code();
            responseData.message = response.message();

            String str = Constants.getGson().toJson(responseData);
            str = str.replaceAll("(\\\\n|\\\\r)\\s*", "");

            /*save to file*/
            String url = request.url().host() + request.url().encodedPath();
            String path = TestConfig.TESTS_FOLDER
                    .concat("SavedTests")
                    .concat(File.separator)
                    .concat(TEST_NAME)
                    .concat(File.separator)
                    .concat(url.replace("https://", "").replace("http://", "").replace("/", "\\"));

            if (!path.endsWith(FILE_EXTENSION)) {
                path = path + "\\" + request.method().toLowerCase() + "_" + getFileName(chain);
            }

            File file = new File((path));
            file.getParentFile().mkdirs();

            final String fileName = file.getName().substring(0, file.getName().indexOf(FILE_EXTENSION));
            File[] files = file.getParentFile().listFiles((d, name) -> name.startsWith(fileName));

            String pathWithoutExtension = path.substring(0, path.lastIndexOf(FILE_EXTENSION));

            Integer endIndex = null;
            if (files != null && files.length > 0) {
                String name = files[files.length - 1].getAbsolutePath();
                String nameWithoutExtension = name.substring(0, name.lastIndexOf(FILE_EXTENSION));
                String extension = nameWithoutExtension.substring(pathWithoutExtension.length());
                if (extension.length() > 1 && extension.startsWith("_")) {
                    extension = extension.substring(1);
                    endIndex = Integer.valueOf(extension);
                } else {
                    endIndex = 0;
                }
            }
            if (endIndex != null) {
                //CHECK FILE CONTENT
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String fileStr = reader.lines().collect(Collectors.joining(System.lineSeparator()));

                //str = str.replace("\n ","").replace("\n","").replace("\r","");
                fileStr = fileStr.replaceAll("(\\\\n|\\\\r)\\s*", "");

                if (str.equals(fileStr)) {
                    return response;
                }


                path = pathWithoutExtension + "_" + ++endIndex + FILE_EXTENSION;
                file = new File(path);
            }

            FileWriter fw = new FileWriter(file);
            fw.write(str);
            fw.flush();
            fw.close();
        }

        return response;
    }

    private String getFileName(Chain chain) {
        String fileName = chain.request().url().pathSegments().get(chain.request().url().pathSegments().size() - 1);
        if (!fileName.isEmpty()) {
            if (!fileName.endsWith(FILE_EXTENSION)) {
                fileName = fileName.concat(FILE_EXTENSION);
            }
        } else {
            fileName = "index" + FILE_EXTENSION;
        }
        return fileName;
    }
}

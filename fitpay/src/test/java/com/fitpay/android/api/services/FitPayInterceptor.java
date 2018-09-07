package com.fitpay.android.api.services;

import com.fitpay.android.TestConfig;
import com.fitpay.android.TestConstants;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.MockResponseData;
import com.fitpay.android.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.fitpay.android.utils.HttpLogging.getTestName;

public abstract class FitPayInterceptor implements Interceptor {

    private static final String TAG = FitPayInterceptor.class.getSimpleName();
    private static final String FILE_EXTENSION = ".json";

    private String mContentType = "application/json";

    public static Map<String, Integer> callList = new HashMap<>();

    public Response getResponse(Chain chain, Request request) throws IOException {

        if (TestConstants.testConfig.useRealTests()) {
            return chain.proceed(request);
        }

        String method = chain.request().method().toLowerCase();

        Response response = null;
        // Get Request URI.
        final URI uri = chain.request().url().uri();

        if (TestConstants.testConfig.showHTTPLogs()) {
            FPLog.d(TAG, "--> Request url: [" + method.toUpperCase() + "]" + uri.toString());
        }

        String url = chain.request().url().host() + chain.request().url().encodedPath();
        String path = TestConfig.TESTS_FOLDER
                .concat(TestConstants.testConfig.testsVersion())
                .concat(File.separator)
                .concat(getTestName())
                .concat(File.separator)
                .concat(url.replace("https://", "").replace("http://", "").replace("/", "\\"));

        if (!path.endsWith(".json")) {
            path = path + "\\" + chain.request().method().toLowerCase() + "_" + getFileName(chain);
        }

        if (callList.containsKey(path)) {
            int index = callList.get(path) + 1;
            callList.replace(path, index);
            String pathWithoutExtension = path.substring(0, path.lastIndexOf(FILE_EXTENSION));
            path = pathWithoutExtension.concat("_").concat(String.valueOf(index)).concat(FILE_EXTENSION);
        } else {
            callList.put(path, 0);
        }

        String fileStr = getFile(path);
        if (!StringUtils.isEmpty(fileStr)) {
            MockResponseData responseData = Constants.getGson().fromJson(fileStr, MockResponseData.class);

            if (TestConstants.testConfig.showHTTPLogs()) {
                FPLog.d(TAG, "Response: " + fileStr);
            }

            response = new Response.Builder()
                    .code(responseData.code)
                    .message(responseData.message)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse(mContentType), responseData.body.getBytes()))
                    .addHeader("content-type", mContentType)
                    .build();
        } else {
            response = chain.proceed(chain.request());
        }

        if (TestConstants.testConfig.showHTTPLogs()) {
            FPLog.d(TAG, "<-- END [" + method.toUpperCase() + "]" + uri.toString());
        }
        return response;
    }

    private String getFileName(Chain chain) {
        String fileName = chain.request().url().pathSegments().get(chain.request().url().pathSegments().size() - 1);
        if (!fileName.isEmpty()) {
            if (!fileName.endsWith(".json")) {
                fileName = fileName.concat(FILE_EXTENSION);
            }
        } else {
            fileName = "index" + FILE_EXTENSION;
        }
        return fileName;
    }

    private String getFile(String fileName) {
        File file = new File(fileName);

        BufferedReader reader = null;
        String fileStr = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            fileStr = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (FileNotFoundException e) {
            FPLog.e(TAG, e);
        }

        return fileStr;
    }
}

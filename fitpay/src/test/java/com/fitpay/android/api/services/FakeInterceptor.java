/*
 * Copyright (C) 2016. Tien Hoang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fitpay.android.api.services;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.fitpay.android.utils.HttpLogging.getTestName;

/**
 *
 */
public class FakeInterceptor implements Interceptor {
    private static final String TAG = FakeInterceptor.class.getSimpleName();
    private static final String FILE_EXTENSION = ".json";

    private String mContentType = "application/json";

    public static Map<String, Integer> callList = new HashMap<>();

    public FakeInterceptor() {
    }

    /**
     * Set content type for header
     *
     * @param contentType Content type
     * @return FakeInterceptor
     */
    public FakeInterceptor setContentType(String contentType) {
        mContentType = contentType;
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        List<String> listSuggestionFileName = new ArrayList<>();
        String method = chain.request().method().toLowerCase();

        Response response = null;
        // Get Request URI.
        final URI uri = chain.request().url().uri();
        Log.d(TAG, "--> Request url: [" + method.toUpperCase() + "]" + uri.toString());




        String url = chain.request().url().host() + chain.request().url().encodedPath();
        String path = "d:\\fitpay\\" + getTestName() + "\\" + url.replace("https://", "").replace("http://", "").replace("/", "\\");

        if(!path.endsWith(".json")) {
            path = path + "\\" + chain.request().method().toLowerCase() + "_" + getFileName(chain);
        }


        if(callList.containsKey(path)){
            int index = callList.get(path) + 1;
            callList.replace(path, index);
            path = path.concat("_").concat(String.valueOf(index));
        } else {
            callList.put(path, 0);
        }

        String responseFileName = path;

/*
        String defaultFileName = getFileName(chain);

        //create file name with http method
        //eg: getLogin.json
        listSuggestionFileName.add(method + upCaseFirstLetter(defaultFileName));

        //eg: login.json
        listSuggestionFileName.add(defaultFileName);

        String responseFileName = getFirstFileNameExist(listSuggestionFileName, uri);*/
        if (responseFileName != null) {
            //String fileName = getFilePath(uri, responseFileName);
            //Log.d(TAG, "Read data from file: " + fileName);
//            try {
            String fileStr = getFile(responseFileName);
                /*
                InputStream is = mContext.getAssets().open(fileName);
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder responseStringBuilder = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    responseStringBuilder.append(line).append('\n');
                }
                String fileStr = responseStringBuilder.toString();
                */
            Log.d(TAG, "Response: " + fileStr);
            response = new Response.Builder()
                    .code(200)
                    .message(fileStr)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse(mContentType), fileStr.getBytes()))
                    .addHeader("content-type", mContentType)
                    .build();
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage(), e);
//            }
        } else {
            for (String file : listSuggestionFileName) {
                Log.e(TAG, "File not exist: " + getFilePath(uri, file));
            }
            response = chain.proceed(chain.request());
        }

        Log.d(TAG, "<-- END [" + method.toUpperCase() + "]" + uri.toString());
        return response;
    }

    private String upCaseFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getFirstFileNameExist(List<String> inputFileNames, URI uri) throws IOException {
        String mockDataPath = uri.getHost() + uri.getPath();
        mockDataPath = mockDataPath.substring(0, mockDataPath.lastIndexOf('/'));
        Log.d(TAG, "Scan files in: " + mockDataPath);
        //List all files in folder
        //String[] files = mContext.getAssets().list(mockDataPath);

        List<String> files = getResourceFiles(mockDataPath);

        for (String fileName : inputFileNames) {
            if (fileName != null) {
                for (String file : files) {
                    if (fileName.equals(file)) {
                        return fileName;
                    }
                }
            }
        }
        return null;
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

    private String getFilePath(URI uri, String fileName) {
        String path;
        if (uri.getPath().lastIndexOf('/') != uri.getPath().length() - 1) {
            path = uri.getPath().substring(0, uri.getPath().lastIndexOf('/') + 1);
        } else {
            path = uri.getPath();
        }
        return uri.getHost() + path + fileName;
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        //File file = new File(classLoader.getResource(fileName).getFile());

        File file = new File(fileName);

        BufferedReader reader = null;
        String fileStr = null;
        try {
            reader = new BufferedReader( new FileReader( file ) );
            fileStr = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (FileNotFoundException e) {
            fileStr = "No Message";
//            e.printStackTrace();
        }

        return fileStr;

        /*
        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
*/
    }
}
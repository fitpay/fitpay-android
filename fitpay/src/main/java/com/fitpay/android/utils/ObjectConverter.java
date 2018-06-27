package com.fitpay.android.utils;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vlad on 11.03.2016.
 */
final public class ObjectConverter {

    public static <T> Map<String, Object> convertToSimpleMap(T object) {
        Gson gson = new Gson();

        JsonElement objectAsJson = gson.toJsonTree(object);
        LinkedTreeMap objectAsMap = gson.fromJson(objectAsJson, LinkedTreeMap.class);

        Map<String, Object> resultMap = new HashMap<>();
        iterateThroughMap("", objectAsMap, resultMap);

        return resultMap;
    }

    private static void iterateThroughMap(String initialKeyName, LinkedTreeMap treeMap, Map<String, Object> resultMap) {
        for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>) treeMap.entrySet()) {
            if (entry.getValue() instanceof LinkedTreeMap) {
                iterateThroughMap(constructLevelKey(initialKeyName, entry.getKey()), (LinkedTreeMap) entry.getValue(), resultMap);
            } else {
                resultMap.put(constructLevelKey(initialKeyName, entry.getKey()), entry.getValue());
            }
        }
    }

    private static String constructLevelKey(String initialKeyName, String currentEntryKey) {
        return StringUtils.isEmpty(initialKeyName) ? "/" + currentEntryKey : initialKeyName + "/" + currentEntryKey;
    }
}

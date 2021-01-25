package jp.co.ohq.blesampleomron.controller.util;

import android.support.annotation.NonNull;
import android.util.AndroidRuntimeException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Converter {

    @NonNull
    public static String toJson(@NonNull Map<String, String> input) {
        final String ret;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ret = mapper.writeValueAsString(input);
        } catch (IOException e) {
            throw new AndroidRuntimeException(e.getMessage());
        }
        return ret;
    }

    @NonNull
    public static String toJson(@NonNull List<Map<String, String>> input) {
        final String ret;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ret = mapper.writeValueAsString(input);
        } catch (IOException e) {
            throw new AndroidRuntimeException(e.getMessage());
        }
        return ret;
    }

    @NonNull
    public static Map<String, String> toMap(@NonNull String jsonString) {
        final Map<String, String> ret;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ret = mapper.readValue(jsonString, new TypeReference<LinkedHashMap<String, String>>() {
            });
        } catch (IOException e) {
            throw new AndroidRuntimeException(e.getMessage());
        }
        return ret;
    }

    @NonNull
    public static List<Map<String, String>> toMapList(@NonNull String jsonString) {
        final List<Map<String, String>> ret;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ret = mapper.readValue(jsonString, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (IOException e) {
            throw new AndroidRuntimeException(e.getMessage());
        }
        return ret;
    }
}

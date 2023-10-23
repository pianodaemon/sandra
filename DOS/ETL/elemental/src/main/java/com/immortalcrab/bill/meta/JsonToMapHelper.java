package com.immortalcrab.bill.meta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;


@Getter
@AllArgsConstructor
public class JsonToMapHelper {

    protected final @NonNull
    Map<String, Object> ds;

    protected JsonToMapHelper(InputStreamReader reader) throws IOException {
        this(JsonToMapHelper.readFromReader(reader));
    }

    public static Map<String, Object> readFromReader(InputStreamReader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        TypeReference<Map<String, Object>> tr = new TypeReference<Map<String, Object>>() {
        };

        return (mapper.readValue(reader, tr));
    }
}

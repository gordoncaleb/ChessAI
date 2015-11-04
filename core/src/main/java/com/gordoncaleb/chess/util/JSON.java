package com.gordoncaleb.chess.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;

public class JSON {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    static{
        objectMapper.registerModule(new Jdk8Module());
    }

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static <T> T fromJSON(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    public static String toJSON(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }


}

package com.company;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DictEntry {
    private final LinkedHashMap<String, String> dict;

    DictEntry(String[] cols, String[] values) {
        dict = IntStream
                .range(0, cols.length)
                .boxed()
                .collect(Collectors.toMap(i -> cols[i], i -> values[i], (i, j) -> i, LinkedHashMap::new));
    }

    public String getValue(String key) {
        return dict.get(key);
    }

    public Set<String> getCols() {
        return dict.keySet();
    }
}

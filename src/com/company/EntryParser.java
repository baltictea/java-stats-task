package com.company;

import java.util.List;
import java.util.stream.Collectors;

public class EntryParser {

    public static List<DictEntry> DictParse(String[] columns, List<String> lines) {
        return lines.stream()
                .skip(1)
                .map(line -> line.split(","))
                .map(v -> new DictEntry(columns, v))
                .collect(Collectors.toList());
    }
}

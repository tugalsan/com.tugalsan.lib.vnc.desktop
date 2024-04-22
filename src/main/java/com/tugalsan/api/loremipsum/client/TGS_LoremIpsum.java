package com.tugalsan.api.loremipsum.client;

import com.tugalsan.api.list.client.*;
import java.util.*;
import java.util.stream.*;
import com.tugalsan.api.random.client.*;

public class TGS_LoremIpsum {

    public static StringBuilder addWords(StringBuilder sb, int wordCount) {
        var words = TGS_ListUtils.of("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.".split("\\s"));
        Collections.shuffle(words);
        var size = words.size();
        var spc = ' ';
        IntStream.range(0, wordCount).forEachOrdered(i -> sb.append(words.get(i % size)).append(spc));
        return sb;
    }

    public static StringBuilder addWords(StringBuilder sb, int minWordCount, int maxWordCount) {
        return TGS_LoremIpsum.addWords(sb, TGS_RandomUtils.nextInt(minWordCount, maxWordCount));
    }

    public static StringBuilder getWords(int wordCount) {
        var sb = new StringBuilder();
        return TGS_LoremIpsum.addWords(sb, wordCount);
    }

    public static StringBuilder getWords(int minWordCount, int maxWordCount) {
        var sb = new StringBuilder();
        return addWords(sb, minWordCount, maxWordCount);
    }
}

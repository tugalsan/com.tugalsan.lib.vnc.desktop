package com.tugalsan.api.console.client;

import java.util.ArrayList;
import java.util.List;

public class TGS_ConsoleUtils {

    public static List<String> parseLine(String line) {
        List<String> tokens = new ArrayList();
        var cpSpace = " ".codePointAt(0);
        var cpDoubleQuote = "\"".codePointAt(0);
        var sb = new StringBuilder();
        for (var i = 0; i < line.length(); i++) {
            var cpCur = line.codePointAt(i);
            var cpCurIsSpace = cpCur == cpSpace;
            var newTokenStartsWithDoubleQuote = !sb.isEmpty() && sb.codePointAt(0) == cpDoubleQuote;
            var newTokenEndsWithDoubleQuote = sb.length() > 1 && sb.codePointAt(sb.length() - 1) == cpDoubleQuote;
            var newTokenMessageComplete = newTokenStartsWithDoubleQuote && newTokenEndsWithDoubleQuote;
            if (cpCurIsSpace && newTokenMessageComplete) {
                if (sb.length() == 2) {
                    tokens.add("");
                    continue;
                }
                var newToken = sb.substring(1, sb.length() - 1);
                tokens.add(newToken);
                sb.setLength(0);
                continue;
            }
            if (cpCurIsSpace && newTokenStartsWithDoubleQuote) {
                sb.appendCodePoint(cpCur);
                continue;
            }
            if (cpCurIsSpace) {
                var newToken = sb.toString();
                tokens.add(newToken);
                sb.setLength(0);
                continue;
            }
            sb.appendCodePoint(cpCur);
        }
        if (!sb.isEmpty()) {
            var newTokenStartsWithDoubleQuote = !sb.isEmpty() && sb.codePointAt(0) == cpDoubleQuote;
            var newTokenEndsWithDoubleQuote = sb.length() > 1 && sb.codePointAt(sb.length() - 1) == cpDoubleQuote;
            var newTokenMessageComplete = newTokenStartsWithDoubleQuote && newTokenEndsWithDoubleQuote;
            if (newTokenMessageComplete) {
                var newToken = sb.substring(1, sb.length() - 1);
                tokens.add(newToken);
            } else {
                tokens.add(sb.toString());
            }
        }
        return tokens;
    }
}

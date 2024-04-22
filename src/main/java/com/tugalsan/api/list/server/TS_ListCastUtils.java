package com.tugalsan.api.list.server;

import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class TS_ListCastUtils {

    public List<String> toList(StringTokenizer st) {
        return TGS_StreamUtils.toLst(
                Collections.list(st).stream()
                        .map(token -> (String) token)
        );
    }

    public static List<String> toString(StringTokenizer input) {
        if (input == null) {
            return null;
        }
        List<String> lst = TGS_ListUtils.of();
        while (input.hasMoreTokens()) {
            lst.add(input.nextToken());
        }
        return lst;
    }
}

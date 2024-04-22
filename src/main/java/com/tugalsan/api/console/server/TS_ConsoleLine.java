package com.tugalsan.api.console.server;

import com.tugalsan.api.list.client.TGS_ListCastUtils;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import java.util.stream.IntStream;

public class TS_ConsoleLine {

    public String commandName;
    public CharSequence[] commadArgs;

    private TS_ConsoleLine(String... mainArguments) {
        commandName = mainArguments[0];
        this.commadArgs = TGS_ListCastUtils.toArrayCharSequence(
                TGS_StreamUtils.toLst(
                        IntStream.range(0, mainArguments.length)
                                .filter(i -> i != 0)
                                .mapToObj(i -> (CharSequence) mainArguments[i])
                )
        );
    }

    public static TS_ConsoleLine of(String... mainArguments) {
        return new TS_ConsoleLine(mainArguments);
    }
}

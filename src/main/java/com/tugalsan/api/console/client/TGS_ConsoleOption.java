package com.tugalsan.api.console.client;

import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import java.util.List;

public class TGS_ConsoleOption {

    private TGS_ConsoleOption(TGS_RunnableType2<CharSequence, List<CharSequence>> run, List<String> alias) {
        this.run = run;
        this.alias = alias;
    }
    final public TGS_RunnableType2<CharSequence, List<CharSequence>> run;
    final public List<String> alias;

    public static TGS_ConsoleOption of(TGS_RunnableType2<CharSequence, List<CharSequence>> run, List<String> alias) {
        return new TGS_ConsoleOption(run, alias);
    }

    public static TGS_ConsoleOption of(TGS_RunnableType2<CharSequence, List<CharSequence>> run, String... alias) {
        return of(run, TGS_ListUtils.of(alias));
    }

    public boolean is(CharSequence cmdName) {
        return alias.stream()
                .filter(a -> TGS_CharSetCast.equalsLocaleIgnoreCase(a, cmdName))
                .findAny().isPresent();
    }

    @Override
    public String toString() {
        return TGS_ConsoleOption.class.getSimpleName() + "{" + "alias=" + alias + '}';
    }
}

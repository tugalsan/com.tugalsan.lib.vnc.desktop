package com.tugalsan.api.desktop.server;

import java.awt.Component;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.JOptionPane;

public class TS_DesktopDialogInputListUtils {

    public static Optional<Integer> show(Component parent, String title, String message, int defaultIdx, String... options) {
        return show(parent, title, message, defaultIdx, List.of(options));
    }

    public static Optional<Integer> show(Component parent, String title, String message, int defaultIdx, List<String> options) {
        if (options.isEmpty()) {
            return Optional.empty();
        }
        if (defaultIdx < 0) {
            defaultIdx = 0;
        }
        if (defaultIdx > options.size()) {
            return Optional.empty();
        }
        var result = JOptionPane.showInputDialog(
                parent,
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options.toArray(String[]::new),
                options.get(defaultIdx)
        );
        return result == null
                ? Optional.empty()
                : IntStream.range(0, options.size())
                        .filter(i -> Objects.equals(options.get(i), result))
                        .mapToObj(i -> Optional.of(i))
                        .findAny().orElse(Optional.empty());
    }
}

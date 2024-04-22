package com.tugalsan.api.desktop.server;

import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class TS_DesktopPathUtils {

    public static Path currentFolder() {
        return Path.of("").toAbsolutePath().normalize();
    }

    public static enum Type {
        DIRECTORIES_ONLY, FILES_ONLY, FILES_AND_DIRECTORIES
    }

    public static Optional<Path> chooseFiles(String title, Optional<Path> initFolder, String... acceptedFileTypes) {
        return choose(title, initFolder, Type.FILES_ONLY, acceptedFileTypes);
    }

    public static Optional<Path> chooseFileOrDirectory(String title, Optional<Path> initFolder) {
        return choose(title, initFolder, Type.FILES_AND_DIRECTORIES);
    }

    public static Optional<Path> chooseDirectory(String title, Optional<Path> initFolder) {
        return choose(title, initFolder, Type.DIRECTORIES_ONLY);
    }

    private static Optional<Path> choose(String title, Optional<Path> initFolder, Type type, String... acceptedFileTypes) {
        var c = new JFileChooser();
        c.setDialogTitle(title);
        c.setCurrentDirectory(initFolder.isEmpty() ? new File(".") : initFolder.get().toFile());
        switch (type) {
            case DIRECTORIES_ONLY ->
                c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            case FILES_ONLY ->
                c.setFileSelectionMode(JFileChooser.FILES_ONLY);
            default ->
                c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        if (acceptedFileTypes.length == 0) {
            c.setAcceptAllFileFilterUsed(false);
        } else {
            c.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    return "👓";
                }

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    var filenameLowerCase = TGS_CharSetCast.toLocaleLowerCase(f.getName());
                    return Arrays.stream(acceptedFileTypes)
                            .map(ft -> TGS_CharSetCast.toLocaleLowerCase(ft))
                            .filter(ft -> filenameLowerCase.endsWith("." + ft))
                            .findAny().isPresent();
                }
            });
        }
        return c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                ? Optional.of(c.getSelectedFile().toPath())
                : Optional.empty();
    }

    public static Optional<Path> save(String title, Optional<Path> initFolder) {
        var c = new JFileChooser();
        c.setDialogTitle(title);
        c.setCurrentDirectory(initFolder.isEmpty() ? new File(".") : initFolder.get().toFile());
        return c.showSaveDialog(null) == JFileChooser.APPROVE_OPTION
                ? Optional.of(c.getSelectedFile().toPath())
                : Optional.empty();
    }

    public static boolean run(Path file) {
        return TGS_UnSafe.call(() -> {
            Desktop.getDesktop().open(file.toFile());
            return true;
        }, e -> {
            e.printStackTrace();
            return false;
        });
    }
}

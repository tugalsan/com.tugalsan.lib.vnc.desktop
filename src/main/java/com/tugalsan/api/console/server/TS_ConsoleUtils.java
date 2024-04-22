package com.tugalsan.api.console.server;

import com.tugalsan.api.console.client.TGS_ConsoleOption;
import com.tugalsan.api.console.client.TGS_ConsoleUtils;
import com.tugalsan.api.input.server.TS_InputKeyboardUtils;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.IntStream;

public class TS_ConsoleUtils {

    private final static TS_Log d = TS_Log.of(TS_ConsoleUtils.class);

    public static void bindToOutputStream(OutputStream os) {
        var con = new PrintStream(os);
        System.setOut(con);
        System.setErr(con);
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void mainLoop(List<String> quitCommands, List<String> clearScreen, List<TGS_ConsoleOption> runOptions, final CharSequence... initCmdAndArguments) {
        var runHelp = TGS_ConsoleOption.of((cmd, args) -> {
            runOptions.forEach(ro -> d.cr("help", ro.toString()));
        }, TGS_ListUtils.of("h", "help"));
        var runQuit = TGS_ConsoleOption.of((cmd, args) -> {
            //NOTHING
        }, quitCommands);
        var runCls = TGS_ConsoleOption.of((cmd, args) -> {
            //NOTHING
        }, quitCommands);
        var runUnknown = TGS_ConsoleOption.of((cmd, args) -> {
            d.ce("mainLoop", "ERROR: dont know what 2 do with args:");
            d.ci("mainLoop", "firstArg", cmd);
            IntStream.range(0, args.size()).forEachOrdered(i -> {
                d.ci("mainLoop", "restArgs", i, args.get(i));
            });
        }, quitCommands);
        TS_ConsoleUtils.clearScreen();
        if (initCmdAndArguments != null && initCmdAndArguments.length > 0) {
            var fullInitCmd = String.join(" ", initCmdAndArguments);
            var fullInitCmd_ParsedLine = TGS_ConsoleUtils.parseLine(fullInitCmd);
            if (!fullInitCmd_ParsedLine.isEmpty()) {
                var fullInitCmd_ParsedList = TGS_ListUtils.sliceFirstToken(TGS_StreamUtils.toLst(fullInitCmd_ParsedLine.stream().map(s -> (CharSequence) s)));
                if (runQuit.is(fullInitCmd_ParsedList.value0)) {
                    return;
                }
                if (runHelp.is(fullInitCmd_ParsedList.value0)) {
                    runHelp.run.run(fullInitCmd_ParsedList.value0, fullInitCmd_ParsedList.value1);
                }
                var selectedCustomRun = runOptions.stream().filter(runCustom -> runCustom.is(fullInitCmd_ParsedList.value0))
                        .findFirst().orElse(null);
                if (selectedCustomRun == null) {
                    runUnknown.run.run(fullInitCmd_ParsedList.value0, fullInitCmd_ParsedList.value1);
                } else {
                    selectedCustomRun.run.run(fullInitCmd_ParsedList.value0, fullInitCmd_ParsedList.value1);
                }
            }
        }
        while (true) {
            d.cr("main", "newCommand:");
            var line = TS_InputKeyboardUtils.readLineFromConsole().trim();
            TS_ConsoleUtils.clearScreen();
            d.cr("main", "givenCommand", line);
            var parsedLine = TGS_ConsoleUtils.parseLine(line);
            var parsedList = TGS_ListUtils.sliceFirstToken(TGS_StreamUtils.toLst(parsedLine.stream().map(s -> (CharSequence) s)));
            if (runQuit.is(parsedList.value0)) {
                return;
            }
            if (runCls.is(parsedList.value0)) {
                continue;
            }
            if (runHelp.is(parsedList.value0)) {
                runHelp.run.run(parsedList.value0, parsedList.value1);
            }
            var selectedCustomRun = runOptions.stream().filter(runCustom -> runCustom.is(parsedList.value0))
                    .findFirst().orElse(null);
            if (selectedCustomRun == null) {
                runUnknown.run.run(parsedList.value0, parsedList.value1);
            } else {
                selectedCustomRun.run.run(parsedList.value0, parsedList.value1);
            }
        }
    }

}

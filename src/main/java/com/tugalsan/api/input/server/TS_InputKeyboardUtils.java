package com.tugalsan.api.input.server;

import com.tugalsan.api.unsafe.client.*;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.Scanner;
import java.util.stream.IntStream;

public class TS_InputKeyboardUtils {

    public static String readLineFromConsole() {
        return new Scanner(System.in).nextLine();
    }

    public static void typeF(int i) {
        switch (i) {
            case 1 ->
                typeKeyEvent(KeyEvent.VK_F1);
            case 2 ->
                typeKeyEvent(KeyEvent.VK_F2);
            case 3 ->
                typeKeyEvent(KeyEvent.VK_F3);
            case 4 ->
                typeKeyEvent(KeyEvent.VK_F4);
            case 5 ->
                typeKeyEvent(KeyEvent.VK_F5);
            case 6 ->
                typeKeyEvent(KeyEvent.VK_F6);
            case 7 ->
                typeKeyEvent(KeyEvent.VK_F7);
            case 8 ->
                typeKeyEvent(KeyEvent.VK_F8);
            case 9 ->
                typeKeyEvent(KeyEvent.VK_F9);
            case 10 ->
                typeKeyEvent(KeyEvent.VK_F10);
            case 11 ->
                typeKeyEvent(KeyEvent.VK_F11);
            case 12 ->
                typeKeyEvent(KeyEvent.VK_F12);
        }
    }

    public static void typeUp() {
        typeKeyEvent(KeyEvent.VK_UP);
    }

    public static void typeDown() {
        typeKeyEvent(KeyEvent.VK_DOWN);
    }

    public static void typeLeft() {
        typeKeyEvent(KeyEvent.VK_LEFT);
    }

    public static void typeRight() {
        typeKeyEvent(KeyEvent.VK_RIGHT);
    }

    public static void typeTab() {
        typeKeyEvent(KeyEvent.VK_TAB);
    }

    public static void typeDelRight() {
        typeKeyEvent(KeyEvent.VK_DELETE);
    }

    public static void typeDelLeft() {
        typeKeyEvent(KeyEvent.VK_BACK_SPACE);
    }

    public static void typeSpace() {
        typeKeyEvent(KeyEvent.VK_SPACE);
    }

    public static void typeEsc() {
        typeKeyEvent(KeyEvent.VK_ESCAPE);
    }

    public static void typeEnter() {
        typeKeyEvent(KeyEvent.VK_ENTER);
    }

    public static void typeTab(int count) {
        IntStream.range(0, count).forEachOrdered(i -> typeKeyEvent(KeyEvent.VK_TAB));
    }

    public static void typeKeyEvent(int keyEvent) {
        TGS_UnSafe.run(() -> {
            var robot = TS_InputCommonUtils.robot();
            robot.keyPress(keyEvent);
            robot.keyRelease(keyEvent);
        });
    }

    public static void pressKeyEvent(int keyEvent) {
        TGS_UnSafe.run(() -> {
            var robot = TS_InputCommonUtils.robot();
            robot.keyPress(keyEvent);
        });
    }

    public static void releaseKeyEvent(int keyEvent) {
        TGS_UnSafe.run(() -> {
            var robot = TS_InputCommonUtils.robot();
            robot.keyRelease(keyEvent);
        });
    }

    public static void typeString(String text) {
        toClipboard(text);
        fromClipboard();
    }

    public static void fromClipboard() {
        TGS_UnSafe.run(() -> {
            var robot = TS_InputCommonUtils.robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        });
    }

    public static void toClipboard(CharSequence text) {
        var selection = new StringSelection(text.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

}

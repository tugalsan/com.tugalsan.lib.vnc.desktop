package com.tugalsan.api.log.client;

//import elemental2.dom.*;

public class TGC_LogUtils {
    
//    public static void plain(CharSequence text) {//NO NEED TO ADD a dep for a func
//        DomGlobal.console.log(text); 
//    }

    public static native void plain(CharSequence text) /*-{console.log(String(text));}-*/;
    
    public static native void error(CharSequence text) /*-{console.log('%c ' + String(text), 'color: red; font-weight: bold; background-color: #242424;');}-*/;

    public static native void info(CharSequence text) /*-{console.log('%c ' + String(text), 'color: gray; font-weight: bold; background-color: #242424;');}-*/;

    public static native void link(CharSequence text) /*-{console.log('%c ' + String(text), 'color: blue; font-weight: bold; background-color: #242424;');}-*/;

    public static native void result(CharSequence text) /*-{console.log('%c ' + String(text), 'color: green; font-weight: bold; background-color: #242424;');}-*/;

    public static native void hidden(CharSequence text) /*-{console.log('%c ' + String(text), 'color: #242424; font-weight: bold; background-color: #242424;');}-*/;
}

package com.tugalsan.api.charset.client;

import java.nio.charset.*;

public class TGS_CharSet {

//    public static String ISO_TURKISH() {return "ISO_8859_9";}
    public static String IBM_TURKISH() {//USED BY TS_FilePdfItext.getFontInternal
        return "Cp857";
    }

    public static Charset of(CharSequence charsetName) {
        return Charset.availableCharsets().get(charsetName.toString());
    }
}

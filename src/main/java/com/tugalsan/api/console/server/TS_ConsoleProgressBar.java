package com.tugalsan.api.console.server;

import com.tugalsan.api.charset.client.TGS_CharSetUTF8;
import com.tugalsan.api.coronator.client.TGS_Coronator;
import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.math.client.TGS_MathUtils;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TS_ConsoleProgressBar {

//    private final static TS_Log d = TS_Log.of(true, TS_ConsoleProgressBar.class);

    public static enum Style {
        PERCENTAGE, STEP
    }

    private int calculatePercentageFor(int stepPossible) {
        return TGS_MathUtils.percentageValueInt(stepPossible, stepSize);
    }

    private String calculateStyleFor(int stepPossible) {
        return style == Style.STEP
                ? String.valueOf(stepPossible) + "/" + String.valueOf(stepSize)
                : String.valueOf(calculatePercentageFor(stepPossible)) + "%";
    }

    private String formatStyle() {
        formmattedStyleLabel.setLength(0);
        formmattedStyleLabel.append(calculateStyleFor(stepCurrent));
        while (styleLabelPrefix.length() + styleLabelSuffix.length() + formmattedStyleLabel.length() < styleSize) {
            formmattedStyleLabel.insert(0, ' ');
        }
        formmattedStyleLabel.insert(0, styleLabelPrefix);
        formmattedStyleLabel.append(styleLabelSuffix);
        return formmattedStyleLabel.toString();
    }
    final StringBuilder formmattedStyleLabel = new StringBuilder();

    private TS_ConsoleProgressBar(int stepSize, Style style, int labelSize) {
        this.stepCurrent = 0;
        this.stepSize = stepSize;
        this.labelSize = labelSize;
        this.style = style;
        this.styleSize = styleLabelPrefix.length() + calculateStyleFor(stepSize).length() + styleLabelSuffix.length();
        this.labelBuffer = new StringBuilder();
        Stream.generate(() -> " ")
                .limit(labelSize)
                .forEach(labelBuffer::append);
        this.lineBuffer = new StringBuilder();
        Stream.generate(() -> TGS_CharSetUTF8.UTF8_INCOMPLETE())
                .limit(stepSize)
                .forEach(lineBuffer::append);
        Stream.generate(() -> " ")
                .limit(stepSize)
                .forEach(lineBuffer::append);
        lineBuffer.insert(0, "\r");
    }
    final private int stepSize, styleSize, labelSize;
    final public Style style;
    private int stepCurrent;
    final private StringBuilder labelBuffer;
    final private StringBuilder lineBuffer;
    final private String styleLabelPrefix = " [";
    final private String styleLabelSuffix = "] ";

    public int size() {
        return stepSize;
    }

    public static TS_ConsoleProgressBar of(int stepSize, Style style, int labelSize) {
        return new TS_ConsoleProgressBar(stepSize, style, labelSize);
    }

    public int getCurrent() {
        return stepCurrent;
    }

    public int getPercentageValue() {
        return calculatePercentageFor(stepCurrent);
    }

    public int getLabel() {
        return stepCurrent;
    }

    public TS_ConsoleProgressBar setCurrent(int stepNew, String labelNew) {
        stepCurrent = TGS_Coronator.ofInt()
                .anoint(val -> stepNew)
                .anointIf(val -> stepNew < 0, val -> 0)
                .anointIf(val -> stepNew > stepSize, val -> stepSize)
                .coronate();
        labelBuffer.setLength(0);
        if (labelNew != null) {
            labelBuffer.append(labelNew);
            labelBuffer.setLength(labelSize);
            if (labelNew.length() > labelBuffer.length() && labelBuffer.length() > 3) {
                labelBuffer.setCharAt(labelSize - 1, '.');
                labelBuffer.setCharAt(labelSize - 2, '.');
                labelBuffer.setCharAt(labelSize - 3, '.');
            }
        }
        IntStream.rangeClosed(1, stepSize).forEach(stepIndex -> {
//            d.ci("setCurrent", stepIndex, stepCurrent, stepIndex > stepCurrent ? TGS_CharSetUTF8.UTF8_INCOMPLETE() : TGS_CharSetUTF8.UTF8_COMPLETE());
            lineBuffer.replace(stepIndex, stepIndex + 1, stepIndex > stepCurrent ? TGS_CharSetUTF8.UTF8_INCOMPLETE() : TGS_CharSetUTF8.UTF8_COMPLETE());
        });
        lineBuffer.replace(1 + stepSize, 1 + stepSize + styleSize, formatStyle());
        lineBuffer.setLength(1 + stepSize + styleSize);
        lineBuffer.append(labelBuffer);
        return this;
    }

    public TS_ConsoleProgressBar showCurrent() {
        System.out.print(lineBuffer);
        return this;
    }

    public void forEach(TGS_RunnableType1<TS_ConsoleProgressBar> progress) {
        IntStream.rangeClosed(0, stepSize).forEach(stepNumber -> {
            progress.run(setCurrent(stepNumber, null));
        });
    }
}

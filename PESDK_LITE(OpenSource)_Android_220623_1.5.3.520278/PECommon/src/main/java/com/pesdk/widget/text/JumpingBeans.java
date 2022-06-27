
package com.pesdk.widget.text;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public final class JumpingBeans {

    private static final String ELLIPSIS_GLYPH = "…";
    private static final String THREE_DOTS_ELLIPSIS = "...";
    private static final int THREE_DOTS_ELLIPSIS_LENGTH = 3;

    private final JumpingBeansSpan[] jumpingBeans;
    private final WeakReference<TextView> textView;

    private JumpingBeans(JumpingBeansSpan[] beans, TextView textView) {
        this.jumpingBeans = beans;
        this.textView = new WeakReference<>(textView);
    }

    public static Builder with(@NonNull TextView textView) {
        return new Builder(textView);
    }

    public void stopJumping() {
        for (JumpingBeansSpan bean : jumpingBeans) {
            if (bean != null) {
                bean.teardown();
            }
        }
        cleanupSpansFrom(textView.get());
    }

    private static void cleanupSpansFrom(TextView textView) {
        if (textView == null) {
            return;
        }
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            CharSequence cleanText = removeJumpingBeansSpansFrom((Spanned) text);
            textView.setText(cleanText);
        }
    }

    private static CharSequence removeJumpingBeansSpansFrom(Spanned text) {
        SpannableStringBuilder sbb = new SpannableStringBuilder(text.toString());
        Object[] spans = text.getSpans(0, text.length(), Object.class);
        for (Object span : spans) {
            if (!(span instanceof JumpingBeansSpan)) {
                sbb.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span), text.getSpanFlags(span));
            }
        }
        return sbb;
    }

    public static class Builder {

        private static final float DEFAULT_ANIMATION_DUTY_CYCLE = 0.65f;
        private static final int DEFAULT_LOOP_DURATION = 1300;   // ms
        private static final int DEFAULT_WAVE_CHAR_DELAY = -1;

        private final TextView textView;

        private int startPos;
        private int endPos;

        private float animRange = DEFAULT_ANIMATION_DUTY_CYCLE;
        private int loopDuration = DEFAULT_LOOP_DURATION;
        private int waveCharDelay = DEFAULT_WAVE_CHAR_DELAY;
        private CharSequence text;
        private boolean wave;

        Builder(TextView textView) {
            this.textView = textView;
        }

        private static CharSequence appendThreeDotsEllipsisTo(TextView textView) {
            CharSequence text = getTextSafe(textView);
            if (text.length() > 0 && endsWithEllipsisGlyph(text)) {
                text = text.subSequence(0, text.length() - 1);
            }

            if (!endsWithThreeEllipsisDots(text)) {
                text = new SpannableStringBuilder(text).append(THREE_DOTS_ELLIPSIS);  // Preserve spans in original text
            }
            return text;
        }

        private static CharSequence getTextSafe(TextView textView) {
            return !TextUtils.isEmpty(textView.getText()) ? textView.getText() : "";
        }

        private static boolean endsWithEllipsisGlyph(CharSequence text) {
            CharSequence lastChar = text.subSequence(text.length() - 1, text.length());
            return ELLIPSIS_GLYPH.equals(lastChar);
        }

        // For readability
        @SuppressWarnings("SimplifiableIfStatement")
        private static boolean endsWithThreeEllipsisDots(CharSequence text) {
            if (text.length() < THREE_DOTS_ELLIPSIS_LENGTH) {
                // TODO we should try to normalize "invalid" ellipsis (e.g., ".." or "....")
                return false;
            }
            CharSequence lastThreeChars = text.subSequence(text.length() - THREE_DOTS_ELLIPSIS_LENGTH, text.length());
            return THREE_DOTS_ELLIPSIS.equals(lastThreeChars);
        }

        private static CharSequence ensureTextCanJump(int startPos, int endPos, CharSequence text) {
            if (text == null) {
                throw new NullPointerException("The textView text must not be null");
            }

            if (endPos < startPos) {
                throw new IllegalArgumentException("The start position must be smaller than the end position");
            }

            if (startPos < 0) {
                throw new IndexOutOfBoundsException("The start position must be non-negative");
            }

            if (endPos > text.length()) {
                throw new IndexOutOfBoundsException("The end position must be smaller than the text length");
            }
            return text;
        }

        @NonNull
        public Builder appendJumpingDots() {
            CharSequence text = appendThreeDotsEllipsisTo(textView);
            this.text = text;
            this.wave = true;
            this.startPos = text.length() - THREE_DOTS_ELLIPSIS_LENGTH;
            this.endPos = text.length();
            return this;
        }

        /**
         * 将三个跳点追加到TextView文本的末尾。
         */
        @NonNull
        public Builder makeTextJump(@IntRange(from = 0) int startPos, @IntRange(from = 0) int endPos) {
            CharSequence text = textView.getText();
            ensureTextCanJump(startPos, endPos, text);

            this.text = text;
            this.wave = true;
            this.startPos = startPos;
            this.endPos = endPos;

            return this;
        }


        /**
         * 设置实际用于动画的动画循环时间的比例。
         */
        @NonNull
        public Builder setAnimatedDutyCycle(@FloatRange(from = 0f, to = 1f, fromInclusive = false) float animatedRange) {
            if (animatedRange <= 0f || animatedRange > 1f) {
                throw new IllegalArgumentException("The animated range must be in the (0, 1] range");
            }
            this.animRange = animatedRange;
            return this;
        }

        /**
         * 设置跳跃循环的持续时间。
         */
        @NonNull
        public Builder setLoopDuration(@IntRange(from = 1) int loopDuration) {
            if (loopDuration < 1) {
                throw new IllegalArgumentException("The loop duration must be bigger than zero");
            }
            this.loopDuration = loopDuration;
            return this;
        }

        /**
         * 设置延迟时间
         */
        @NonNull
        public Builder setWavePerCharDelay(@IntRange(from = 0) int waveCharOffset) {
            if (waveCharOffset < 0) {
                throw new IllegalArgumentException("The wave char offset must be non-negative");
            }
            this.waveCharDelay = waveCharOffset;
            return this;
        }

        /**
         * 波浪还是整体
         */
        @NonNull
        public Builder setIsWave(boolean wave) {
            this.wave = wave;
            return this;
        }

        @NonNull
        public JumpingBeans build() {
            SpannableStringBuilder sbb = new SpannableStringBuilder(text);
            JumpingBeansSpan[] spans;
            if (wave) {
                spans = buildWavingSpans(sbb);
            } else {
                spans = buildSingleSpan(sbb);
            }

            textView.setText(sbb);
            return new JumpingBeans(spans, textView);
        }

        @SuppressWarnings("Range")
        private JumpingBeansSpan[] buildWavingSpans(SpannableStringBuilder sbb) {
            JumpingBeansSpan[] spans;
            if (waveCharDelay == DEFAULT_WAVE_CHAR_DELAY) {
                waveCharDelay = loopDuration / (3 * (endPos - startPos));
            }

            spans = new JumpingBeansSpan[endPos - startPos];
            for (int pos = startPos; pos < endPos; pos++) {
                JumpingBeansSpan jumpingBean =
                        new JumpingBeansSpan(textView, loopDuration, pos - startPos, waveCharDelay, animRange);
                sbb.setSpan(jumpingBean, pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans[pos - startPos] = jumpingBean;
            }
            return spans;
        }

        private JumpingBeansSpan[] buildSingleSpan(SpannableStringBuilder sbb) {
            JumpingBeansSpan[] spans;
            spans = new JumpingBeansSpan[]{new JumpingBeansSpan(textView, loopDuration, 0, 0, animRange)};
            sbb.setSpan(spans[0], startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spans;
        }

    }

}

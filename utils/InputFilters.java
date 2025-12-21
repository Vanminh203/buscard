package utils;

import javax.swing.text.*;

public class InputFilters {

        // Chỉ số và dấu / , giới hạn 10 ký tự
        public static class DateSlashFilter extends DocumentFilter {
            private final int limit;
            public DateSlashFilter(int limit) { this.limit = limit; }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {

                if (text == null) text = "";

                // Chỉ cho nhập số hoặc /
                if (!text.matches("[0-9/]*")) return;

                Document doc = fb.getDocument();
                int newLength = doc.getLength() - length + text.length();
                if (newLength > limit) return;

                super.replace(fb, offset, length, text, attrs);
            }
        }

    public static class MoneyFormatFilter extends DocumentFilter {

        private final int maxDigits;

        public MoneyFormatFilter(int maxDigits) {
            this.maxDigits = maxDigits;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            Document doc = fb.getDocument();
            String old = doc.getText(0, doc.getLength());

            StringBuilder sb = new StringBuilder(old);
            sb.replace(offset, offset + length, text == null ? "" : text);

            // Bỏ dấu phẩy
            String raw = sb.toString().replace(",", "");

            // Chỉ số
            if (!raw.matches("\\d*")) return;

            // Giới hạn số ký tự số
            if (raw.length() > maxDigits) return;

            if (raw.isEmpty()) {
                fb.replace(0, doc.getLength(), "", attrs);
                return;
            }

            long val = Long.parseLong(raw);
            String formatted = String.format("%,d", val);

            fb.replace(0, doc.getLength(), formatted, attrs);
        }
    }
    //  Giới hạn số + chiều dài
    public static class NumericLimitFilter extends DocumentFilter {
        private final int limit;

        public NumericLimitFilter(int limit) {
            this.limit = limit;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (text != null && !text.matches("\\d*")) return; // chỉ số

            Document doc = fb.getDocument();
            int newLength = doc.getLength() - length + (text == null ? 0 : text.length());

            if (newLength <= limit) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}

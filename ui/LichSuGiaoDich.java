package ui;

import service.SmartCardService;
import controller.CardController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LichSuGiaoDich extends JFrame {

    private final CardController cardController;
    private JTable table;

    public LichSuGiaoDich(CardController controller) {
        this.cardController = controller;

        setTitle("Lịch sử giao dịch");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(560, 420);
        
        buildUI();
        loadData();
    }

    private void buildUI() {

    // ROOT BACKGROUND
    JPanel root = new JPanel(new BorderLayout());
    root.setBorder(new EmptyBorder(18, 18, 18, 18));
    root.setBackground(new Color(241, 245, 249));  // same as NhapMaPin
    setContentPane(root);

    // HEADER (giống Nhập mã PIN)
    JPanel header = new JPanel();
    header.setOpaque(false);
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

    JLabel lbTitle = new JLabel("LỊCH SỬ GIAO DỊCH TRÊN THẺ");
    lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
    lbTitle.setForeground(new Color(15, 23, 42));
    lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lbSub = new JLabel("Danh sách các giao dịch đã thực hiện trên thẻ.");
    lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lbSub.setForeground(new Color(71, 85, 105));
    lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

    header.add(lbTitle);
    header.add(Box.createRigidArea(new Dimension(0, 6)));
    header.add(lbSub);

    root.add(header, BorderLayout.NORTH);


    // CARD (giống Nhập mã pin)
    JPanel card = new KhoiTaoThe.RoundedPanel(16, Color.WHITE);
    card.setBorder(new EmptyBorder(26, 26, 26, 26));
    card.setLayout(new BorderLayout());
    root.add(card, BorderLayout.CENTER);

    // TABLE
    String[] cols = { "Thời gian", "Loại giao dịch", "Số tiền (VND)" };

    DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    table = new JTable(model);
    table.setRowHeight(28);
    table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

    table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    table.getTableHeader().setForeground(new Color(15, 23, 42));

    // alternating rows
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean isSel, boolean hasFocus,
                                                       int row, int col) {

            Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);

            if (isSel) {
                c.setBackground(new Color(186, 230, 253));  // xanh nhẹ
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(new Color(30, 41, 59));
            }
            return c;
        }
    });

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createEmptyBorder());

    card.add(scroll, BorderLayout.CENTER);

    // FOOTER
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    footer.setOpaque(false);
    footer.setBorder(new EmptyBorder(12, 0, 0, 0));

    JButton btnClose = new JButton("Đóng");

    // NÚT ĐÓNG GIỐNG NÚT "Xác nhận" của Nhập PIN
    btnClose.setUI(new KhoiTaoThe.ModernButtonUI(
            new Color(16, 185, 129),   // xanh lá
            Color.WHITE
    ));
    btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnClose.addActionListener(e -> dispose());

    footer.add(btnClose);
    card.add(footer, BorderLayout.SOUTH);
}
    //  ĐỔ BÓNG CARD 
    private JComponent wrapCardShadow(JComponent content) {
        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // bóng mờ mềm giống MainFrame
                g2.setColor(new Color(0, 0, 0, 22));
                g2.fillRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 26, 26);

                g2.dispose();
            }
        };
        wrapper.setLayout(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.setOpaque(false);
        return wrapper;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(16, 185, 129)); 
        btn.setBorder(new EmptyBorder(10, 28, 10, 28));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // bo góc + bóng
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // bóng nút
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, c.getWidth() - 6, c.getHeight() - 6, 12, 12);

                // nền nút
                g2.setColor(btn.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);

                super.paint(g2, c);
            }
        });

        return btn;
    }

    private void loadData() {
        List<SmartCardService.HistoryRecord> list = cardController.getHistory();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        for (SmartCardService.HistoryRecord r : list) {

            LocalDateTime dt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(r.timestamp),
                    ZoneId.systemDefault()
            );

            String timeStr = dt.format(fmt);

            String loai;
            String soTien;

            if (r.type == 1) {
                loai = "Nạp tiền";
                soTien = "+" + String.format("%,d", r.amount);
            } else {
                if (r.amount == CardController.GIA_VE_NGAY)
                    loai = "Mua vé ngày";
                else if (r.amount == CardController.GIA_VE_THANG)
                    loai = "Mua vé tháng";
                else if (r.amount == CardController.GIA_VE_NAM)
                    loai = "Mua vé năm";
                else
                    loai = "Thanh toán khác";

                soTien = "-" + String.format("%,d", r.amount);
            }

            model.addRow(new Object[]{timeStr, loai, soTien});
        }
    }
}

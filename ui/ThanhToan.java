package ui;

import controller.CardController;
import model.TheXeBus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ThanhToan extends JFrame {

    private final MainFrame mainFrame;
    private final TheXeBus theHienTai;
    private final CardController cardController;

    private JRadioButton rdVeNgay, rdVeThang, rdVeNam;
    private JLabel lbGiaTri, lbHetHan;

    // Giá cố định
    private static final int GIA_VE_NGAY  = 12000;
    private static final int GIA_VE_THANG = 300000;
    private static final int GIA_VE_NAM   = 3200000;

    public ThanhToan(MainFrame mainFrame, TheXeBus card, CardController controller) {
        this.mainFrame = mainFrame;
        this.theHienTai = card;
        this.cardController = controller;

        setTitle("Mua vé xe bus");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 300));

        buildUI();
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        root.setBackground(new Color(241, 245, 249));
        setContentPane(root);

        //   HEADER  
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("MUA VÉ SỬ DỤNG THẺ");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbTitle.setForeground(new Color(15, 23, 42));
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Chọn loại vé muốn mua. Tiền sẽ trừ trực tiếp từ số dư trong thẻ.");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSub.setForeground(new Color(71, 85, 105));
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        //   CARD CONTAINER  
        JPanel card = new KhoiTaoThe.RoundedPanel(16, Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setLayout(new BorderLayout(20, 0));
        root.add(card, BorderLayout.CENTER);

        // --- LEFT: CHỌN LOẠI VÉ ---
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lbChon = new JLabel("Loại vé:");
        lbChon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbChon.setForeground(new Color(51, 65, 85));

        rdVeNgay  = new JRadioButton("Vé ngày – 12.000 VND");
        rdVeThang = new JRadioButton("Vé tháng – 300.000 VND");
        rdVeNam   = new JRadioButton("Vé năm – 3.200.000 VND");

        rdVeNgay.setOpaque(false);
        rdVeThang.setOpaque(false);
        rdVeNam.setOpaque(false);

        rdVeNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rdVeThang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rdVeNam.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ButtonGroup group = new ButtonGroup();
        group.add(rdVeNgay);
        group.add(rdVeThang);
        group.add(rdVeNam);

        rdVeNgay.addActionListener(e -> updateSummary());
        rdVeThang.addActionListener(e -> updateSummary());
        rdVeNam.addActionListener(e -> updateSummary());

        left.add(lbChon);
        left.add(Box.createVerticalStrut(10));
        left.add(rdVeNgay);
        left.add(Box.createVerticalStrut(6));
        left.add(rdVeThang);
        left.add(Box.createVerticalStrut(6));
        left.add(rdVeNam);

        card.add(left, BorderLayout.WEST);

        // --- RIGHT: THÔNG TIN VÉ ---
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lbThongTin = new JLabel("Thông tin vé");
        lbThongTin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbThongTin.setForeground(new Color(51, 65, 85));

        lbGiaTri = new JLabel("Giá trị: —");
        lbGiaTri.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbGiaTri.setForeground(new Color(30, 64, 175));

        lbHetHan = new JLabel("Hết hạn: —");
        lbHetHan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbHetHan.setForeground(new Color(30, 64, 175));

        JLabel lbRule = new JLabel("<html><ul>" +
                "<li>Mua vé tháng sẽ thay thế vé ngày.</li>" +
                "<li>Mua vé năm sẽ thay thế vé ngày & vé tháng.</li>" +
                "</ul></html>");
        lbRule.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbRule.setForeground(new Color(100, 116, 139));

        right.add(lbThongTin);
        right.add(Box.createVerticalStrut(10));
        right.add(lbGiaTri);
        right.add(Box.createVerticalStrut(6));
        right.add(lbHetHan);
        right.add(Box.createVerticalStrut(16));
        right.add(lbRule);

        card.add(right, BorderLayout.CENTER);

        //   FOOTER  
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton btnQL = new JButton("Quay lại");
        btnQL.setUI(new KhoiTaoThe.ModernButtonUI(new Color(100, 116, 139), Color.WHITE));
        btnQL.addActionListener(e -> dispose());

        JButton btnTT = new JButton("Thanh toán");
        btnTT.setUI(new KhoiTaoThe.ModernButtonUI(new Color(16, 185, 129), Color.WHITE));
        btnTT.addActionListener(this::onThanhToan);

        footer.add(btnQL);
        footer.add(btnTT);

        card.add(footer, BorderLayout.SOUTH);

        pack();
    }

    // Cập nhật phần tóm tắt (giá, hết hạn) khi chọn loại vé
    private void updateSummary() {
    int amount = 0;
    String textHetHan = "Hết hạn: —";

    LocalDateTime now = LocalDateTime.now();   // thời điểm hiện tại
    LocalDateTime expire = null;

    if (rdVeNgay.isSelected()) {
        amount = GIA_VE_NGAY;
        // Vé ngày: hết hạn đúng thời điểm này của ngày hôm sau
        expire = now.plusDays(1);
    }
    else if (rdVeThang.isSelected()) {
        amount = GIA_VE_THANG;
        // Vé tháng: hết hạn đúng thời điểm này của tháng sau
        expire = now.plusMonths(1);
    }
    else if (rdVeNam.isSelected()) {
        amount = GIA_VE_NAM;
        // Vé năm: hết hạn đúng thời điểm này của năm sau
        expire = now.plusYears(1);
    }

    if (amount > 0) {
        lbGiaTri.setText("Giá trị: " + String.format("%,d VND", amount));

        if (expire != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            textHetHan = "Hết hạn: " + expire.format(fmt);
        }
    } else {
        lbGiaTri.setText("Giá trị: —");
    }

    lbHetHan.setText(textHetHan);
}
        private void onThanhToan(ActionEvent e) {
        int money = 0;
        byte passType = 0;

        if (rdVeNgay.isSelected()) {
            money = GIA_VE_NGAY;
            passType = 1;
        } else if (rdVeThang.isSelected()) {
            money = GIA_VE_THANG;
            passType = 2;
        } else if (rdVeNam.isSelected()) {
            money = GIA_VE_NAM;
            passType = 3;
        }

        if (money <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn loại vé muốn mua!",
                    "Chưa chọn vé",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Gọi nhập PIN để thực hiện thanh toán
        NhapMaPin pinForm = new NhapMaPin(
                mainFrame,
                theHienTai,
                cardController,
                NhapMaPin.ActionType.PAY,
                money
        );

        pinForm.setLocationRelativeTo(this);
        pinForm.setVisible(true);
        dispose();
}
}

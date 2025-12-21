package ui;

import controller.CardController;
import model.TheXeBus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import utils.InputFilters;

public class NapTien extends JFrame {

    private final MainFrame mainFrame;
    private final TheXeBus theHienTai;
    private final CardController cardController;

    private JTextField txtSoTien;
    private JButton btnXacNhan, btnQuayLai;

    public NapTien(MainFrame mainFrame, TheXeBus card, CardController controller) {
        this.mainFrame = mainFrame;
        this.theHienTai = card;
        this.cardController = controller;

        setTitle("Nạp tiền");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 350));

        buildUI();
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        root.setBackground(new Color(241, 245, 249));  // nền giống Nhập PIN
        setContentPane(root);

        //  HEADER 
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("NẠP TIỀN VÀO THẺ");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbTitle.setForeground(new Color(15, 23, 42));
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Nhập số tiền muốn nạp vào tài khoản của thẻ.");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSub.setForeground(new Color(71, 85, 105));
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        //  CARD 
        JPanel card = new KhoiTaoThe.RoundedPanel(16, Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setLayout(new BorderLayout());
        root.add(card, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtSoTien = new JTextField();
        styleInput(txtSoTien);
        ((AbstractDocument) txtSoTien.getDocument()).setDocumentFilter(new InputFilters.MoneyFormatFilter(7));


        form.add(makeForm("Số tiền nạp", txtSoTien,
                "Nhập số tiền muốn nạp (>= 1.000 VND và <= 5.000.000)"));

        card.add(form, BorderLayout.CENTER);

        //  FOOTER 
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);

        btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setUI(new KhoiTaoThe.ModernButtonUI(
                new Color(100, 116, 139), Color.WHITE
        ));
        btnQuayLai.addActionListener(e -> dispose());

        btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setUI(new KhoiTaoThe.ModernButtonUI(
                new Color(16, 185, 129), Color.WHITE
        ));
        btnXacNhan.addActionListener(this::onConfirm);

        footer.add(btnQuayLai);
        footer.add(btnXacNhan);

        card.add(footer, BorderLayout.SOUTH);

        pack();
    }

    private JPanel makeForm(String label, JComponent field, String hint) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(new Color(51, 65, 85));

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabel.setForeground(new Color(100, 116, 139));

        box.add(lb);
        box.add(Box.createRigidArea(new Dimension(0, 8)));
        box.add(field);
        box.add(Box.createRigidArea(new Dimension(0, 6)));
        box.add(hintLabel);

        return box;
    }

    private void styleInput(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(new Color(15, 23, 42));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }
    // XỬ LÝ XÁC NHẬN
    
    private void onConfirm(ActionEvent e) {
        String moneyStr = txtSoTien.getText().replace(",", "").trim();

        if (moneyStr.isEmpty() || !moneyStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ!");
            return;
        }

        int amount = Integer.parseInt(moneyStr);

        if (amount < 1000) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền nạp phải >= 1.000 VND");
            return;
        }

        if (amount > 5_000_000) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền nạp tối đa là 5.000.000 VND!",
                    "Số tiền quá lớn",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mở màn hình nhập PIN để xác thực
        NhapMaPin pin = new NhapMaPin(
                mainFrame,
                theHienTai,
                cardController,
                NhapMaPin.ActionType.TOP_UP_REQUEST,
                amount
        );

        pin.setLocationRelativeTo(this);
        pin.setVisible(true);

        dispose();
    }
}

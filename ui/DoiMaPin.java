package ui;

import controller.CardController;
import model.TheXeBus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.text.AbstractDocument;
import utils.InputFilters;

public class DoiMaPin extends JFrame {

    private final TheXeBus theHienTai;
    private final CardController cardController;

    private JPasswordField txtPinCu;
    private JPasswordField txtPinMoi;
    private JPasswordField txtNhapLai;
    private JButton btnXacNhan, btnQuayLai;

    public DoiMaPin(TheXeBus card, CardController controller) {
        this.theHienTai = card;
        this.cardController = controller;

        setTitle("Đổi mã PIN");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 380));

        buildUI();
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(241, 245, 249));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        // ==== HEADER ====
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("ĐỔI MÃ PIN");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbTitle.setForeground(new Color(15, 23, 42));
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Nhập mã PIN cũ và đặt mã PIN mới (6 chữ số).");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSub.setForeground(new Color(71, 85, 105));
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        // ==== FORM CARD ====
        JPanel card = new KhoiTaoThe.RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(card, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtPinCu = new JPasswordField();
        txtPinMoi = new JPasswordField();
        txtNhapLai = new JPasswordField();

        stylePassword(txtPinCu);
        ((AbstractDocument) txtPinCu.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(6));
        stylePassword(txtPinMoi);
        ((AbstractDocument) txtPinMoi.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(6));
        stylePassword(txtNhapLai);
        ((AbstractDocument) txtNhapLai.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(6));

        form.add(makeField("PIN cũ", txtPinCu, "Nhập 6 chữ số PIN ban đầu"));
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(makeField("PIN mới", txtPinMoi, "Mã PIN mới gồm 6 chữ số"));
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(makeField("Nhập lại PIN mới", txtNhapLai, "Xác nhận PIN mới"));

        card.add(form, BorderLayout.CENTER);

        // ==== FOOTER ====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setUI(new KhoiTaoThe.ModernButtonUI(new Color(100, 116, 139), Color.WHITE));
        btnQuayLai.addActionListener(e -> dispose());

        btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setUI(new KhoiTaoThe.ModernButtonUI(new Color(16, 185, 129), Color.WHITE));
        btnXacNhan.addActionListener(this::onXacNhan);

        footer.add(btnQuayLai);
        footer.add(btnXacNhan);

        card.add(footer, BorderLayout.SOUTH);

        pack();
    }

    private JPanel makeField(String title, JComponent field, String hint) {

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 4, 0);

        JLabel lb = new JLabel(title);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(new Color(51, 65, 85));
        box.add(lb, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        box.add(field, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.weightx = 1;

        JLabel hb = new JLabel(hint);
        hb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hb.setForeground(new Color(100, 116, 139));
        box.add(hb, gc);

        return box;
}

    private void stylePassword(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setForeground(new Color(15, 23, 42));
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(new Color(15, 23, 42));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    private void onXacNhan(ActionEvent e) {

        String oldPin = new String(txtPinCu.getPassword()).trim();
        String newPin = new String(txtPinMoi.getPassword()).trim();
        String rePin  = new String(txtNhapLai.getPassword()).trim();
        
        if (oldPin.isEmpty()) {
            showErr("Bạn chưa nhập pin cũ");
            return;
}
        if (newPin.isEmpty()) {
            showErr("Bạn chưa nhập pin mới");
            return;
}       
        if (rePin.isEmpty()) {
            showErr("Bạn chưa xác nhận lại pin");
            return;
}
        if (!oldPin.matches("\\d{6}")) {
            showErr("PIN cũ phải gồm 6 chữ số!");
            return;
        }
        if (!newPin.matches("\\d{6}")) {
            showErr("PIN mới phải gồm 6 chữ số!");
            return;
        }
        if (!newPin.equals(rePin)) {
            showErr("PIN nhập lại không khớp!");
            return;
        }
        if(oldPin.equals(newPin)){
            showErr("PIN mới không được trùng với PIN cũ!");
            return;
        }

        //  = GỌI CONTROLLER ĐỔI PIN  =
        int status = cardController.changePin(oldPin, newPin);

        if (status == 0) {
            dispose();  // thành công
        } else if (status == 2) {
            dispose();  // thẻ bị khóa → đóng luôn form
    }
}
    private void showErr(String s) {
        JOptionPane.showMessageDialog(this, s, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

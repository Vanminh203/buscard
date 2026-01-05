package ui;

import controller.CardController;
import model.TheXeBus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.text.AbstractDocument;
import utils.InputFilters;

public class NhapMaPin extends JFrame {

    public enum ActionType {
        LOGIN,
        TOP_UP_REQUEST,  // chỉ tạo yêu cầu nạp tiền (dùng với màn hình Xác nhận)
        PAY,
        DELETE_CARD,
        UPDATE_INFO,
        UNLOCK,
    }

    private final MainFrame mainFrame;
    private final CardController cardController;
    private final ActionType actionType;
    private Integer amount;
    private String hoTenMoi, ngaySinhMoi,cccdMoi;
    private byte[] anhMoi;
    
    private JPasswordField txtPin;
    private JButton btnOK;   

    public NhapMaPin(MainFrame f, TheXeBus card, CardController ctl, ActionType type) {
        this.mainFrame = f;
        this.cardController = ctl;
        this.actionType = type;
        
        setTitle("Nhập mã PIN");
        if (cardController.isReconnecting()) {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        } else {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
}
        setMinimumSize(new Dimension(420, 260));

        buildUI();
        setLocationRelativeTo(null);
    }
    
    public NhapMaPin(MainFrame f, TheXeBus c, CardController ctl, ActionType type, int amount) {
        this(f, c, ctl, type);
        this.amount = amount;
    }

    public NhapMaPin(MainFrame f, TheXeBus c, CardController ctl, ActionType type,
                     String hoTenMoi, String ngaySinhMoi, String cccdMoi, byte[] anhMoi) {
    this(f, c, ctl, type);
    this.hoTenMoi = hoTenMoi;
    this.ngaySinhMoi = ngaySinhMoi;
    this.cccdMoi = cccdMoi;   
    this.anhMoi = anhMoi;
    }


    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(new Color(241, 245, 249));
        setContentPane(root);

        // HEADER
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("NHẬP MÃ PIN");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbTitle.setForeground(new Color(15, 23, 42));
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Vui lòng nhập mã PIN để xác thực.");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbSub.setForeground(new Color(71, 85, 105));
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        // CARD
        JPanel card = new KhoiTaoThe.RoundedPanel(16, Color.WHITE);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setLayout(new BorderLayout());
        root.add(card, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtPin = new JPasswordField();
        stylePassword(txtPin);
        ((AbstractDocument) txtPin.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(6));
        form.add(makeField("PIN", txtPin, "PIN gồm 6 chữ số"));

        // Gắn DocumentListener để kiểm tra PIN realtime
        txtPin.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { checkPin(); }
            @Override public void removeUpdate(DocumentEvent e) { checkPin(); }
            @Override public void changedUpdate(DocumentEvent e) { checkPin(); }
        });

        card.add(form, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton btnHuy = new JButton("Huỷ");
        btnHuy.setUI(new KhoiTaoThe.ModernButtonUI(new Color(100, 116, 139), Color.WHITE));
        if (cardController.isReconnecting()) {
        btnHuy.setEnabled(false);
        } else {
        btnHuy.addActionListener(e -> dispose());
}


        btnOK = new JButton("Xác nhận");
        btnOK.setUI(new KhoiTaoThe.ModernButtonUI(new Color(16, 185, 129), Color.WHITE));
        btnOK.addActionListener(this::onConfirm);
        btnOK.setEnabled(false); 

        footer.add(btnHuy);
        footer.add(btnOK);

        card.add(footer, BorderLayout.SOUTH);

        pack();
    }

    private JPanel makeField(String title, JComponent field, String hint) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 4, 0);

        // Label (PIN)
        JLabel lb = new JLabel(title);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(new Color(51, 65, 85));
        gc.gridy = 0;
        wrap.add(lb, gc);

        // Input field
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 6, 0);
        wrap.add(field, gc);

        // Hint (PIN gồm 6 chữ số)
        JLabel hb = new JLabel(hint);
        hb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hb.setForeground(new Color(100, 116, 139));
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 0, 0);
        wrap.add(hb, gc);

        return wrap;
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

    //     =
    // CHECK PIN REAL-TIME
    //     =
    private void checkPin() {
        String pin = new String(txtPin.getPassword());
        boolean valid = pin.matches("\\d{6}");

        btnOK.setEnabled(valid);
    }
    
    // XỬ LÝ KHI BẤM OK
    private void onConfirm(ActionEvent e) {

        String pin = new String(txtPin.getPassword()).trim();

        // 1) LUỒNG MỞ KHÓA THẺ
        if (actionType == ActionType.UNLOCK) {
            boolean ul = cardController.unlockCard(pin);
            if (!ul) return;
            JOptionPane.showMessageDialog(this, "Mở khóa thành công!");
            dispose();
            return;
        }
        // 2) CÁC LUỒNG KHÁC PHẢI VERIFY PIN
        boolean ok = cardController.verifyPin(pin,this);
        if (!ok) return;
        switch (actionType) {
            case LOGIN -> {
            }
            case TOP_UP_REQUEST -> {
                if (amount != null) {
                    boolean nt = cardController.topUpDirect(amount);
                    if (nt) {
                        JOptionPane.showMessageDialog(this, "Nạp tiền thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Nạp tiền thất bại!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            case PAY -> {
                if (amount != null) {
                    cardController.pay(amount);
                }
            }
            case DELETE_CARD ->
                    cardController.deleteCard();
            case UPDATE_INFO ->
                    cardController.updateInfo(hoTenMoi, ngaySinhMoi, cccdMoi, anhMoi);
        }
        dispose();
}

}

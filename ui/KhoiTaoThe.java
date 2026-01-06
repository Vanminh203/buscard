package ui;

import controller.CardController;
import model.TheXeBus;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.AbstractDocument;
import utils.InputFilters;

public class KhoiTaoThe extends JFrame {

    private final MainFrame mainFrame;
    private final CardController cardController;
    private byte[] anhBytes;

    // UI components
    private JLabel lbTitle;
    private JLabel lbPreview;
    private JButton btnThemAnh;
    private JButton btnTaoThe;
    private JButton btnQuayLai;
    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JTextField txtCccd;


    public KhoiTaoThe(MainFrame mainFrame, CardController controller) {
        this.mainFrame = mainFrame;
        this.cardController = controller;

        setTitle("Khởi tạo thẻ");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));

        buildUI();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Root
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(241, 245, 249)); // #F1F5F9
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        lbTitle = new JLabel("NHẬP THÔNG TIN");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbTitle.setForeground(new Color(15, 23, 42));
        // CĂN GIỮA
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Tạo thẻ mới cho hành khách (Ảnh + Thông tin cá nhân + CCCD).");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSub.setForeground(new Color(71, 85, 105));
        // CĂN GIỮA
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        // Card container
        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(card, BorderLayout.CENTER);

        // Main content inside card
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        card.add(content, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        // Left: Preview + button
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // Title căn giữa
        JLabel lbImgTitle = new JLabel("ẢNH TRÊN THẺ");
        lbImgTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbImgTitle.setForeground(new Color(51, 65, 85));
        lbImgTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Preview căn giữa
        lbPreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);

        // KÍCH THƯỚC CỐ ĐỊNH (để nó không bị co về 0)
        Dimension pvSize = new Dimension(150, 190);
        lbPreview.setPreferredSize(pvSize);
        lbPreview.setMinimumSize(pvSize);
        lbPreview.setMaximumSize(pvSize);

        // ĐỂ NÓ VẼ NỀN
        lbPreview.setOpaque(true);
        lbPreview.setBackground(new Color(248, 250, 252)); // #F8FAFC
        lbPreview.setForeground(new Color(100, 116, 139)); // #64748B

        // VIỀN KHUNG
        lbPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1), // #CBD5E1
                new EmptyBorder(6, 6, 6, 6)
        ));

        // căn giữa trong BoxLayout
        lbPreview.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Button căn giữa
        btnThemAnh = new JButton("Thêm ảnh");
        btnThemAnh.setUI(new ModernButtonUI(new Color(59, 130, 246), Color.WHITE));
        btnThemAnh.setRolloverEnabled(true);
        btnThemAnh.setAlignmentX(Component.CENTER_ALIGNMENT); // <-- quan trọng
        btnThemAnh.addActionListener(this::onChonAnh);

        left.add(lbImgTitle);
        left.add(Box.createRigidArea(new Dimension(0, 10)));
        left.add(lbPreview);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(btnThemAnh);
        left.add(Box.createVerticalGlue());

        
        // Right: Form
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(makeField("Họ và tên", txtHoTen = new JTextField(), "Chỉ chữ cái và khoảng trắng"));
        right.add(Box.createRigidArea(new Dimension(0, 12)));
        right.add(makeField("Ngày sinh", txtNgaySinh = new JTextField(), "Định dạng dd/MM/yyyy"));
        right.add(Box.createRigidArea(new Dimension(0, 12)));
        right.add(makeField("Số CCCD", txtCccd = new JTextField(), "12 chữ số"));
        right.add(Box.createRigidArea(new Dimension(0, 12)));

        styleTextField(txtHoTen);
        styleTextField(txtNgaySinh);
        ((AbstractDocument) txtNgaySinh.getDocument()).setDocumentFilter(new InputFilters.DateSlashFilter(10));
        styleTextField(txtCccd);
        ((AbstractDocument) txtCccd.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(12));
        // Tạo wrapper để center khối bên trái
        JPanel leftWrap = new JPanel(new GridBagLayout());
        leftWrap.setOpaque(false);
        leftWrap.add(left); // left sẽ nằm giữa leftWrap

        // Layout left-right
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.40;   // tăng nhẹ để cột trái thoáng hơn
        content.add(leftWrap, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.60;
        content.add(right, gc);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setUI(new ModernButtonUI(new Color(100, 116, 139), Color.WHITE)); // #64748B
        btnQuayLai.setRolloverEnabled(true);
        btnQuayLai.addActionListener(e -> dispose());

        btnTaoThe = new JButton("Tạo thẻ");
        btnTaoThe.setUI(new ModernButtonUI(new Color(16, 185, 129), Color.WHITE)); // #10B981
        btnTaoThe.setRolloverEnabled(true);
        btnTaoThe.addActionListener(this::onTaoThe);

        footer.add(btnQuayLai);
        footer.add(btnTaoThe);

        card.add(footer, BorderLayout.SOUTH);

        pack();
    }

    private JPanel makeField(String title, JComponent field, String hint) {

        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 4, 0);

        // Label chính
        JLabel lb = new JLabel(title);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(new Color(51, 65, 85));
        box.add(lb, gc);

        // Ô nhập
        gc.gridy++;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 6, 0);
        box.add(field, gc);

        // Hint
        gc.gridy++;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 0, 0);

        JLabel hb = new JLabel(hint);
        hb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hb.setForeground(new Color(100, 116, 139));
        box.add(hb, gc);

        return box;
}
    
    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(15, 23, 42));
        tf.setCaretColor(new Color(15, 23, 42));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    //   Events  
    private void onChonAnh(ActionEvent e) {
        try {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                JOptionPane.showMessageDialog(this, "File không phải ảnh hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int w = 150, h = 190;
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(scaled, "jpg", baos);
            baos.flush();
            byte[] data = baos.toByteArray();
            baos.close();

            if (data.length > 31744) {
                JOptionPane.showMessageDialog(this,
                        "Ảnh sau khi nén vẫn quá lớn (>31KB). Hãy chọn ảnh nhỏ hơn!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.anhBytes = data;
            lbPreview.setText(null);
            lbPreview.setIcon(new ImageIcon(scaled));

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi chọn ảnh: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTaoThe(ActionEvent e) {
        String hoTen = txtHoTen.getText().trim();
        String ngaySinh = txtNgaySinh.getText().trim();
        String cccd = txtCccd.getText().trim();

        if (hoTen.isEmpty()) { showErr("Họ và tên không được để trống!"); return; }
        if (!hoTen.matches("^[\\p{L} ]+$")) { showErr("Họ và tên chỉ được chứa chữ cái và khoảng trắng!"); return; }

        if (ngaySinh.isEmpty()) { showErr("Ngày sinh không được để trống!"); return; }
        if (!ngaySinh.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            showErr("Ngày sinh phải theo định dạng dd/MM/yyyy (ví dụ: 01/12/2000)");
            return;
        }
        if (!isValidDate(ngaySinh)) { showErr("Ngày sinh không hợp lệ!"); return; }
        if (!cccd.matches("\\d{12}")) { showErr("CCCD phải gồm 12 chữ số!"); return;}

        if (anhBytes == null || anhBytes.length == 0) {
            showErr("Vui lòng thêm ảnh cho thẻ trước khi tạo!");
            return;
        }
        btnTaoThe.setEnabled(false);

new SwingWorker<TheXeBus, Void>() {

    @Override
    protected TheXeBus doInBackground() throws Exception {
        return cardController.createNewCard(
                hoTen, ngaySinh, cccd, anhBytes
        );
    }

    @Override
    protected void done() {
        btnTaoThe.setEnabled(true);
        try {
            TheXeBus theMoi = get();

            if (theMoi != null) {
                mainFrame.hienThiThongTinThe(theMoi);

                JOptionPane.showMessageDialog(
                        KhoiTaoThe.this,
                        "Khởi tạo thẻ thành công\n" + "Mã PIN mặc định: 123456\n",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                showErr("Tạo thẻ thất bại. Vui lòng kiểm tra kết nối thẻ/đầu đọc!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showErr("Lỗi khi khởi tạo thẻ: " + ex.getMessage());
            }
        }
    }.execute();
    }
    
    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date d = sdf.parse(date);
            return !d.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }
    //   UI helpers  
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(6, 6, w - 12, h - 12, radius, radius);

            // card
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 12, h - 12, radius, radius);

            g2.dispose();
        }
    }

    static class ModernButtonUI extends BasicButtonUI {
        private final Color bg;
        private final Color fg;

        ModernButtonUI(Color bg, Color fg) {
            this.bg = bg;
            this.fg = fg;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setForeground(fg);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            boolean pressed = b.getModel().isArmed() && b.getModel().isPressed();
            boolean hover = b.getModel().isRollover();

            Color base = bg;
            if (pressed) base = base.darker();
            else if (hover) base = new Color(
                    Math.min(255, base.getRed() + 10),
                    Math.min(255, base.getGreen() + 10),
                    Math.min(255, base.getBlue() + 10)
            );

            g2.setColor(base);
            g2.fillRoundRect(0, 0, w, h, 14, 14);

            g2.dispose();
            super.paint(g, c);
        }
    }
}
package ui;

import controller.CardController;
import model.TheXeBus;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.AbstractDocument;
import utils.InputFilters;

public class DoiThongTin extends JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(DoiThongTin.class.getName());

    private TheXeBus theHienTai;
    private MainFrame mainFrame;
    private CardController cardController;
    private byte[] anhMoiBytes;

    // UI components
    private JLabel lbPreview;
    private JTextField txtHoTenMoi;
    private JTextField txtNgaySinhMoi;
    private JTextField txtCccdMoi;
    private JButton btnThayAnh;
    private JButton btnXacNhan;
    private JButton btnQuayLai;

    public DoiThongTin(MainFrame mainFrame, TheXeBus card, CardController controller) {
        this.mainFrame = mainFrame;
        this.theHienTai = card;
        this.cardController = controller;

        setTitle("Đổi thông tin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));

        buildUI();
        loadCurrentData();

        setLocationRelativeTo(null);
    }

    private void buildUI() {
        //  ==== Root  ====
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(241, 245, 249)); // #F1F5F9
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        //  ==== Header  ====
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("ĐỔI THÔNG TIN THẺ");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbTitle.setForeground(new Color(15, 23, 42));
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbSub = new JLabel("Cập nhật họ tên, ngày sinh và ảnh chân dung của hành khách.");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSub.setForeground(new Color(71, 85, 105));
        lbSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lbTitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        //  ==== Card container  ====
        JPanel card = new KhoiTaoThe.RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(card, BorderLayout.CENTER);

        //  ==== Main content  ====
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        card.add(content, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        //   Left: Ảnh + nút Thay ảnh  
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lbImgTitle = new JLabel("ẢNH TRÊN THẺ");
        lbImgTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbImgTitle.setForeground(new Color(51, 65, 85));
        lbImgTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lbPreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        Dimension pvSize = new Dimension(150, 190);
        lbPreview.setPreferredSize(pvSize);
        lbPreview.setMinimumSize(pvSize);
        lbPreview.setMaximumSize(pvSize);
        lbPreview.setOpaque(true);
        lbPreview.setBackground(new Color(248, 250, 252)); // #F8FAFC
        lbPreview.setForeground(new Color(100, 116, 139)); // #64748B
        lbPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 6, 6, 6)
        ));
        lbPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnThayAnh = new JButton("Thay ảnh");
        btnThayAnh.setUI(new KhoiTaoThe.ModernButtonUI(new Color(59, 130, 246), Color.WHITE));
        btnThayAnh.setRolloverEnabled(true);
        btnThayAnh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnThayAnh.addActionListener(this::onThayAnh);

        left.add(lbImgTitle);
        left.add(Box.createRigidArea(new Dimension(0, 10)));
        left.add(lbPreview);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(btnThayAnh);
        left.add(Box.createVerticalGlue());

        //   Right: Form thông tin  
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        txtHoTenMoi = new JTextField();
        txtNgaySinhMoi = new JTextField();
        txtCccdMoi = new JTextField();
        
        styleTextField(txtHoTenMoi);
        styleTextField(txtNgaySinhMoi);
        ((AbstractDocument) txtNgaySinhMoi.getDocument()).setDocumentFilter(new InputFilters.DateSlashFilter(10));
        styleTextField(txtCccdMoi);
        ((AbstractDocument) txtCccdMoi.getDocument()).setDocumentFilter(new InputFilters.NumericLimitFilter(12));

        right.add(makeField("Họ và tên", txtHoTenMoi, "Chỉ chữ cái và khoảng trắng"));
        right.add(Box.createRigidArea(new Dimension(0, 10)));
        right.add(makeField("Ngày sinh", txtNgaySinhMoi, "Định dạng dd/MM/yyyy"));
        right.add(Box.createRigidArea(new Dimension(0, 10)));
        right.add(makeField("Số CCCD", txtCccdMoi, "12 chữ số"));
        right.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Bọc left để căn giữa
        JPanel leftWrap = new JPanel(new GridBagLayout());
        leftWrap.setOpaque(false);
        leftWrap.add(left);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.35;
        content.add(leftWrap, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0.65;
        content.add(right, gc);

        //  ==== Footer buttons  ====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setUI(new KhoiTaoThe.ModernButtonUI(new Color(100, 116, 139), Color.WHITE));
        btnQuayLai.setRolloverEnabled(true);
        btnQuayLai.addActionListener(e -> dispose());

        btnXacNhan = new JButton("Lưu thay đổi");
        btnXacNhan.setUI(new KhoiTaoThe.ModernButtonUI(new Color(16, 185, 129), Color.WHITE));
        btnXacNhan.setRolloverEnabled(true);
        btnXacNhan.addActionListener(this::onXacNhan);

        footer.add(btnQuayLai);
        footer.add(btnXacNhan);

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

    // Ô nhập liệu
    gc.gridy++;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 6, 0);
    box.add(field, gc);

    // Hint dưới ô nhập
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

    //  ==== Load dữ liệu hiện tại lên form  ====
    private void loadCurrentData() {
        if (theHienTai == null) return;

        // Họ tên & ngày sinh hiện tại
        txtHoTenMoi.setText(theHienTai.getHoTen());
        txtNgaySinhMoi.setText(theHienTai.getNgaySinh());
        txtCccdMoi.setText(theHienTai.getCccd());

        // Ảnh hiện tại
        try {
            byte[] data = theHienTai.getAnh();
            if (data != null && data.length > 0) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img != null) {
                    // scale cho vừa khung
                    int w = lbPreview.getPreferredSize().width;
                    int h = lbPreview.getPreferredSize().height;
                    Image scaled = img.getScaledInstance(w - 12, h - 12, Image.SCALE_SMOOTH);
                    lbPreview.setText(null);
                    lbPreview.setIcon(new ImageIcon(scaled));
                }
            }
        } catch (Exception e) {
            logger.warning("Lỗi khi load ảnh hiện tại: " + e.getMessage());
        }
    }

    //  ==== Events  ====

    private void onThayAnh(ActionEvent e) {
        chonAnhMoi();
    }

    private void onXacNhan(ActionEvent e) {
        String hoTenMoi = txtHoTenMoi.getText().trim();
        String ngaySinhMoi = txtNgaySinhMoi.getText().trim();
        String cccdMoi = txtCccdMoi.getText().trim();

        //   1. Kiểm tra dữ liệu cơ bản  
        if (hoTenMoi.isEmpty() || ngaySinhMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập đầy đủ thông tin");
            return;
        }

        // Họ tên: chỉ chữ + khoảng trắng
        if (!hoTenMoi.matches("^[\\p{L} ]+$")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Họ tên chỉ được chứa chữ cái và khoảng trắng!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Ngày sinh: dd/MM/yyyy
        if (!ngaySinhMoi.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ngày sinh phải theo định dạng dd/MM/yyyy (ví dụ: 01/12/2000)",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (!isValidDate(ngaySinhMoi)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ngày sinh không hợp lệ!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
                if (!cccdMoi.matches("\\d{12}")) {
            JOptionPane.showMessageDialog(this,
                "CCCD phải gồm đúng 12 chữ số!",
                "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Kiểm tra model / controller
        if (theHienTai == null || cardController == null || mainFrame == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy thông tin thẻ hoặc controller!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        //   2. Không update trực tiếp, mở form nhập PIN  
        NhapMaPin nhapPin = new NhapMaPin(
            mainFrame,
            theHienTai,
            cardController,
            NhapMaPin.ActionType.UPDATE_INFO,
            hoTenMoi,
            ngaySinhMoi,
            cccdMoi,
            anhMoiBytes
        );

        nhapPin.setLocationRelativeTo(this);
        nhapPin.setVisible(true);

        // Đóng form hiện tại
        this.dispose();
    }

    private void chonAnhMoi() {
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

            this.anhMoiBytes = data;
            lbPreview.setText(null);
            lbPreview.setIcon(new ImageIcon(scaled));

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chọn ảnh: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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
}

package ui;

import service.SmartCardService;
import controller.CardController;
import model.TheXeBus;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainFrame extends javax.swing.JFrame {

    // --- Services & Controllers ---
    private SmartCardService smartCardService;
    private CardController cardController;
    // --- UI COMPONENTS ---
    private JPanel sideMenuPanel;
    private JPanel mainContentPanel;
    private RoundedPanel cardViewPanel;

    // Labels hiển thị thông tin
    private JLabel lbSoTheValue, lbHoTenvalue, lbNgaySinhValue, lbCccdValue, lbSoDuValue;
    private JLabel lbVeHienTaiValue;
    private JLabel lbThayanh;
    // Buttons
    private ModernButton btnKhoiTaoThe;
    private ModernButton btnXoaThe, btnMoKhoaThe;
    private ModernButton btnKetNoi, btnNgatketnoi;
    private ModernButton btnThayDoiMaPin, btnDoiThongTin;
    private ModernButton btnThanhToan, btnNapTien;
    private ModernButton btnLichSu; 

    public MainFrame() {
        initComponents();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        e.printStackTrace();
    });

        smartCardService = new SmartCardService();
        cardController = new CardController(this, smartCardService);
        initCustomUI();
        setTitle("Hệ thống quản lý Thẻ Xe Buýt");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        capNhatTrangThaiNut(null);
        btnKetNoi.setEnabled(true);
        btnNgatketnoi.setEnabled(false);
    }

    public void onConnectedUI() {
        btnKetNoi.setEnabled(false);
        btnNgatketnoi.setEnabled(true);
        btnKhoiTaoThe.setEnabled(true);
    }

    public void onDisconnectedUI() {
        btnKetNoi.setEnabled(true);
        btnNgatketnoi.setEnabled(false);
        capNhatTrangThaiNut(null);
    }

    private void initCustomUI() {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().setBackground(new Color(241, 245, 249));

    // 1. SIDEBAR
    JPanel menuContent = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color top = Color.decode("#1CB5E0");
            Color bot = Color.decode("#000046");
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bot);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRect(w - 3, 0, 3, h);

            g2.dispose();
        }
    };

    menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));
    menuContent.setOpaque(false);
    menuContent.setBorder(new EmptyBorder(30, 20, 30, 20));

    JLabel appIcon = new JLabel("🚌");
    appIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
    appIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel appTitle = new JLabel("BUS MANAGER");
    appTitle.setForeground(Color.WHITE);
    appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
    appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    menuContent.add(appIcon);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(appTitle);
    menuContent.add(Box.createRigidArea(new Dimension(0, 20)));

    btnKetNoi = new ModernButton("Kết nối thẻ", new Color(16, 185, 129), Color.WHITE);
    btnNgatketnoi = new ModernButton("Ngắt kết nối", new Color(245, 158, 11), Color.WHITE);
    btnKhoiTaoThe = new ModernButton("Khởi tạo thẻ", new Color(59, 130, 246), Color.WHITE);
    btnXoaThe = new ModernButton("Xóa thẻ", new Color(239, 68, 68), Color.WHITE);
    btnMoKhoaThe = new ModernButton("Mở khóa thẻ", new Color(245, 158, 11), Color.WHITE);
    btnThayDoiMaPin = new ModernButton("Đổi mã PIN", new Color(71, 85, 105), Color.WHITE);
    btnDoiThongTin = new ModernButton("Đổi thông tin", new Color(71, 85, 105), Color.WHITE);
    btnThanhToan = new ModernButton("Thanh toán", new Color(14, 165, 233), Color.WHITE);
    btnNapTien = new ModernButton("Nạp tiền", new Color(14, 165, 233), Color.WHITE);
    btnLichSu = new ModernButton("Lịch sử giao dịch", new Color(107, 114, 128), Color.WHITE);

    setupEvents();

    addSideBarLabel(menuContent, "KẾT NỐI HỆ THỐNG");
    menuContent.add(btnKetNoi);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnNgatketnoi);
    menuContent.add(Box.createRigidArea(new Dimension(0, 15)));

    addSideBarLabel(menuContent, "QUẢN LÝ THẺ");
    menuContent.add(btnKhoiTaoThe);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnXoaThe);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnMoKhoaThe);
    menuContent.add(Box.createRigidArea(new Dimension(0, 15)));
 

    addSideBarLabel(menuContent, "DỊCH VỤ");
    menuContent.add(btnNapTien);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnThanhToan);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnLichSu);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnThayDoiMaPin);
    menuContent.add(Box.createRigidArea(new Dimension(0, 5)));
    menuContent.add(btnDoiThongTin);

    JScrollPane scrollPane = new JScrollPane(menuContent);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.setPreferredSize(new Dimension(280, 0));

    scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(51, 65, 85);
            this.trackColor = new Color(30, 41, 59);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

        private JButton createZeroButton() {
            JButton j = new JButton();
            j.setPreferredSize(new Dimension(0, 0));
            return j;
        }
    });

    // 2. MAIN CONTENT
      
    mainContentPanel = new JPanel(new BorderLayout());
    mainContentPanel.setBackground(new Color(241, 245, 249));
    mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    JPanel centerWrap = new JPanel(new GridBagLayout());
    centerWrap.setOpaque(false);

    cardViewPanel = new RoundedPanel(30, Color.WHITE);
    cardViewPanel.setPreferredSize(new Dimension(800, 420));
    cardViewPanel.setLayout(new BorderLayout());

    JPanel cardHeader = new JPanel(new BorderLayout());
    cardHeader.setOpaque(false);
    cardHeader.setBorder(new EmptyBorder(20, 30, 5, 30));

    JLabel lbCardTitle = new JLabel("THẺ XE BUÝT THÔNG MINH");
    lbCardTitle.setHorizontalAlignment(JLabel.CENTER);
    lbCardTitle.setForeground(new Color(14, 165, 233));
    lbCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
    cardHeader.add(lbCardTitle, BorderLayout.CENTER);

    cardViewPanel.add(cardHeader, BorderLayout.NORTH);

    // BODY INFO
    JPanel body = new JPanel(new GridBagLayout());
    body.setOpaque(false);
    body.setBorder(new EmptyBorder(10, 40, 20, 40));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;

    // ẢNH
    lbThayanh = new JLabel("NO PHOTO", SwingConstants.CENTER);
    lbThayanh.setPreferredSize(new Dimension(210, 120));
    lbThayanh.setOpaque(true);
    lbThayanh.setBackground(new Color(226, 232, 240));
    lbThayanh.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2, true));
    lbThayanh.setFont(new Font("Segoe UI", Font.BOLD, 12));
    lbThayanh.setForeground(Color.GRAY);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 7;  // tăng thêm 1 dòng cho CCCD
    gbc.weightx = 0;
    gbc.insets = new Insets(10, 0, 0, 50);
    body.add(lbThayanh, gbc);

    gbc.gridheight = 1;
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.CENTER;

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.insets = new Insets(10, 0, 15, 0);
    body.add(createHorizontalInfo("SỐ THẺ:", 14, 20), gbc);

    gbc.gridy = 1;
    body.add(createHorizontalInfo("HỌ VÀ TÊN:", 14, 20), gbc);

    gbc.gridy = 2;
    body.add(createHorizontalInfo("NGÀY SINH:", 14, 20), gbc);

    gbc.gridy = 3;
    body.add(createHorizontalInfo("SỐ CCCD:", 14, 20), gbc); 

    gbc.gridy = 4;
    body.add(createHorizontalInfo("VÉ HIỆN TẠI:", 14, 16), gbc);

    // Separator
    JSeparator sep = new JSeparator();
    sep.setForeground(new Color(226, 232, 240));
    gbc.gridy = 5;
    gbc.insets = new Insets(0, 0, 10, 0);
    body.add(sep, gbc);

    // Balance
    JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    balancePanel.setOpaque(false);

    JLabel lbLabelSoDu = new JLabel("SỐ DƯ KHẢ DỤNG: ");
    lbLabelSoDu.setFont(new Font("Segoe UI", Font.BOLD, 23));
    lbLabelSoDu.setForeground(new Color(100, 116, 139));

    lbSoDuValue = new JLabel("--- VND");
    lbSoDuValue.setFont(new Font("Segoe UI", Font.BOLD, 23));
    lbSoDuValue.setForeground(new Color(22, 163, 74));

    balancePanel.add(lbLabelSoDu);
    balancePanel.add(lbSoDuValue);

    gbc.gridy = 6;
    gbc.insets = new Insets(0, 0, 0, 0);
    body.add(balancePanel, gbc);

    gbc.gridy = 7;
    gbc.weighty = 1.0;
    body.add(Box.createGlue(), gbc);

    cardViewPanel.add(body, BorderLayout.CENTER);

    centerWrap.add(cardViewPanel);
    mainContentPanel.add(centerWrap, BorderLayout.CENTER);

    getContentPane().add(scrollPane, BorderLayout.WEST);
    getContentPane().add(mainContentPanel, BorderLayout.CENTER);
}
    private JPanel createHorizontalInfo(String title, int titleSize, int valueSize) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JLabel lbT = new JLabel(title);
        lbT.setFont(new Font("Segoe UI", Font.BOLD, titleSize));
        lbT.setForeground(new Color(148, 163, 184));
        lbT.setPreferredSize(new Dimension(150, 30));
        lbT.setVerticalAlignment(SwingConstants.CENTER);

        JLabel lbV = new JLabel("...");
        lbV.setFont(new Font("Segoe UI", Font.BOLD, valueSize));
        lbV.setForeground(new Color(30, 41, 59));
        lbV.setBorder(new javax.swing.border.EmptyBorder(0, 6, 0, 0));
        lbV.setVerticalAlignment(SwingConstants.CENTER);

        g.gridx = 0; g.weightx = 0; p.add(lbT, g);
        g.gridx = 1; g.weightx = 1; p.add(lbV, g);

        if (title.contains("SỐ THẺ"))           lbSoTheValue = lbV;
        else if (title.contains("HỌ VÀ TÊN"))   lbHoTenvalue = lbV;
        else if (title.contains("NGÀY SINH"))   lbNgaySinhValue = lbV;
        else if (title.contains("SỐ CCCD")) lbCccdValue = lbV;
        else if (title.contains("VÉ HIỆN TẠI")) lbVeHienTaiValue = lbV;

        return p;
    }
    private void addSideBarLabel(JPanel panel, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(new Color(251, 191, 36));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private void setupEvents() {
        btnKhoiTaoThe.addActionListener(evt -> btnKhoiTaoTheActionPerformed(evt));
        btnXoaThe.addActionListener(evt -> btnXoaTheActionPerformed(evt));
        btnMoKhoaThe.addActionListener(evt -> btnMoKhoaTheActionPerformed(evt));
        btnKetNoi.addActionListener(evt -> btnKetNoiActionPerformed(evt));
        btnNgatketnoi.addActionListener(evt -> btnNgatketnoiActionPerformed(evt));
        btnThayDoiMaPin.addActionListener(evt -> btnThayDoiMaPinActionPerformed(evt));
        btnDoiThongTin.addActionListener(evt -> btnDoiThongTinActionPerformed(evt));
        btnThanhToan.addActionListener(evt -> btnThanhToanActionPerformed(evt));
        btnNapTien.addActionListener(evt -> btnNapTienActionPerformed(evt));
        btnLichSu.addActionListener(evt -> btnLichSuActionPerformed(evt));
    }
    // LOGIC
    private void btnKhoiTaoTheActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;

        KhoiTaoThe khoiTaoThe = new KhoiTaoThe(this, cardController);
        khoiTaoThe.setLocationRelativeTo(this);
        khoiTaoThe.setAlwaysOnTop(true);
        khoiTaoThe.setVisible(true);
    }

    private void btnKetNoiActionPerformed(java.awt.event.ActionEvent evt) {
        cardController.onConnect();
    }

    private void btnNgatketnoiActionPerformed(java.awt.event.ActionEvent evt) {
        cardController.onDisconnect();
    }

    private void btnThayDoiMaPinActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        DoiMaPin doiMaPin = new DoiMaPin(the, cardController);
        doiMaPin.setLocationRelativeTo(this);
        doiMaPin.setVisible(true);
    }

    private void btnMoKhoaTheActionPerformed(java.awt.event.ActionEvent evt) {
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        // 1. Kiểm tra thẻ có thật sự bị khóa
        if (!the.isBiKhoa()) {
            JOptionPane.showMessageDialog(this,
                    "Thẻ này hiện tại không bị khóa!",
                    "Không thể mở khóa",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. Yêu cầu xác thực trước khi mở khóa (nhập PIN cũ)
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Thẻ đang bị khóa.\nBạn cần nhập đúng mã PIN cũ để mở khóa.\nTiếp tục?",
                "Xác nhận mở khóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // 3. Mở form nhập PIN – loại mở khóa
        NhapMaPin pinForm = new NhapMaPin(
                this,
                the,
                cardController,
                NhapMaPin.ActionType.UNLOCK  
        );

        pinForm.setLocationRelativeTo(this);
        pinForm.setVisible(true);
    }


    private void btnDoiThongTinActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        DoiThongTin dtt = new DoiThongTin(this, the, cardController);
        dtt.setLocationRelativeTo(this);
        dtt.setAlwaysOnTop(true);
        dtt.setVisible(true);
    }

    private void btnNapTienActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        NapTien nt = new NapTien(this, the, cardController);
        nt.setLocationRelativeTo(this);
        nt.setAlwaysOnTop(true);
        nt.setVisible(true);
    }

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        ThanhToan tt = new ThanhToan(this, the, cardController);
        tt.setLocationRelativeTo(this);
        tt.setAlwaysOnTop(true);
        tt.setVisible(true);
    }

    private void btnLichSuActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        LichSuGiaoDich ls = new LichSuGiaoDich(cardController);
        ls.setLocationRelativeTo(this);
        ls.setAlwaysOnTop(true);
        ls.setVisible(true);
    }

    private void btnXoaTheActionPerformed(java.awt.event.ActionEvent evt) {
        if (!ensureConnected()) return;
        TheXeBus the = cardController.getCurrentCard();
        if (the == null) return;

        NhapMaPin nmp = new NhapMaPin(this, the, cardController, NhapMaPin.ActionType.DELETE_CARD);
        nmp.setLocationRelativeTo(this);
        nmp.setVisible(true);
    }

    private boolean ensureConnected() {
        if (!cardController.isConnected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng kết nối với thẻ trước!", "Chưa kết nối", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ==== UI state enable/disable ====
    public void capNhatTrangThaiNut(TheXeBus card) {

        if (card == null) {
            btnMoKhoaThe.setEnabled(false);
            btnThayDoiMaPin.setEnabled(false);
            btnDoiThongTin.setEnabled(false);
            btnThanhToan.setEnabled(false);
            btnNapTien.setEnabled(false);
            btnLichSu.setEnabled(false);
            btnKhoiTaoThe.setEnabled(cardController.isConnected());
            btnXoaThe.setEnabled(false);
            return;
        }

        if (card.isBiKhoa()) {
            btnMoKhoaThe.setEnabled(true);
            btnKhoiTaoThe.setEnabled(false);
            btnNapTien.setEnabled(false);
            btnThanhToan.setEnabled(false);
            btnThayDoiMaPin.setEnabled(false);
            btnDoiThongTin.setEnabled(false);
            btnXoaThe.setEnabled(false);
            btnLichSu.setEnabled(false); 
            return;
        }

        btnMoKhoaThe.setEnabled(false);
        btnKhoiTaoThe.setEnabled(true);
        btnThayDoiMaPin.setEnabled(true);
        btnDoiThongTin.setEnabled(true);
        btnThanhToan.setEnabled(true);
        btnNapTien.setEnabled(true);
        btnXoaThe.setEnabled(true);
        btnLichSu.setEnabled(true);
    }
    public void capNhatTrangThaiNut_KhiTheBiKhoa() {
        btnMoKhoaThe.setEnabled(true);
        btnNgatketnoi.setEnabled(true);
        btnKhoiTaoThe.setEnabled(false);
        btnNapTien.setEnabled(false);
        btnThanhToan.setEnabled(false);
        btnThayDoiMaPin.setEnabled(false);
        btnDoiThongTin.setEnabled(false);
        btnXoaThe.setEnabled(false);
        btnLichSu.setEnabled(false);
}

    public void enableReconnectButtons() {
        btnKhoiTaoThe.setEnabled(false);
        btnNgatketnoi.setEnabled(true);
    }
    public void disableReconnectButtons() {
        btnKhoiTaoThe.setEnabled(false);
        btnNgatketnoi.setEnabled(false);
}

    public void hienThiThongTinThe(TheXeBus the) {

    if (the == null) return;

    // 1) Thông tin cơ bản
    lbSoTheValue.setText(the.getSoThe());
    lbHoTenvalue.setText(the.getHoTen());
    lbNgaySinhValue.setText(the.getNgaySinh());
    lbCccdValue.setText(the.getCccd());
    lbSoDuValue.setText(String.format("%,d VND", the.getSoDu()));
    capNhatTrangThaiNut(the);
    // 2) Ảnh
    
    try {
        byte[] data = the.getAnh();

        if (data != null && data.length > 0) {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));

            if (img != null) {
                // Giữ đúng tỉ lệ ảnh
                int w = lbThayanh.getWidth();
                int h = lbThayanh.getHeight();

                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                lbThayanh.setIcon(new ImageIcon(scaled));
                lbThayanh.setText("");
            } else {
                lbThayanh.setIcon(null);
                lbThayanh.setText("NO PHOTO");
            }

        } else {
            lbThayanh.setIcon(null);
            lbThayanh.setText("NO PHOTO");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        lbThayanh.setIcon(null);
        lbThayanh.setText("NO PHOTO");
    }
        // 3) Vé hiện tại
        
        byte passType = the.getPassType();
        long expTsLong = the.getPassExpire() & 0xFFFFFFFFL;   // Fix int → unsigned long

        if (passType == 0 || expTsLong == 0) {
            lbVeHienTaiValue.setText("Chưa có vé khả dụng");
            return;
        }
        // Xác định loại vé
        String loai;
        switch (passType) {
            case 1 -> loai = "Vé ngày";
            case 2 -> loai = "Vé tháng";
            case 3 -> loai = "Vé năm";
            default -> loai = "Vé không xác định";
        }
        // Epoch → LocalDateTime
        LocalDateTime expTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(expTsLong),
                ZoneId.systemDefault()
        );
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        // Nếu đã hết hạn
        if (expTime.isBefore(LocalDateTime.now())) {
            lbVeHienTaiValue.setText(
                    loai + " (ĐÃ HẾT HẠN: " + expTime.format(fmt) + ")"
            );
        } else {
            lbVeHienTaiValue.setText(
                    loai + " (Hết hạn: " + expTime.format(fmt) + ")"
            );
        }
}
    
    public void clearThongTinThe() {
        lbSoTheValue.setText("...");
        lbHoTenvalue.setText("...");
        lbNgaySinhValue.setText("...");
        lbCccdValue.setText("...");
        lbSoDuValue.setText("--- VND");
        if (lbVeHienTaiValue != null) {
            lbVeHienTaiValue.setText("...");
        }
        lbThayanh.setIcon(null);
        lbThayanh.setText("NO PHOTO");
        capNhatTrangThaiNut(null);
    }
    public void clearThongTinThe_OnlyInfo() {
        lbSoTheValue.setText("...");
        lbHoTenvalue.setText("...");
        lbNgaySinhValue.setText("...");
        lbCccdValue.setText("...");
        lbSoDuValue.setText("--- VND");
        if (lbVeHienTaiValue != null) lbVeHienTaiValue.setText("...");
        lbThayanh.setIcon(null);
        lbThayanh.setText("NO PHOTO");
}
    
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 601, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>
}
//   UI Components  
class ModernButton extends JButton {
    private final Color normalColor;
    private final Color hoverColor;

    public ModernButton(String text, Color bgColor, Color fgColor) {
        super(text);
        this.normalColor = bgColor;
        this.hoverColor = bgColor.brighter();

        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(fgColor);
        setBackground(bgColor);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setHorizontalAlignment(SwingConstants.CENTER);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        setMaximumSize(new Dimension(180, 50));
        setMinimumSize(new Dimension(150, 50));
        setPreferredSize(new Dimension(170, 50));

        setContentAreaFilled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverColor);
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(normalColor);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isEnabled()) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));

        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundedPanel extends JPanel {
    private final int radius;
    private final Color backgroundColor;

    public RoundedPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int shadowOffset = 6;
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fillRoundRect(shadowOffset, shadowOffset, w - shadowOffset, h - shadowOffset, radius, radius);

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, w - shadowOffset, h - shadowOffset, radius, radius);

        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(0, 0, w - shadowOffset - 1, h - shadowOffset - 1, radius, radius);

        g2.dispose();
    }
}
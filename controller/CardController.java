package controller;

import database.CardKeyRepository;
import ui.MainFrame;
import ui.NhapMaPin;
import service.SmartCardService;
import model.TheXeBus;
import java.awt.Window;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.swing.*;

public class CardController {

    private final SmartCardService smartCardService;
    private final MainFrame mainFrame;

    private boolean connected = false;
    private boolean authenticated = false;
    private boolean reconnecting = false;
    private TheXeBus currentCard;
    
    // Giá vé cố định
    public static final int GIA_VE_NGAY  = 12000;
    public static final int GIA_VE_THANG = 300000;
    public static final int GIA_VE_NAM   = 3200000;

    public CardController(MainFrame mainFrame, SmartCardService smartCardService) {
        this.mainFrame = mainFrame;
        this.smartCardService = smartCardService;
    }
    // GETTERS
    public boolean isConnected() {
        return connected;
    }
    public boolean isAuthenticated() {
        return authenticated;
    }
    public TheXeBus getCurrentCard() {
        return currentCard;
    }
    public java.util.List<SmartCardService.HistoryRecord> getHistory() {
        try {
            return smartCardService.getHistory();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Không đọc được lịch sử giao dịch!");
            return java.util.Collections.emptyList();
        }
    }
     public boolean isReconnecting() {
        return reconnecting;
    }
    // CONNECT CARD
    public void onConnect() {

        if (connected) return;

        // 1. Kết nối thẻ
        if (!smartCardService.connectCard()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Không thể kết nối thẻ. Vui lòng kiểm tra đầu đọc!",
                "Lỗi", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 2. Đồng bộ AES Default
        try {
            smartCardService.setupAES("123456");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Không thể sinh AES key!");
            return;
        }

        // 3. Đọc thông tin thẻ
        SmartCardService.CardInfo info;
        try {
            info = smartCardService.getCardInfo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                "Không đọc được thông tin thẻ!");
            return;
        }

        connected = true;
        authenticated = false;
        mainFrame.onConnectedUI();

        // 4. Thẻ trắng
        if (!info.initialized) {
            JOptionPane.showMessageDialog(mainFrame,
                "Chưa có thông tin thẻ. Vui lòng tạo thẻ mới.");
            mainFrame.capNhatTrangThaiNut(null);
            return;
        }

        // 5. Thẻ khóa
        if (info.locked) {
            JOptionPane.showMessageDialog(mainFrame,
                "Thẻ đã bị khóa do nhập sai PIN quá số lần cho phép.\n" +
                "Vui lòng mở khóa thẻ để tiếp tục.",
                "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
            loadCardInfoToUI();
            mainFrame.capNhatTrangThaiNut(getCurrentCard());
            return;
        }

        // 6. Nhập PIN
        NhapMaPin pinForm = new NhapMaPin(
            mainFrame, null, this, NhapMaPin.ActionType.LOGIN);
        pinForm.setLocationRelativeTo(mainFrame);
        pinForm.setVisible(true);

        if (reconnecting) {
            mainFrame.disableReconnectButtons();
        }
}

    // DISCONNECT CARD
    public void onDisconnect() {
        if (!connected) return;

        boolean ok = smartCardService.disconnectCard();
        if (ok) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Ngắt kết nối đầu đọc thành công.",
                    "Ngắt kết nối",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "Ngắt kết nối đầu đọc không thành công.",
                    "Ngắt kết nối",
                    JOptionPane.WARNING_MESSAGE);
        }
        connected = false;
        authenticated = false;
        currentCard = null;
        reconnecting = true;
        mainFrame.onDisconnectedUI();
        mainFrame.clearThongTinThe();
    }
    // VERIFY PIN
    public boolean verifyPin(String pin) {
    try {
        int sw = smartCardService.verifyPinOnCard(pin);

        if (sw == 0x9000) {

            // 1. Lấy cardCode
            SmartCardService.CardInfo info = smartCardService.getCardInfo();
            String cardCode = info.cardCode; 

            // 2. RSA Verify
            try {
                CardKeyRepository repo = new CardKeyRepository();
                String[] pub = repo.getKey(cardCode);

                if (pub == null) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Không tìm thấy public key trong DB!");
                    return false;
                }

                PublicKey pk = buildPublicKey(pub[0], pub[1]);
                boolean ok = smartCardService.rsaAuthenticateUsing(pk);

                if (!ok) {
                    JOptionPane.showMessageDialog(mainFrame,
                        "RSA verify thất bại!",
                        "Lỗi RSA", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Lỗi RSA: " + ex.getMessage());
                return false;
            }

            // Xác thực thành công
            authenticated = true;

            if (reconnecting) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Kết nối lại thẻ thành công!");
                mainFrame.enableReconnectButtons();
                reconnecting = false;
            }

            // **Load info đúng 1 lần**
            loadCardInfoToUI();
            return true;
        }

        // ====== Xử lý lỗi ======
        if ((sw & 0xFFF0) == 0x63C0) {
            int remain = sw & 0xF;
            JOptionPane.showMessageDialog(mainFrame,
                    "Sai mã PIN! Bạn còn " + remain + " lần thử.");
            return false;
        }

        if (sw == 0x6983) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Thẻ đã bị khóa!");

            loadCardInfoToUI();
            for (Window w : Window.getWindows()) {
                if (w instanceof NhapMaPin) w.dispose();
            }
            mainFrame.capNhatTrangThaiNut(currentCard);
            return false;
        }

        JOptionPane.showMessageDialog(mainFrame,
                "Lỗi không xác định! SW = " + Integer.toHexString(sw));
        return false;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame, "Lỗi xác thực PIN!");
        return false;
    }
}

private PublicKey buildPublicKey(String modHex, String expHex) throws Exception {
    KeyFactory kf = KeyFactory.getInstance("RSA");

    BigInteger mod = new BigInteger(1, hexToBytes(modHex));  
    BigInteger exp = new BigInteger(1, hexToBytes(expHex));

    return kf.generatePublic(new RSAPublicKeySpec(mod, exp));
}


    // LOAD CARD INFO TO UI
    private void loadCardInfoToUI() {
        try {
            SmartCardService.CardInfo info = smartCardService.getCardInfo();
            if (info == null) return;

            byte[] photo = null;
            if (info.photoLen > 0) {
                photo = smartCardService.readPhoto(info.photoLen);
            }
            currentCard = new TheXeBus(
                    info.cardCode,
                    info.hoTen,
                    info.ngaySinh,
                    info.cccd,       
                    info.balance,
                    info.locked,
                    photo,
                    info.passType,
                    info.passExpire
            );

            mainFrame.hienThiThongTinThe(currentCard);
            mainFrame.capNhatTrangThaiNut(currentCard);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Không load được dữ liệu thẻ!");
        }
    }

    // CREATE NEW CARD
    public TheXeBus createNewCard(
        String hoTen,
        String ngaySinh,
        String cccd,
        byte[] photoBytes) {

    try {
        //   1. Tự sinh số thẻ VN01, VN02...  
        String cardCode = smartCardService.generateCardCode();

        //   2. Khởi tạo thẻ trên SmartCard  
        if (!smartCardService.initCard(cardCode, hoTen, ngaySinh, cccd)) {
            JOptionPane.showMessageDialog(mainFrame, "Khởi tạo thẻ thất bại!");
            return null;
        }
        //   3. Ghi ảnh xuống thẻ
        if (photoBytes != null && photoBytes.length > 0) {
            if (!smartCardService.writePhotoOnCard(photoBytes)) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Ghi ảnh xuống thẻ thất bại!");
            }
        }

        //   4. Lưu PUBLIC KEY của thẻ vào Firebase
        try {
            String[] pub = smartCardService.getRSAPublicKeyHex(); // [mod, exp]

            CardKeyRepository repo = new CardKeyRepository();
            repo.saveKey(cardCode, pub[0], pub[1]);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Không lưu được public key vào DB!\n" + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        //   5. Load dữ liệu thẻ vào UI  
        loadCardInfoToUI();
        return currentCard;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame, "Lỗi khởi tạo thẻ!");
        return null;
    }
}
    
    // UPDATE INFO
    public void updateInfo(String name, String dob, String cccd, byte[] photo) {
    try {
        boolean ok = smartCardService.updatePersonalInfoOnCard(name, dob, cccd);

        if (!ok) {
            JOptionPane.showMessageDialog(mainFrame, "Lỗi cập nhật thông tin cá nhân!");
            return;
        }
        if (photo != null) {
            boolean okPhoto = smartCardService.writePhotoOnCard(photo);
            if (!okPhoto) {
                JOptionPane.showMessageDialog(mainFrame, "Lỗi ghi ảnh xuống thẻ!");
            }
        }
        loadCardInfoToUI();
        JOptionPane.showMessageDialog(mainFrame, "Cập nhật thông tin thành công!");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame,
                "Lỗi cập nhật thông tin: " + e.getMessage());
    }
}
    public boolean topUpDirect(int amount) {
    try {
        boolean ok = smartCardService.topUp(amount);
        if (!ok) return false;

        loadCardInfoToUI();
        return true;

    } catch (Exception e) {
        return false;
    }
}

    // PAY / MUA VÉ
    public boolean pay(int amount) {

    if (!authenticated) {
        JOptionPane.showMessageDialog(mainFrame, "Bạn cần đăng nhập PIN trước!");
        return false;
    }

    // Lấy currentCard không cần load lại từ thẻ
    TheXeBus the = getCurrentCard();
    long now = System.currentTimeMillis() / 1000;

    boolean conHan = (the.getPassType() != 0) && (the.getPassExpire() > now);

    if (conHan) {
        if (the.getPassType() == 3) {
            JOptionPane.showMessageDialog(mainFrame,
                "Vé năm đang còn hiệu lực!");
            return false;
        }
        if (the.getPassType() == 2 &&
            (amount == GIA_VE_NGAY || amount == GIA_VE_THANG)) {
            JOptionPane.showMessageDialog(mainFrame,
                "Vé tháng đang còn hiệu lực!");
            return false;
        }
        if (the.getPassType() == 1 &&
            amount == GIA_VE_NGAY) {
            JOptionPane.showMessageDialog(mainFrame,
                "Vé ngày đang còn hiệu lực!");
            return false;
        }
    }

    try {
        int sw = smartCardService.payTicket(amount);

        if (sw != 0x9000) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Thanh toán thất bại! SW=" + Integer.toHexString(sw));
            return false;
        }

        // GHI VÉ MỚI
        byte passType = 0;
        long expire = now;

        if (amount == GIA_VE_NGAY) {
            passType = 1;
            expire += 86400;
        } else if (amount == GIA_VE_THANG) {
            passType = 2;
            expire += 2592000;
        } else if (amount == GIA_VE_NAM) {
            passType = 3;
            expire += 31536000;
        }

        smartCardService.setPass(passType, expire);

        // **Chỉ load lại UI khi mọi thứ đã OK**
        loadCardInfoToUI();

        JOptionPane.showMessageDialog(mainFrame, "Thanh toán thành công!");
        return true;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame,
            "Lỗi thanh toán!");
        return false;
    }
}
    // CHANGE PIN
   public int changePin(String oldPin, String newPin) {
    try {
        // 1) Trước tiên phải VERIFY PIN
        int swVerify = smartCardService.verifyPinOnCard(oldPin);

        if (swVerify != 0x9000) {
            // Sai PIN: 63Cx
            if ((swVerify & 0xFFF0) == 0x63C0) {
                int remain = swVerify & 0x000F;

                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Sai mã PIN cũ! Bạn còn " + remain + " lần thử.",
                        "PIN sai",
                        JOptionPane.ERROR_MESSAGE
                );

                return 1;
            }
            // Thẻ bị khóa do sai PIN nhiều lần
            if (swVerify == 0x6983) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Thẻ đã bị khóa do nhập sai PIN quá số lần cho phép!",
                        "Thẻ bị khóa",
                        JOptionPane.ERROR_MESSAGE
                );

                loadCardInfoToUI();
                mainFrame.capNhatTrangThaiNut(getCurrentCard());
                return 2;
            }
            // Các lỗi khác
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Không thể xác thực PIN cũ! SW = " + Integer.toHexString(swVerify),
                    "Lỗi xác thực",
                    JOptionPane.ERROR_MESSAGE
            );

            return -1;
        }
        // 2) VERIFY thành công → AES key đã được sinh ra
        int sw = smartCardService.changePin(oldPin, newPin);

        if (sw == 0x9000) {
            JOptionPane.showMessageDialog(mainFrame, "Đổi PIN thành công!");
            loadCardInfoToUI();
            return 0;
        }

        // CHANGE_PIN trả lỗi 63Cx (hiếm gặp)
        if ((sw & 0xFFF0) == 0x63C0) {
            int remain = sw & 0x000F;

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Sai mã PIN cũ! Bạn còn " + remain + " lần thử.",
                    "PIN sai",
                    JOptionPane.ERROR_MESSAGE
            );

            return 1;
        }
        // Lỗi phổ biến: AES key không hợp lệ hoặc không verify trước
        if (sw == 0x6982) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Không đủ điều kiện bảo mật (SW = 6982).\n" +
                    "Có thể bạn chưa xác thực PIN hoặc AES key không hợp lệ!",
                    "Lỗi đổi PIN",
                    JOptionPane.ERROR_MESSAGE
            );
            return -1;
        }
        // Thẻ bị khóa
        if (sw == 0x6983) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Thẻ đã bị khóa do nhập sai PIN quá số lần!",
                    "Thẻ bị khóa",
                    JOptionPane.ERROR_MESSAGE
            );

            loadCardInfoToUI();
            mainFrame.capNhatTrangThaiNut(getCurrentCard());
            return 2;
        }
        // Các lỗi khác
        JOptionPane.showMessageDialog(
                mainFrame,
                "Lỗi đổi PIN! SW = " + Integer.toHexString(sw),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );

        return -1;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame,
                "Lỗi đổi PIN: " + e.getMessage());
        return -1;
    }
}
   public void setPass(byte type, long expire) {
    try {
        int sw = smartCardService.setPass(type, expire);

        if (sw == 0x9000) {
            loadCardInfoToUI();    // cập nhật UI khi vé được ghi
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "Lỗi ghi vé xuống thẻ! SW=" + Integer.toHexString(sw),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame,
                "Lỗi setPass: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
    // UNLOCK THẺ
    public boolean unlockCard(String pin) {
    try {
        boolean ok = smartCardService.unlockCard(pin);

        if (!ok) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Mã PIN bạn nhập không đúng!",
                    "Sai PIN",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        loadCardInfoToUI();
        mainFrame.capNhatTrangThaiNut(currentCard);
        return true;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(mainFrame, "Lỗi mở khóa thẻ!");
        return false;
    }
}
    // DELETE CARD
    public boolean deleteCard() {
        if (!authenticated) {
            JOptionPane.showMessageDialog(mainFrame, "Bạn cần đăng nhập PIN trước!");
            return false;
        }

        try {
            if (!smartCardService.deleteCard()) {
                JOptionPane.showMessageDialog(mainFrame, "Xóa thẻ thất bại!");
                return false;
            }

            // LƯU cardCode TRƯỚC KHI xoá currentCard
            String cardCode = currentCard.getSoThe();

            // Reset UI & state
            currentCard = null;
            authenticated = false;
            mainFrame.clearThongTinThe();

            // XOÁ PUBLIC KEY TRONG FIRESTORE
            try {
                CardKeyRepository repo = new CardKeyRepository();
                repo.deleteKey(cardCode);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Xóa thẻ thành công nhưng lỗi khi xóa key trên Firestore!\n" + ex.getMessage(),
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(mainFrame, "Xóa thẻ thành công!");
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Lỗi xóa thẻ!");
            return false;
        }
    }
    public boolean doRSAAuthentication() {
        try {
            // 1) Lấy cardCode của thẻ
            SmartCardService.CardInfo info = smartCardService.getCardInfo();
            String cardCode = info.cardCode;

            // 2) Lấy public key từ Firestore
            CardKeyRepository repo = new CardKeyRepository();
            String[] pub = repo.getKey(cardCode);
            if (pub == null) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Không tìm thấy public key trong DB!");
                return false;
            }

            PublicKey pk = buildPublicKey(pub[0], pub[1]);

            // 3) RSA Challenge-response
            boolean ok = smartCardService.rsaAuthenticateUsing(pk);
            if (!ok) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "RSA xác thực thất bại!",
                        "Lỗi RSA",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            return ok;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Lỗi RSA: " + ex.getMessage());
            return false;
        }
}

    private byte[] hexToBytes(String hex) {
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i/2] = (byte)((Character.digit(hex.charAt(i), 16) << 4)
                         + Character.digit(hex.charAt(i+1), 16));
    }
    return data;
}
}

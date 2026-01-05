package service;

import javax.smartcardio.*;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import java.util.*;
import java.util.concurrent.*;

public class SmartCardService {

    private CardTerminal terminal;
    private Card card;
    public CardChannel channel;

    private static int lastCardSeq = 0;

    // AID
    private static final byte[] APPLET_AID = {
            (byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x00,(byte)0x00
    };

    // INS
    private static final byte CLA_APP         = (byte) 0x00;
    private static final byte INS_INIT_CARD   = (byte) 0x10;
    private static final byte INS_RSA_PUB     = (byte) 0x11;
    private static final byte INS_RSA_SIGN    = (byte) 0x12;
    
    private static final byte INS_VERIFY_PIN  = (byte) 0x20;
    private static final byte INS_CHANGE_PIN  = (byte) 0x21;
    
    private static final byte INS_GET_INFO    = (byte) 0x30;
    private static final byte INS_UPDATE_INFO = (byte) 0x31;
    private static final byte INS_WRITE_PHOTO = (byte) 0x32;
    private static final byte INS_READ_PHOTO  = (byte) 0x33;
    private static final byte INS_GET_HISTORY = (byte) 0x34;

    private static final byte INS_TOP_UP      = (byte) 0x40;
    private static final byte INS_PAY_TICKET  = (byte) 0x41;
    private static final byte INS_SET_PASS    = (byte) 0x42;
    
    private static final byte INS_LOCK_CARD   = (byte) 0x50;
    private static final byte INS_UNLOCK_CARD = (byte) 0x51;
    
    private static final byte INS_DELETE_CARD = (byte) 0x60;

    // AES SESSION KEY DERIVED AFTER VERIFY_PIN
    private SecretKeySpec aesKey;
    private PublicKey rsaPublicKey;
    // Timeout / Executor
    private static final long APDU_TIMEOUT_MS = 5000;
    private final ExecutorService apduExecutor = Executors.newCachedThreadPool();

    // MODEL
    public static class CardInfo {
        public short cardId;
        public int balance;
        public boolean locked;
        public boolean initialized;
        public String hoTen;
        public String ngaySinh;
        public String cccd;
        public short photoLen;
        public boolean pinChanged;
        public byte passType;
        public int  passExpire;
        public String cardCode;
    }

    public static class HistoryRecord {
        public byte type;
        public int amount;
        public int timestamp;
    }
    // CONNECT
    public boolean connectCard() {
        try {
            TerminalFactory tf = TerminalFactory.getDefault();
            List<CardTerminal> list = tf.terminals().list();
            if (list.isEmpty()) return false;

            terminal = list.get(0);
            if (!terminal.waitForCardPresent(2000)) return false;

            card = terminal.connect("*");
            channel = card.getBasicChannel();

            CommandAPDU select = new CommandAPDU(0x00,0xA4,0x04,0x00,APPLET_AID);
            ResponseAPDU resp = transmit(select);

            return resp.getSW() == 0x9000;

        } catch (Exception e) {
            safeClose();
            return false;
        }
    }
    
    public String generateCardCode() {
    lastCardSeq++;
    return (lastCardSeq < 10)
            ? "VN0" + lastCardSeq
            : "VN" + lastCardSeq;
}

    public boolean disconnectCard() {
        safeClose();
        return true;
    }
    
    public void safeClose() {
        try { 
            if (card != null) card.disconnect(false); 
        } catch (Exception ignored) {}
        card = null;
        channel = null;
    }

    private ResponseAPDU transmit(CommandAPDU cmd) throws Exception {
        Future<ResponseAPDU> f = apduExecutor.submit(() -> channel.transmit(cmd));
        return f.get(APDU_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private ResponseAPDU send(byte ins, byte[] data) throws Exception {
        if (data == null) data = new byte[0];
        CommandAPDU cmd = new CommandAPDU(CLA_APP, ins, 0, 0, data);
        return transmit(cmd);
    }

    private ResponseAPDU sendLe(byte ins, int le) throws Exception {
        CommandAPDU cmd = new CommandAPDU(CLA_APP, ins, 0, 0, le);
        return transmit(cmd);
    }

    // RSA SECURITY
    public void loadRSAPublicKey() throws Exception {
        ResponseAPDU resp = sendLe(INS_RSA_PUB, 256);
        byte[] d = resp.getData();

        int modLen = d[0] & 0xFF;
        byte[] modulus = Arrays.copyOfRange(d, 1, 1 + modLen);

        int expLenPos = 1 + modLen;
        int expLen = d[expLenPos] & 0xFF;
        byte[] exponent = Arrays.copyOfRange(d, expLenPos + 1, expLenPos + 1 + expLen);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        rsaPublicKey = kf.generatePublic(
            new X509EncodedKeySpec(buildX509RSA(modulus, exponent))
        );
    }

    private byte[] buildX509RSA(byte[] mod, byte[] exp) {
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            out.write(0x30);
            byte[] body = buildRSABody(mod, exp);
            out.write(encodeLen(body.length));
            out.write(body);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] buildRSABody(byte[] mod, byte[] exp) throws Exception {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(0x30);
        o.write(0x0D);
        o.write(new byte[]{
                0x06,0x09,0x2A,(byte)0x86,0x48,(byte)0x86,(byte)0xF7,0x0D,0x01,0x01,0x01,
                0x05,0x00
        });

        byte[] key = buildRSAPublicKey(mod, exp);
        o.write(0x03);
        o.write(encodeLen(key.length + 1));
        o.write(0x00);
        o.write(key);

        return o.toByteArray();
    }

    private byte[] buildRSAPublicKey(byte[] mod, byte[] exp) throws Exception {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(0x30);
        byte[] seq = buildSeq(mod, exp);
        o.write(encodeLen(seq.length));
        o.write(seq);
        return o.toByteArray();
    }

    private byte[] buildSeq(byte[] mod, byte[] exp) throws Exception {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(0x02);
        o.write(encodeLen(mod.length));
        o.write(mod);

        o.write(0x02);
        o.write(encodeLen(exp.length));
        o.write(exp);

        return o.toByteArray();
    }

    private byte[] encodeLen(int len) {
        if (len < 128) return new byte[]{ (byte)len };
        return new byte[]{ (byte)0x81, (byte)len };
    }

    public void setupAES(String pin) throws Exception {
        byte[] p = pin.getBytes();
        byte[] data = new byte[1 + p.length];
        data[0] = (byte)p.length;
        System.arraycopy(p, 0, data, 1, p.length);

        // gửi xuống thẻ
        send((byte)0x13, data);

        // PC tự derive AES
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha.digest(p);
        aesKey = new SecretKeySpec(digest, 0, 16, "AES");
}

    // AES ENCRYPT
    private byte[] aesEncrypt(byte[] plain) throws Exception {

        if (plain.length % 16 != 0) {
            int newLen = ((plain.length / 16) + 1) * 16;
            plain = Arrays.copyOf(plain, newLen);
        }

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] enc = cipher.doFinal(plain);

        byte[] out = new byte[16 + enc.length];
        System.arraycopy(iv,0,out,0,16);
        System.arraycopy(enc,0,out,16,enc.length);

        return out;
    }
     
    // VERIFY PIN
    public int verifyPinOnCard(String pin) throws Exception {

        SmartCardService.CardInfo info = getCardInfo();
        byte[] p = pin.getBytes();
        byte[] data;

        if (!info.pinChanged) {

            // plaintext PIN send
            data = new byte[1 + p.length];
            data[0] = (byte)p.length;
            System.arraycopy(p,0,data,1,p.length);

            ResponseAPDU resp = send(INS_VERIFY_PIN, data);
            if (resp.getSW() == 0x6983) {
            send(INS_LOCK_CARD, new byte[0]);
        }
            return resp.getSW();
        }

        // AES ENCRYPTED VERIFY PIN
        byte[] plain = new byte[1 + p.length];
        plain[0] = (byte)p.length;
        System.arraycopy(p,0,plain,1,p.length);

        byte[] enc = aesEncrypt(plain);
        ResponseAPDU resp = send(INS_VERIFY_PIN, enc);
        if (resp.getSW() == 0x6983) {
           send(INS_LOCK_CARD, new byte[0]);
}
        return resp.getSW();
}

    // NEW INIT_CARD (AES ENCRYPTED – NO PIN – WITH CCCD)
     
    public boolean initCard(String cardCode, String hoTen, String ngaySinh, String cccd)
        throws Exception {
       
        byte[] code = cardCode.getBytes();
        byte[] name = hoTen.getBytes();
        byte[] dob  = ngaySinh.getBytes();
        byte[] id   = cccd.getBytes();

        byte[] plain = new byte[
                1 + code.length +
                1 + name.length +
                1 + dob.length +
                1 + id.length
        ];

        int k = 0;
        plain[k++] = (byte)code.length;
        System.arraycopy(code, 0, plain, k, code.length);
        k += code.length;

        plain[k++] = (byte)name.length;
        System.arraycopy(name, 0, plain, k, name.length);
        k += name.length;

        plain[k++] = (byte)dob.length;
        System.arraycopy(dob, 0, plain, k, dob.length);
        k += dob.length;

        plain[k++] = (byte)id.length;
        System.arraycopy(id, 0, plain, k, id.length);

        byte[] enc = aesEncrypt(plain);

        ResponseAPDU resp = send(INS_INIT_CARD, enc);
        return resp.getSW() == 0x9000;
}
     
    // SECURE UPDATE INFO
     
    public boolean updatePersonalInfoOnCard(String name, String dob, String cccd)
            throws Exception {

        byte[] n = name.getBytes();
        byte[] d = dob.getBytes();
        byte[] id = cccd.getBytes();

        byte[] plain = new byte[
                1+n.length +
                1+d.length +
                1+id.length
        ];

        int i = 0;

        plain[i++] = (byte)n.length;
        System.arraycopy(n,0,plain,i,n.length);
        i += n.length;

        plain[i++] = (byte)d.length;
        System.arraycopy(d,0,plain,i,d.length);
        i += d.length;

        plain[i++] = (byte)id.length;
        System.arraycopy(id,0,plain,i,id.length);

        byte[] enc = aesEncrypt(plain);
        ResponseAPDU resp = send(INS_UPDATE_INFO, enc);

        return resp.getSW() == 0x9000;
    }

    // CHANGE PIN (AES ENCRYPT)
     
    public int changePin(String oldPin, String newPin) throws Exception {

        byte[] o = oldPin.getBytes();
        byte[] n = newPin.getBytes();

        byte[] plain = new byte[1+o.length + 1+n.length];
        int k = 0;

        plain[k++] = (byte)o.length;
        System.arraycopy(o,0,plain,k,o.length); 
        k += o.length;

        plain[k++] = (byte)n.length;
        System.arraycopy(n,0,plain,k,n.length);

        byte[] enc = aesEncrypt(plain);
        ResponseAPDU resp = send(INS_CHANGE_PIN, enc);
        
        if (resp.getSW() == 0x9000) {
        setupAES(newPin);  
    }
        return resp.getSW();
    }

     
    // WRITE PHOTO
     
    public boolean writePhotoOnCard(byte[] img) throws Exception {
        
        setupAES("123456");
        if (img == null || img.length == 0) return true;

        int offset = 0;
        final int CHUNK = 180;

        short total = (short)img.length;

        while (offset < img.length) {
            int len = Math.min(CHUNK, img.length - offset);

            byte[] plain = new byte[4 + len];
            plain[0] = (byte)(total >> 8);
            plain[1] = (byte)(total);
            plain[2] = (byte)(offset >> 8);
            plain[3] = (byte)(offset);

            System.arraycopy(img, offset, plain, 4, len);

            byte[] enc = aesEncrypt(plain);
            ResponseAPDU resp = send(INS_WRITE_PHOTO, enc);

            if (resp.getSW() != 0x9000) return false;

            offset += len;
        }

        return true;
    }
     
    // GET INFO
    public CardInfo getCardInfo() throws Exception {

        ResponseAPDU resp = sendLe(INS_GET_INFO, 255);
        if (resp.getSW() != 0x9000) return null;

        byte[] enc = resp.getData();

        // CASE 1 — THẺ TRẮNG (plaintext)
        // plaintext length luôn < 40 bytes
        if (enc.length < 40) {
            return parsePlainGetInfo(enc);
        }

        // CASE 2 — THẺ ĐÃ KHỞI TẠO: decrypt AES
        byte[] iv = Arrays.copyOfRange(enc, 0, 16);
        byte[] ciphertext = Arrays.copyOfRange(enc, 16, enc.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] d = cipher.doFinal(ciphertext);

        // parse decrypted data
        return parsePlainGetInfo(d);
}

    private CardInfo parsePlainGetInfo(byte[] d) throws Exception {
        int i = 0;
        CardInfo info = new CardInfo();

        int codeLen = d[i++] & 0xFF;
        info.cardCode = new String(d, i, codeLen);
        i += codeLen;

        if (d[i] == 0x1F) i++;   // delimiter

        info.cardId = (short)(((d[i] & 0xFF) << 8) | (d[i+1] & 0xFF));
        i += 2;

        info.balance =
            ((d[i] & 0xFF) << 24) |
            ((d[i+1] & 0xFF) << 16) |
            ((d[i+2] & 0xFF) << 8) |
            (d[i+3] & 0xFF);
        i += 4;

        info.locked      = d[i++] != 0;
        info.initialized = d[i++] != 0;     // 0 → THẺ TRẮNG
        info.pinChanged  = d[i++] != 0;

        int nlen = d[i++] & 0xFF;
        info.hoTen = new String(d, i, nlen);
        i += nlen;

        int dlen = d[i++] & 0xFF;
        info.ngaySinh = new String(d, i, dlen);
        i += dlen;

        int idlen = d[i++] & 0xFF;
        info.cccd = new String(d, i, idlen);
        i += idlen;

        info.photoLen = (short)(((d[i] & 0xFF) << 8) | (d[i+1] & 0xFF));
        i += 2;

        info.passType = d[i++];

        info.passExpire =
            ((d[i] & 0xFF) << 24) |
            ((d[i+1] & 0xFF) << 16) |
            ((d[i+2] & 0xFF) << 8) |
            (d[i+3] & 0xFF);

        return info;
}

    // READ PHOTO
    public byte[] readPhoto(short len) throws Exception {

        byte[] out = new byte[len];
        int off = 0;

        while (off < len) {

            int plainLen = len - off;
            if (plainLen > 200) plainLen = 200;

            int paddedLen = ((plainLen + 15) / 16) * 16;
            int le = 16 + paddedLen;

            CommandAPDU cmd = new CommandAPDU(CLA_APP,INS_READ_PHOTO,(off >> 8) & 0xFF,off & 0xFF,le);

            ResponseAPDU resp = transmit(cmd);
            if (resp.getSW() != 0x9000) return null;

            byte[] enc = resp.getData();

            byte[] iv  = Arrays.copyOfRange(enc, 0, 16);
            byte[] cph = Arrays.copyOfRange(enc, 16, enc.length);

            Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
            aes.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

            byte[] plain = aes.doFinal(cph);

            System.arraycopy(plain, 0, out, off, plainLen);
            off += plainLen;
    }
    return out;
}
    // MONEY OPS
    public boolean topUp(int amount) throws Exception {
        long ts = System.currentTimeMillis()/1000L;

        byte[] d = new byte[8];
        d[0]=(byte)(amount>>24);
        d[1]=(byte)(amount>>16);
        d[2]=(byte)(amount>>8);
        d[3]=(byte)(amount);

        d[4]=(byte)(ts>>24);
        d[5]=(byte)(ts>>16);
        d[6]=(byte)(ts>>8);
        d[7]=(byte)(ts);

        byte[] enc = aesEncrypt(d);
        return send(INS_TOP_UP, enc).getSW() == 0x9000;
    }

    public int payTicket(int amount) throws Exception {

        long ts = System.currentTimeMillis()/1000L;

        byte[] d = new byte[8];
        d[0]=(byte)(amount>>24);
        d[1]=(byte)(amount>>16);
        d[2]=(byte)(amount>>8);
        d[3]=(byte)(amount);

        d[4]=(byte)(ts>>24);
        d[5]=(byte)(ts>>16);
        d[6]=(byte)(ts>>8);
        d[7]=(byte)(ts);

        byte[] enc = aesEncrypt(d);
        return send(INS_PAY_TICKET, enc).getSW();

    }

    public int setPass(byte type, long expire) throws Exception {

        byte[] d = new byte[5];
        d[0]=type;

        d[1]=(byte)(expire>>24);
        d[2]=(byte)(expire>>16);
        d[3]=(byte)(expire>>8);
        d[4]=(byte)(expire);

        return send(INS_SET_PASS,d).getSW();
    }

    // HISTORY
     
    public List<HistoryRecord> getHistory() throws Exception {

            ResponseAPDU resp = sendLe(INS_GET_HISTORY,255);
            if (resp.getSW()!=0x9000) return Collections.emptyList();

            // ciphertext
            byte[] enc = resp.getData();

            // tách IV và ciphertext
            byte[] iv = Arrays.copyOfRange(enc, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(enc, 16, enc.length);

            // decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
            byte[] d = cipher.doFinal(ciphertext);

            int i=0;

            int count = d[i++] & 0xFF;

            List<HistoryRecord> out = new ArrayList<>();

            for (int k=0;k<count;k++) {

                HistoryRecord r = new HistoryRecord();
                r.type = d[i++];

                r.amount = ((d[i]&0xFF)<<24)|((d[i+1]&0xFF)<<16)|
                           ((d[i+2]&0xFF)<<8)|(d[i+3]&0xFF);
                i+=4;

                r.timestamp = ((d[i]&0xFF)<<24)|((d[i+1]&0xFF)<<16)|
                              ((d[i+2]&0xFF)<<8)|(d[i+3]&0xFF);
                i+=4;

                out.add(r);
            }
            return out;
    }
    
    public boolean unlockCard(String pin) throws Exception {
        byte[] p = pin.getBytes();

        byte[] data = new byte[1 + p.length];
        data[0] = (byte)p.length;
        System.arraycopy(p, 0, data, 1, p.length);

        boolean ok = send(INS_UNLOCK_CARD, data).getSW() == 0x9000;
        if (ok) {
            setupAES(pin);   
        }
        return ok;
}
    public boolean deleteCard() throws Exception {
        boolean ok = send(INS_DELETE_CARD,new byte[0]).getSW()==0x9000;
        if (ok) {
            // reset AES to default pin
            setupAES("123456"); 
        }
        return ok;
}


    private String bytesToHex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public String[] getRSAPublicKeyHex() throws Exception {

        loadRSAPublicKey();

        RSAPublicKey pub = (RSAPublicKey) rsaPublicKey;

        byte[] mod = pub.getModulus().toByteArray();
        byte[] exp = pub.getPublicExponent().toByteArray();

        // Chỉ strip leading zero của modulus
        if (mod.length > 1 && mod[0] == 0x00) {
            mod = Arrays.copyOfRange(mod, 1, mod.length);
        }

        // KHÔNG strip exponent
        return new String[]{ bytesToHex(mod), bytesToHex(exp) };
}
    
    private byte[] stripLeadingZero(byte[] in) {
    if (in.length > 1 && in[0] == 0x00) {
        return Arrays.copyOfRange(in, 1, in.length);
    }
    return in;
}
    public boolean rsaAuthenticateUsing(PublicKey pubKey) throws Exception {

        // tạo nonce random
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);

        // gửi nonce xuống thẻ → thẻ ký bằng private key
        ResponseAPDU sig = send(INS_RSA_SIGN, nonce);
        byte[] signature = sig.getData();

        Signature verifier = Signature.getInstance("SHA1withRSA");
        verifier.initVerify(pubKey);
        verifier.update(nonce);

        return verifier.verify(signature);
    }
}

package model;

public class TheXeBus {

    private String soThe;      
    private String hoTen;       
    private String ngaySinh;    
    private String cccd;        
    private int soDu;           
    private boolean biKhoa;     
    private byte[] anh;         

    //   Thông tin vé  
    private byte passType;
    // Thời điểm hết hạn vé
    private long passExpire;

    //   Constructor
    public TheXeBus(String soThe,
                    String hoTen,
                    String ngaySinh,
                    String cccd,
                    int soDu,
                    boolean biKhoa,
                    byte[] anh,
                    byte passType,
                    int passExpire) {

        this.soThe = soThe;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.cccd = cccd;
        this.soDu = soDu;
        this.biKhoa = biKhoa;
        this.anh = anh;
        this.passType = passType;
        this.passExpire = passExpire;
    }

    //   Getters & Setters  

    public String getSoThe() {
        return soThe;
    }

    public void setSoThe(String soThe) {
        this.soThe = soThe;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public int getSoDu() {
        return soDu;
    }

    public void setSoDu(int soDu) {
        this.soDu = soDu;
    }

    public boolean isBiKhoa() {
        return biKhoa;
    }

    public void setBiKhoa(boolean biKhoa) {
        this.biKhoa = biKhoa;
    }

    public byte[] getAnh() {
        return anh;
    }

    public void setAnh(byte[] anh) {
        this.anh = anh;
    }

    //   Vé hiện tại  
    public byte getPassType() {
        return passType;
    }

    public void setPassType(byte passType) {
        this.passType = passType;
    }

    public long getPassExpire() {
        return passExpire;
    }

    public void setPassExpire(long passExpire) {
        this.passExpire = passExpire;
    }

    public void napTien(int amount) {
        this.soDu += amount;
    }

    public boolean thanhToan(int amount) {
        if (soDu < amount) return false;
        this.soDu -= amount;
        return true;
    }

    @Override
    public String toString() {
        return "TheXeBus{" +
                "soThe='" + soThe + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh='" + ngaySinh + '\'' +
                ", cccd='" + cccd + '\'' +
                ", soDu=" + soDu +
                ", biKhoa=" + biKhoa +
                ", anh=" + (anh != null ? anh.length + " bytes" : "null") +
                ", passType=" + passType +
                ", passExpire=" + passExpire +
                '}';
    }
}

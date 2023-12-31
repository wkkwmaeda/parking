package model;

public class Reservation {
    private int reserv_id;
    private String carnum;
    private int cuid;
    private String parkdate;
    private String cuname; 
    private String ci;
    private String co;
    private String address;
    private String tel;

    public Reservation(String cuname, String address, String tel, String ci, String co) {
        this.cuname = cuname;
        this.address = address;
        this.tel = tel;
        this.ci = ci;
        this.co = co;
    }

    public Reservation(int reserv_id, String carnum, int cuid, String cuname, String parkdate) {
        this.reserv_id = reserv_id;
        this.carnum = carnum;
        this.cuid = cuid;
        this.cuname = cuname;
        this.parkdate = parkdate;
        
    }

    public int getReserv_id() {
        return reserv_id;
    }

    public void setReserv_id(int reserv_id) {
        this.reserv_id = reserv_id;
    }

    public String getCarnum() {
        return carnum;
    }

    public void setCarnum(String carnum) {
        this.carnum = carnum;
    }

    public int getCuid() {
        return cuid;
    }

    public void setCuid(int cuid) {
        this.cuid = cuid;
    }

    public String getParkdate() {
        return parkdate;
    }

    public void setParkdate(String parkdate) {
        this.parkdate = parkdate;
    }
    
 // customerNameのgetter（取得用メソッド）
    public String getCuname() {
        return cuname;
    }

    // customerNameのsetter（設定用メソッド）
    public void setCuname(String cuname) {
        this.cuname = cuname;
    }
    

    // ciのセッター
    public void setCi(String ci) {
        this.ci = ci;
    }

    // ciのゲッター
    public String getCi() {
        return ci;
    }

    // coのセッター
    public void setCo(String co) {
        this.co = co;
    }

    // coのゲッター
    public String getCo() {
        return co;
    }
}


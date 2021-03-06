package ve.com.strikersfran.myfamily.model;

public class ListFamiliar {

    private String uid;
    private String estatus;
    private Long lastUpdate;

    public ListFamiliar() {
    }

    public ListFamiliar(String uid, String estatus) {
        this.uid = uid;
        this.estatus = estatus;
        this.lastUpdate = System.currentTimeMillis()/1000;
    }

    public String getUid() {
        return uid;
    }

    public String getEstatus() {
        return estatus;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }
}

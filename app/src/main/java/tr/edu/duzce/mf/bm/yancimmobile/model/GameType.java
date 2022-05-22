package tr.edu.duzce.mf.bm.yancimmobile.model;

public class GameType {
    private long id;
    private int drawableId;
    private String name;

    public GameType(long id, int drawableId, String name) {
        this.id = id;
        this.drawableId = drawableId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

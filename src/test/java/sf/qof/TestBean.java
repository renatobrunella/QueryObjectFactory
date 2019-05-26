package sf.qof;

import java.util.Date;

public class TestBean {
    private int id;
    private Integer num;
    private String name;
    private Date date;
    private boolean red;
    private boolean green;
    private TestBean parent;
    private Integer boxedId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public boolean isRed() {
        return red;
    }

    public void setRed(boolean red) {
        this.red = red;
    }

    public boolean getGreen() {
        return green;
    }

    public void setGreen(boolean green) {
        this.green = green;
    }

    public TestBean getParent() {
        return parent;
    }

    public void setParent(TestBean parent) {
        this.parent = parent;
    }

    public Integer getBoxedId() {
        return boxedId;
    }

    public void setBoxedId(Integer boxedId) {
        this.boxedId = boxedId;
    }
}

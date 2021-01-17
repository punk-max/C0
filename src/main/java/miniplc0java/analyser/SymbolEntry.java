package miniplc0java.analyser;

public class SymbolEntry {
    String name;
    boolean isConstant;
    boolean isInitialized;
    Type type;
    int level;
    int belong;
    int loca;
    int arga;

    public SymbolEntry(String name, boolean isConstant, boolean isInitialized, Type type, int level, int belong,int loca) {
        this.name = name;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.type = type;
        this.level = level;
        this.belong = belong;
        this.loca = loca;
        this.arga = -1;
    }

    public SymbolEntry(String name, boolean isConstant, boolean isInitialized, Type type, int level, int belong, int loca, int arga) {
        this.name = name;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.type = type;
        this.level = level;
        this.belong = belong;
        this.loca = loca;
        this.arga = arga;
    }

    public int getArga() {
        return arga;
    }

    public void setArga(int arga) {
        this.arga = arga;
    }

    public int getLoca() {
        return loca;
    }

    public void setLoca(int loca) {
        this.loca = loca;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBelong() {
        return belong;
    }

    public void setBelong(int belong) {
        this.belong = belong;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

}

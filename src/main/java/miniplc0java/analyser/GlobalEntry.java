package miniplc0java.analyser;

public class GlobalEntry {
    int id;
    String name;
    Object value;
    boolean isConstant;
    boolean isInitialized;
    boolean isFunc;
    Type type;

    public GlobalEntry(int id,String name, boolean isConstant, boolean isInitialized, Type type) {
        this.id=id;
        this.name = name;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.type = type;
        this.isFunc = false;
    }

    public GlobalEntry(int id, String name, boolean isConstant, boolean isInitialized, boolean isFunc, Type type) {
        this.id = id;
        this.name = name;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.isFunc = isFunc;
        this.type = type;
    }

    public GlobalEntry(int id, String name, Object value, boolean isConstant, boolean isInitialized, boolean isFunc, Type type) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.isFunc = isFunc;
        this.type = type;
    }

    public GlobalEntry(int id, String name, Object value, boolean isConstant, boolean isInitialized, Type type) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.isFunc = false;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFunc() {
        return isFunc;
    }

    public void setFunc(boolean func) {
        isFunc = func;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

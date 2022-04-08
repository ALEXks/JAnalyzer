package analyzer.characteristics;

public class Characteristic<T> {
    private T val;
    private String name;

    public Characteristic(String name, T val){
        this.name = name;
        this.val = val;
    }

    public T getVal(){return val;}
    public void setVal(T val) {
        this.val = val;
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

}

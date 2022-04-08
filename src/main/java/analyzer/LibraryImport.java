package analyzer;

public class LibraryImport {
    public static native String readStat(String path);
    static {
        String nativeLibPath = System.getProperty("user.dir") + "/src/libLibraryImport.jnilib";
        try {
            System.load(nativeLibPath);
        }
        catch(Exception e) {
            System.out.println("Error: cannot load library!");
        }
    }
}

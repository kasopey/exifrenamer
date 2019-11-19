package pl.samodzielo.exifrenamer;

public class Fixtures {

    public static final String SEP = System.getProperty("file.separator");

    public static final String WORK_DIR = System.getProperty("user.dir") + SEP + "src" + SEP + "test" + SEP + "resources" + SEP;

    public static final String IMAGE_FILE = "file.jpg";

    public static final String NOT_IMAGE_FILE = "not-image-file.txt";

    public static void setWorkingDir() {
        System.setProperty("user.dir", WORK_DIR);
    }

}

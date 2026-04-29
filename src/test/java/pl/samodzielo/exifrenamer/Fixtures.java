package pl.samodzielo.exifrenamer;

import java.nio.file.Paths;

public class Fixtures {

    public static final String SEP = System.getProperty("file.separator");

    public static final String WORK_DIR = Paths.get("").toAbsolutePath() + SEP + "src" + SEP + "test" + SEP + "resources" + SEP;

    public static final String IMAGE_FILE = "file.jpg";

    public static final String NOT_IMAGE_FILE = "not-image-file.txt";

    public static final String FILE_WITHOUT_EXIF = "file-without-exif.jpg";

}

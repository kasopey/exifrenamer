package pl.samodzielo.exifrenamer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.samodzielo.exifrenamer.exception.ExifRenamerArgumentException;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class ArgumentParserTest {

    private static final String SEP = System.getProperty("file.separator");
    private static final String WORK_DIR = System.getProperty("user.dir") + SEP + "src" + SEP + "test" + SEP + "resources" + SEP;
    private static final String WORK_DIR_CUSTOM = WORK_DIR + "custom-dir";
    private static final String FILE = "file.jpg";
    private static final String DATE_TIME_TO_SET_STRING = "1985.06.15_14-00-00";
    private static final ZonedDateTime DATE_TIME_TO_SET_DATE = LocalDateTime.parse(DATE_TIME_TO_SET_STRING, DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT)).atZone(ZoneId.systemDefault());


    @BeforeEach
    void setUp() {
        System.setProperty("user.dir", WORK_DIR);
    }

    @Test
    void should_set_default_mode_and_directory_from_params() {
        ArgumentParser parser = new ArgumentParser(new String[]{"-d", WORK_DIR_CUSTOM});
        Assertions.assertFalse(parser.isEditExifMode());
        Assertions.assertEquals(Paths.get(WORK_DIR_CUSTOM), parser.getWorkingDirectory());
    }

    @Test
    void should_set_default_mode_and_default_current_directory() {
        ArgumentParser parser = new ArgumentParser(new String[]{});
        Assertions.assertFalse(parser.isEditExifMode());
        Assertions.assertEquals(Paths.get(System.getProperty("user.dir")), parser.getWorkingDirectory());
    }

    @Test
    void should_set_editExifMode_and_file_and_date_from_params_in_default_current_directory() {
        ArgumentParser parser = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING});
        Assertions.assertTrue(parser.isEditExifMode());
        Assertions.assertEquals(Paths.get(WORK_DIR), parser.getWorkingDirectory());
        Assertions.assertEquals(new File(WORK_DIR + SEP + FILE), parser.getFileToEdit());
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, parser.getDateTimeToSet());
    }

    @Test
    void should_set_editExifMode_and_file_and_date_from_params_and_directory() {
        ArgumentParser parser = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING, "-d", WORK_DIR_CUSTOM});
        Assertions.assertTrue(parser.isEditExifMode());
        Assertions.assertEquals(Paths.get(WORK_DIR_CUSTOM), parser.getWorkingDirectory());
        Assertions.assertEquals(new File(WORK_DIR_CUSTOM + SEP + FILE), parser.getFileToEdit());
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, parser.getDateTimeToSet());
    }

    @Test
    void should_throw_exception_because_of_missing_arguments() {
        Assertions.assertThrows(ExifRenamerArgumentException.class, () -> {
            ArgumentParser parser = new ArgumentParser(new String[]{"-w", "-f", FILE});
        });
        Assertions.assertThrows(ExifRenamerArgumentException.class, () -> {
            ArgumentParser parser = new ArgumentParser(new String[]{"-w", "-t", DATE_TIME_TO_SET_STRING});
        });
    }

}
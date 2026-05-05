package pl.samodzielo.exifrenamer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private static final String WORK_DIR = Paths.get("").toAbsolutePath() + SEP + "src" + SEP + "test" + SEP + "resources" + SEP;
    private static final String WORK_DIR_CUSTOM = WORK_DIR + "custom-dir";
    private static final String FILE = "file.jpg";
    private static final String DATE_TIME_TO_SET_STRING = "1985.06.15_14-00-00";
    private static final ZonedDateTime DATE_TIME_TO_SET_DATE = LocalDateTime.parse(DATE_TIME_TO_SET_STRING, DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT)).atZone(ZoneId.systemDefault());

    @Test
    void should_set_default_mode_and_directory_from_params() {
        ArgumentParser parser = new ArgumentParser(new String[]{"-d", WORK_DIR_CUSTOM});
        assertFalse(parser.isEditExifMode());
        assertEquals(Paths.get(WORK_DIR_CUSTOM), parser.getWorkingDirectory());
    }

    @Test
    void should_set_default_mode_and_default_current_directory() {
        ArgumentParser parser = new ArgumentParser(new String[]{});
        assertFalse(parser.isEditExifMode());
        assertEquals(Paths.get("").toAbsolutePath(), parser.getWorkingDirectory());
    }

    @Test
    void should_set_editExifMode_and_file_and_date_from_params_and_directory() {
        ArgumentParser parser = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING, "-d", WORK_DIR_CUSTOM});
        assertTrue(parser.isEditExifMode());
        assertEquals(Paths.get(WORK_DIR_CUSTOM), parser.getWorkingDirectory());
        assertEquals(new File(WORK_DIR_CUSTOM + SEP + FILE).toPath(), parser.getFileToEdit());
        assertEquals(DATE_TIME_TO_SET_DATE, parser.getDateTimeToSet());
    }

    @Test
    void should_set_noRename_flag_from_short_and_long_form() {
        ArgumentParser shortForm = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING, "-d", WORK_DIR_CUSTOM, "-n"});
        assertTrue(shortForm.isNoRename());

        ArgumentParser longForm = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING, "-d", WORK_DIR_CUSTOM, "--no-rename"});
        assertTrue(longForm.isNoRename());

        ArgumentParser without = new ArgumentParser(new String[]{"-w", "-f", FILE, "-t", DATE_TIME_TO_SET_STRING, "-d", WORK_DIR_CUSTOM});
        assertFalse(without.isNoRename());
    }

    @Test
    void should_throw_exception_because_of_missing_arguments() {
        assertThrows(ExifRenamerArgumentException.class, () -> {
            new ArgumentParser(new String[]{"-w", "-f", FILE});
        });
        assertThrows(ExifRenamerArgumentException.class, () -> {
            new ArgumentParser(new String[]{"-w", "-t", DATE_TIME_TO_SET_STRING});
        });
    }

}
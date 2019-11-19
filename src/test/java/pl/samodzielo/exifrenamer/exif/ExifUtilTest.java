package pl.samodzielo.exifrenamer.exif;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.samodzielo.exifrenamer.ArgumentParser;
import pl.samodzielo.exifrenamer.Fixtures;
import pl.samodzielo.exifrenamer.exception.TagNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static pl.samodzielo.exifrenamer.Fixtures.*;
import static pl.samodzielo.exifrenamer.Fixtures.WORK_DIR;

class ExifUtilTest {

    private static final String FILE_WITHOUT_EXIF = "file-without-exif.jpg";

    private static final String DATE_TIME_TO_SET_STRING = "2018.07.19_10-06-40";

    private static final ZonedDateTime DATE_TIME_TO_SET_DATE = LocalDateTime.parse(DATE_TIME_TO_SET_STRING, DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT)).atZone(ZoneId.systemDefault());

    @BeforeEach
    void setup() {
        Fixtures.setWorkingDir();
    }

    @Test
    void should_return_dateTime_from_exif() throws IOException, ImageReadException, TagNotFoundException {
        Optional<ZonedDateTime> dateTime = new ExifUtil().getDateTimeFromExif(new File(WORK_DIR + SEP + IMAGE_FILE));
        Assertions.assertTrue(dateTime.isPresent());
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, dateTime.get());
    }

    @Test
    void should_return_empty_dateTime_because_exif_data_are_missing() throws IOException, ImageReadException, TagNotFoundException {
        Optional<ZonedDateTime> dateTime = new ExifUtil().getDateTimeFromExif(new File(WORK_DIR + SEP + FILE_WITHOUT_EXIF));
        Assertions.assertFalse(dateTime.isPresent());
    }

    @Test
    void should_set_dateTime_in_exif() throws IOException, ImageWriteException, ImageReadException, TagNotFoundException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        File image = new File(WORK_DIR + SEP + FILE_WITHOUT_EXIF);

        Files.copy(image.toPath(), temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Optional<ZonedDateTime> dateTime = new ExifUtil().getDateTimeFromExif(temporary);
        Assertions.assertFalse(dateTime.isPresent());


        new ExifUtil().setDateTimeInExif(temporary, DATE_TIME_TO_SET_DATE);
        dateTime = new ExifUtil().getDateTimeFromExif(temporary);
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, dateTime.get());

        temporary.deleteOnExit();
    }

}
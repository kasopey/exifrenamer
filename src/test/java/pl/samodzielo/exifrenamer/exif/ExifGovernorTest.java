package pl.samodzielo.exifrenamer.exif;

import static pl.samodzielo.exifrenamer.Fixtures.FILE_WITHOUT_EXIF;
import static pl.samodzielo.exifrenamer.Fixtures.IMAGE_FILE;
import static pl.samodzielo.exifrenamer.Fixtures.SEP;
import static pl.samodzielo.exifrenamer.Fixtures.WORK_DIR;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.samodzielo.exifrenamer.ArgumentParser;
import pl.samodzielo.exifrenamer.Fixtures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class ExifAccessorTest {

    private static final String DATE_TIME_TO_SET_STRING = "2018.07.19_10-06-40";

    private static final ZonedDateTime DATE_TIME_TO_SET_DATE = LocalDateTime.parse(DATE_TIME_TO_SET_STRING, DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT)).atZone(ZoneId.systemDefault());

    @BeforeEach
    void setup() {
        Fixtures.setWorkingDir();
    }

    @Test
    void should_return_dateTime_from_exif() {
        Optional<ZonedDateTime> dateTime = new ExifGovernor(Paths.get(WORK_DIR + SEP + IMAGE_FILE)).getDateTimeFromExif();
        Assertions.assertTrue(dateTime.isPresent());
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, dateTime.get());
    }

    @Test
    void should_return_empty_dateTime_because_exif_data_are_missing() {
        Optional<ZonedDateTime> dateTime = new ExifGovernor(Paths.get(WORK_DIR + SEP + FILE_WITHOUT_EXIF)).getDateTimeFromExif();
        Assertions.assertFalse(dateTime.isPresent());
    }

    @Test
    void should_set_dateTime_in_exif() throws IOException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        File image = new File(WORK_DIR + SEP + FILE_WITHOUT_EXIF);

        Files.copy(image.toPath(), temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifGovernor exifAccessor = new ExifGovernor(temporary.toPath());
        Optional<ZonedDateTime> dateTime = exifAccessor.getDateTimeFromExif();
        Assertions.assertFalse(dateTime.isPresent());


        exifAccessor.setDateTimeInExif(DATE_TIME_TO_SET_DATE);
        dateTime = exifAccessor.getDateTimeFromExif();
        Assertions.assertEquals(DATE_TIME_TO_SET_DATE, dateTime.get());

        JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(temporary);
        TiffField originalField = metadata.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        Assertions.assertNotNull(originalField);
        Assertions.assertEquals("2018:07:19 10:06:40", originalField.getStringValue());

        temporary.deleteOnExit();
    }

    @Test
    void should_not_overwrite_dateTime_when_already_set() throws IOException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        File image = new File(WORK_DIR + SEP + IMAGE_FILE);
        Files.copy(image.toPath(), temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);

        JpegImageMetadata before = (JpegImageMetadata) Imaging.getMetadata(temporary);
        TiffField originalValue = before.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);
        Assertions.assertNotNull(originalValue);

        ExifGovernor exifAccessor = new ExifGovernor(temporary.toPath());
        ZonedDateTime newDateTime = DATE_TIME_TO_SET_DATE.plusYears(1);

        ImagingException thrown = Assertions.assertThrows(ImagingException.class,
                () -> exifAccessor.setDateTime(newDateTime));
        Assertions.assertEquals("Tag DateTime already set", thrown.getMessage());

        JpegImageMetadata after = (JpegImageMetadata) Imaging.getMetadata(temporary);
        TiffField unchanged = after.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);
        Assertions.assertEquals(originalValue.getStringValue(), unchanged.getStringValue());

        temporary.deleteOnExit();
    }

    @Test
    void should_not_overwrite_dateTimeOriginal_when_already_set() throws IOException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        File image = new File(WORK_DIR + SEP + IMAGE_FILE);
        Files.copy(image.toPath(), temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);

        JpegImageMetadata before = (JpegImageMetadata) Imaging.getMetadata(temporary);
        TiffField originalValue = before.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        Assertions.assertNotNull(originalValue);

        ExifGovernor exifAccessor = new ExifGovernor(temporary.toPath());
        ZonedDateTime newDateTime = DATE_TIME_TO_SET_DATE.plusYears(1);

        ImagingException thrown = Assertions.assertThrows(ImagingException.class,
                () -> exifAccessor.setDateTimeOriginal(newDateTime));
        Assertions.assertEquals("Tag DateTimeOriginal already set", thrown.getMessage());

        JpegImageMetadata after = (JpegImageMetadata) Imaging.getMetadata(temporary);
        TiffField unchanged = after.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        Assertions.assertEquals(originalValue.getStringValue(), unchanged.getStringValue());

        temporary.deleteOnExit();
    }

}
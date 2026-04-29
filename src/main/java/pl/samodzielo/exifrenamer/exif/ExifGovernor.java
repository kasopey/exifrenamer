package pl.samodzielo.exifrenamer.exif;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.samodzielo.exifrenamer.exception.TagNotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ExifGovernor {

    private static Logger LOGGER = LoggerFactory.getLogger(ExifGovernor.class);

    private final Path file;
    private static final TagInfoAscii TAG_DATE_TIME = TiffTagConstants.TIFF_TAG_DATE_TIME;
    private static final TagInfoAscii TAG_DATE_TIME_ORIGINAL = ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL;

    public ExifGovernor(Path file) {
        this.file = file;
    }

    public Optional<ZonedDateTime> getDateTimeFromExif() {
        try {
            final ImageMetadata metadata = Imaging.getMetadata(file.toFile());
            if (metadata != null) {
                LOGGER.info("Processing: " + file.toFile().getName());
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                ZonedDateTime date = getDateTimeFromExif(jpegMetadata);
                return Optional.ofNullable(date);
            }
        } catch (IOException | TagNotFoundException e) {
            LOGGER.info(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public String setDateTimeInExif(final ZonedDateTime dateTimeToSet) throws IOException {
        setDateTime(dateTimeToSet);
        return setDateTimeOriginal(dateTimeToSet);
    }

    public String setDateTime(final ZonedDateTime dateTimeToSet) throws IOException {
        return writeSingleTag(TAG_DATE_TIME, false, dateTimeToSet);
    }

    public String setDateTimeOriginal(final ZonedDateTime dateTimeToSet) throws IOException {
        return writeSingleTag(TAG_DATE_TIME_ORIGINAL, true, dateTimeToSet);
    }

    private String writeSingleTag(final TagInfoAscii tag, final boolean inExifSubDirectory, final ZonedDateTime dateTimeToSet) throws IOException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        String newFileName = writeSingleTag(file.toFile(), temporary, tag, inExifSubDirectory, dateTimeToSet);

        Path changedImage = temporary.toPath();
        Files.copy(changedImage, file, StandardCopyOption.REPLACE_EXISTING);
        temporary.deleteOnExit();
        return newFileName;
    }

    private ZonedDateTime getDateTimeFromExif(final JpegImageMetadata jpegMetadata) throws TagNotFoundException {
//        final TagInfo tagInfo = new TagInfoAscii("DateTimeOriginal", 36867, 20, TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD);
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(TAG_DATE_TIME);
        if (field == null) {
            throw new TagNotFoundException(String.format("Tag %d with name %s not found", TAG_DATE_TIME.tag, TAG_DATE_TIME.name));
        } else {
            String value = field.getValueDescription();
            ZonedDateTime date = ZonedDateTime.parse(value,
                    DateTimeFormatter.ofPattern("''yyyy:MM:dd HH:mm:ss''")
                            .withZone(ZoneId.systemDefault()));
            return date;
        }
    }

    private String writeSingleTag(final File sourceImage, final File destinationImage, final TagInfoAscii tag, final boolean inExifSubDirectory, final ZonedDateTime dateTimeToSet) throws IOException, ImagingException {
        // code from https://github.com/mjremijan/thoth-jpg/blob/master/src/main/java/org/thoth/imaging/WriteExifMetadataExample.java
        // @author Michael Remijan mjremijan@yahoo.com @mjremijan
        try (OutputStream fos = new FileOutputStream(destinationImage)) {
            TiffOutputSet outputSet = null;

            final ImageMetadata metadata = Imaging.getMetadata(sourceImage);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                final TiffField existing = jpegMetadata.findExifValueWithExactMatch(tag);
                if (existing != null) {
                    throw new ImagingException(String.format("Tag %s already set", tag.name));
                }
                final TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }

            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            final TiffOutputDirectory directory = inExifSubDirectory
                    ? outputSet.getOrCreateExifDirectory()
                    : outputSet.getOrCreateRootDirectory();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
            String formattedString = dateTimeToSet.format(formatter);

            directory.add(tag, formattedString);

            BufferedOutputStream bos = new BufferedOutputStream(fos);

            new ExifRewriter().updateExifMetadataLossless(sourceImage, bos, outputSet);
            return formattedString;
        }
    }

}

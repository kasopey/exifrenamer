package pl.samodzielo.exifrenamer.exif;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
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

public class ExifUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(ExifUtil.class);

    public Optional<ZonedDateTime> getDateTimeFromExif(final File image) throws TagNotFoundException, IOException, ImageReadException {
        final IImageMetadata metadata = Imaging.getMetadata(image);
        String oldName = image.getName();
        if (metadata != null) {
            LOGGER.info("Processing: " + oldName);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            ZonedDateTime date = getDateTimeFromExif(jpegMetadata);
            return Optional.ofNullable(date);
        }
        return Optional.empty();
    }

    public String setDateTimeInExif(final File jpegImageFile, final ZonedDateTime dateTimeToSet) throws IOException, ImageReadException, ImageWriteException {
        File temporary = File.createTempFile("exifrenamer", ".jpg");
        String newFileName = setDateTimeInExif(jpegImageFile, temporary, dateTimeToSet);

        Path changedImage = temporary.toPath();
        Path originalPath = jpegImageFile.toPath();
        Files.copy(changedImage, originalPath, StandardCopyOption.REPLACE_EXISTING);
        temporary.deleteOnExit();
        return newFileName;
    }

    private ZonedDateTime getDateTimeFromExif(final JpegImageMetadata jpegMetadata) throws TagNotFoundException {
        final TagInfo tagInfo = new TagInfoAscii("DateTimeOriginal", 36867, 20, TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD);

        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            throw new TagNotFoundException(String.format("Tag %d with name %s not found", tagInfo.tag, tagInfo.name));
        } else {
            String value = field.getValueDescription();
            ZonedDateTime date = ZonedDateTime.parse(value,
                    DateTimeFormatter.ofPattern("''yyyy:MM:dd HH:mm:ss''")
                            .withZone(ZoneId.systemDefault()));
            return date;
        }
    }

    private String setDateTimeInExif(final File sourceImage, final File destinationImage, final ZonedDateTime dateTimeToSet) throws IOException, ImageReadException, ImageWriteException {
        // code from https://github.com/mjremijan/thoth-jpg/blob/master/src/main/java/org/thoth/imaging/WriteExifMetadataExample.java
        // @author Michael Remijan mjremijan@yahoo.com @mjremijan
        try (OutputStream fos = new FileOutputStream(destinationImage)) {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final IImageMetadata metadata = Imaging.getMetadata(sourceImage);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no ExifUtil metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the ExifUtil data to write.
                    //
                    // Usually, we want to update existing ExifUtil metadata by changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            // Example of how to add a field/tag to the output set.
            //
            // Note that you should first remove the field/tag if it already
            // exists in this directory, or you may end up with duplicate tags. See above.
            //
            // Certain fields/tags are expected in certain ExifUtil directories;
            // Others can occur in more than one directory (and often have a
            // different meaning in different directories).
            //
            // TagInfo constants often contain a description of what
            // directories are associated with a given tag.
            //
            // see org.apache.commons.imaging.formats.tiff.constants.AllTagConstants
            //
            final TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();

            exifDirectory.removeField(TiffConstants.TIFF_TAG_DATE_TIME);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
            String formattedString = dateTimeToSet.format(formatter);

            exifDirectory.add(TiffConstants.TIFF_TAG_DATE_TIME, formattedString);

            BufferedOutputStream bos = new BufferedOutputStream(fos);

            new ExifRewriter().updateExifMetadataLossless(sourceImage, bos, outputSet);
            return formattedString;
        }
    }

}

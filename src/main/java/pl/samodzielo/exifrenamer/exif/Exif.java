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
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Exif {

    @SuppressWarnings("UnnecessaryLocalVariable")
    public ZonedDateTime getTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) throws TagNotFoundException, ParseException {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            throw new TagNotFoundException(String.format("Tag %d with name %s not found", tagInfo.tag, tagInfo.name));
        } else {
            String value = field.getValueDescription();
            ZonedDateTime date = ZonedDateTime.parse(value,
                    DateTimeFormatter.ofPattern("''yyyy:MM:dd HH:mm:ss''").withZone(ZoneId.systemDefault()));
            return date;
        }
    }

    public void setDateTimeInExif(final File jpegImageFile, final File dst) throws IOException, ImageReadException, ImageWriteException {
        try (OutputStream fos = new FileOutputStream(dst)) {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final IImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by changing
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
            // Certain fields/tags are expected in certain Exif directories;
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
            exifDirectory.add(TiffConstants.TIFF_TAG_DATE_TIME,  "2021:01:01 01:02:03" );


            BufferedOutputStream bos = new BufferedOutputStream(fos);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, bos, outputSet);
        }
    }

}

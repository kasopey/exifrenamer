package pl.samodzielo.exifrenamer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import pl.samodzielo.exifrenamer.exif.Exif;
import pl.samodzielo.exifrenamer.exif.TagNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Main {

    public static final String DATE_PATTERN = "YYYY.MM.dd_HH-mm-ss";

    public static void main(String... arg) throws ImageWriteException, ImageReadException, IOException, TagNotFoundException, ParseException {
        Exif exif = new Exif();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Path workingDirectory = Paths.get("./all");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workingDirectory)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    File image = entry.toFile();
                    final IImageMetadata metadata = Imaging.getMetadata(image);
                    String oldName = image.getName();
                    if (metadata != null) {
                        System.out.println("Processing: " + oldName);
                        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                        ZonedDateTime date = exif.getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
                        String newName = DateTimeFormatter.ofPattern(DATE_PATTERN).format(date) + ".jpg";
                        Files.move(entry, entry.resolveSibling(newName), REPLACE_EXISTING);
                        System.out.println(String.format("File =(%s) renamed to =(%s)", oldName, newName));
                    } else {
                        exif.setDateTimeInExif(image, new File("./xxx.jpg"));
                    }
                }
            }
        }
    }

}

package pl.samodzielo.exifrenamer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.samodzielo.exifrenamer.exception.TagNotFoundException;
import pl.samodzielo.exifrenamer.exif.ExifUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws ImageWriteException, ImageReadException, IOException, TagNotFoundException, ParseException {
        ArgumentParser config = new ArgumentParser(args);
        ExifUtil exifUtil = new ExifUtil();
        if (config.isEditExifMode()) {
            exifUtil.setDateTimeInExif(config.getFileToEdit(), config.getDateTimeToSet());
            String newName = DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT).format(config.getDateTimeToSet()) + ".jpg";
            Files.move(config.getFileToEdit().toPath(), config.getFileToEdit().toPath().resolveSibling(newName), REPLACE_EXISTING);
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(config.getWorkingDirectory())) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry) && ImageIO.read(entry.toFile()) != null) {
                        File sourceImage = entry.toFile();
                        LOGGER.info("Processing file " + sourceImage);
                        Optional<ZonedDateTime> datetime = exifUtil.getDateTimeFromExif(sourceImage);
                        if (datetime.isPresent()) {
                            String oldName = sourceImage.getName();
                            LOGGER.info("Processing: " + oldName);
                            String newName = DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT).format(datetime.get()) + ".jpg";
                            Files.move(entry, entry.resolveSibling(newName), REPLACE_EXISTING);
                            LOGGER.info("File =({}) renamed to =({})", oldName, newName);
                        }
                    }
                }
            }
        }
    }

}

package pl.samodzielo.exifrenamer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.samodzielo.exifrenamer.exception.TagNotFoundException;
import pl.samodzielo.exifrenamer.exif.ExifFacade;
import pl.samodzielo.exifrenamer.exif.ExifGovernor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    // TODO: jesli nazwa pliku jest ok, to go pomijaj
    public static void main(String... args) throws ImageWriteException, ImageReadException, IOException, TagNotFoundException, ParseException {
        ArgumentParser config = new ArgumentParser(args);
        ExifFacade facade = new ExifFacade();
        if (config.isEditExifMode()) {
            ExifGovernor exifGovernor = new ExifGovernor(config.getFileToEdit());
            exifGovernor.setDateTimeInExif(config.getDateTimeToSet());
            String newName = calculateNewName(config.getFileToEdit(), config.getDateTimeToSet());
            Files.move(config.getFileToEdit(), config.getFileToEdit().resolveSibling(newName), REPLACE_EXISTING);
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(config.getWorkingDirectory())) {
                for (Path entry : stream) {
                    LOGGER.info("Processing entry: " + entry);
                    facade.isImage(entry)
                        .map(sourceImage -> {
                            LOGGER.info("Processing file " + sourceImage);
                            Optional<ZonedDateTime> datetime = new ExifGovernor(sourceImage).getDateTimeFromExif();
                            if (datetime.isPresent()) {
                                String newName = calculateNewName(sourceImage, datetime.get());
                                return Paths.get(newName);
                            }
                            return null;
                        }).ifPresent(path -> {
                            try {
                                Files.move(entry, entry.resolveSibling(path), REPLACE_EXISTING);
                                LOGGER.info("File =({}) renamed to =({})", entry, path);
                            } catch (IOException e) {
                                LOGGER.info(e.getMessage(), e);
                            }
                        });
                }
            }
        }
    }

    private static String calculateNewName(Path oldFile, ZonedDateTime zonedDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ArgumentParser.DATE_TIME_TO_SET_FORMAT);
        return dateTimeFormatter.format(zonedDateTime) + "." + FilenameUtils.getExtension(oldFile.getFileName().getFileName().toString());
    }

}

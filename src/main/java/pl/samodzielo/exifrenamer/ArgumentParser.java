package pl.samodzielo.exifrenamer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.samodzielo.exifrenamer.exception.ExifRenamerArgumentException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ArgumentParser {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ArgumentParser.class);

    public static final String DATE_TIME_TO_SET_FORMAT = "yyyy.MM.dd_HH-mm-ss";

    private boolean editExifMode;

    private Path workingDirectory;

    private ZonedDateTime dateTimeToSet;

    private File fileToEdit;

    public ArgumentParser(String[] args) {
        LOGGER.info("Parsing arguments: {}", String.join(" ", args));
        parse(args);
        validate();
    }

    private void parse(String[] args) {
        String fileToEditName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                workingDirectory = Paths.get(args[i + 1]);
                continue;
            }

            if (Arrays.asList(args).contains("-w")) {
                editExifMode = true;

            }
            if (args[i].equals("-t")) {
                LocalDateTime localDateTime = LocalDateTime.parse(args[i + 1], DateTimeFormatter.ofPattern(DATE_TIME_TO_SET_FORMAT));
                dateTimeToSet = localDateTime.atZone(ZoneId.systemDefault());
            }

            if (args[i].equals("-f")) {
                fileToEditName = args[i + 1];
            }

        }
        if (workingDirectory == null) {
            workingDirectory = Paths.get(System.getProperty("user.dir"));
        }
        fileToEdit = new File(workingDirectory + System.getProperty("file.separator") + fileToEditName);
    }

    private void validate() {
        if (editExifMode) {
            if (dateTimeToSet == null) {
                throw new ExifRenamerArgumentException("Missing or invalid datetime to set argument");
            }
            if (fileToEdit == null || !fileToEdit.exists()) {
                throw new ExifRenamerArgumentException("Missing or invalid file to set argument");
            }
        }

    }

    public boolean isEditExifMode() {
        return editExifMode;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public ZonedDateTime getDateTimeToSet() {
        return dateTimeToSet;
    }

    public File getFileToEdit() {
        return fileToEdit;
    }
}

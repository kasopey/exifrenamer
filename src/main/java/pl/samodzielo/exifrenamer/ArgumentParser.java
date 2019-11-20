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

    private Path fileToEdit;

    public ArgumentParser(String[] args) {
        parse(args);
        validate();
    }

    private void parse(String[] args) {
        String fileToEditName = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                workingDirectory = Paths.get(args[i + 1]);
                continue;
            }

            if (Arrays.asList(args).contains("-w")) {
                editExifMode = true;

            }
            if ("-t".equals(args[i])) {
                LocalDateTime localDateTime = LocalDateTime.parse(args[i + 1], DateTimeFormatter.ofPattern(DATE_TIME_TO_SET_FORMAT));
                dateTimeToSet = localDateTime.atZone(ZoneId.systemDefault());
            }

            if ("-f".equals(args[i])) {
                fileToEditName = args[i + 1];
            }

            if ("-h".equals(args[i]) || "--help".equals(args[i])) {
                printHelp();
            }

        }
        if (workingDirectory == null) {
            workingDirectory = Paths.get(System.getProperty("user.dir"));
        }
        fileToEdit = Paths.get(workingDirectory + System.getProperty("file.separator") + fileToEditName);
    }

    private void validate() {
        if (editExifMode) {
            if (dateTimeToSet == null) {
                throw new ExifRenamerArgumentException("Missing or invalid datetime to set argument");
            }
            if (fileToEdit == null || !fileToEdit.toFile().exists()) {
                throw new ExifRenamerArgumentException("Missing or invalid file to set argument");
            }
        }
    }

    private void printHelp() {
        final String NL = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Program arguments:");
        sb.append(NL);
        sb.append("-h, --help - prints this help").append(NL);
        sb.append("-d - sets working directory (default: .)").append(NL);
        sb.append("-w - switches application to edit EXIF data mode:").append(NL);
        sb.append("    -t - dateTime to set in format ").append(DATE_TIME_TO_SET_FORMAT).append(NL);
        sb.append("    -f - file to edit EXIF DATA").append(NL);
        LOGGER.info(sb.toString());
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

    public Path getFileToEdit() {
        return fileToEdit;
    }
}

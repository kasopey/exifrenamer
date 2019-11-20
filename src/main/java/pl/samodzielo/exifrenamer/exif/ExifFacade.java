package pl.samodzielo.exifrenamer.exif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ExifFacade {

    private static Logger LOGGER = LoggerFactory.getLogger(ExifFacade.class);

    public Optional<Path> isImage(Path entry) {
        try {
            if (Files.isRegularFile(entry) && ImageIO.read(entry.toFile()) != null) {
                return Optional.of(entry);
            }
            return Optional.empty();
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
            return Optional.empty();
        }
    }
}

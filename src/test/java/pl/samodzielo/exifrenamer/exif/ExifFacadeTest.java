package pl.samodzielo.exifrenamer.exif;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.samodzielo.exifrenamer.Fixtures;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static pl.samodzielo.exifrenamer.Fixtures.*;

class ExifFacadeTest {

    private ExifFacade when;

    private Path givenPath;

    @BeforeEach
    void setup() {
        Fixtures.setWorkingDir();
        when = new ExifFacade();
    }

    @Test
    void should_return_optional_with_value_for_not_existent_file() {
        givenFile(IMAGE_FILE);

        Optional<Path> image = when.isImage(givenPath);

        assertThat(image.get().getFileName().getFileName().toString()).isEqualTo(IMAGE_FILE);
    }

    @Test
    void should_return_empty_optional_for_not_existent_file() {
        givenFile("missing-file");

        Optional<Path> image = when.isImage(givenPath);

        assertThat(image.isPresent()).isFalse();
    }

    @Test
    void should_return_empty_optional_for_not_image_file() {
        givenFile(NOT_IMAGE_FILE);

        Optional<Path> image = when.isImage(givenPath);

        assertThat(image.isPresent()).isFalse();
    }

    private void givenFile(String file) {
        givenPath = Paths.get(WORK_DIR + SEP + file);
    }

}
package pl.samodzielo.exifrenamer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static final String SEP = System.getProperty("file.separator");

    private static final String WORK_DIR = "src" + SEP + "test" + SEP + "resources" + SEP;

    @BeforeEach
    private void setUp() throws IOException {
        Path tempDir = Files.createTempDirectory("exifrenamer-test");
        System.err.println(tempDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(new File(WORK_DIR).toPath())) {

            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {

                    Files.copy(entry, tempDir.resolve(entry.getFileName()), REPLACE_EXISTING);
                }
            }
        }
    }

    @Test
    void test() {
        System.err.println("Ss");
    }

}
package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import java.io.IOException;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = ".steps",
        plugin = {"rerun:target/rerun.txt", "pretty", "html:target/cucumber-reports"}
)
public class TestRunner {

    @BeforeClass
    public static void setUp() throws IOException {
        if (isMacOrLinux()) {
            // Path to the bash script
            String scriptPath = "src/test/java/runners/randomize_filenames.sh";

            // Running the script
            Runtime.getRuntime().exec(scriptPath);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (isMacOrLinux()) {
            // Path to the bash script that removes prefixes
            String scriptPath = "src/test/java/runners/remove_prefixes.sh";

            // Running the script
            Runtime.getRuntime().exec(scriptPath);
        }
    }

    private static boolean isMacOrLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac") || os.contains("linux") || os.contains("unix");
    }
}



package MyRunner;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = { "stepDefinitions", "utils" },
        plugin = {
                "pretty",
                "json:target/cucumber.json",
                "junit:target/cucumber.xml"
        },
        tags = "@ALL",
        monochrome = true,
        dryRun = false
)
public class TestRunner {
}
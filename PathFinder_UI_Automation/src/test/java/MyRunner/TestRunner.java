
package MyRunner;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * JUnit 4 Cucumber runner for all scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",   // ✅ Correct feature path
    glue = { "stepDefinitions" },               // ✅ Glue should point to step definitions only
    plugin = {
        "pretty",
        "html:target/cucumber-reports.html",
        "json:target/cucumber.json",
        "junit:target/cucumber.xml"
    },
    monochrome = true,
    dryRun = false
)
public class TestRunner {
}

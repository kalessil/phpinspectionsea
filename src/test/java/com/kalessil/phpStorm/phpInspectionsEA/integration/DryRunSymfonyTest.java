package com.kalessil.phpStorm.phpInspectionsEA.integration;

import com.intellij.codeInspection.InspectionsReportConverter;
import com.intellij.testFramework.InspectionTestCase;

public class DryRunSymfonyTest extends InspectionTestCase {
    public void test() throws InspectionsReportConverter.ConversionException {
        /*
        final InspectionManagerEx app = (InspectionManagerEx) InspectionManager.getInstance(myProject);
        final String location         = "D:/Projects/phpinspectionsea/src/test/resources/fixtures/dryrun/symfony";

        final List<File> results    = new ArrayList<>();
        VirtualFile sourceDirectory = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(location));
        AnalysisScope scope         = this.createAnalysisScope(sourceDirectory); // ? .getParent()
        ProgressManager.getInstance().runProcess(() -> {
            app.createNewGlobalContext(true)
               .launchInspectionsOffline(scope, location, false, results); // false: run global tools only
        }, new ProgressIndicatorBase() {});

        new PlainTextFormatter().convert(
            location + "/out",
            location + "/out",
            app.createNewGlobalContext(true).getTools(),
            results
        );
        */
    }

    @Override
    protected String getTestDataPath() {
        return "";
    }
}

package com.kalessil.phpStorm.phpInspectionsEA.integration;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.InspectionsReportConverter;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.PlainTextFormatter;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.InspectionTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

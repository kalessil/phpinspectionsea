package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;

import com.intellij.codeInspection.InspectionEP;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.codeInspection.InspectionEP.GLOBAL_INSPECTION;
import static com.intellij.codeInspection.LocalInspectionEP.LOCAL_INSPECTION;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnknownInspectionInspector extends BasePhpInspection {
    private static final String message = "Unknown inspection: %s.";

    final static private Pattern inspectionsShortNames;
    static {
        inspectionsShortNames = Pattern.compile("(?:[A-Z][a-z]+){2,}");
    }

    final private static Set<String> inspections = new HashSet<>();
    static {
        ApplicationManager.getApplication().invokeLater(() -> {
            Arrays.stream(GLOBAL_INSPECTION.getExtensions())
                    .map(InspectionEP::getShortName)
                    .forEach(inspections::add);
            Arrays.stream(LOCAL_INSPECTION.getExtensions())
                    .map(InspectionEP::getShortName)
                    .forEach(inspections::add);
        });
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UnknownInspectionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unknown inspection suppression";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpDocTag(@NotNull PhpDocTag tag) {
                if ("@noinspection".equals(tag.getName())) {
                    final Set<String> candidates = new HashSet<>();

                    // Newer version of IDE better parsing the element, try leveraging this.
                    Stream.of(tag.getChildren())
                            .filter(c -> c instanceof PhpDocRef )
                            .map(c -> ((PhpDocRef) c).getName())
                            .filter(c -> c != null && ! c.isEmpty() )
                            .forEach(candidates::add);
                    // Older version of IDE, we have to parse ourselves.
                    if (candidates.isEmpty()) {
                        final Matcher regexMatcher = inspectionsShortNames.matcher(tag.getTagValue().trim());
                        while (regexMatcher.find()) {
                            final String shortName = regexMatcher.group(0);
                            if (shortName != null) {
                                candidates.add(shortName);
                            }
                        }
                    }

                    if (! candidates.isEmpty()) {
                        final List<String> unknown = candidates.stream()
                                .filter(c -> ! inspections.contains(c) && ! inspections.contains(c + "Inspection"))
                                .collect(Collectors.toList());
                        if (! unknown.isEmpty()) {
                            final PsiElement target = tag.getFirstChild();
                            if (target != null) {
                                holder.registerProblem(
                                        target,
                                        String.format(MessagesPresentationUtil.prefixWithEa(message), String.join(", ", unknown))
                                );
                            }
                            unknown.clear();
                        }
                    }
                }
            }
        };
    }
}

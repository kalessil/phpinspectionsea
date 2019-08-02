package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TestingUnfriendlyApisInspector extends PhpInspection {
    // Inspection options.
    public int COMPLAIN_THRESHOLD = 5;
    public int SCREAM_THRESHOLD   = 7;

    private final static String messagePattern = "%s mocks has been introduced here: either the test case should be refactored or API has design issues.";

    private final static Set<String> methods = new HashSet<>();
    static {
        methods.add("getMock");
        methods.add("createMock");
        methods.add("getMockForTrait");
        methods.add("getMockForAbstractClass");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "TestingUnfriendlyApisInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(method);
                if (nameIdentifier != null && !method.isAbstract() && this.isTestContext(method)) {
                    final long mocksCount = PsiTreeUtil.findChildrenOfType(method, MethodReference.class).stream()
                            .filter(reference -> methods.contains(reference.getName()))
                            .filter(reference -> {
                                final PsiElement grandParent = reference.getParent().getParent();
                                /* ->setConstructorArgs([ $this->createMock(...) ]) */
                                if (grandParent instanceof ArrayCreationExpression) {
                                    final PsiElement candidate = grandParent.getParent().getParent();
                                    if (candidate instanceof MethodReference) {
                                        final String methodName = ((MethodReference) candidate).getName();
                                        return methodName != null && !methodName.equals("setConstructorArgs");
                                    }
                                }

                                /* new Clazz( $this->createMock(...) ) */
                                return !(grandParent instanceof NewExpression) &&
                                        ExpressionSemanticUtil.getScope(reference) == method;
                            })
                            .count();

                    if (mocksCount >= SCREAM_THRESHOLD) {
                        holder.registerProblem(
                                nameIdentifier,
                                String.format(messagePattern, mocksCount),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                    } else if (mocksCount >= COMPLAIN_THRESHOLD) {
                        holder.registerProblem(
                                nameIdentifier,
                                String.format(messagePattern, mocksCount),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        );
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addSpinner("Complain threshold:", COMPLAIN_THRESHOLD, (input) -> COMPLAIN_THRESHOLD = input);
            component.addSpinner("Scream threshold:", SCREAM_THRESHOLD, (input) -> SCREAM_THRESHOLD = input);
        });
    }
}

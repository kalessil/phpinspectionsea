package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MissingIssetImplementationInspector extends BasePhpInspection {
    private static final String messagePattern = "%c% needs to implement __isset to properly work here.";

    private static final Set<String> magicClasses = new HashSet<>();
    static {
        magicClasses.add("\\SimpleXMLElement");
        magicClasses.add("\\stdClass");
    }

    @NotNull
    public String getShortName() {
        return "MissingIssetImplementationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpEmpty(PhpEmpty expression) {
                analyzeDispatchedExpressions(expression.getVariables());
            }

            public void visitPhpIsset(PhpIsset expression) {
                analyzeDispatchedExpressions(expression.getVariables());
            }

            private void analyzeDispatchedExpressions(@NotNull PhpExpression[] parameters) {
                final Project project       = holder.getProject();
                final PhpIndex projectIndex = PhpIndex.getInstance(project);

                for (final PhpExpression parameter : parameters) {
                    if (parameter instanceof FieldReference) {
                        final FieldReference reference = (FieldReference) parameter;
                        /* if the field name is not implicit or the field resolved, continue */
                        if (null == reference.getNameNode() || null != reference.resolve()) {
                            continue;
                        }

                        /* false-positives: in the $this context we are dealing with dynamic properties */
                        final PhpExpression variable = reference.getClassReference();
                        if (null == variable || variable.getText().equals("$this")) {
                            continue;
                        }

                        /* long way around: identify an lookup classes */
                        final Set<String> resolvedTypes = variable.getType().global(project).filterUnknown().getTypes();
                        for (final String type : resolvedTypes) {
                            final String normalizedType = Types.getType(type);
                            /* false-positives: SimpleXMLElement */
                            if (normalizedType.startsWith("\\") && !magicClasses.contains(normalizedType)) {
                                final Collection<PhpClass> classes = projectIndex.getClassesByFQN(normalizedType);
                                if (!classes.isEmpty()) {
                                    final PhpClass clazz   = classes.iterator().next();
                                    final boolean hasField = null != clazz.findFieldByName(parameter.getName(), false);
                                    if (!hasField && null == clazz.findMethodByName("__isset")) {
                                        final String message = messagePattern.replace("%c%", clazz.getFQN());
                                        holder.registerProblem(parameter, message, ProblemHighlightType.GENERIC_ERROR);

                                        break;
                                    }
                                }
                            }
                        }
                        resolvedTypes.clear();
                    }
                }
            }
        };
    }
}

package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

public class MissingIssetImplementationInspector extends BasePhpInspection {
    private static final String messagePattern = "%c% needs to implement __isset to properly work here.";

    private static final Set<String> magicClasses = new HashSet<>();
    static {
        magicClasses.add("\\SimpleXMLElement");
        magicClasses.add("\\stdClass");
        magicClasses.add("\\DOMDocument");
    }

    @NotNull
    public String getShortName() {
        return "MissingIssetImplementationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpEmpty(@NotNull PhpEmpty expression) {
                this.analyzeDispatchedExpressions(expression.getVariables());
            }

            @Override
            public void visitPhpIsset(@NotNull PhpIsset expression) {
                this.analyzeDispatchedExpressions(expression.getVariables());
            }

            private void analyzeDispatchedExpressions(@NotNull PhpExpression[] parameters) {
                final Project project       = holder.getProject();
                final PhpIndex projectIndex = PhpIndex.getInstance(project);

                for (final PhpExpression parameter : parameters) {
                    if (parameter instanceof FieldReference) {
                        final FieldReference reference = (FieldReference) parameter;
                        final String parameterName     = parameter.getName();
                        /* if the field name is not implicit or the field resolved, continue */
                        if (
                            reference.getNameNode() == null || parameterName == null ||
                            OpenapiResolveUtil.resolveReference(reference) != null
                        ) {
                            continue;
                        }

                        /* false-positives: in the $this context we are dealing with dynamic properties */
                        final PhpExpression variable = reference.getClassReference();
                        if (null == variable || variable.getText().equals("$this")) {
                            continue;
                        }

                        /* long way around: identify an lookup classes */
                        final Set<String> resolvedTypes = new HashSet<>();
                        final PhpType resolved          = OpenapiResolveUtil.resolveType(variable, project);
                        if (resolved != null) {
                            resolved.filterUnknown().getTypes().forEach(t -> resolvedTypes.add(Types.getType(t)));
                        }
                        for (final String type : resolvedTypes) {
                            /* false-positives: SimpleXMLElement, stdClass */
                            if (type.startsWith("\\") && !magicClasses.contains(type)) {
                                final Collection<PhpClass> classes = projectIndex.getClassesByFQN(type);
                                final PhpClass clazz               = classes.isEmpty() ? null : classes.iterator().next();
                                /* resolved class FQN might differ from what type states */
                                if (clazz != null && !magicClasses.contains(clazz.getFQN())) {
                                    final boolean hasField = OpenapiResolveUtil.resolveField(clazz, parameterName) != null;
                                    if (!hasField && OpenapiResolveUtil.resolveMethod(clazz, "__isset") == null) {
                                        final String message = messagePattern.replace("%c%", type);
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

package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class GetSetMethodCorrectnessInspector extends BasePhpInspection {
    private static final String messagePattern = "It's probably a wrong field was used here (%s, %s).";

    private static final Pattern regexTargetName;
    static {
        regexTargetName = Pattern.compile("^(set|get|is)([A-Z][a-z]+)+$");
    }

    @NotNull
    public String getShortName() {
        return "GetSetMethodCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final String methodName = method.getName();
                if (!methodName.isEmpty() && regexTargetName.matcher(methodName).matches()) {
                    final boolean isTargetMethod = !method.isAbstract() &&
                                                   method.getAccess().isPublic() &&
                                                   method.getParameters().length < 2;
                    if (isTargetMethod) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                        if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                            final Set<String> usedFields = new HashSet<>();
                            for (final FieldReference reference : PsiTreeUtil.findChildrenOfType(body, FieldReference.class)) {
                                final PsiElement base = reference.getFirstChild();
                                if (base instanceof Variable && ((Variable) base).getName().equals("this")) {
                                    final PsiElement context = reference.getParent();
                                    if (!(context instanceof MemberReference) && !(context instanceof ParameterList)) {
                                        usedFields.add(reference.getName());
                                    }
                                }
                                if (usedFields.size() > 1) {
                                    break;
                                }
                            }
                            if (usedFields.size() == 1) {
                                final String fieldName            = usedFields.iterator().next();
                                final String methodNameNormalized = methodName.replaceFirst("^(set|get|is)", "").toLowerCase();
                                final String fieldNameNormalized  = fieldName.replaceFirst("^(is)", "").replaceAll("_", "").toLowerCase();
                                /* TODO: or we searching fields with lower Levenshtein distance */
                                if (!methodNameNormalized.contains(fieldNameNormalized) && !fieldNameNormalized.contains(methodNameNormalized)) {
                                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                                    if (nameNode != null) {
                                        final boolean isDelegating = PsiTreeUtil.findChildrenOfType(body, MethodReference.class).stream()
                                                .anyMatch(reference -> methodName.equals(reference.getName()));
                                        if (!isDelegating) {
                                            final int similarity = StringUtils.getLevenshteinDistance(methodName, fieldName);
                                            holder.registerProblem(
                                                    nameNode,
                                                    String.format(messagePattern, fieldName, similarity)
                                            );
                                        }
                                    }
                                }
                            }
                            usedFields.clear();
                        }
                    }
                }
            }
        };
    }
}

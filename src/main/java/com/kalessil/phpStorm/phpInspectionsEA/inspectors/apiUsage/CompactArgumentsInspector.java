package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class CompactArgumentsInspector extends BasePhpInspection {
    private static final String messagePattern = "'$%v%' might not be defined in the scope";

    @NotNull
    public String getShortName() {
        return "CompactArgumentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check requirements */
                final PsiElement[] params = reference.getParameters();
                final String function     = reference.getName();
                if (0 == params.length || StringUtil.isEmpty(function) || !function.equals("compact")) {
                    return;
                }
                final Function scope = ExpressionSemanticUtil.getScope(reference);
                if (null == scope) {
                    return;
                }

                /* extract variables names needed */
                final HashSet<String> variablesCompacted = new HashSet<>();
                for (PsiElement compactParameter : params) {
                    if (!(compactParameter instanceof StringLiteralExpression)) {
                        continue;
                    }

                    final StringLiteralExpression expression = (StringLiteralExpression) compactParameter;
                    final String name                        = expression.getContents();
                    if (!StringUtil.isEmpty(name) && !PhpRefactoringUtil.containsLocalVariables(expression)) {
                        variablesCompacted.add(name);
                    }
                }

                /* if we have something to analyze, collect what scope provides */
                if (variablesCompacted.size() > 0) {
                    final HashSet<String> variablesDeclared = new HashSet<>();
                    /* parameters can be compacted */
                    for (Parameter scopeParameter : scope.getParameters()) {
                        variablesDeclared.add(scopeParameter.getName());
                    }
                    /* local variables can be compacted */
                    for (VariableImpl scopeVariable : PsiTreeUtil.findChildrenOfType(scope, VariableImpl.class)) {
                        variablesDeclared.add(scopeVariable.getName());
                    }

                    /* analyze and report suspicious parameters, release refs afterwards */
                    for (String subject : variablesCompacted) {
                        if (!variablesDeclared.contains(subject)) {
                            final String message = messagePattern.replace("%v%", subject);
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                    variablesDeclared.clear();
                    variablesCompacted.clear();
                }
            }
        };
    }
}

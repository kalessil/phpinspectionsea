package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DispatchingThisIntoClosuresInspector extends BasePhpInspection {
    private static final String message = "Will not work. You have to define a temporary variable (e.g. $self) and use it instead.";

    @NotNull
    public String getShortName() {
        return "DispatchingThisIntoClosuresInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunction(Function function) {
                /* closure must defined in a method */
                if (OpenapiTypesUtil.isLambda(function) && ExpressionSemanticUtil.getScope(function) instanceof Method) {
                    final List<Variable> use = ExpressionSemanticUtil.getUseListVariables(function);
                    if (null != use && use.size() > 0) {
                        for (Variable variable : use) {
                            if (variable.getName().equals("this")) {
                                holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);
                                break;
                            }
                        }
                        use.clear();
                    }
                }
            }
        };
    }
}

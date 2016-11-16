package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateIntervalSpecificationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Date interval specification seems to be invalid";

    @NotNull
    public String getShortName() {
        return "DateIntervalSpecificationInspection";
    }

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexDateTimeAlike = null;
    @SuppressWarnings("CanBeFinal")
    static private Pattern regexRegular = null;
    static {
        regexDateTimeAlike = Pattern.compile("^P\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");
        regexRegular       = Pattern.compile("^P((\\d+Y)?(\\d+M)?(\\d+D)?(\\d+W)?)?(T(\\d+H)?(\\d+M)?(\\d+S)?)?$");
    }
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpNewExpression(NewExpression expression) {
                /* before inspecting check parameters amount */
                final PsiElement[] params = expression.getParameters();
                if (1 != params.length) {
                    return;
                }

                /* check if it's the target class */
                final ClassReference classReference = expression.getClassReference();
                if (null == classReference) {
                    return;
                }
                /* TODO: child classes support */
                final String classFQN = classReference.getFQN();
                if (StringUtil.isEmpty(classFQN) || !classFQN.equals("\\DateInterval")) {
                    return;
                }

                /* now try getting string literal and test against valid patterns */
                final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(params[0]);
                if (null != pattern) {
                    final String patternText = pattern.getContents();
                    /* do not process patterns with inline variables */
                    if (patternText.indexOf('$') >= 0 && PsiTreeUtil.findChildrenOfType(pattern, Variable.class).size() > 0) {
                        return;
                    }

                    Matcher regexMatcher = regexRegular.matcher(patternText);
                    if (!regexMatcher.find()) {
                        regexMatcher = regexDateTimeAlike.matcher(patternText);
                        if (!regexMatcher.find()) {
                            /* report the issue */
                            holder.registerProblem(pattern, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }
        };
    }
}

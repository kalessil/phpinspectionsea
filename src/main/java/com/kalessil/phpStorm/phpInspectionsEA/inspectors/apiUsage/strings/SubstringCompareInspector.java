package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

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

public class SubstringCompareInspector extends PhpInspection {
    private static final String message = "The specified length doesn't match the string length.";

    private static final Set<String> outerFunctions = new HashSet<>();
    static {
        outerFunctions.add("strtolower");
        outerFunctions.add("mb_strtolower");
        outerFunctions.add("strtoupper");
        outerFunctions.add("mb_strtoupper");
        outerFunctions.add("mb_convert_case");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SubstringCompareInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Substring comparison flaws";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("substr") || functionName.equals("mb_substr"))) {
                    PsiElement offset            = null;
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && OpenapiTypesUtil.isNumber(arguments[1])) {
                        offset = arguments[1];
                    } else if (arguments.length == 3 && OpenapiTypesUtil.isNumber(arguments[2])) {
                        offset = arguments[2];
                    }
                    if (offset != null) {
                        /* the substring extraction can be wrapped into a case manipulation call */
                        PsiElement compareCandidate = reference.getParent();
                        if (compareCandidate instanceof ParameterList) {
                            compareCandidate = compareCandidate.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(compareCandidate)) {
                                final String outerName = ((FunctionReference) compareCandidate).getName();
                                if (outerName != null && outerFunctions.contains(outerName)) {
                                    compareCandidate = compareCandidate.getParent();
                                }
                            }
                        }
                        /* we are expecting specific binary operations */
                        if (compareCandidate instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) compareCandidate;
                            if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binary.getOperationType())) {
                                PsiElement stringCandidate = binary.getLeftOperand();
                                if (OpenapiTypesUtil.isFunctionReference(stringCandidate)) {
                                    stringCandidate = binary.getRightOperand();
                                }
                                final StringLiteralExpression literal = ExpressionSemanticUtil.resolveAsStringLiteral(stringCandidate);
                                if (literal != null && literal.getFirstPsiChild() == null) {
                                    /* workaround for https://youtrack.jetbrains.com/issue/WI-44824 */
                                    final String patched   = literal.isSingleQuote() ? literal.getContents() : literal.getContents().replaceAll("\\\\r", "\r");
                                    final String unescaped = PhpStringUtil.unescapeText(patched, literal.isSingleQuote());
                                    /* plain vs mb_* string functions magic */
                                    int stringLength = unescaped.length();
                                    if (functionName.equals("substr")) {
                                        stringLength += unescaped.replaceAll("\\p{ASCII}", "").length();
                                    }

                                    int givenOffset  = 0;
                                    boolean isTarget;
                                    try {
                                        givenOffset = Integer.parseInt(offset.getText());
                                        if (arguments.length == 2) {
                                            isTarget = stringLength > 0 && givenOffset < 0 && stringLength != Math.abs(givenOffset);
                                        } else {
                                            isTarget = stringLength > 0 && stringLength != Math.abs(givenOffset);
                                        }
                                    } catch (final NumberFormatException lengthParsingHasFailed) {
                                        isTarget = false;
                                    }

                                    if (isTarget) {
                                        final String replacement = String.valueOf(givenOffset > 0 ? stringLength : -stringLength);
                                        holder.registerProblem(
                                                offset,
                                                ReportingUtil.wrapReportedMessage(message),
                                                new LengthFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class LengthFix extends UseSuggestedReplacementFixer {
        private static final String title = "Set correct value for the length parameter";

        @NotNull
        @Override
        public String getName() {
            return ReportingUtil.wrapReportedMessage(title);
        }

        LengthFix(@NotNull String expression) {
            super(expression);
        }
    }
}

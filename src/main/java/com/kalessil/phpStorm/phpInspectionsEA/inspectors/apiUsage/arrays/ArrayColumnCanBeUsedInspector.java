package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayColumnCanBeUsedInspector extends PhpInspection {
    private static final String messagePattern = "'%s' would fit more here (it also faster, but loses original keys).";

    // Inspection options.
    public boolean REPORT_PROPERTIES_MAPPING = false;

    @NotNull
    @Override
    public String getShortName() {
        return "ArrayColumnCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'array_column(...)' can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550)) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("array_map")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2 && arguments[1] != null && OpenapiTypesUtil.isLambda(arguments[0])) {
                            final Function closure       = (Function) (arguments[0] instanceof Function ? arguments[0] : arguments[0].getFirstChild());
                            final Parameter[] parameters = closure.getParameters();
                            if (parameters.length > 0) {
                                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(closure);
                                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                                    final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                                    if (last instanceof PhpReturn) {
                                        final PsiElement candidate = ExpressionSemanticUtil.getReturnValue((PhpReturn) last);
                                        if (candidate instanceof ArrayAccessExpression) {
                                            final ArrayAccessExpression access = (ArrayAccessExpression) candidate;
                                            final PhpPsiElement value          = access.getValue();
                                            if (value instanceof Variable && parameters[0].getName().equals(value.getName())) {
                                                final ArrayIndex index = access.getIndex();
                                                final PsiElement key   = index == null ? null : index.getValue();
                                                if (key != null) {
                                                        final String replacement = String.format(
                                                                "%sarray_column(%s, %s)",
                                                                reference.getImmediateNamespaceName(),
                                                                arguments[1].getText(),
                                                                key.getText()
                                                        );
                                                        holder.registerProblem(
                                                                reference,
                                                                String.format(ReportingUtil.wrapReportedMessage(messagePattern), replacement),
                                                                new UseArrayColumnFixer(replacement)
                                                        );
                                                }
                                            }
                                        } else if (candidate instanceof FieldReference) {
                                            final boolean supportsObjects = REPORT_PROPERTIES_MAPPING && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP700);
                                            if (supportsObjects) {
                                                final FieldReference fieldReference = (FieldReference) candidate;
                                                final PhpPsiElement value           = fieldReference.getFirstPsiChild();
                                                if (value instanceof Variable && parameters[0].getName().equals(value.getName())) {
                                                    final String columnForReplacement;
                                                    final String fieldName = fieldReference.getName();
                                                    if (fieldName != null && !fieldName.isEmpty()) {
                                                        columnForReplacement = String.format("'%s'", fieldName);
                                                    } else {
                                                        final PsiElement dynamicFieldName = value.getNextPsiSibling();
                                                        columnForReplacement = dynamicFieldName instanceof Variable ? dynamicFieldName.getText() : null;
                                                    }
                                                    if (columnForReplacement != null) {
                                                        final String replacement = String.format(
                                                                "%sarray_column(%s, %s)",
                                                                reference.getImmediateNamespaceName(),
                                                                arguments[1].getText(),
                                                                columnForReplacement
                                                        );
                                                        holder.registerProblem(
                                                                reference,
                                                                String.format(ReportingUtil.wrapReportedMessage(messagePattern), replacement),
                                                                new UseArrayColumnFixer(replacement)
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseArrayColumnFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Use array_column(...) instead";

        @NotNull
        @Override
        public String getName() {
            return ReportingUtil.wrapReportedMessage(title);
        }

        UseArrayColumnFixer(@NotNull String expression) {
            super(expression);
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Report properties mapping (refactoring unfriendly)", REPORT_PROPERTIES_MAPPING, (isSelected) -> REPORT_PROPERTIES_MAPPING = isSelected)
        );
    }
}

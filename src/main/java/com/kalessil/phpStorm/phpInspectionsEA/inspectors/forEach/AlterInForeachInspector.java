package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang.StringUtils;
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

public class AlterInForeachInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_USING_VALUE_BY_REF = false;

    private static final String patternSuggestReference = "Can be refactored as '$%c% = ...' if $%v% is defined as a reference (ensure that array supplied). Suppress if causes memory mismatches.";
    private static final String messageMissingUnset     = "This variable must be unset just after foreach to prevent possible side-effects.";
    private static final String messageKeyReference     = "Provokes a PHP Fatal error (key element cannot be a reference).";
    private static final String patternAmbiguousUnset   = "Unsetting $%v% is not needed because it's not a reference.";

    @NotNull
    public String getShortName() {
        return "AlterInForeachInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                /* lookup for reference preceding value */
                final Variable objForeachValue = foreach.getValue();
                if (null != objForeachValue) {
                    PsiElement prevElement = objForeachValue.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (null != prevElement) {
                        PhpPsiElement nextExpression = foreach.getNextPsiSibling();

                        if (OpenapiTypesUtil.is(prevElement, PhpTokenTypes.opBIT_AND)) {
                            /* === requested by the community, not part of original idea === */
                            /* look up for parents which doesn't have following statements, eg #53 */
                            PsiElement foreachParent = foreach.getParent();
                            while (null == nextExpression && null != foreachParent) {
                                if (!(foreachParent instanceof GroupStatement)) {
                                    nextExpression = ((PhpPsiElement) foreachParent).getNextPsiSibling();
                                }
                                foreachParent = foreachParent.getParent();

                                if (null == foreachParent || foreachParent instanceof Function || foreachParent instanceof PhpFile) {
                                    break;
                                }
                            }
                            /* === requested by the community, not part of original idea === */

                            /* allow return/end of control flow after the loop - no issues can be introduced */
                            boolean isRequirementFullFilled = false;
                            while (nextExpression instanceof PhpDocComment) {
                                nextExpression = nextExpression.getNextPsiSibling();
                            }
                            if (null == nextExpression || nextExpression instanceof PhpReturn || nextExpression instanceof PhpThrow) {
                                isRequirementFullFilled = true;
                            }
                            /* check unset is applied to value-variable */
                            final String foreachValueName = objForeachValue.getName();
                            if (nextExpression instanceof PhpUnset && !StringUtils.isEmpty(foreachValueName)) {
                                for (PhpPsiElement unsetArgument : ((PhpUnset) nextExpression).getArguments()) {
                                    // skip non-variable expressions
                                    if (!(unsetArgument instanceof Variable)) {
                                        continue;
                                    }

                                    // check argument matched foreach value name
                                    final String unsetArgumentName = unsetArgument.getName();
                                    if (!StringUtils.isEmpty(unsetArgumentName) && unsetArgumentName.equals(foreachValueName)) {
                                        isRequirementFullFilled = true;
                                        break;
                                    }
                                }
                            }

                            /* check if warning needs to be reported */
                            if (!isRequirementFullFilled) {
                                holder.registerProblem(objForeachValue, messageMissingUnset);
                            }
                        } else {
                            /* check for unset in parent foreach-statements: foreach-{foreach}-unset */
                            ForeachStatement currentForeach = foreach;
                            while (
                                !(nextExpression instanceof PhpUnset)
                                && currentForeach.getParent() instanceof GroupStatement
                                && currentForeach.getParent().getParent() instanceof ForeachStatement
                            ) {
                                currentForeach = (ForeachStatement) currentForeach.getParent().getParent();
                                nextExpression = currentForeach.getNextPsiSibling();
                            }

                            /* check if un-sets non-reference value - not needed at all, probably forgotten to cleanup */
                            if (nextExpression instanceof PhpUnset) {
                                final PhpPsiElement[] unsetArguments = ((PhpUnset) nextExpression).getArguments();
                                if (unsetArguments.length > 0) {
                                    final String foreachValueName = objForeachValue.getName();

                                    for (PhpPsiElement unsetExpression : unsetArguments) {
                                        if (!(unsetArguments[0] instanceof Variable)) {
                                            continue;
                                        }

                                        final String unsetArgumentName = unsetExpression.getName();
                                        if (
                                            !StringUtils.isEmpty(unsetArgumentName) &&
                                            !StringUtils.isEmpty(foreachValueName) &&
                                            unsetArgumentName.equals(foreachValueName)
                                        ) {
                                            final String message = patternAmbiguousUnset.replace("%v%", foreachValueName);
                                            holder.registerProblem(unsetExpression, message, ProblemHighlightType.WEAK_WARNING);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                this.strategyKeyCanNotBeReference(foreach);
            }

            private void strategyKeyCanNotBeReference(ForeachStatement foreach) {
                /* lookup for incorrect reference preceding key: foreach (... as &$key => ...) */
                final Variable objForeachKey = foreach.getKey();
                if (null != objForeachKey) {
                    PsiElement prevElement = objForeachKey.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (OpenapiTypesUtil.is(prevElement, PhpTokenTypes.opBIT_AND)) {
                        holder.registerProblem(prevElement, messageKeyReference, ProblemHighlightType.ERROR);
                    }
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                if (!SUGGEST_USING_VALUE_BY_REF /*|| ... PHP7 ...*/) {
                    return;
                }

                final PhpPsiElement operand = assignmentExpression.getVariable();
                if (!(operand instanceof ArrayAccessExpression)) {
                    return;
                }

                /* ensure assignment structure is complete */
                final ArrayAccessExpression container = (ArrayAccessExpression) operand;
                if (
                    null == container.getIndex() ||
                    null == container.getValue() ||
                    !(container.getIndex().getValue() instanceof Variable)
                ) {
                    return;
                }


                /* get parts of assignment */
                final PhpPsiElement objForeachSourceCandidate = container.getValue();
                final PhpPsiElement objForeachKeyCandidate    = container.getIndex().getValue();

                PsiElement parent = assignmentExpression.getParent();
                while (null != parent && !(parent instanceof PhpFile)) {
                    /* terminate if reached callable */
                    if (parent instanceof Function) {
                        return;
                    }

                    if (parent instanceof ForeachStatement) {
                        /* get parts of foreach: array, key, value */
                        final ForeachStatement objForeach = (ForeachStatement) parent;
                        final Variable objForeachValue    = objForeach.getValue();
                        final Variable objForeachKey      = objForeach.getKey();
                        final PsiElement objForeachArray  = objForeach.getArray();

                        /* report if aggressive optimization possible: foreach(... as &$value) */
                        if (
                            null != objForeachArray && null != objForeachKey && null != objForeachValue &&
                            OpenapiEquivalenceUtil.areEqual(objForeachKey, objForeachKeyCandidate) &&
                            OpenapiEquivalenceUtil.areEqual(objForeachArray, objForeachSourceCandidate)
                        ) {
                            final String strName = objForeachValue.getName();
                            if (!StringUtils.isEmpty(strName)) {
                                final String message = patternSuggestReference
                                        .replace("%c%", strName)
                                        .replace("%v%", strName);
                                holder.registerProblem(operand, message, ProblemHighlightType.WEAK_WARNING);

                                return;
                            }
                        }
                    }

                    parent = parent.getParent();
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Suggest using value by reference", SUGGEST_USING_VALUE_BY_REF, (isSelected) -> SUGGEST_USING_VALUE_BY_REF = isSelected)
        );
    }
}

package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousBinaryOperationInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "SuspiciousBinaryOperationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                if (
                    InstanceOfTraitStrategy.apply(expression, holder) ||
                    EqualsInAssignmentContextStrategy.apply(expression, holder) ||
                    GreaterOrEqualInHashElementStrategy.apply(expression, holder) ||
                    IdenticalOperandsStrategy.apply(expression, holder) ||
                    MisplacedOperatorStrategy.apply(expression, holder) ||
                    HardcodedBooleansStrategy.apply(expression, holder)
                ) {
                    return;
                }

                //noinspection UnnecessaryReturnStatement
                return;
            }
        };
    }
}

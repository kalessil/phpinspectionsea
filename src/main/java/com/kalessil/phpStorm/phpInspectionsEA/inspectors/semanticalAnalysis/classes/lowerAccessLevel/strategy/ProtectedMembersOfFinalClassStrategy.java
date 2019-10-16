package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.fixers.MakePrivateFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.utils.ModifierExtractionUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ProtectedMembersOfFinalClassStrategy {
    private static final String message = "Since the class is final, the member can be declared private.";

    public static void apply(@NotNull PhpClassMember subject, @NotNull ProblemsHolder holder) {
        final PhpClass clazz          = subject.getContainingClass();
        final boolean isTargetContext = clazz != null && clazz.isFinal() && subject.getModifier().isProtected();
        if (isTargetContext) {
            final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
            if (parent == null || !isOverride(subject, clazz, parent)) {
                final PsiElement modifier = ModifierExtractionUtil.getProtectedModifier(subject);
                if (modifier != null) {
                    holder.registerProblem(
                            modifier,
                            ReportingUtil.wrapReportedMessage(message),
                            new MakePrivateFixer(holder.getProject(), modifier)
                    );
                }
            }
        }
    }

    private static boolean isOverride(@NotNull PhpClassMember member, @NotNull PhpClass clazz, @NotNull PhpClass parent) {
        final String memberName = member.getName();
        if (member instanceof Field) {
            return parent.findFieldByName(memberName, ((Field) member).isConstant()) != null;
        }
        if (member instanceof Method) {
            return OpenapiResolveUtil.resolveMethod(parent, memberName) != null ||
                   Arrays.stream(clazz.getTraits()).anyMatch(t -> OpenapiResolveUtil.resolveMethod(t, memberName) != null);
        }
        return false;
    }
}

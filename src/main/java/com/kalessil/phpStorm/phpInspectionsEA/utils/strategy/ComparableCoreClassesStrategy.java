package com.kalessil.phpStorm.phpInspectionsEA.utils.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;

final public class ComparableCoreClassesStrategy {
    private final static HashSet<String> comparableObjects = new HashSet<>();
    static {
        comparableObjects.add("\\Closure");
        comparableObjects.add("\\DateTime");
        comparableObjects.add("\\DateTimeImmutable");
        comparableObjects.add("\\IntlBreakIterator");
        comparableObjects.add("\\IntlTimeZone");
        comparableObjects.add("\\PDO");
        comparableObjects.add("\\PDOStatement");
        comparableObjects.add("\\ArrayObject");
        comparableObjects.add("\\SplObjectStorage");
    }

    public static boolean apply (@Nullable PsiElement leftOperand, @Nullable PsiElement rightOperand, ProblemsHolder holder) {
        /* validate parameters and prepare needed objects */
        if (null == leftOperand || null == rightOperand) {
            return false;
        }
        final Function scope = ExpressionSemanticUtil.getScope(leftOperand);
        if (null == scope) {
            return false;
        }
        final PhpIndex projectIndex = PhpIndex.getInstance(holder.getProject());

        return isComparableObject(leftOperand, scope, projectIndex, holder) && isComparableObject(rightOperand, scope, projectIndex, holder);

    }

    private static boolean isComparableObject(@NotNull PsiElement operand, @NotNull Function scope, @NotNull PhpIndex projectIndex, ProblemsHolder holder) {
        /* extract types of operand, check if classes are/inherited from \DateTime */
        final HashSet<String> operandTypes = new HashSet<>();
        TypeFromPsiResolvingUtil.resolveExpressionType(operand, scope, projectIndex, operandTypes);
        if (!TypesSemanticsUtil.isNullableObjectInterface(operandTypes)) {
            operandTypes.clear();
            return false;
        }

        /* collect classes to check for \DateTime relationship */
        final LinkedList<PhpClass> operandClasses = new LinkedList<>();
        for (String classFQN : operandTypes) {
            if (classFQN.charAt(0) == '\\') {
                operandClasses.addAll(PhpIndexUtil.getObjectInterfaces(classFQN, projectIndex, false));
            }
        }
        operandTypes.clear();

        /* inspect classes for being a/child of special once */
        for (PhpClass clazz : operandClasses) {
            final HashSet<PhpClass> hierarchy = InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true);
            for (PhpClass oneClass : hierarchy){
                if (comparableObjects.contains(oneClass.getFQN())) {
                    return true;
                }
            }
            hierarchy.clear();
        }
        operandClasses.clear();

        return false;
    }
}

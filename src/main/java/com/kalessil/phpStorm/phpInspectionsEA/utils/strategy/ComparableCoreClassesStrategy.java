package com.kalessil.phpStorm.phpInspectionsEA.utils.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static boolean apply (@Nullable PsiElement leftOperand, @Nullable PsiElement rightOperand) {
        if (leftOperand != null && rightOperand != null) {
            final Function scope = ExpressionSemanticUtil.getScope(leftOperand);
            if (scope != null) {
                return isComparableObject(leftOperand) || isComparableObject(rightOperand);
            }
        }

        return false;
    }

    private static boolean isComparableObject(@NotNull PsiElement operand) {
        /* extract types of operand, check if classes are/inherited from \DateTime */
        final Set<String> operandTypes = new HashSet<>();
        if (operand instanceof PhpTypedElement) {
            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) operand, operand.getProject());
            if (resolved != null) {
                resolved.filterUnknown().getTypes().forEach(t -> operandTypes.add(Types.getType(t)));
            }
        }
        if (!TypesSemanticsUtil.isNullableObjectInterface(operandTypes)) {
            operandTypes.clear();
            return false;
        }

        /* collect classes to check for \DateTime relationship */
        final PhpIndex index                = PhpIndex.getInstance(operand.getProject());
        final List<PhpClass> operandClasses = new ArrayList<>();
        operandTypes.stream()
                .filter(fqn  -> fqn.charAt(0) == '\\')
                .forEach(fqn -> operandClasses.addAll(OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(fqn, index)));
        operandTypes.clear();

        /* inspect classes for being a/child of special once */
        for (final PhpClass clazz : operandClasses) {
            final boolean hasAny = InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream()
                    .anyMatch(c -> comparableObjects.contains(c.getFQN()));
            if (hasAny) {
                return true;
            }
        }
        operandClasses.clear();

        return false;
    }
}

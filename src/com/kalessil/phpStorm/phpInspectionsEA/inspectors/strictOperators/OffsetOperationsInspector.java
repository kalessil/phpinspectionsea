package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use square brackets instead of curvy for deeper analysis";
    private static final String strProblemNoOffsetSupport = "This container does not support offsets operations";
    private static final String strProblemInvalidIndex = "Wrong index type (%p% is incompatible with %a%)";

    @NotNull
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayAccessExpression(ArrayAccessExpression expression) {
                PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode || null == expression.getValue() || null == expression.getIndex()) {
                    return;
                }

                // promote using []
                if (bracketNode.getText().equals("}")) {
//                    holder.registerProblem(expression, strProblemUseSquareBrackets, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                // ensure offsets operations are supported
                HashSet<String> allowedIndexTypes = new HashSet<String>();
                if (!isContainerSupportsArrayAccess(expression.getValue(), allowedIndexTypes)) {
                    holder.registerProblem(expression.getValue(), strProblemNoOffsetSupport, ProblemHighlightType.WEAK_WARNING);

                    allowedIndexTypes.clear();
                    return;
                }
                // TODO: check why allowedIndexTypes can be empty


                // ensure index is one of (string, float, bool, null) when we acquired possible types information
                // TODO: hash-elements e.g. array initialization
                PhpPsiElement indexValue = expression.getIndex().getValue();
                if (null != indexValue && allowedIndexTypes.size() > 0) {
                    // resolve types with custom resolver, native gives type sets which not comparable properly
                    HashSet<String> possibleIndexTypes = new HashSet<String>();
                    TypeFromPlatformResolverUtil.resolveExpressionType(indexValue, possibleIndexTypes);

                    // now check if any type provided and check sets validity
                    if (possibleIndexTypes.size() > 0) {
                        // take possible and clean them respectively allowed to keep only conflicted
                        if (allowedIndexTypes.size() > 0) {
                            filterPossibleTypesWhichAreNotAllowed(possibleIndexTypes, allowedIndexTypes);
                        }

                        if (possibleIndexTypes.size() > 0) {
                            String strError = strProblemInvalidIndex
                                    .replace("%p%", possibleIndexTypes.toString())
                                    .replace("%a%", allowedIndexTypes.toString());
                            holder.registerProblem(indexValue, strError, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                    possibleIndexTypes.clear();
                }

                // clear valid types collection
                allowedIndexTypes.clear();
            }
        };
    }

    private boolean isContainerSupportsArrayAccess(@NotNull PsiElement container, @NotNull HashSet<String> indexTypesSupported) {
        boolean supportsOffsets = false;

        PhpIndex objIndex = PhpIndex.getInstance(container.getProject());

        HashSet<String> containerTypes = new HashSet<String>();
        TypeFromPlatformResolverUtil.resolveExpressionType(container, containerTypes);
        // failed to resolve, don't try to guess anything
        if (0 == containerTypes.size()) {
            return true;
        }

        for (String typeToCheck : containerTypes) {
            if (typeToCheck.equals(Types.strArray) || typeToCheck.equals(Types.strString)) {
                indexTypesSupported.add(Types.strString);
                indexTypesSupported.add(Types.strFloat);
                indexTypesSupported.add(Types.strInteger);
                indexTypesSupported.add(Types.strBoolean);
                indexTypesSupported.add(Types.strNull);
                indexTypesSupported.add(Types.strMixed);
                indexTypesSupported.add(Types.strStatic);

                supportsOffsets = true;
                continue;
            }

            // assume is just null-ble declaration
            if (typeToCheck.equals(Types.strNull)) {
                continue;
            }
            // some of possible types are wrong
            if (!StringUtil.isEmpty(typeToCheck) && typeToCheck.charAt(0) != '\\') {
                supportsOffsets = false;
                break;
            }

            for (PhpClass classToCheck : PhpIndexUtil.getObjectInterfaces(typeToCheck, objIndex)) {
                // custom offsets management, follow annotated types
                Method offsetSetMethod = classToCheck.findMethodByName("offsetSet");
                if (null != offsetSetMethod) {
                    if (offsetSetMethod.getParameters().length > 0) {
                        TypeFromPlatformResolverUtil.resolveExpressionType(offsetSetMethod.getParameters()[0], indexTypesSupported);
                    }

                    supportsOffsets = true;
                    continue;
                }

                // magic methods, demand regular array offset types
                Method magicMethod = classToCheck.findMethodByName("__get");
                if (null == magicMethod) {
                    magicMethod = classToCheck.findMethodByName("__set");
                }
                if (null != magicMethod) {
                    indexTypesSupported.add(Types.strString);
                    indexTypesSupported.add(Types.strFloat);
                    indexTypesSupported.add(Types.strInteger);
                    indexTypesSupported.add(Types.strBoolean);
                    indexTypesSupported.add(Types.strNull);
                    indexTypesSupported.add(Types.strMixed);
                    indexTypesSupported.add(Types.strStatic);

                    supportsOffsets = true;
                }
            }

        }
        containerTypes.clear();

        return supportsOffsets;
    }

    private void filterPossibleTypesWhichAreNotAllowed(
            @NotNull HashSet<String> possibleIndexTypes,
            @NotNull HashSet<String> allowedIndexTypes
    ) {
        HashSet<String> secureIterator = new HashSet<String>();

        final boolean isAnyObjectAllowed = allowedIndexTypes.contains(Types.strObject);
        final boolean isAnyScalarAllowed = allowedIndexTypes.contains(Types.strMixed);
        for (String possibleType : possibleIndexTypes) {
            // allowed, or matches null, mixed (assuming it's null-ble or scalar)
            if (
                possibleType.equals(Types.strMixed) || possibleType.equals(Types.strNull) ||
                allowedIndexTypes.contains(possibleType)
            ) {
                continue;
            }

            if (isAnyObjectAllowed && !StringUtil.isEmpty(possibleType) && possibleType.charAt(0) == '\\') {
                continue;
            }

            // TODO: check classes relations

            // scalar types, check if mixed allowed
            if (!isAnyScalarAllowed) {
                secureIterator.add(possibleType);
            }
        }

        possibleIndexTypes.clear();
        possibleIndexTypes.addAll(secureIterator);
        secureIterator.clear();
    }
}

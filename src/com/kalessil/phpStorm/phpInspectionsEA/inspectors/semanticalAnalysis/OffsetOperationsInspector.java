package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use [ ] instead of { } for deeper analysis";
    private static final String strProblemNoOffsetSupport = "This container may not support offset operations (%c%)";
    private static final String strProblemInvalidIndex = "Wrong index type (%p% is incompatible with %a%)";

    @NotNull
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayAccessExpression(ArrayAccessExpression expression) {
                PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode || null == expression.getValue()) {
                    return;
                }

                // promote using []
                if (bracketNode.getText().equals("}")) {
                    holder.registerProblem(expression, strProblemUseSquareBrackets, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                // ensure offsets operations are supported
                HashSet<String> allowedIndexTypes = new HashSet<String>();
                if (!isContainerSupportsArrayAccess(expression, allowedIndexTypes)) {
                    String strError = strProblemNoOffsetSupport.replace("%c%", allowedIndexTypes.toString());
                    holder.registerProblem(expression, strError, ProblemHighlightType.GENERIC_ERROR);

                    allowedIndexTypes.clear();
                    return;
                }

                // ensure index is one of (string, float, bool, null) when we acquired possible types information
                // TODO: hash-elements e.g. array initialization
                if (null != expression.getIndex()) {
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
                }

                // clear valid types collection
                allowedIndexTypes.clear();
            }
        };
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isContainerSupportsArrayAccess(@NotNull ArrayAccessExpression expression, @NotNull HashSet<String> indexTypesSupported) {

        // ok JB parses `$var[]= ...` always as array, lets make it working properly and report them later
        PsiElement container = expression.getValue();
        if (null == container) {
            return false;
        }

        boolean isWrongResolvedArrayPush = false;
        if (expression.getParent() instanceof AssignmentExpression) {
            if (((AssignmentExpression) expression.getParent()).getVariable() == expression) {
                isWrongResolvedArrayPush = (null == expression.getIndex() || null == expression.getIndex().getValue());
            }
        }

        // TODO: report to JB and get rid of this workarounds, move workaround into TypeFromPlatformResolverUtil.resolveExpressionType
        HashSet<String> containerTypes = new HashSet<String>();
        if (isWrongResolvedArrayPush) {
            TypeFromPsiResolvingUtil.resolveExpressionType(
                    container,
                    ExpressionSemanticUtil.getScope(expression),
                    PhpIndex.getInstance(expression.getProject()),
                    containerTypes
            );
        } else {
            TypeFromPlatformResolverUtil.resolveExpressionType(container, containerTypes);
        }

        // failed to resolve, don't try to guess anything
        if (0 == containerTypes.size()) {
            return true;
        }

        PhpIndex objIndex = PhpIndex.getInstance(container.getProject());

        boolean supportsOffsets = false;
        boolean commonTypesAdded = false;
        for (String typeToCheck : containerTypes) {
            // assume is just null-ble declaration or we shall just rust to mixed
            if (typeToCheck.equals(Types.strNull)) {
                continue;
            }
            if (typeToCheck.equals(Types.strMixed)) {
                supportsOffsets = true;
                continue;
            }

            // commonly used case: string and array
            if (typeToCheck.equals(Types.strArray) || typeToCheck.equals(Types.strString)) {
                if (!commonTypesAdded) {
                    addCommonIndexTypes(indexTypesSupported);
                    commonTypesAdded = true;
                }

                supportsOffsets = true;
                continue;
            }

            // some of possible types are scalars, what's wrong
            if (!StringUtil.isEmpty(typeToCheck) && typeToCheck.charAt(0) != '\\') {
                supportsOffsets = false;
                break;
            }

            for (PhpClass classToCheck : PhpIndexUtil.getObjectInterfaces(typeToCheck, objIndex)) {
                boolean isOffsetFunctionsPrecessed = false;

                // custom offsets management, follow annotated types
                Method offsetSetMethod = classToCheck.findMethodByName("offsetSet");
                if (null != offsetSetMethod) {
                    final PsiElement[] offsetSetParams = offsetSetMethod.getParameters();
                    if (offsetSetParams.length > 0) {
                        TypeFromPlatformResolverUtil.resolveExpressionType(offsetSetParams[0], indexTypesSupported);
                    }

                    supportsOffsets = true;
                    isOffsetFunctionsPrecessed = true;
                }
                // custom offsets management, follow annotated types
                Method offsetGetMethod = classToCheck.findMethodByName("offsetGet");
                if (null != offsetGetMethod) {
                    final PsiElement[] offsetGetParams = offsetGetMethod.getParameters();
                    if (offsetGetParams.length > 0) {
                        TypeFromPlatformResolverUtil.resolveExpressionType(offsetGetParams[0], indexTypesSupported);
                    }

                    supportsOffsets = true;
                    isOffsetFunctionsPrecessed = true;
                }
                if (isOffsetFunctionsPrecessed) {
                    continue;
                }

                // magic methods, demand regular array offset types
                Method magicMethod = classToCheck.findMethodByName("__get");
                if (null == magicMethod) {
                    magicMethod = classToCheck.findMethodByName("__set");
                }
                if (null != magicMethod) {
                    if (!commonTypesAdded) {
                        addCommonIndexTypes(indexTypesSupported);
                        commonTypesAdded = true;
                    }

                    supportsOffsets = true;
                }
            }

        }

        // when might not support offset access, reuse types container to report back why
        if (!supportsOffsets) {
            indexTypesSupported.clear();
            indexTypesSupported.addAll(containerTypes);
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

    private void addCommonIndexTypes(@NotNull HashSet<String> container) {
        container.add(Types.strString);
        container.add(Types.strFloat);
        container.add(Types.strInteger);
        container.add(Types.strBoolean);
        container.add(Types.strNull);
    }
}

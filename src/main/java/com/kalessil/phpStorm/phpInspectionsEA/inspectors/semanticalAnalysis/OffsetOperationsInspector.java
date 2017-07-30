package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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
    private static final String messageUseSquareBrackets = "Using [ ] instead of { } makes possible to analyze this expression.";
    private static final String patternNoOffsetSupport   = "'%c%' may not support offset operations (or it's type not annotated properly: %t%).";
    private static final String patternInvalidIndex      = "Resolved index type (%p%) is incompatible with possible %a%. Probably just proper type hinting needed.";

    @NotNull
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayAccessExpression(ArrayAccessExpression expression) {
                final PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode || null == expression.getValue()) {
                    return;
                }

                /* promote using [] instead of {} */
                if (PhpTokenTypes.chRBRACE == bracketNode.getNode().getElementType()) {
                    holder.registerProblem(expression, messageUseSquareBrackets, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                // ensure offsets operations are supported, do nothing if no types were resolved
                final HashSet<String> allowedIndexTypes = new HashSet<>();
                if (!isContainerSupportsArrayAccess(expression, allowedIndexTypes) && allowedIndexTypes.size() > 0) {
                    final String message = patternNoOffsetSupport
                            .replace("%t%", allowedIndexTypes.toString())
                            .replace("%c%", expression.getValue().getText());
                    holder.registerProblem(expression, message);

                    allowedIndexTypes.clear();
                    return;
                }

                // ensure index is one of (string, float, bool, null) when we acquired possible types information
                // TODO: hash-elements e.g. array initialization
                if (null != expression.getIndex()) {
                    final PhpPsiElement indexValue = expression.getIndex().getValue();
                    if (null != indexValue && allowedIndexTypes.size() > 0) {
                        // resolve types with custom resolver, native gives type sets which not comparable properly
                        final HashSet<String> possibleIndexTypes = new HashSet<>();
                        TypeFromPlatformResolverUtil.resolveExpressionType(indexValue, possibleIndexTypes);

                        // now check if any type provided and check sets validity
                        if (possibleIndexTypes.size() > 0) {
                            // take possible and clean them respectively allowed to keep only conflicted
                            if (allowedIndexTypes.size() > 0) {
                                filterPossibleTypesWhichAreNotAllowed(possibleIndexTypes, allowedIndexTypes);
                            }

                            if (possibleIndexTypes.size() > 0) {
                                final String message = patternInvalidIndex
                                        .replace("%p%", possibleIndexTypes.toString())
                                        .replace("%a%", allowedIndexTypes.toString());
                                holder.registerProblem(indexValue, message);
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
        final PsiElement container = expression.getValue();
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
        final HashSet<String> containerTypes = new HashSet<>();
        if (container instanceof PhpTypedElement) {
            if (isWrongResolvedArrayPush) {
                TypeFromPsiResolvingUtil.resolveExpressionType(
                        container,
                        ExpressionSemanticUtil.getScope(expression),
                        PhpIndex.getInstance(expression.getProject()),
                        containerTypes
                );
            } else {
                ((PhpTypedElement) container).getType()
                        .global(container.getProject()).filterUnknown().getTypes().stream()
                        .map(Types::getType)
                        .forEach(containerTypes::add);
            }
        }


        /* === cleanup resolved types === */
        if (containerTypes.contains(Types.strMixed)) {      // mixed are not analyzable
            containerTypes.clear();
            return true;
        }
        if (2 == containerTypes.size() &&
            containerTypes.contains(Types.strInteger) && containerTypes.contains(Types.strString)
        ) {                                                 // foreach loops keys are recognized this way
            containerTypes.clear();
            return true;
        }
        if (containerTypes.contains(Types.strCallable)) {   // treat callable as array
            containerTypes.remove(Types.strCallable);
            containerTypes.add(Types.strArray);
            containerTypes.add(Types.strString);
        }
        containerTypes.remove(Types.strNull);     // don't process nulls
        containerTypes.remove(Types.strObject);   // don't process generalized objects
        containerTypes.remove(Types.strEmptySet); // don't process mysterious empty set type

        /* === if we could not resolve container, do nothing === */
        if (0 == containerTypes.size()) {
            return true;
        }


        final PhpIndex objIndex = PhpIndex.getInstance(container.getProject());
        boolean supportsOffsets = false;
        for (String typeToCheck : containerTypes) {
            /* FIXME: appeared e.g. \array, see #65  */
            typeToCheck = Types.getType(typeToCheck);

            // commonly used case: string and array
            if (typeToCheck.equals(Types.strArray) || typeToCheck.equals(Types.strString)) {
                supportsOffsets = true;

                /* here we state which regular index types we want to promote */
                indexTypesSupported.add(Types.strString);
                indexTypesSupported.add(Types.strInteger);

                continue;
            }

            // some of possible types are scalars, what's wrong
            if (!StringUtil.isEmpty(typeToCheck) && typeToCheck.charAt(0) != '\\') {
                supportsOffsets = false;
                break;
            }

            // now we are at point when analyzing classes only
            for (PhpClass classToCheck : PhpIndexUtil.getObjectInterfaces(typeToCheck, objIndex, false)) {
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
                    supportsOffsets = true;

                    /* here we state which regular index types we want to promote */
                    indexTypesSupported.add(Types.strString);
                    indexTypesSupported.add(Types.strInteger);
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
        final HashSet<String> secureIterator = new HashSet<>();

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

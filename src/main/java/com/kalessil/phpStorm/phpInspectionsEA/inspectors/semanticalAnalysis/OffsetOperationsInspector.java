package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 * (c) Denis Ryabov <...>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String patternNoOffsetSupport = "'%s' may not support offset operations (or its type not annotated properly: %s).";
    private static final String patternInvalidIndex    = "Resolved index type (%s) is incompatible with possible %s. Probably just proper type hinting needed.";

    @NotNull
    @Override
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Array and string offset validity";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode || null == expression.getValue()) {
                    return;
                }

                // ensure offsets operations are supported, do nothing if no types were resolved
                final Set<String> allowedIndexTypes = new HashSet<>();
                if (!isContainerSupportsArrayAccess(holder.getProject(), expression, allowedIndexTypes) && !allowedIndexTypes.isEmpty()) {
                    holder.registerProblem(
                            expression,
                            String.format(MessagesPresentationUtil.prefixWithEa(patternNoOffsetSupport), expression.getValue().getText(), allowedIndexTypes.toString())
                    );

                    allowedIndexTypes.clear();
                    return;
                }

                // ensure index is one of (string, float, bool, null) when we acquired possible types information
                // TODO: hash-elements e.g. array initialization
                if (!allowedIndexTypes.isEmpty() && expression.getIndex() != null) {
                    final PhpPsiElement indexValue = expression.getIndex().getValue();
                    if (indexValue instanceof PhpTypedElement) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) indexValue, holder.getProject());
                        if (resolved != null) {
                            final Set<String> indexTypes = new HashSet<>();
                            resolved.filterUnknown().getTypes().forEach(t -> indexTypes.add(Types.getType(t)));
                            if (!indexTypes.isEmpty()) {
                                filterPossibleTypesWhichAreNotAllowed(indexTypes, allowedIndexTypes);
                                if (!indexTypes.isEmpty()) {
                                    holder.registerProblem(
                                            indexValue,
                                            String.format(MessagesPresentationUtil.prefixWithEa(patternInvalidIndex), indexTypes.toString(), allowedIndexTypes.toString())
                                    );
                                    indexTypes.clear();
                                }
                            }
                        }
                    }
                }

                // clear valid types collection
                allowedIndexTypes.clear();
            }
        };
    }

    private boolean isContainerSupportsArrayAccess(@NotNull Project project, @NotNull ArrayAccessExpression expression, @NotNull Set<String> indexTypesSupported) {

        // ok JB parses `$var[]= ...` always as array, lets make it working properly and report them later
        final PsiElement container = expression.getValue();
        if (null == container) {
            return false;
        }

        final Set<String> containerTypes = new HashSet<>();
        if (container instanceof PhpTypedElement) {
            PhpType type = null;
            // Initially, try scope-limited types resolution from the single possible value.
            final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(container);
            if (variants.size() == 1) {
                final PsiElement value = variants.iterator().next();
                if (value instanceof PhpTypedElement) {
                    type = OpenapiResolveUtil.resolveType((PhpTypedElement) value, project);
                }
            }
            variants.clear();
            // As fallback, resort more generic types resolution.
            if (type == null) {
                type = OpenapiResolveUtil.resolveType((PhpTypedElement) container, project);
            }
            if (type != null && !type.hasUnknown()) {
                type.getTypes().forEach(t -> containerTypes.add(Types.getType(t)));
                Stream.of(Types.strSelf, Types.strStatic).forEach(containerTypes::remove);
            }
        }

        /* === cleanup resolved types === */
        if (containerTypes.contains(Types.strMixed)) {      // mixed are not analyzable
            containerTypes.clear();
            return true;
        }
        if (containerTypes.size() == 2 && containerTypes.contains(Types.strString)) {
            final boolean isForeachKeyType    = containerTypes.contains(Types.strInteger);
            final boolean isCoreApiStringType = containerTypes.contains(Types.strBoolean);
            if (isForeachKeyType || isCoreApiStringType) {
                containerTypes.clear();
                return true;
            }
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
        if (containerTypes.isEmpty()) {
            return true;
        }

        final PhpIndex index    = PhpIndex.getInstance(project);
        boolean supportsOffsets = false;
        for (final String typeToCheck : containerTypes) {
            // commonly used case: string and array
            if (typeToCheck.equals(Types.strArray) || typeToCheck.equals(Types.strString)) {
                supportsOffsets = true;

                /* here we state which regular index types we want to promote */
                indexTypesSupported.add(Types.strString);
                indexTypesSupported.add(Types.strInteger);

                continue;
            }

            // some of possible types are scalars, what's wrong
            if (!typeToCheck.isEmpty() && typeToCheck.charAt(0) != '\\') {
                supportsOffsets = false;
                break;
            }

            // now we are at point when analyzing classes only
            final List<PhpClass> classes = OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(typeToCheck, index);
            for (final PhpClass clazz : classes) {
                /* custom offsets management, follow annotated types */
                for (final String methodName : Arrays.asList("offsetGet", "offsetSet", "__get", "__set")) {
                    final Method method = OpenapiResolveUtil.resolveMethod(clazz, methodName);
                    if (method != null) {
                        /* regular array index types can be applied */
                        if (methodName.startsWith("__")) {
                            indexTypesSupported.add(Types.strString);
                            indexTypesSupported.add(Types.strInteger);
                        }
                        /* user-defined index types can be applied */
                        else {
                            final Parameter[] parameters = method.getParameters();
                            if (parameters.length > 0) {
                                final PhpType type = OpenapiResolveUtil.resolveType(parameters[0], project);
                                if (type != null) {
                                    type.filterUnknown().getTypes().forEach(t -> indexTypesSupported.add(Types.getType(t)));
                                }
                            }
                        }
                        supportsOffsets = true;
                    }
                }
            }
            classes.clear();
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
            @NotNull Set<String> possibleIndexTypes,
            @NotNull Set<String> allowedIndexTypes
    ) {
        final Set<String> secureIterator = new HashSet<>();

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

            if (isAnyObjectAllowed && !possibleType.isEmpty() && possibleType.charAt(0) == '\\') {
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

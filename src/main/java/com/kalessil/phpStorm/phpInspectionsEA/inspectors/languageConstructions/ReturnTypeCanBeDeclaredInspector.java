package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReturnTypeCanBeDeclaredInspector extends BasePhpInspection {
    private static final String messagePattern = "': %t%' can be declared as return type hint.";

    private static final Set<String> returnTypes = new HashSet<>();
    private static final Set<String> voidTypes   = new HashSet<>();
    static {
        /* +class/interface reference for PHP7.0+; +void for PHP7.1+ */
        returnTypes.add("self");
        returnTypes.add("array");
        returnTypes.add("callable");
        returnTypes.add("bool");
        returnTypes.add("float");
        returnTypes.add("int");
        returnTypes.add("string");

        voidTypes.add("null");
        voidTypes.add("void");
    }

    @NotNull
    public String getShortName() {
        return "ReturnTypeCanBeDeclaredInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final Project project      = holder.getProject();
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.RETURN_TYPES) && null == method.getReturnType()) {
                    final PsiElement methodNameNode = NamedElementUtil.getNameIdentifier(method);
                    final boolean isMagicFunction   = method.getName().startsWith("__");
                    if (!isMagicFunction && null != methodNameNode) {
                        final boolean supportNullableTypes = php.hasFeature(PhpLanguageFeature.NULLABLES);
                        if (method.isAbstract()) {
                            handleAbstractMethod(method, methodNameNode, supportNullableTypes);
                        } else {
                            handleMethod(method, methodNameNode, supportNullableTypes);
                        }
                    }
                }
            }

            private void handleAbstractMethod(
                @NotNull Method method,
                @NotNull PsiElement target,
                boolean supportNullableTypes
            ) {
                if (null != method.getDocComment()) {
                    handleMethod(method, target, supportNullableTypes);
                }
            }

            private void handleMethod(
                @NotNull Method method,
                @NotNull PsiElement target,
                boolean supportNullableTypes
            ) {
                /* ignore DocBlock, resolve and normalize types instead (DocBlock is involved, but nevertheless) */
                final Set<String> normalizedTypes = new HashSet<>();
                for (String type : method.getType().global(holder.getProject()).filterUnknown().getTypes()) {
                    normalizedTypes.add(Types.getType(type));
                }
                checkNonImplicitNullReturn(method, normalizedTypes);

                final int typesCount = normalizedTypes.size();
                /* case 1: offer using void */
                if (0 == typesCount && supportNullableTypes) {
                    final String suggestedType = Types.strVoid;
                    final String message       = messagePattern.replace("%t%", suggestedType);
                    holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                }
                /* case 2: offer using type */
                if (1 == typesCount) {
                    final String singleType    = normalizedTypes.iterator().next();
                    final String suggestedType
                        = voidTypes.contains(singleType) ? Types.strVoid : compactType(singleType, method);

                    final boolean isLegitBasic = singleType.startsWith("\\") || returnTypes.contains(singleType);
                    final boolean isLegitVoid  = supportNullableTypes && suggestedType.equals(Types.strVoid);
                    if (isLegitBasic || isLegitVoid) {
                        final String message = messagePattern.replace("%t%", suggestedType);
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                    }
                }
                /* case 3: offer using nullable type */
                if (2 == typesCount && supportNullableTypes && normalizedTypes.contains(Types.strNull)) {
                    normalizedTypes.remove(Types.strNull);

                    final String nullableType = normalizedTypes.iterator().next();
                    final String suggestedType
                        = voidTypes.contains(nullableType) ? Types.strVoid : compactType(nullableType, method);

                    final boolean isLegitNullable = nullableType.startsWith("\\") || returnTypes.contains(nullableType);
                    final boolean isLegitVoid     = suggestedType.equals(Types.strVoid);
                    if (isLegitNullable || isLegitVoid) {
                        final String typeHint = isLegitVoid ? suggestedType : "?" + suggestedType;
                        final String message  = messagePattern.replace("%t%", typeHint);
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(typeHint));
                    }
                }
            }

            private String compactType(@NotNull String type, @NotNull Method method) {
                String compacted = type;
                if (type.startsWith("\\")) {
                    final PhpClass clazz   = method.getContainingClass();
                    final String nameSpace = null == clazz ? null : clazz.getNamespaceName();
                    if (null != nameSpace && nameSpace.length() > 1 && type.startsWith(nameSpace)) {
                        compacted = compacted.replace(nameSpace, "");
                    }
                }
                return compacted;
            }

            private void checkNonImplicitNullReturn(@NotNull Method method, @NotNull Set<String> types) {
                if (!types.isEmpty() && !types.contains(Types.strNull) && !types.contains(Types.strVoid)) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                    final PsiElement last     = null == body ? null : ExpressionSemanticUtil.getLastStatement(body);
                    if ((null == last && !method.isAbstract()) || (!(last instanceof PhpReturn) && !(last instanceof PhpThrow))) {
                        types.add(Types.strNull);
                    }
                }
            }
        };
    }

    private class DeclareReturnTypeFix implements LocalQuickFix {
        final private String type;

        @NotNull
        @Override
        public String getName() {
            return "Declare the return type";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        DeclareReturnTypeFix(@NotNull String type) {
            this.type = type;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression) {
                final Method method = (Method) expression.getParent();
                final PsiElement body
                        = method.isAbstract() ? method.getLastChild() : ExpressionSemanticUtil.getGroupStatement(method);
                if (null != body) {
                    PsiElement injectionPoint = body.getPrevSibling();
                    if (injectionPoint instanceof PsiWhiteSpace) {
                        injectionPoint = injectionPoint.getPrevSibling();
                    }
                    if (null != injectionPoint) {
                        final Function donor = PhpPsiElementFactory.createFunction(project, "function(): " + type + "{}");
                        PsiElement implant   = donor.getReturnType();
                        while (null != implant && PhpTokenTypes.chRPAREN != implant.getNode().getElementType()) {
                            injectionPoint.getParent().addAfter(implant, injectionPoint);
                            implant = implant.getPrevSibling();
                        }
                    }
                }
            }
        }
    }
}
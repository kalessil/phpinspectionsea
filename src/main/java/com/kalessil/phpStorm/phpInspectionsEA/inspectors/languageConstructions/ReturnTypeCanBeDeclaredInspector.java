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

import java.util.Collection;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReturnTypeCanBeDeclaredInspector extends BasePhpInspection {
    private static final String messagePattern = "%s can be declared as return type hint";

    private static final Collection<String> returnTypes = new HashSet<>();
    private static final Collection<String> voidTypes   = new HashSet<>();

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
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(final Method method) {
                final Project          project       = problemsHolder.getProject();
                final PhpLanguageLevel languageLevel = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();

                if (languageLevel.hasFeature(PhpLanguageFeature.RETURN_TYPES) && (method.getReturnType() == null)) {
                    final PsiElement methodNameNode   = NamedElementUtil.getNameIdentifier(method);
                    final boolean    isCommonFunction = !method.getName().startsWith("__");

                    if (isCommonFunction && (methodNameNode != null)) {
                        final boolean supportNullableTypes = languageLevel.hasFeature(PhpLanguageFeature.NULLABLES);

                        if (method.isAbstract()) {
                            if (method.getDocComment() != null) {
                                handleMethod(method, methodNameNode, supportNullableTypes);
                            }
                        }
                        else {
                            handleMethod(method, methodNameNode, supportNullableTypes);
                        }
                    }
                }
            }

            private void handleMethod(@NotNull final Method method, @NotNull final PsiElement target, final boolean supportNullableTypes) {
                /* ignore DocBlock, resolve and normalize types instead (DocBlock is involved, but nevertheless) */
                final Collection<String> normalizedTypes = new HashSet<>();

                for (final String type : method.getType().global(problemsHolder.getProject()).filterUnknown().getTypes()) {
                    normalizedTypes.add(Types.getType(type));
                }

                checkNonImplicitNullReturn(method, normalizedTypes);

                final int typesCount = normalizedTypes.size();

                /* case 1: offer using void */
                if ((typesCount == 0) && supportNullableTypes) {
                    final String suggestedType = Types.strVoid;
                    final String message       = String.format(messagePattern, suggestedType);

                    problemsHolder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                }

                /* case 2: offer using type */
                if (typesCount == 1) {
                    final String singleType    = normalizedTypes.iterator().next();
                    final String suggestedType = voidTypes.contains(singleType) ? Types.strVoid : compactType(singleType, method);

                    final boolean isLegitBasic = singleType.startsWith("\\") || returnTypes.contains(singleType);
                    final boolean isLegitVoid  = supportNullableTypes && suggestedType.equals(Types.strVoid);

                    if (isLegitBasic || isLegitVoid) {
                        final String message = String.format(messagePattern, suggestedType);
                        problemsHolder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                    }
                }

                /* case 3: offer using nullable type */
                if ((typesCount == 2) && supportNullableTypes && normalizedTypes.contains(Types.strNull)) {
                    normalizedTypes.remove(Types.strNull);

                    final String nullableType  = normalizedTypes.iterator().next();
                    final String suggestedType = voidTypes.contains(nullableType) ? Types.strVoid : compactType(nullableType, method);

                    final boolean isLegitNullable = nullableType.startsWith("\\") || returnTypes.contains(nullableType);
                    final boolean isLegitVoid     = suggestedType.equals(Types.strVoid);

                    if (isLegitNullable || isLegitVoid) {
                        final String typeHint = isLegitVoid ? suggestedType : ('?' + suggestedType);
                        final String message  = String.format(messagePattern, typeHint);

                        problemsHolder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(typeHint));
                    }
                }
            }

            private String compactType(@NotNull final String type, @NotNull final PhpClassMember method) {
                String compacted = type;
                if (type.startsWith("\\")) {
                    final PhpClass clazz     = method.getContainingClass();
                    final String   nameSpace = (clazz == null) ? null : clazz.getNamespaceName();

                    if ((nameSpace != null) && (nameSpace.length() > 1) && type.startsWith(nameSpace)) {
                        compacted = compacted.replace(nameSpace, "");
                    }
                }
                return compacted;
            }

            private void checkNonImplicitNullReturn(@NotNull final Method method, @NotNull final Collection<String> types) {
                if (method.isAbstract() ||
                    types.isEmpty() ||
                    types.contains(Types.strNull) ||
                    types.contains(Types.strVoid)) {
                    return;
                }

                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                final PsiElement     last = (body == null) ? null : ExpressionSemanticUtil.getLastStatement(body);

                if ((last == null) || (!(last instanceof PhpReturn) && !(last instanceof PhpThrow))) {
                    types.add(Types.strNull);
                }
            }
        };
    }

    private static class DeclareReturnTypeFix implements LocalQuickFix {
        private final String type;

        DeclareReturnTypeFix(@NotNull final String type) {
            this.type = type;
        }

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

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();

            if (expression == null) {
                return;
            }

            final Method     method = (Method) expression.getParent();
            final PsiElement body   = method.isAbstract() ? method.getLastChild() : ExpressionSemanticUtil.getGroupStatement(method);

            if (body == null) {
                return;
            }

            PsiElement injectionPoint = body.getPrevSibling();

            if (injectionPoint instanceof PsiWhiteSpace) {
                injectionPoint = injectionPoint.getPrevSibling();
            }

            if (injectionPoint != null) {
                final Function donor   = PhpPsiElementFactory.createFunction(project, "function(): " + type + "{}");
                PsiElement     implant = donor.getReturnType();

                while ((implant != null) && (implant.getNode().getElementType() != PhpTokenTypes.chRPAREN)) {
                    injectionPoint.getParent().addAfter(implant, injectionPoint);
                    implant = implant.getPrevSibling();
                }
            }
        }
    }
}

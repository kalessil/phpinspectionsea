package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.intentions.PhpImportClassIntention;
import com.jetbrains.php.lang.intentions.PhpSimplifyFQNIntention;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    public boolean optionSimplifyFQN = true;

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
            @Override
            public void visitPhpMethod(final Method method) {
                handleFunction(method);
            }

            @Override
            public void visitPhpFunction(final Function function) {
                handleFunction(function);
            }

            private void handleFunction(@NotNull final Function function) {
                if (function.getReturnType() != null) {
                    return;
                }

                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(function);

                if (nameIdentifier == null) {
                    return;
                }

                final boolean isMethod      = function instanceof Method;
                final boolean isMagicMethod = isMethod && function.getName().startsWith("__");

                if (isMagicMethod) {
                    return;
                }

                if (isMethod && ((Method) function).isAbstract() && (function.getDocComment() == null)) {
                    return;
                }

                /* ignore DocBlock, resolve and normalize types instead (DocBlock is involved, but nevertheless) */
                final Collection<String> normalizedTypes = new HashSet<>();
                final Set<String>        supportedTypes  = function.getType().global(problemsHolder.getProject()).filterUnknown().getTypes();

                if (supportedTypes.size() > 2) {
                    return;
                }

                for (final String type : supportedTypes) {
                    normalizedTypes.add(Types.getType(type));
                }

                if (checkIfReturnsNullImplicitly(function, normalizedTypes)) {
                    normalizedTypes.add(Types.strNull);
                }

                final Project          project       = problemsHolder.getProject();
                final PhpLanguageLevel languageLevel = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();

                final boolean supportNullableTypes = languageLevel.hasFeature(PhpLanguageFeature.NULLABLES);
                final int     typesCount           = normalizedTypes.size();

                if ((typesCount == 0) && supportNullableTypes) {
                    /* case 1: offer using void */
                    final String suggestedType = Types.strVoid;
                    final String message       = String.format(messagePattern, suggestedType);

                    problemsHolder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                }
                else if (typesCount == 1) {
                    /* case 2: offer using type */
                    final String singleType    = normalizedTypes.iterator().next();
                    final String suggestedType = voidTypes.contains(singleType) ? Types.strVoid : compactType(singleType, function);

                    final boolean isLegitBasic = singleType.startsWith("\\") || returnTypes.contains(singleType);
                    final boolean isLegitVoid  = supportNullableTypes && suggestedType.equals(Types.strVoid);

                    if (isLegitBasic || isLegitVoid) {
                        final String message = String.format(messagePattern, suggestedType);
                        problemsHolder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(suggestedType));
                    }
                }
                else if ((typesCount == 2) && supportNullableTypes && normalizedTypes.contains(Types.strNull)) {
                    /* case 3: offer using nullable type */
                    normalizedTypes.remove(Types.strNull);

                    final String nullableType  = normalizedTypes.iterator().next();
                    final String suggestedType = voidTypes.contains(nullableType) ? Types.strVoid : compactType(nullableType, function);

                    final boolean isLegitNullable = nullableType.startsWith("\\") || returnTypes.contains(nullableType);
                    final boolean isLegitVoid     = suggestedType.equals(Types.strVoid);

                    if (isLegitNullable || isLegitVoid) {
                        final String typeHint = isLegitVoid ? suggestedType : ('?' + suggestedType);
                        final String message  = String.format(messagePattern, typeHint);

                        problemsHolder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DeclareReturnTypeFix(typeHint));
                    }
                }
            }

            private String compactType(@NotNull final String type, @NotNull final PsiElement function) {
                if (type.startsWith("\\")) {
                    if (function instanceof Method) {
                        final PhpClass clazz     = ((Method) function).getContainingClass();
                        final String   nameSpace = (clazz == null) ? null : clazz.getNamespaceName();

                        if ((nameSpace != null) && (nameSpace.length() > 1) && type.startsWith(nameSpace)) {
                            return type.replace(nameSpace, "");
                        }
                    }

                    final Collection<PhpUse> useList = PsiTreeUtil.findChildrenOfType(function.getContainingFile(), PhpUse.class);

                    for (final PhpUse useItem : useList) {
                        final PhpReference useReference = useItem.getTargetReference();

                        if ((useReference instanceof ClassReference) &&
                            Objects.equals(useReference.getFQN(), type)) {
                            final String useAlias = useItem.getAliasName();

                            if (useAlias != null) {
                                return useAlias;
                            }

                            return useReference.getName();
                        }
                    }
                }

                return type;
            }

            private boolean checkIfReturnsNullImplicitly(@NotNull final PsiElement function, @NotNull final Collection<String> types) {
                if ((function instanceof Method) && ((Method) function).isAbstract()) {
                    return false;
                }

                if (types.isEmpty() ||
                    types.contains(Types.strNull) ||
                    types.contains(Types.strVoid)) {
                    return false;
                }

                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);

                if (body == null) {
                    return false;
                }

                final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);

                return (last == null) ||
                       (!(last instanceof PhpReturn) && !(last instanceof PhpThrow));
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Simplify FQN automatically", optionSimplifyFQN, (isSelected) -> optionSimplifyFQN = isSelected);
        });
    }

    private class DeclareReturnTypeFix implements LocalQuickFix {
        private final String type;

        DeclareReturnTypeFix(@NotNull final String type) {
            this.type = type;
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare " + type + " as the return type";
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

            final PsiElement function = expression.getParent();
            final PsiElement body = ((function instanceof Method) && ((Method) function).isAbstract())
                                    ? function.getLastChild()
                                    : ExpressionSemanticUtil.getGroupStatement(function);

            if (body == null) {
                return;
            }

            PsiElement injectionPoint = body.getPrevSibling();

            if (injectionPoint instanceof PsiWhiteSpace) {
                injectionPoint = injectionPoint.getPrevSibling();
            }

            if (injectionPoint != null) {
                PsiElement       classReference       = PhpPsiElementFactory.createPhpPsiFromText(project, ClassReference.class, "function f(): " + type);
                final PsiElement injectionPointParent = injectionPoint.getParent();

                while (true) {
                    injectionPointParent.addAfter(classReference, injectionPoint);
                    classReference = classReference.getPrevSibling();

                    if ((classReference == null) ||
                        Objects.equals(classReference.getText(), PhpTokenTypes.chRPAREN.toString())) {
                        break;
                    }
                }

                if (optionSimplifyFQN && (type.indexOf('\\', 1) > -1)) {
                    final PsiElement returnType = ((Function) function).getReturnType();

                    if (returnType == null) {
                        return;
                    }

                    final PsiElementBaseIntentionAction simplifyFQNIntention = new PhpSimplifyFQNIntention();

                    if (simplifyFQNIntention.isAvailable(project, null, returnType)) {
                        simplifyFQNIntention.invoke(project, null, returnType);
                        return;
                    }

                    final PsiElementBaseIntentionAction importClassIntention = new PhpImportClassIntention();

                    if (importClassIntention.isAvailable(project, null, returnType)) {
                        importClassIntention.invoke(project, null, returnType);
                    }
                }
            }
        }
    }
}

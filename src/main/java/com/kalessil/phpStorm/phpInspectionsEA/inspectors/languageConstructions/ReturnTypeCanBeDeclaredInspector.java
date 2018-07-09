package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReturnTypeCanBeDeclaredInspector extends BasePhpInspection {
    private static final String messagePattern = "': %t%' can be declared as return type hint%n%.";

    // Inspection options.
    public boolean LOOKUP_PHPDOC_RETURN_DECLARATIONS = true;

    private static final Set<String> returnTypes  = new HashSet<>();
    private static final Set<String> voidTypes    = new HashSet<>();
    private static final Set<String> magicMethods = new HashSet<>();
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

        magicMethods.add("__construct");
        magicMethods.add("__destruct");
        magicMethods.add("__call");
        magicMethods.add("__callStatic");
        magicMethods.add("__get");
        magicMethods.add("__set");
        magicMethods.add("__isset");
        magicMethods.add("__unset");
        magicMethods.add("__sleep");
        magicMethods.add("__wakeup");
        magicMethods.add("__toString");
        magicMethods.add("__invoke");
        magicMethods.add("__set_state");
        magicMethods.add("__clone");
        magicMethods.add("__debugInfo");
    }

    @NotNull
    public String getShortName() {
        return "ReturnTypeCanBeDeclaredInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* TODO: support functions - see https://github.com/kalessil/phpinspectionsea/pull/320 */

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.RETURN_TYPES) && !magicMethods.contains(method.getName())) {
                    final boolean isTarget = OpenapiElementsUtil.getReturnType(method) == null;
                    if (isTarget) {
                        final PsiElement methodNameNode = NamedElementUtil.getNameIdentifier(method);
                        if (methodNameNode != null) {
                            final boolean supportNullableTypes = php.hasFeature(PhpLanguageFeature.NULLABLES);
                            if (method.isAbstract()) {
                                handleAbstractMethod(method, methodNameNode, supportNullableTypes);
                            } else {
                                handleMethod(method, methodNameNode, supportNullableTypes);
                            }
                        }
                    }
                }
            }

            private void handleAbstractMethod(@NotNull Method method, @NotNull PsiElement target, boolean supportNullableTypes) {
                final PhpDocComment docBlock = method.getDocComment();
                if (docBlock != null && docBlock.getReturnTag() != null) {
                    handleMethod(method, target, supportNullableTypes);
                }
            }

            private void handleMethod(@NotNull Method method, @NotNull PsiElement target, boolean supportNullableTypes) {
                /* suggest nothing when the type is only partially resolved */
                final PhpType resolvedReturnType = OpenapiResolveUtil.resolveType(method, holder.getProject());
                if (resolvedReturnType == null) {
                    return;
                } else if (resolvedReturnType.hasUnknown()) {
                    /* adding class interface leading to promise-type for interface method */
                    boolean isInfluencedByInterface = resolvedReturnType.size() == 2 && resolvedReturnType.filterUnknown().size() == 1;
                    if (!isInfluencedByInterface) {
                        return;
                    }
                }

                /* ignore DocBlock, resolve and normalize types instead (DocBlock is involved, but nevertheless) */
                final Set<String> normalizedTypes = resolvedReturnType.filterUnknown().getTypes().stream().
                        map(Types::getType).collect(Collectors.toSet());
                this.checkUnrecognizedGenerator(method, normalizedTypes);
                this.checkReturnStatements(method, normalizedTypes);

                final int typesCount = normalizedTypes.size();
                /* case 1: offer using void */
                if (supportNullableTypes && typesCount == 0) {
                    final PsiElement firstReturn = PsiTreeUtil.findChildOfType(method, PhpReturn.class);
                    if (firstReturn == null || ExpressionSemanticUtil.getScope(firstReturn) != method) {
                        final LocalQuickFix fixer = this.isMethodOverridden(method) ? null : new DeclareReturnTypeFix(Types.strVoid);
                        final String message      = messagePattern
                                .replace("%t%", Types.strVoid)
                                .replace("%n%", fixer == null ? " (please use change signature intention to fix this)" : "");
                        holder.registerProblem(target, message, fixer);
                    }
                }
                /* case 2: offer using type */
                if (1 == typesCount) {
                    final String singleType    = normalizedTypes.iterator().next();
                    final String suggestedType = voidTypes.contains(singleType) ? Types.strVoid : compactType(singleType, method);
                    final boolean isLegitBasic = singleType.startsWith("\\") || returnTypes.contains(singleType) || suggestedType.equals("self");
                    final boolean isLegitVoid  = supportNullableTypes && suggestedType.equals(Types.strVoid);
                    if (isLegitBasic || isLegitVoid) {
                        final LocalQuickFix fixer = this.isMethodOverridden(method) ? null : new DeclareReturnTypeFix(suggestedType);
                        final String message      = messagePattern
                            .replace("%t%", suggestedType)
                            .replace("%n%", fixer == null ? " (please use change signature intention to fix this)" : "")
                        ;
                        holder.registerProblem(target, message, fixer);
                    }
                }
                /* case 3: offer using nullable type */
                if (supportNullableTypes && 2 == typesCount && normalizedTypes.contains(Types.strNull)) {
                    normalizedTypes.remove(Types.strNull);

                    final String nullableType = normalizedTypes.iterator().next();
                    final String suggestedType
                        = voidTypes.contains(nullableType) ? Types.strVoid : compactType(nullableType, method);

                    final boolean isLegitNullable = nullableType.startsWith("\\") || returnTypes.contains(nullableType) || suggestedType.equals("self");
                    final boolean isLegitVoid     = suggestedType.equals(Types.strVoid);
                    if (isLegitNullable || isLegitVoid) {
                        final String typeHint     = isLegitVoid ? suggestedType : '?' + suggestedType;
                        final LocalQuickFix fixer = this.isMethodOverridden(method) ? null : new DeclareReturnTypeFix(typeHint);
                        final String message      = messagePattern
                            .replace("%t%", typeHint)
                            .replace("%n%", fixer == null ? " (please use change signature intention to fix this)" : "")
                        ;
                        holder.registerProblem(target, message, fixer);
                    }
                }
            }

            /* use change signature intention promoter */
            private boolean isMethodOverridden(@NotNull Method method) {
                boolean result       = false;
                final PhpClass clazz = method.getContainingClass();
                if (clazz != null && !clazz.isFinal() && !method.isFinal() && !method.getAccess().isPrivate()) {
                    final String methodName = method.getName();
                    result =
                        InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream()
                                .anyMatch(c -> c != clazz && c.findOwnMethodByName(methodName) != null) ||
                        OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), PhpIndex.getInstance(method.getProject())).stream()
                                .anyMatch(c -> c.findOwnMethodByName(methodName) != null);
                }
                return result;
            }

            @NotNull
            private String compactType(@NotNull String type, @NotNull Method method) {
                String result = null;
                if (type.startsWith("\\") || type.equals("static")) { /* PS 2016.2 resolves self as static */
                    /* Strategy 1: respect `@return self` */
                    if (LOOKUP_PHPDOC_RETURN_DECLARATIONS) {
                        final PhpDocComment phpDoc      = method.getDocComment();
                        final PhpDocReturnTag phpReturn = phpDoc == null ? null : phpDoc.getReturnTag();
                        final boolean hasSelfReference  = PsiTreeUtil.findChildrenOfType(phpReturn, PhpDocType.class).stream()
                                .anyMatch(t -> {
                                    final String text = t.getText();
                                    return text.equals("self") || text.equals("$this");
                                });
                        if (hasSelfReference) {
                            result = "self";
                        }
                    }
                    /* be sure to send back static for avoiding false-positives */
                    result = (result == null && type.equals("static")) ? type : result;
                    /* Strategy 2: scan imports */
                    if (result == null) {
                        PsiElement groupCandidate = method.getContainingClass();
                        groups:
                        while (groupCandidate != null && !(groupCandidate instanceof PsiFile)) {
                            if (groupCandidate instanceof GroupStatement) {
                                final List<PhpUse> imports = new ArrayList<>();
                                /* scan for imports in current group statement */
                                for (final PsiElement child : groupCandidate.getChildren()) {
                                    if (child instanceof PhpUseList) {
                                        imports.addAll(PsiTreeUtil.findChildrenOfType(child, PhpUse.class));
                                    }
                                }
                                /* iterate imports and search for targets */
                                for (final PhpUse imported : imports) {
                                    final PhpReference useReference = imported.getTargetReference();
                                    if (useReference instanceof ClassReference && type.equals(useReference.getFQN())) {
                                        final String useAlias = imported.getAliasName();
                                        result                = useAlias == null ? useReference.getName() : useAlias;
                                        imports.clear();
                                        break groups;
                                    }
                                }
                                imports.clear();
                            }
                            groupCandidate = groupCandidate.getParent();
                        }
                    }
                    /* Strategy 3: relative QN for classes in sub-namespace */
                    if (result == null || result.isEmpty()) {
                        final PhpClass clazz   = method.getContainingClass();
                        final String nameSpace = null == clazz ? null : clazz.getNamespaceName();
                        if (nameSpace != null && nameSpace.length() > 1 && type.startsWith(nameSpace)) {
                            result = type.replace(nameSpace, "");
                        }
                    }
                }
                return result == null ? type : result;
            }

            private void checkUnrecognizedGenerator(@NotNull Method method, @NotNull Set<String> types) {
                if (!types.contains("\\Generator")) {
                    final PhpYield yield = PsiTreeUtil.findChildOfType(method, PhpYield.class);
                    if (yield != null && ExpressionSemanticUtil.getScope(yield) == method) {
                        types.add("\\Generator");
                    }
                }
            }

            private void checkReturnStatements(@NotNull Method method, @NotNull Set<String> types) {
                if (!types.isEmpty() && !method.isAbstract()) {
                    /* non-implicit null return: omitted last return statement */
                    if (!types.contains(Types.strNull) && !types.contains(Types.strVoid)) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                        final PsiElement last     = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
                        if (last == null || (!(last instanceof PhpReturn) && !(last instanceof PhpThrow))) {
                            types.add(Types.strNull);
                        }
                    }
                    /* buggy parameter type resolving: no type, but null as default value */
                    if (types.size() == 1 && types.contains(Types.strNull)) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                        if (body != null) {
                            final PhpReturn expression = PsiTreeUtil.findChildOfType(body, PhpReturn.class);
                            if (expression != null) {
                                final PsiElement value = ExpressionSemanticUtil.getReturnValue(expression);
                                if (value != null && !PhpLanguageUtil.isNull(value)) {
                                    types.remove(Types.strNull);
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Respect `@return self`", LOOKUP_PHPDOC_RETURN_DECLARATIONS, (isSelected) -> LOOKUP_PHPDOC_RETURN_DECLARATIONS = isSelected)
        );
    }

    private static final class DeclareReturnTypeFix implements LocalQuickFix {
        private static final String title = "Declare the return type";

        final private String type;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        DeclareReturnTypeFix(@NotNull String type) {
            this.type = type;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof Method) {
                    final Method method   = (Method) parent;
                    final PsiElement body = method.isAbstract() ? method.getLastChild() : ExpressionSemanticUtil.getGroupStatement(method);
                    if (body != null) {
                        PsiElement injectionPoint = body.getPrevSibling();
                        if (injectionPoint instanceof PsiWhiteSpace) {
                            injectionPoint = injectionPoint.getPrevSibling();
                        }
                        if (injectionPoint != null) {
                            final Function donor = PhpPsiElementFactory.createFunction(project, "function(): " + type + "{}");
                            PsiElement implant   = OpenapiElementsUtil.getReturnType(donor);
                            while (implant != null && !OpenapiTypesUtil.is(implant, PhpTokenTypes.chRPAREN)) {
                                injectionPoint.getParent().addAfter(implant, injectionPoint);
                                implant = implant.getPrevSibling();
                            }
                        }
                    }
                }
            }
        }
    }
}
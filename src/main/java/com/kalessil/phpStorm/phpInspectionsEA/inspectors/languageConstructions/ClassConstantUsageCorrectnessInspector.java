package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassConstantUsageCorrectnessInspector extends BasePhpInspection {
    private static final String message = "::class result and the class qualified name are not identical (case mismatch).";

    final private static Set<String> validReferences = new HashSet<>();
    static {
        validReferences.add("self");
        validReferences.add("static");
        validReferences.add("parent");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ClassConstantUsageCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "::class constant usage correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClassConstantReference(@NotNull ClassConstantReference constantReference) {
                final String constantName = constantReference.getName();
                if (constantName != null && constantName.equals("class")) {
                    final PsiElement reference = constantReference.getClassReference();
                    if (reference instanceof ClassReference) {
                        final ClassReference clazz = (ClassReference) reference;
                        final String referencedQn  = reference.getText();
                        final PsiElement resolved  = validReferences.contains(referencedQn) ? null : OpenapiResolveUtil.resolveReference(clazz);
                        if (resolved instanceof PhpClass) {
                            /* the resolved class will accumulate case issue in its FQN */
                            final List<String> variants = this.getVariants(clazz, (PhpClass) resolved);
                            if (! variants.isEmpty()) {
                                if (variants.stream().noneMatch(referencedQn::equals)) {
                                    holder.registerProblem(
                                            reference,
                                            MessagesPresentationUtil.prefixWithEa(message)
                                    );
                                }
                                variants.clear();
                            }
                        }
                    }
                }
            }

            @NotNull
            private List<String> getVariants(@NotNull ClassReference reference, @NotNull PhpClass clazz) {
                final List<String> result           = new ArrayList<>();
                final String referenceText          = reference.getText();
                final String referenceTextLowercase = referenceText.toLowerCase();
                if (referenceText.startsWith("\\")) {
                    /* FQN specified, resolve as we might have case issues there */
                    final Project project               = holder.getProject();
                    final Collection<PhpClass> resolved = PhpIndex.getInstance(project).getClassesByFQN(referenceText);
                    if (!resolved.isEmpty()) {
                        result.add(resolved.iterator().next().getFQN());
                    }
                } else {
                    PhpNamespace namespace = null;
                    PsiElement current     = reference.getParent();
                    while (current != null && !(current instanceof PsiFile)) {
                        if (current instanceof PhpNamespace) {
                            namespace = (PhpNamespace) current;
                            break;
                        }
                        current = current.getParent();
                    }

                    final String classFqn = clazz.getFQN();
                    if (referenceText.contains("\\")) {
                        /* RQN specified, check if resolved class in the same NS */
                        final String NsFqn = namespace == null ? null : namespace.getFQN();
                        if (NsFqn != null && ! NsFqn.equals("\\")) {
                            final String classFqnLowercase = classFqn.toLowerCase();
                            if (
                                classFqnLowercase.startsWith(NsFqn.toLowerCase()) ||
                                classFqnLowercase.endsWith('\\' + referenceTextLowercase)
                            ) {
                                result.add(classFqn.substring(classFqn.length() - referenceText.length()));
                            }
                        }
                        /* RQN specified, check if resolved class in aliased NS */
                        final List<PhpUse> uses = new ArrayList<>();
                        if (namespace != null) {
                            final GroupStatement body      = namespace.getStatements();
                            if (body != null) {
                                Arrays.stream(body.getStatements())
                                        .filter(statement  -> statement instanceof PhpUseList)
                                        .forEach(statement -> Collections.addAll(uses, ((PhpUseList) statement).getDeclarations()));
                            }
                            for (final PhpUse use : uses) {
                                final String alias = use.getAliasName();
                                if (alias != null && referenceTextLowercase.startsWith(alias.toLowerCase())) {
                                    final PhpReference targetReference = use.getTargetReference();
                                    if (targetReference != null) {
                                        result.add(clazz.getFQN().replace(targetReference.getText(), alias).replaceAll("^\\\\", ""));
                                    }
                                }
                            }
                        }
                        uses.clear();
                    } else {
                        final List<PhpUse> uses = new ArrayList<>();
                        if (namespace != null) {
                            /* find imports inside know namespace */
                            final GroupStatement body = namespace.getStatements();
                            if (body != null) {
                                Arrays.stream(body.getStatements())
                                        .filter(statement  -> statement instanceof PhpUseList)
                                        .forEach(statement -> Collections.addAll(uses, ((PhpUseList) statement).getDeclarations()));
                            }
                        } else {
                            final PsiFile file = reference.getContainingFile();
                            if (file instanceof PhpFile) {
                                /* find imports inside a file without namespace */
                                ((PhpFile) file).getTopLevelDefs().values().stream()
                                        .filter(definition  -> definition instanceof PhpUse)
                                        .forEach(definition -> uses.add((PhpUse) definition));
                            } else {
                                /* fallback, the most greedy strategy */
                                uses.addAll(PsiTreeUtil.findChildrenOfType(current, PhpUse.class));
                            }
                        }
                        /* imports (incl. aliases) */
                        for (final PhpUse use : uses) {
                            if (use.getFQN().equalsIgnoreCase(classFqn)) {
                                final String alias    = use.getAliasName();
                                final PsiElement what = use.getFirstChild();
                                if (alias != null) {
                                    /* alias as it is */
                                    result.add(alias);
                                } else if (what instanceof ClassReference) {
                                    /* resolve the imported class, as its the source for correct naming */
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) what);
                                    if (resolved instanceof PhpClass) {
                                        final PhpClass resolvedImport = (PhpClass) resolved;
                                        final boolean importPrecise   = resolvedImport.getFQN().endsWith(what.getText());
                                        if (!importPrecise || !resolvedImport.getName().equals(referenceText)) {
                                            result.add(resolvedImport.getFQN());
                                        }
                                    }
                                }
                            } else {
                                final String alias = use.getAliasName();
                                if (alias != null && alias.equalsIgnoreCase(referenceText)) {
                                    result.add(alias);
                                }
                            }
                        }
                        uses.clear();
                    }
                }
                return result;
            }
        };
    }
}

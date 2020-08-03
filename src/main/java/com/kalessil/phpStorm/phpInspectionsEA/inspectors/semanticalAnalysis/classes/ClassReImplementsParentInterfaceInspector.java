package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ImplementsList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
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

public class ClassReImplementsParentInterfaceInspector extends BasePhpInspection {
    private static final String patternIndirectDuplication = "'%s' is already announced in '%s'.";

    @NotNull
    @Override
    public String getShortName() {
        return "ClassReImplementsParentInterfaceInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Class implements interfaces multiple times";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                final List<ClassReference> implemented = clazz.getImplementsList().getReferenceElements();
                if (!implemented.isEmpty()) {
                    /* resolve own interfaces an maintain relation to original element */
                    final Map<PsiElement, PhpClass> ownInterfaces = new LinkedHashMap<>(implemented.size());
                    for (final ClassReference reference : implemented) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved instanceof PhpClass) {
                            ownInterfaces.put(reference, (PhpClass) resolved);
                        }
                    }
                    implemented.clear();

                    if (!ownInterfaces.isEmpty()) {
                        /* Case: indirect declaration duplication (parent already implements) */
                        final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                        if (parent != null) {
                            final Set<PhpClass> inherited = InterfacesExtractUtil.getCrawlInheritanceTree(parent, false);
                            if (!inherited.isEmpty()) {
                                final Set<PsiElement> processed = new HashSet<>();
                                for (final Map.Entry<PsiElement, PhpClass> entry : ownInterfaces.entrySet()) {
                                    final PhpClass ownInterface = entry.getValue();
                                    if (inherited.contains(ownInterface) && processed.add(entry.getKey())) {
                                        holder.registerProblem(
                                                entry.getKey(),
                                                String.format(MessagesPresentationUtil.prefixWithEa(patternIndirectDuplication), ownInterface.getFQN(), parent.getFQN()),
                                                new TheLocalFix()
                                        );
                                    }
                                }
                                processed.clear();
                                inherited.clear();
                            }
                        }
                        ownInterfaces.clear();
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Remove unnecessary implements entry";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement parent     = expression.getParent();
            if (parent instanceof ImplementsList && !project.isDisposed()) {
                final ImplementsList implementsList   = (ImplementsList) parent;
                final List<ClassReference> references = implementsList.getReferenceElements();
                if (references.size() == 1) {
                    /* drop implements section completely; implementsList.delete() breaks further SCA */
                    expression.delete();                     // <- interface
                    implementsList.getFirstChild().delete(); // <- implements keyword
                } else {
                    final boolean cleanupLeftHand = references.get(0) != expression;
                    PsiElement commaCandidate     = cleanupLeftHand ? expression.getPrevSibling() : expression.getNextSibling();
                    if (commaCandidate instanceof PsiWhiteSpace) {
                        commaCandidate = cleanupLeftHand ? commaCandidate.getPrevSibling() : commaCandidate.getNextSibling();
                    }

                    /* drop single implements entry from the list */
                    expression.delete();
                    commaCandidate.delete();
                }
            }
        }
    }
}
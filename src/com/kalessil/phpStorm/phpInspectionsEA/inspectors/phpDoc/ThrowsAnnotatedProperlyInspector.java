package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class ThrowsAnnotatedProperlyInspector extends BasePhpInspection {
    private static final String strProblemMissing       = "Implicitly thrown exception is not handled/annotated: '%c%'";
    private static final String strProblemInternalCalls = "Nested call's exception is not handled/annotated: '%c%'";
    private static final String strProblemViolates      = "Implicitly thrown exception violates @inheritdoc (contract interface violation): '%c%'";

    @NotNull
    public String getShortName() {
        return "ThrowsAnnotatedProperlyInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if (StringUtil.isEmpty(strMethodName) || null == objMethodName) {
                    return;
                }

                PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }
                String strClassFQN = clazz.getFQN();
                /* skip un-explorable and test classes */
                if (
                    StringUtil.isEmpty(strClassFQN) ||
                    strClassFQN.contains("\\Tests\\") || strClassFQN.contains("\\Test\\") ||
                    strClassFQN.endsWith("Test")
                ) {
                    return;
                }

                // find all throw statements
                HashSet<String> notCoveredThrows = new HashSet<String>();
                Collection<PhpThrow> throwStatements = PsiTreeUtil.findChildrenOfType(method, PhpThrow.class);
                if (throwStatements.size() > 0) {
                    HashSet<String> declared = new HashSet<String>();
                    /** if no doc-block defined, don't bother people */
                    ThrowsResolveUtil.ResolveType resolvingStatus = ThrowsResolveUtil.resolveThrownExceptions(method, declared);
                    if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == resolvingStatus) {
                        throwStatements.clear();
                        declared.clear();
                        return;
                    }

                    final boolean isInheritDoc = resolvingStatus ==ThrowsResolveUtil.ResolveType.RESOLVED_INHERIT_DOC;

                    // process all throw new ... expressions
                    for (PhpThrow throwStatement : throwStatements) {
                        PhpPsiElement what = throwStatement.getFirstPsiChild();
                        if (what instanceof NewExpression) {
                            ClassReference whatsClass = ((NewExpression) what).getClassReference();
                            if (null != whatsClass && whatsClass.resolve() instanceof PhpClass) {
                                // match FQNs
                                PhpClass resolved = (PhpClass) whatsClass.resolve();
                                String strResolvedFQN = resolved.getFQN();

                                /* if not yet reported and not declared */
                                if (!StringUtil.isEmpty(strResolvedFQN) && !notCoveredThrows.contains(strResolvedFQN) && !declared.contains(strResolvedFQN)) {
                                    /* no direct match, lookup parent exceptions covered */
                                    boolean isParentCovered = false;
                                    HashSet<PhpClass> resolvedParents = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(resolved);
                                    for (PhpClass resolvedParent : resolvedParents) {
                                        String strResolvedParentFQN = resolvedParent.getFQN();

                                        /* if parent already reported or declared */
                                        if (notCoveredThrows.contains(strResolvedParentFQN) || declared.contains(strResolvedParentFQN)) {
                                            isParentCovered = true;
                                            break;
                                        }
                                    }
                                    resolvedParents.clear();

                                    /* report if even parent exceptions not covered */
                                    if (!isParentCovered) {
                                        notCoveredThrows.add(strResolvedFQN);
                                    }
                                }
                            }
                        }
                    }
                    throwStatements.clear();
                    declared.clear();

                    /* report for all violations at once */
                    if (notCoveredThrows.size() > 0) {
                        String strError = isInheritDoc ? strProblemViolates : strProblemMissing;
                        ProblemHighlightType highlight = isInheritDoc ?
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING :
                                ProblemHighlightType.WEAK_WARNING;

                        strError = strError.replace("%c%", notCoveredThrows.toString());
                        holder.registerProblem(objMethodName, strError, highlight);
                    }
                }

                /* heavy analysis with resolving all calls inside the method */
                if (!isOnTheFly) {
                    /* check what internal calls can bring runtime */
                    HashSet<String> possible = new HashSet<String>();
                    CollectPossibleThrowsUtil.collectAnnotatedExceptions(method, possible);

                    /* once more obtain declared, overhead but fine for background analysis */
                    HashSet<String> declared = new HashSet<String>();
                    ThrowsResolveUtil.ResolveType resolvingStatus = ThrowsResolveUtil.resolveThrownExceptions(method, declared);
                    if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == resolvingStatus) {
                        declared.clear();
                        possible.clear();
                        return;
                    }

                    /* check possible - declared are covered properly */
                    PhpIndex index = PhpIndex.getInstance(holder.getProject());
                    HashSet<String> notCovered = new HashSet<String>();
                    for (String onePossible : possible) {

                        /* if not reported and not declared */
                        if (!notCoveredThrows.contains(onePossible) && !declared.contains(onePossible)) {
                            boolean isParentCovered = false;

                            /* no direct match, lookup parent exceptions covered */
                            Collection<PhpClass> possibleClasses = index.getClassesByFQN(onePossible);
                            if (possibleClasses.size() > 0) {
                                PhpClass firsPossibleClass = possibleClasses.iterator().next();
                                HashSet<PhpClass> resolvedParents = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(firsPossibleClass);
                                for (PhpClass resolvedParent : resolvedParents) {
                                    String strResolvedParentFQN = resolvedParent.getFQN();

                                    /* if parent declared or reported already */
                                    if (notCoveredThrows.contains(strResolvedParentFQN) || declared.contains(strResolvedParentFQN)) {
                                        isParentCovered = true;
                                        break;
                                    }
                                }
                                resolvedParents.clear();
                            }

                            if (!isParentCovered) {
                                notCovered.add(onePossible);
                            }
                        }
                    }
                    declared.clear();
                    possible.clear();

                    /* report all nested once at once */
                    if (notCovered.size() > 0) {
                        String strError = strProblemInternalCalls.replace("%c%", notCovered.toString());
                        holder.registerProblem(objMethodName, strError, ProblemHighlightType.WEAK_WARNING);

                        notCovered.clear();
                    }
                }

                notCoveredThrows.clear();
            }
        };
    }
}
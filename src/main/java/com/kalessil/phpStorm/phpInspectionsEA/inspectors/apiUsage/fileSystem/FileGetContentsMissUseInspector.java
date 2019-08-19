package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FileGetContentsMissUseInspector extends PhpInspection {
    private static final String messagePattern = "'%s' would consume less cpu and memory resources here.";

    private static final HashMap<String, String> functionsMapping = new HashMap<>();
    static {
        functionsMapping.put("md5", "md5_file");
        functionsMapping.put("sha1", "sha1_file");
        functionsMapping.put("hash", "hash_file");
        functionsMapping.put("hash_hmac", "hash_hmac_file");
        functionsMapping.put("file_put_contents", "copy");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "FileGetContentsMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'file_get_contents(...)' misused";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("file_get_contents")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        PsiElement parent = reference.getParent();
                        /* inner call can be silenced, un-wrap it */
                        if (parent instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) parent;
                            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opSILENCE)) {
                                parent = unary.getParent();
                            }
                        }
                        if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                final FunctionReference outerCall = (FunctionReference) grandParent;
                                final String outerName            = outerCall.getName();
                                if (outerName != null && functionsMapping.containsKey(outerName)) {
                                    final Set<String> sources = ExpressionSemanticUtil.resolveAsString(arguments[0]);
                                    if (sources.size() == 0 || sources.stream().noneMatch(s -> s.startsWith("php://"))) {
                                        if (outerName.equals("file_put_contents")) {
                                            final PsiElement[] outerArguments = outerCall.getParameters();
                                            if (outerArguments.length == 2) {
                                                final String replacement = String.format(
                                                        "%s%s(%s, %s)",
                                                        outerCall.getImmediateNamespaceName(),
                                                        functionsMapping.get(outerName),
                                                        arguments[0].getText(),
                                                        outerArguments[0].getText()
                                                );
                                                holder.registerProblem(
                                                        outerCall,
                                                        String.format(messagePattern, replacement),
                                                        new UseCopyFix(replacement)
                                                );
                                            }
                                        } else if (outerName.equals("hash")) {
                                            final PsiElement[] outerArguments = outerCall.getParameters();
                                            if (outerArguments.length == 2) {
                                                final String replacement = String.format(
                                                        "%s%s(%s, %s)",
                                                        outerCall.getImmediateNamespaceName(),
                                                        functionsMapping.get(outerName),
                                                        outerArguments[0].getText(),
                                                        arguments[0].getText()
                                                );
                                                holder.registerProblem(
                                                        outerCall,
                                                        String.format(messagePattern, replacement),
                                                        new UseFileHashFix(replacement)
                                                );
                                            }
                                        } else if (outerName.equals("hash_hmac")) {
                                            final PsiElement[] outerArguments = outerCall.getParameters();
                                            if (outerArguments.length == 3) {
                                                final String replacement = String.format(
                                                        "%s%s(%s, %s, %s)",
                                                        outerCall.getImmediateNamespaceName(),
                                                        functionsMapping.get(outerName),
                                                        outerArguments[0].getText(),
                                                        arguments[0].getText(),
                                                        outerArguments[2].getText()
                                                );
                                                holder.registerProblem(
                                                        outerCall,
                                                        String.format(messagePattern, replacement),
                                                        new UseFileHashFix(replacement)
                                                );
                                            }
                                        } else {
                                            final String replacement = String.format(
                                                    "%s%s(%s)",
                                                    outerCall.getImmediateNamespaceName(),
                                                    functionsMapping.get(outerName),
                                                    arguments[0].getText()
                                            );
                                            holder.registerProblem(
                                                    outerCall,
                                                    String.format(messagePattern, replacement),
                                                    new UseFileHashFix(replacement)
                                            );
                                        }
                                    }
                                    sources.clear();
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseFileHashFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use file hash function instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseFileHashFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseCopyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use copy(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseCopyFix(@NotNull String expression) {
            super(expression);
        }
    }
}

package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TypoSafeNamingInspector extends PhpInspection {
    // Inspection options.
    public boolean ALLOW_GETTER_SETTER_PAIRS   = true;
    public boolean ALLOW_SINGULAR_PLURAL_PAIRS = true;

    private static final String messagePatternMethod   = "Methods '%s' and '%s' naming is quite similar, consider renaming one for avoiding misuse.";
    private static final String messagePatternProperty = "Properties '%s' and '%s' naming is quite similar, consider renaming one for avoiding misuse.";

    @NotNull
    @Override
    public String getShortName() {
        return "TypoSafeNamingInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Typo safe naming";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                if (! clazz.isAnonymous() && ! this.isTestContext(clazz)) {
                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                    if (nameNode != null) {
                        this.analyzeMethods(clazz, Arrays.asList(clazz.getOwnMethods()), nameNode);
                        this.analyzeFields(clazz, Arrays.asList(clazz.getOwnFields()), nameNode);
                    }
                }
            }

            private void analyzeMethods(@NotNull PhpClass clazz, @NotNull Collection<Method> methods, @NotNull PsiElement nameNode) {
                final String[] names = methods.stream().map(PhpNamedElement::getName).sorted().toArray(String[]::new);
                if (names.length > 1) {
                    final Map<String, Method> mapping = methods.stream().collect(Collectors.toMap(Method::getName, Function.identity(), (m1, m2) -> m1));
                    for (int outer = 0; outer < names.length; ++outer) {
                        for (int inner = outer + 1; inner < names.length; ++inner) {
                            if (StringUtils.getLevenshteinDistance(names[outer], names[inner]) < 2 && ! names[outer].equals(names[inner])) {
                                final Method outerMethod = mapping.get(names[outer]);
                                final Method innerMethod = mapping.get(names[inner]);
                                if (outerMethod.getContainingClass() == clazz || innerMethod.getContainingClass() == clazz) {
                                    long outerMandatory = Arrays.stream(outerMethod.getParameters()).filter(p -> !p.isOptional()).count();
                                    long innerMandatory = Arrays.stream(innerMethod.getParameters()).filter(p -> !p.isOptional()).count();
                                    if (innerMandatory == outerMandatory) {
                                        if (names[outer].replaceAll("^(set|get)", "").equals(names[inner].replaceAll("^(set|get)", ""))) {
                                            if (! ALLOW_GETTER_SETTER_PAIRS) {
                                                holder.registerProblem(
                                                        nameNode,
                                                        ReportingUtil.wrapReportedMessage(String.format(TypoSafeNamingInspector.messagePatternMethod, names[outer], names[inner]))
                                                );
                                            }
                                        } else if (names[outer].replaceAll("s$", "").equals(names[inner].replaceAll("s$", ""))) {
                                            if (! ALLOW_SINGULAR_PLURAL_PAIRS) {
                                                holder.registerProblem(
                                                        nameNode,
                                                        ReportingUtil.wrapReportedMessage(String.format(TypoSafeNamingInspector.messagePatternMethod, names[outer], names[inner]))
                                                );
                                            }
                                        } else {
                                            holder.registerProblem(
                                                    nameNode,
                                                    ReportingUtil.wrapReportedMessage(String.format(TypoSafeNamingInspector.messagePatternMethod, names[outer], names[inner]))
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mapping.clear();
                }
            }

            private void analyzeFields(@NotNull PhpClass clazz, @NotNull Collection<Field> fields, @NotNull PsiElement nameNode) {
                final String[] names = fields.stream().filter(f -> ! f.isConstant()).map(PhpNamedElement::getName).sorted().toArray(String[]::new);
                if (names.length > 1) {
                    final Map<String, Field> mapping = fields.stream().filter(f -> ! f.isConstant()).collect(Collectors.toMap(Field::getName, Function.identity(), (f1, f2) -> f1));
                    for (int outer = 0; outer < names.length; ++outer) {
                        for (int inner = outer + 1; inner < names.length; ++inner) {
                            if (StringUtils.getLevenshteinDistance(names[outer], names[inner]) < 2 && ! names[outer].equals(names[inner])) {
                                final Field outerField = mapping.get(names[outer]);
                                final Field innerField = mapping.get(names[inner]);
                                if (outerField.getContainingClass() == clazz || innerField.getContainingClass() == clazz) {
                                    if (names[outer].replaceAll("s$", "").equals(names[inner].replaceAll("s$", ""))) {
                                        if (! ALLOW_SINGULAR_PLURAL_PAIRS) {
                                            holder.registerProblem(
                                                    nameNode,
                                                    ReportingUtil.wrapReportedMessage(String.format(TypoSafeNamingInspector.messagePatternProperty, names[outer], names[inner]))
                                            );
                                        }
                                    } else {
                                        holder.registerProblem(
                                                nameNode,
                                                ReportingUtil.wrapReportedMessage(String.format(TypoSafeNamingInspector.messagePatternProperty, names[outer], names[inner]))
                                        );
                                    }
                                }
                            }
                        }
                    }
                    mapping.clear();
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Allow 'get.../set...' pattern",  ALLOW_GETTER_SETTER_PAIRS, (isSelected) -> ALLOW_GETTER_SETTER_PAIRS = isSelected);
            component.addCheckbox("Allow '.../...s' pattern",  ALLOW_SINGULAR_PLURAL_PAIRS, (isSelected) -> ALLOW_SINGULAR_PLURAL_PAIRS = isSelected);
        });
    }
}

package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpEchoStatement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;

public class ExpressionSemanticUtilTest extends CodeInsightFixtureTestCase {
    public void testCountExpressionsInGroup() {
        Project project = myFixture.getProject();
        GroupStatement statement;

        String blockWithCommentsOnly = "{ /** dock-block*/ /* comment*/ /** doc-block*/ }";
        statement = PhpPsiElementFactory.createFromText(project, GroupStatement.class, blockWithCommentsOnly);
        assertNotNull(statement);
        assertEquals(0, ExpressionSemanticUtil.countExpressionsInGroup(statement));

        String blockWith2Statements  = "{ /** dock-block*/ echo 1; return 2; /** doc-block*/ }";
        statement = PhpPsiElementFactory.createFromText(project, GroupStatement.class, blockWith2Statements);
        assertNotNull(statement);
        assertEquals(2, ExpressionSemanticUtil.countExpressionsInGroup(statement));
    }

    public void testGetLastStatement() {
        Project project = myFixture.getProject();
        GroupStatement statement;

        String blockWithCommentsOnly = "{ /** dock-block*/ /* comment*/ /** doc-block*/ }";
        statement = PhpPsiElementFactory.createFromText(project, GroupStatement.class, blockWithCommentsOnly);
        assertNotNull(statement);
        assertNull(ExpressionSemanticUtil.getLastStatement(statement));

        String blockWith2Statements  = "{ /** dock-block*/ echo 1; return 2; /** doc-block*/ }";
        statement = PhpPsiElementFactory.createFromText(project, GroupStatement.class, blockWith2Statements);
        assertNotNull(statement);
        PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(statement);
        assertNotNull(lastStatement);
        assertInstanceOf(lastStatement, PhpReturn.class);
    }
}

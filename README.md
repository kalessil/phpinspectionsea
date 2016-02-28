Php Inspections (EA Extended) is a Static Code Analysis tool for PHP, 
distributed as a PhpStorm plugin.

Inspections Lists
---

| Group              | Short Name                                   | Full Name                                           | Quick fix |
| :----------------- | :------------------------------------------- | :-------------------------------------------------- | --------: |
| Type compatibility | IsEmptyFunctionUsageInspection               | 'empty(...)' usage                                  | no |
| Type compatibility | TypeUnsafeComparisonInspection               | Type unsafe comparison                              | no |
| Type compatibility | TypeUnsafeArraySearchInspection              | 'in_array(...)', 'array_search()' type unsafe usage | no |
| Type compatibility | ArrayTypeOfParameterByDefaultValueInspection | Parameter could be declared as array                | no |
| Type compatibility | ForeachSourceInspection                      | Foreach source to iterate over                      | no |
| Type compatibility | GenericObjectTypeUsageInspection             | Usage of object type in PhpDoc                      | no |
| Control flow        | UnSafeIsSetOverArrayInspection              | 'isset(...)' usage                                  | no |
| Control flow        | TernaryOperatorSimplifyInspection           | Ternary operator could be simplified                | no |
| Control flow        | IfReturnReturnSimplificationInspection      | If-return-return could be simplified                | no |
| Control flow        | DefaultValueInElseBranchInspection          | Default value is hidden in else branch              | no |
| Control flow        | NotOptimalIfConditionsInspection            | Non-optimal if conditions                           | no |
| Control flow        | LoopWhichDoesNotLoopInspection              | Loop which does not loop                            | no |
| Control flow        | ThrowRawExceptionInspection                 | General '\Exception' is thrown                      | no |
| Control flow        | DisconnectedForeachInstructionInspection    | Statement could be decoupled from foreach           | no |
| Control flow        | ExceptionsAnnotatingAndHandlingInspection   | Exceptions handling and annotating                  | no |
| Control flow        | DegradedSwitchInspection                    | Switch-case could be simplified                     | no |
| Control flow        | ForeachInvariantsInspection                 | Foreach usage possible                              | no |
| Control flow        | OneTimeUseVariablesInspection               | One-time use variables                              | Prio1 |
| Control flow        | PdoApiUsageInspection                       | PDO api usage                                       | no |
| ---                 | PhpUnitTestsInspection                      | PhpUnit: bugs and best practices                    | no |
| Unused              | OnlyWritesOnParameterInspection             | Parameter/variable is not used                      | no |
| Unused              | AmbiguousMemberInitializationInspection     | Ambiguous class property initialization             | Prio1 |
| Unused              | UselessUnsetInspection                      | Useless unset                                       | Prio1 |
| Compatibility       | AliasFunctionsUsageInspection               | Alias functions usage                               | yes |
| Compatibility       | DeprecatedConstructorStyleInspection        | Deprecated constructor style                        | Prio1 |
| Compatibility       | DeprecatedIniOptionsInspection              | Deprecated configuration options                    | no |
| Compatibility       | RandomApiMigrationInspection                | Random API migration                                | yes |
| Compatibility       | MktimeUsageInspection                       | 'gmmktime()'/'mktime()' usage                       | no |



Confusing constructs
---
shortName="SenselessTernaryOperatorInspection"            displayName="Suspicious ternary operator"
shortName="ClassMethodNameMatchesFieldNameInspection"     displayName="Method name matches existing field name"
shortName="SenselessProxyMethodInspection"                displayName="Senseless proxy function"
shortName="NestedTernaryOperatorInspection"               displayName="Nested ternary operator"
shortName="UselessReturnInspection"                       displayName="Useless return"
shortName="ParameterByRefWithDefaultInspection"           displayName="Parameter passed by reference has default value"

Code style
---
shortName="UnNecessaryDoubleQuotesInspection"             displayName="Unnecessary double quotes"
shortName="IfConditionalsWithoutCurvyBracketsInspection"  displayName="Missing or empty conditionals group statement"
shortName="SenselessCommaInArrayDefinitionInspection"     displayName="Unnecessary last comma in array definition"
shortName="ElvisOperatorCanBeUsedInspection"              displayName="Elvis operator can be used"
shortName="NullCoalescingOperatorCanBeUsedInspection"     displayName="Null coalescing operator can be used"
shortName="AccessModifierPresentedInspection"             displayName="Access modifiers shall be defined"
shortName="PrefixedIncDecrementEquivalentInspection"      displayName="Prefixed increment/decrement equivalent"
shortName="CallableReferenceNameMismatchInspection"       displayName="Callable name case mismatched in a call"
shortName="NestedPositiveIfStatementsInspection"          displayName="Nested positive ifs"
shortName="ReferencingObjectsInspection"                  displayName="Referencing objects"
shortName="StaticInvocationViaThisInspection"             displayName="Static methods invocation via '->'"
shortName="NestedNotOperatorsInspection"                  displayName="Nested not operators usage"
shortName="UnnecessaryParenthesesInspection"              displayName="Unnecessary parentheses"
shortName="UnnecessarySemicolonInspection"                displayName="Unnecessary semicolon"
shortName="UnnecessaryUseAliasInspection"                 displayName="Unnecessary aliases"

Architecture
---
shortName="BadExceptionsProcessingInspection"               displayName="Badly organized exception handling"
shortName="MoreThanThreeArgumentsInspection"                displayName="More than 3 parameters in callable"
shortName="CallableParameterUseCaseInTypeContextInspection" displayName="Callable parameter usage violates definition"
shortName="ClassReImplementsParentInterfaceInspection"      displayName="Class re-implements interface of a parent class"
shortName="ClassOverridesFieldOfSuperClassInspection"       displayName="Class overrides a field of a parent class"
shortName="SingletonFactoryPatternViolationInspection"      displayName="Class violates singleton/factory pattern definition"
shortName="PrivateConstructorSemanticsInspection"           displayName="Private constructor semantics"
shortName="LongInheritanceChainInspection"                  displayName="Long inheritance chain"
shortName="PropertyCanBeStaticInspection"                   displayName="Property could be static"
shortName="EmptyClassInspection"                            displayName="Empty class"
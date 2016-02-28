Php Inspections (EA Extended) is a Static Code Analysis tool for PHP, 
distributed as a PhpStorm plugin.

Inspections Lists (Quick-fixes for next release)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Control flow         | OneTimeUseVariablesInspection                   | One-time use variables                              | Prio1 |
| Unused               | AmbiguousMemberInitializationInspection         | Ambiguous class property initialization             | Prio1 |
| Unused               | UselessUnsetInspection                          | Useless unset                                       | Prio1 |
| Compatibility        | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | Prio1 |
| Confusing constructs | SenselessProxyMethodInspection                  | Senseless proxy function                            | Prio1 |
| Code style           | ReferencingObjectsInspection                    | Referencing objects                                 | Prio1 |
| Code style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | Prio1 |
| Code style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | Prio1 |
| Code style           | UnnecessaryUseAliasInspection                   | Unnecessary aliases                                 | Prio1 |
| Code style           | PrefixedIncDecrementEquivalentInspection        | Prefixed increment/decrement equivalent             | Prio1 |
| Architecture         | ClassReImplementsParentInterfaceInspection      | Class re-implements interface of a parent class     | Prio1 |

Inspections Lists (Type compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Type compatibility   | IsEmptyFunctionUsageInspection                  | 'empty(...)' usage                                  | no |
| Type compatibility   | TypeUnsafeComparisonInspection                  | Type unsafe comparison                              | no |
| Type compatibility   | TypeUnsafeArraySearchInspection                 | 'in_array(...)', 'array_search()' type unsafe usage | no |
| Type compatibility   | ArrayTypeOfParameterByDefaultValueInspection    | Parameter could be declared as array                | no |
| Type compatibility   | ForeachSourceInspection                         | Foreach source to iterate over                      | n/a |
| Type compatibility   | GenericObjectTypeUsageInspection                | Usage of object type in PhpDoc                      | n/a |

Inspections Lists (Control flow)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Control flow         | UnSafeIsSetOverArrayInspection                  | 'isset(...)' usage                                  | no |
| Control flow         | TernaryOperatorSimplifyInspection               | Ternary operator could be simplified                | no |
| Control flow         | IfReturnReturnSimplificationInspection          | If-return-return could be simplified                | no |
| Control flow         | DefaultValueInElseBranchInspection              | Default value is hidden in else branch              | no |
| Control flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                           | no |
| Control flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                            | no |
| Control flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                      | no |
| Control flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach           | no |
| Control flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                  | no |
| Control flow         | DegradedSwitchInspection                        | Switch-case could be simplified                     | no |
| Control flow         | ForeachInvariantsInspection                     | Foreach usage possible                              | no |
| Control flow         | PdoApiUsageInspection                           | PDO api usage                                       | no |

Inspections Lists (Without group)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| ---                  | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | no |

Inspections Lists (Unused)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Unused               | OnlyWritesOnParameterInspection                 | Parameter/variable is not used                      | no |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Compatibility        | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | n/a |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime()'/'mktime()' usage                       | no |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Confusing constructs | SenselessTernaryOperatorInspection              | Suspicious ternary operator                         | n/a |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | no |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | n/a |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | no |
| Confusing constructs | ParameterByRefWithDefaultInspection             | Parameter passed by reference has default value     | no |

Inspections Lists (Code style)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Code style           | UnNecessaryDoubleQuotesInspection               | Unnecessary double quotes                           | yes |
| Code style           | IfConditionalsWithoutCurvyBracketsInspection    | Missing or empty conditionals group statement       | no |
| Code style           | SenselessCommaInArrayDefinitionInspection       | Unnecessary last comma in array definition          | no |
| Code style           | ElvisOperatorCanBeUsedInspection                | Elvis operator can be used                          | yes |
| Code style           | NullCoalescingOperatorCanBeUsedInspection       | Null coalescing operator can be used                | yes |
| Code style           | AccessModifierPresentedInspection               | Access modifiers shall be defined                   | no |
| Code style           | CallableReferenceNameMismatchInspection         | Callable name case mismatched in a call             | yes |
| Code style           | NestedPositiveIfStatementsInspection            | Nested positive ifs                                 | no |
| Code style           | StaticInvocationViaThisInspection               | Static methods invocation via '->'                  | yes |
| Code style           | UnnecessaryParenthesesInspection                | Unnecessary parentheses                             | yes |

Inspections Lists (Architecture)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Architecture         | BadExceptionsProcessingInspection               | Badly organized exception handling                  | n/a |
| Architecture         | MoreThanThreeArgumentsInspection                | More than 3 parameters in callable                  | n/a |
| Architecture         | CallableParameterUseCaseInTypeContextInspection | Callable parameter usage violates definition        | no |
| Architecture         | ClassOverridesFieldOfSuperClassInspection       | Class overrides a field of a parent class           | no |
| Architecture         | SingletonFactoryPatternViolationInspection      | Class violates singleton/factory pattern definition | no |
| Architecture         | PrivateConstructorSemanticsInspection           | Private constructor semantics                       | no |
| Architecture         | LongInheritanceChainInspection                  | Long inheritance chain                              | n/a |
| Architecture         | PropertyCanBeStaticInspection                   | Property could be static                            | no |
| Architecture         | EmptyClassInspection                            | Empty class                                         | n/a |

Inspections Lists (Probable bugs)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Performance          | => To be listed                                 | ---                                                 | --- |

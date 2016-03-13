Php Inspections (EA Extended) is a Static Code Analysis tool for PHP, 
distributed as a PhpStorm plugin.

Inspections Lists (Quick-fixes for next release)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Unused               | AmbiguousMemberInitializationInspection         | Ambiguous class property initialization             | Prio1 |
| Code style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | Prio1 |
| Code style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | Prio1 |
| Code style           | UnnecessaryUseAliasInspection                   | Unnecessary aliases                                 | Prio1 |
| Code style           | PrefixedIncDecrementEquivalentInspection        | Prefixed increment/decrement equivalent             | Prio1 |
| Architecture         | ClassReImplementsParentInterfaceInspection      | Class re-implements interface of a parent class     | Prio1 |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                  | Prio1 |
| Performance          | InArrayMissUseInspection                        | 'in_array(...)' misused                             | Prio1 |

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
| Control flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                           | --- |
| Control flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                            | -- |
| Control flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                      | no |
| Control flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach           | no |
| Control flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                  | no |
| Control flow         | DegradedSwitchInspection                        | Switch-case could be simplified                     | no |
| Control flow         | ForeachInvariantsInspection                     | Foreach usage possible                              | no |
| Control flow         | PdoApiUsageInspection                           | PDO api usage                                       | no |
| Control flow         | OneTimeUseVariablesInspection                   | One-time use variables                              | yes |
| Control flow         | MultiAssignmentUsageInspection                  | 'list(...) = ' usage possible                       | --- |

Inspections Lists (PhpUnit)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| PhpUnit              | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | yes |

Inspections Lists (Unused)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Unused               | OnlyWritesOnParameterInspection                 | Parameter/variable is not used                      | --- |
| Unused               | UselessUnsetInspection                          | Useless unset                                       | --- |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Compatibility        | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | --- |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime()'/'mktime()' usage                       | no  |
| Compatibility        | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | yes |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Confusing constructs | SenselessTernaryOperatorInspection              | Suspicious ternary operator                         | --- |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | --- |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | --- |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | --- |
| Confusing constructs | ParameterByRefWithDefaultInspection             | Parameter passed by reference has default value     | --- |
| Confusing constructs | SenselessProxyMethodInspection                  | Senseless proxy function                            | yes |
| Confusing constructs | ReferencingObjectsInspection                    | Referencing objects                                 | yes |

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
| Code style           | DynamicInvocationViaScopeResolutionInspector    | Dynamic methods invocation via '::'                 | yes |

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
| Group                | Short Name                                      | Full Name                                                 | Quick fix |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --------: |
| Probable bugs        | ForgottenDebugOutputInspection                  | Forgotten debug statements                                | --- |
| Probable bugs        | AdditionOperationOnArraysInspection             | Addition operator used on arrays                          | --- |
| Probable bugs        | MagicMethodsValidityInspection                  | Magic methods validity                                    | --- |
| Probable bugs        | SuspiciousLoopInspection                        | Suspicious loop                                           | --- |
| Probable bugs        | ReferenceMismatchInspection                     | Reference mismatch                                        | --- |
| Probable bugs        | ImplicitMagicMethodCallInspection               | Implicit magic method calls                               | no  |
| Probable bugs        | SwitchContinuationInLoopInspection              | Continue misbehaviour in switch                           | no  |
| Probable bugs        | PrintfScanfArgumentsInspection                  | -printf/-scanf arguments count mismatches                 | --- |
| Probable bugs        | RealpathOnRelativePathsInspection               | Phar-incompatible 'realpath()' applied to a relative path | no  |
| Probable bugs        | OffsetOperationsInspection                      | Array and string offset validity                          | --- |
| Probable bugs        | SummerTimeUnsafeTimeManipulationInspection      | Summer-time unsafe date manipulations                     | --- |
| Probable bugs        | MkdirRaceConditionInspection                    | 'mkdir(...)' race condition                               | --- |
| Probable bugs        | IncorrectRandomRangeInspection                  | Incorrect random generation range                         | --- |
| Probable bugs        | UntrustedInclusionInspection                    | Untrusted files inclusion                                 | --- |
| Probable bugs        | NonSecureUniqidUsageInspection                  | Insecure 'uniqid()' usage                                 | yes |
| Probable bugs        | NonSecureExtractUsageInspection                 | Insecure 'extract(...)' usage                             | --- |
| Probable bugs        | NonSecureParseStrUsageInspection                | Insecure 'parse_str(...)' usage                           | --- |
| Probable bugs        | SuspiciousSemicolonInspection                   | Suspicious semicolon                                      | --- |
| Probable bugs        | InconsistentQueryBuildInspection                | Inconsistent 'http_build_query(...)' result               | yes |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                                                                   | Quick fix |
| :------------------- | :---------------------------------------------- | :------------------------------------------------------------------------------------------ | --------: |
| Performance          | IsNullFunctionUsageInspection                   | 'is_null(...)' could be replaced by 'null === ...'                                          | yes |
| Performance          | dirnameCallOnFileConstantInspection             | 'dirname(...)' could be replaced by '__DIR__'                                               | yes |
| Performance          | AmbiguousMethodsCallsInArrayMappingInspection   | Non-optimized arrays mapping                                                                | --- |
| Performance          | SequentialUnSetCallsInspection                  | 'unset(...)' calls could be merged                                                          | --- |
| Performance          | StrlenInEmptyStringCheckContextInspection       | 'strlen(...)' should not be used to check if string is empty                                | --- |
| Performance          | TypesCastingWithFunctionsInspection             | Type casting via PHP4 functions                                                             | yes |
| Performance          | ArrayCastingEquivalentInspection                | Could be replaced with '(array) ...'                                                        | --- |
| Performance          | CountOnPropelCollectionInspection               | 'count(...)' calls on Propel collection                                                     | --- |
| Performance          | CallableInLoopTerminationConditionInspection    | Callable calls in loops termination condition                                               | --- |
| Performance          | SlowArrayOperationsInLoopInspection             | Slow array function used in loop                                                            | --- |
| Performance          | ArraySearchUsedAsInArrayInspection              | 'array_search(...)' could be replaced by 'in_array(...)'                                    | --- |
| Performance          | StrStrUsedAsStrPosInspection                    | 'strstr(...)/stristr(...)' could be replaced with 'strpos(...)/stripos(...)'                | --- |
| Performance          | StrNcmpUsedAsStrPosInspection                   | 'strncmp(...)/strncasecmp(...)' could be replaced with 'strpos(...)/stripos(...)'           | --- |
| Performance          | SubStrUsedAsStrPosInspection                    | 'substr(...)' could be replaced with 'strpos(...)'                                          | --- |
| Performance          | StrTrUsageAsStrReplaceInspection                | 'strtr(...)' could be replaced with 'str_replace(...)'                                      | --- |
| Performance          | CaseInsensitiveStringFunctionsMissUseInspection | 'stristr(...)/stripos()/strripos()' could be replaced with 'strstr(...)/strpos()/strrpos()' | --- |
| Performance          | OpAssignShortSyntaxInspection                   | Short syntax for applied operation                                                          | --- |
| Performance          | AlterInForeachInspection                        | Slow alter in foreach                                                                       | --- |
| Performance          | ForeachOnArrayComponentsInspection              | 'array_keys(...)/array_values(...)' used as foreach array                                   | --- |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                                                          | --- |
| Performance          | LowPerformanceArrayUniqueUsageInspection        | 'array_unique()' low performing usage                                                       | --- |
| Performance          | ArrayPushMissUseInspection                      | 'array_push(...)' misused"                                                                  | yes |
| Performance          | NotOptimalRegularExpressionsInspection          | Non-optimal regular expression                                                              | --- |
| Performance          | VariableFunctionsUsageInspection                | Variable functions usage                                                                    | --- |

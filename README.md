Php Inspections (EA Extended) is a Static Code Analysis tool for PHP, 
distributed as a PhpStorm plugin.

Some facts about this project: https://www.openhub.net/p/phpinspectionsea

Inspections Lists (Quick-fixes for next release)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Performance          | OpAssignShortSyntaxInspection                   | Short syntax for applied operation                  | Prio1 |
| Code style           | PrefixedIncDecrementEquivalentInspection        | Prefixed increment/decrement equivalent             | Prio1 |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                  | Prio1 |
| Performance          | SequentialUnSetCallsInspection                  | 'unset(...)' calls could be merged                  | Prio1 |

Inspections Lists (Type compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Type compatibility   | IsEmptyFunctionUsageInspection                  | 'empty(...)' usage                                  | no  |
| Type compatibility   | TypeUnsafeComparisonInspection                  | Type unsafe comparison                              | no  |
| Type compatibility   | TypeUnsafeArraySearchInspection                 | 'in_array(...)', 'array_search()' type unsafe usage | n/a |
| Type compatibility   | ForeachSourceInspection                         | Foreach source to iterate over                      | n/a |
| Type compatibility   | GenericObjectTypeUsageInspection                | Usage of object type in PhpDoc                      | n/a |
| Type compatibility   | ArrayTypeOfParameterByDefaultValueInspection    | Parameter could be declared as array                | yes |

Inspections Lists (Control flow)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Control flow         | UnSafeIsSetOverArrayInspection                  | 'isset(...)' usage                                  | no  |
| Control flow         | TernaryOperatorSimplifyInspection               | Ternary operator could be simplified                | no  |
| Control flow         | IfReturnReturnSimplificationInspection          | If-return-return could be simplified                | yes       | yes | n/a  |
| Control flow         | DefaultValueInElseBranchInspection              | Default value is hidden in else branch              | no  |
| Control flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                           | n/a |
| Control flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                            | n/a |
| Control flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                      | no  |
| Control flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach           | n/a |
| Control flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                  | yes |
| Control flow         | DegradedSwitchInspection                        | Switch-case could be simplified                     | n/a |
| Control flow         | ForeachInvariantsInspection                     | Foreach usage possible                              | n/a |
| Control flow         | PdoApiUsageInspection                           | PDO api usage                                       | n/a |
| Control flow         | OneTimeUseVariablesInspection                   | One-time use variables                              | yes |
| Control flow         | MultiAssignmentUsageInspection                  | 'list(...) = ' usage possible                       | no  |
| Control flow         | GetTypeMissUseInspector                         | 'gettype(...)' could be replaced with 'is_*(...)'   | yes |

Inspections Lists (PhpUnit)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| PhpUnit              | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | yes |

Inspections Lists (Unused)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Unused               | OnlyWritesOnParameterInspection                 | Parameter/variable is not used                      | n/a |
| Unused               | UselessUnsetInspection                          | Useless unset                                       | n/a |
| Unused               | AmbiguousMemberInitializationInspection         | Ambiguous class property initialization             | yes |
| Unused               | UnusedConstructorDependenciesInspector          | Unused constructor dependencies                     | n/a |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Compatibility        | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | n/a |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes |
| Compatibility        | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | yes |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime()'/'mktime()' usage                       | yes |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| Confusing constructs | SenselessTernaryOperatorInspection              | Suspicious ternary operator                         | n/a |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | n/a |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | n/a |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | n/a |
| Confusing constructs | ParameterByRefWithDefaultInspection             | Parameter passed by reference has default value     | n/a |
| Confusing constructs | SenselessProxyMethodInspection                  | Senseless proxy function                            | yes |
| Confusing constructs | ReferencingObjectsInspection                    | Referencing objects                                 | yes |

Inspections Lists (Code style)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Code style           | UnNecessaryDoubleQuotesInspection               | Unnecessary double quotes                           | yes |
| Code style           | IfConditionalsWithoutCurvyBracketsInspection    | Missing or empty conditionals group statement       | no  |
| Code style           | SenselessCommaInArrayDefinitionInspection       | Unnecessary last comma in array definition          | no  |
| Code style           | ElvisOperatorCanBeUsedInspection                | Elvis operator can be used                          | yes |
| Code style           | NullCoalescingOperatorCanBeUsedInspection       | Null coalescing operator can be used                | yes |
| Code style           | AccessModifierPresentedInspection               | Access modifiers shall be defined                   | no  |
| Code style           | CallableReferenceNameMismatchInspection         | Callable name case mismatched in a call             | yes |
| Code style           | NestedPositiveIfStatementsInspection            | Nested positive ifs                                 | no  |
| Code style           | StaticInvocationViaThisInspection               | Static methods invocation via '->'                  | yes |
| Code style           | UnnecessaryParenthesesInspection                | Unnecessary parentheses                             | yes       | yes | n/a  |
| Code style           | DynamicInvocationViaScopeResolutionInspector    | Dynamic methods invocation via '::'                 | yes |
| Code style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | yes |
| Code style           | UnnecessaryUseAliasInspection                   | Unnecessary aliases                                 | yes |
| Code style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | yes |

Inspections Lists (Architecture)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Architecture         | BadExceptionsProcessingInspection               | Badly organized exception handling                  | n/a |
| Architecture         | MoreThanThreeArgumentsInspection                | More than 3 parameters in callable                  | n/a |
| Architecture         | CallableParameterUseCaseInTypeContextInspection | Callable parameter usage violates definition        | n/a |
| Architecture         | ClassOverridesFieldOfSuperClassInspection       | Class overrides a field of a parent class           | no  |
| Architecture         | SingletonFactoryPatternViolationInspection      | Class violates singleton/factory pattern definition | n/a |
| Architecture         | PrivateConstructorSemanticsInspection           | Private constructor semantics                       | n/a       | yes | n/a  |
| Architecture         | LongInheritanceChainInspection                  | Long inheritance chain                              | n/a |
| Architecture         | PropertyCanBeStaticInspection                   | Property could be static                            | no  |
| Architecture         | EmptyClassInspection                            | Empty class                                         | n/a |
| Architecture         | ClassReImplementsParentInterfaceInspection      | Class re-implements interface of a parent class     | yes |

Inspections Lists (Probable bugs)
---
| Group                | Short Name                                      | Full Name                                                 | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --------: | --: | ---: |
| Probable bugs        | ForgottenDebugOutputInspection                  | Forgotten debug statements                                | n/a |
| Probable bugs        | AdditionOperationOnArraysInspection             | Addition operator used on arrays                          | n/a |
| Probable bugs        | MagicMethodsValidityInspection                  | Magic methods validity                                    | n/a |
| Probable bugs        | SuspiciousLoopInspection                        | Suspicious loop                                           | n/a |
| Probable bugs        | ReferenceMismatchInspection                     | Reference mismatch                                        | n/a |
| Probable bugs        | ImplicitMagicMethodCallInspection               | Implicit magic method calls                               | yes |
| Probable bugs        | PrintfScanfArgumentsInspection                  | -printf/-scanf arguments count mismatches                 | n/a |
| Probable bugs        | RealpathOnRelativePathsInspection               | Phar-incompatible 'realpath()' applied to a relative path | no  |
| Probable bugs        | OffsetOperationsInspection                      | Array and string offset validity                          | n/a |
| Probable bugs        | SummerTimeUnsafeTimeManipulationInspection      | Summer-time unsafe date manipulations                     | n/a |
| Probable bugs        | MkdirRaceConditionInspection                    | 'mkdir(...)' race condition                               | n/a |
| Probable bugs        | IncorrectRandomRangeInspection                  | Incorrect random generation range                         | n/a |
| Probable bugs        | UntrustedInclusionInspection                    | Untrusted files inclusion                                 | n/a |
| Probable bugs        | NonSecureUniqidUsageInspection                  | Insecure 'uniqid()' usage                                 | yes       | yes | n/a  |
| Probable bugs        | NonSecureExtractUsageInspection                 | Insecure 'extract(...)' usage                             | n/a |
| Probable bugs        | NonSecureParseStrUsageInspection                | Insecure 'parse_str(...)' usage                           | n/a |
| Probable bugs        | SuspiciousSemicolonInspection                   | Suspicious semicolon                                      | n/a |
| Probable bugs        | InconsistentQueryBuildInspection                | Inconsistent 'http_build_query(...)' result               | yes |
| Probable bugs        | SwitchContinuationInLoopInspection              | Continue misbehaviour in switch                           | yes |
| Probable bugs        | CompactArgumentsInspection                      | 'compact(...)' variables existence                        | n/a |
| Probable bugs        | DispatchingThisIntoClosuresInspection           | Incorrect dispatching $this into closures                 | n/a |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                                                                   | Quick fix | UTs | QFTs |
| :------------------- | :---------------------------------------------- | :------------------------------------------------------------------------------------------ | --------: | --: | ---: |
| Performance          | IsNullFunctionUsageInspection                   | 'is_null(...)' could be replaced by 'null === ...'                                          | yes |
| Performance          | dirnameCallOnFileConstantInspection             | 'dirname(...)' could be replaced by '__DIR__'                                               | yes |
| Performance          | AmbiguousMethodsCallsInArrayMappingInspection   | Non-optimized arrays mapping                                                                | n/a |
| Performance          | StrlenInEmptyStringCheckContextInspection       | 'strlen(...)' should not be used to check if string is empty                                | n/a |
| Performance          | TypesCastingWithFunctionsInspection             | Type casting via PHP4 functions                                                             | yes |
| Performance          | ArrayCastingEquivalentInspection                | Could be replaced with '(array) ...'                                                        | n/a |
| Performance          | CountOnPropelCollectionInspection               | 'count(...)' calls on Propel collection                                                     | n/a |
| Performance          | CallableInLoopTerminationConditionInspection    | Callable calls in loops termination condition                                               | n/a |
| Performance          | SlowArrayOperationsInLoopInspection             | Slow array function used in loop                                                            | n/a |
| Performance          | ArraySearchUsedAsInArrayInspection              | 'array_search(...)' could be replaced by 'in_array(...)'                                    | no  |
| Performance          | StrStrUsedAsStrPosInspection                    | 'strstr(...)/stristr(...)' could be replaced with 'strpos(...)/stripos(...)'                | no  |
| Performance          | StrNcmpUsedAsStrPosInspection                   | 'strncmp(...)/strncasecmp(...)' could be replaced with 'strpos(...)/stripos(...)'           | no  |
| Performance          | SubStrUsedAsStrPosInspection                    | 'substr(...)' could be replaced with 'strpos(...)'                                          | no  |
| Performance          | StrTrUsageAsStrReplaceInspection                | 'strtr(...)' could be replaced with 'str_replace(...)'                                      | no  |
| Performance          | AlterInForeachInspection                        | Slow alter in foreach                                                                       | n/a |
| Performance          | ForeachOnArrayComponentsInspection              | 'array_keys(...)/array_values(...)' used as foreach array                                   | n/a |
| Performance          | LowPerformanceArrayUniqueUsageInspection        | 'array_unique()' low performing usage                                                       | n/a |
| Performance          | ArrayPushMissUseInspection                      | 'array_push(...)' misused"                                                                  | yes |
| Performance          | FileFunctionMissUseInspection                   | 'file(...)' misused                                                                         | yes       | yes | no  |
| Performance          | NotOptimalRegularExpressionsInspection          | Non-optimal regular expression                                                              | n/a |
| Performance          | VariableFunctionsUsageInspection                | Variable functions usage                                                                    | no  |
| Performance          | SubStrShortHandUsageInspection                  | 'substr(...)' short-hand usage                                                              | yes |
| Performance          | InArrayMissUseInspection                        | 'in_array(...)' misused                                                                     | n/a |
| Performance          | CaseInsensitiveStringFunctionsMissUseInspection | 'stristr(...)/stripos()/strripos()' could be replaced with 'strstr(...)/strpos()/strrpos()' | yes |
| Performance          | SubStrUsedAsArrayAccessInspection               | 'substr(...)' used as index-based access                                                    | yes |
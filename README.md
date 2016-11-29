Php Inspections (EA Extended) is a Static Code Analysis tool for PHP, 
distributed as a PhpStorm plugin.

Some facts about this project: https://www.openhub.net/p/phpinspectionsea

To be tested: 
CompactArgumentsInspector.java (Inspection)
MustReturnSpecifiedTypeStrategy.java (Inspection)

PhpUnitTestsInspector.java (Inspection)
AssertEmptyStrategy.java (Inspection)
AssertNotEmptyStrategy.java (Inspection)
AssertBoolInvertedStrategy.java (Inspection)

ExceptionsAnnotatingAndHandlingInspector.java (QF)
OneTimeUseVariablesInspector.java (QF)
SenselessProxyMethodInspector.java (QF)
SwitchContinuationInLoopInspector.java (QF)
OpAssignShortSyntaxInspector.java (QF)
ArraySearchUsedAsInArrayInspector.java (QF)
IsNullFunctionUsageInspector.java (QF)
NestedNotOperatorsInspector.java (QF)
IfReturnReturnSimplificationInspector.java (QF)
ImplicitMagicMethodCallInspector.java (QF, string cast)
AmbiguousMemberInitializationInspector.java (QF)
AssertInstanceOfStrategy.java (QF)
AssertNotInstanceOfStrategy.java (QF)
SubStrUsedAsArrayAccessInspector.java (QF)

Inspections Lists (Type compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Type compatibility   | IsEmptyFunctionUsageInspection                  | 'empty(...)' usage                                  | no  |
| Type compatibility   | TypeUnsafeComparisonInspection                  | Type unsafe comparison                              | no  |
| Type compatibility   | TypeUnsafeArraySearchInspection                 | 'in_array(...)', 'array_search()' type unsafe usage | n/a |
| Type compatibility   | ForeachSourceInspection                         | Foreach source to iterate over                      | n/a       | yes | n/a  |
| Type compatibility   | GenericObjectTypeUsageInspection                | Usage of object type in PhpDoc                      | n/a |
| Type compatibility   | ArrayTypeOfParameterByDefaultValueInspection    | Parameter could be declared as array                | yes       | yes | no   |

Inspections Lists (Control flow)
---
| Group                | Short Name                                      | Full Name                                                | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :------------------------------------------------------- | --------: | --: | ---: |
| Control flow         | UnSafeIsSetOverArrayInspection                  | 'isset(...)' usage                                       | no        | yes | no   |
| Control flow         | TernaryOperatorSimplifyInspection               | Ternary operator could be simplified                     | no        | yes | no   |
| Control flow         | IfReturnReturnSimplificationInspection          | If-return-return could be simplified                     | yes       | yes | no   |
| Control flow         | DefaultValueInElseBranchInspection              | Default value is hidden in else branch                   | no  |
| Control flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                                | n/a |
| Control flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                                 | n/a |
| Control flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                           | no  |
| Control flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach                | n/a       | yes | n/a  |
| Control flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                       | yes |
| Control flow         | DegradedSwitchInspection                        | Switch-case could be simplified                          | n/a |
| Control flow         | ForeachInvariantsInspection                     | Foreach usage possible                                   | no        | yes | no   |
| Control flow         | PdoApiUsageInspection                           | PDO api usage                                            | n/a       | yes | n/a  |
| Control flow         | OneTimeUseVariablesInspection                   | One-time use variables                                   | yes       | yes | no   |
| Control flow         | MultiAssignmentUsageInspection                  | 'list(...) = ' usage possible                            | n/a       | yes | n/a  |
| Control flow         | GetTypeMissUseInspection                        | 'gettype(...)' could be replaced with 'is_*(...)'        | yes       | yes | no   |
| Control flow         | ArraySearchUsedAsInArrayInspection              | 'array_search(...)' could be replaced by 'in_array(...)' | yes       | yes | no   |

Inspections Lists (PhpUnit)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: |
| PhpUnit              | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | yes |

Inspections Lists (Unused)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Unused               | OnlyWritesOnParameterInspection                 | Parameter/variable is not used                      | n/a |
| Unused               | UselessUnsetInspection                          | Useless unset                                       | no        | yes | no   |
| Unused               | AmbiguousMemberInitializationInspection         | Ambiguous class property initialization             | yes |
| Unused               | UnusedConstructorDependenciesInspection         | Unused constructor dependencies                     | n/a |
| Unused               | SenselessProxyMethodInspection                  | Senseless proxy function                            | yes       | yes | n/a  |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | n/a |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime()'/'mktime()' usage                       | yes |
| Compatibility        | FopenBinaryUnsafeUsageInspection                | Binary-unsafe fopen usage                           | yes       | yes | no   |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Confusing constructs | SenselessTernaryOperatorInspection              | Suspicious ternary operator                         | n/a       | yes | n/a  |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | n/a |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | no        | yes | no   |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | n/a |
| Confusing constructs | ParameterByRefWithDefaultInspection             | Parameter passed by reference has default value     | n/a |
| Confusing constructs | ReferencingObjectsInspection                    | Referencing objects                                 | yes |

Inspections Lists (Code style)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Code style           | UnNecessaryDoubleQuotesInspection               | Unnecessary double quotes                           | yes       | yes | no   |
| Code style           | MissingOrEmptyGroupStatementInspection          | Missing or empty group statement                    | no        | yes | no   |
| Code style           | SenselessCommaInArrayDefinitionInspection       | Unnecessary last comma in array definition          | no  |
| Code style           | AccessModifierPresentedInspection               | Access modifiers shall be defined                   | no        | yes | no   |
| Code style           | CallableReferenceNameMismatchInspection         | Callable name case mismatched in a call             | yes |
| Code style           | NestedPositiveIfStatementsInspection            | Nested positive ifs                                 | no  |
| Code style           | StaticInvocationViaThisInspection               | Static methods invocation via '->'                  | yes       | yes | no   |
| Code style           | UnnecessaryParenthesesInspection                | Unnecessary parentheses                             | yes       | yes | no   |
| Code style           | DynamicInvocationViaScopeResolutionInspection   | Dynamic methods invocation via '::'                 | yes       | yes | no   |
| Code style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | yes |
| Code style           | UnnecessaryUseAliasInspection                   | Unnecessary aliases                                 | yes |
| Code style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | yes |
| Code style           | PrefixedIncDecrementEquivalentInspection        | Prefixed increment/decrement equivalent             | yes       | yes | no   |
| Code style           | OpAssignShortSyntaxInspection                   | Short syntax for applied operation                  | yes       | yes | no   |
| Code style           | UnnecessaryFinalModifierInspecti                | Unnecessary final modifiers                         | yes       | yes | no   |

Inspections Lists (Language level migration)
---
| Group                    | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :----------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Language level migration | ElvisOperatorCanBeUsedInspection                | Elvis operator can be used                          | yes |
| Language level migration | NullCoalescingOperatorCanBeUsedInspection       | Null coalescing operator can be used                | yes       | yes | no   |
| Language level migration | TypesCastingWithFunctionsInspection             | Type casting can be used                            | yes       | yes | no   |
| Language level migration | dirnameCallOnFileConstantInspection             | __DIR__ can be used                                 | yes       | yes | no   |
| Language level migration | IsNullFunctionUsageInspection                   | 'null === ...' can be used                          | yes       | yes | no   |
| Language level migration | ShortListSyntaxCanBeUsedInspection              | Short list syntax can be used                       | yes       | yes | no   |
| Language level migration | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | yes       | yes | no   |
| Language level migration | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes       | yes | no   |

Inspections Lists (Architecture)
---
| Group                | Short Name                                      | Full Name                                           | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --------: | --: | ---: |
| Architecture         | BadExceptionsProcessingInspection               | Badly organized exception handling                  | n/a |
| Architecture         | MoreThanThreeArgumentsInspection                | More than 3 parameters in callable                  | n/a |
| Architecture         | CallableParameterUseCaseInTypeContextInspection | Callable parameter usage violates definition        | n/a |
| Architecture         | ClassOverridesFieldOfSuperClassInspection       | Class overrides a field of a parent class           | n/a |
| Architecture         | SingletonFactoryPatternViolationInspection      | Class violates singleton/factory pattern definition | n/a |
| Architecture         | PrivateConstructorSemanticsInspection           | Private constructor semantics                       | n/a       | yes | n/a  |
| Architecture         | LongInheritanceChainInspection                  | Long inheritance chain                              | n/a       | yes | n/a  |
| Architecture         | PropertyCanBeStaticInspection                   | Property could be static                            | n/a       | yes | n/a  |
| Architecture         | EmptyClassInspection                            | Empty class                                         | n/a       | yes | n/a  |

Inspections Lists (Probable bugs)
---
| Group                | Short Name                                      | Full Name                                                 | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --------: | --: | ---: |
| Probable bugs        | ForgottenDebugOutputInspection                  | Forgotten debug statements                                | n/a       | yes | n/a  |
| Probable bugs        | AdditionOperationOnArraysInspection             | Addition operator used on arrays                          | n/a |
| Probable bugs        | MagicMethodsValidityInspection                  | Magic methods validity                                    | n/a |
| Probable bugs        | SuspiciousLoopInspection                        | Suspicious loop                                           | n/a       | yes | n/a  |
| Probable bugs        | ReferenceMismatchInspection                     | Reference mismatch                                        | n/a |
| Probable bugs        | ImplicitMagicMethodCallInspection               | Implicit magic method calls                               | yes |
| Probable bugs        | PrintfScanfArgumentsInspection                  | -printf/-scanf arguments count mismatches                 | n/a |
| Probable bugs        | RealpathOnRelativePathsInspection               | Phar-incompatible 'realpath()' applied to a relative path | no  |
| Probable bugs        | OffsetOperationsInspection                      | Array and string offset validity                          | n/a       | yes | n/a  |
| Probable bugs        | SummerTimeUnsafeTimeManipulationInspection      | Summer-time unsafe date manipulations                     | n/a       | yes | n/a  |
| Probable bugs        | MkdirRaceConditionInspection                    | 'mkdir(...)' race condition                               | n/a       | yes | n/a  |
| Probable bugs        | IncorrectRandomRangeInspection                  | Incorrect random generation range                         | n/a       | yes | n/a  |
| Probable bugs        | SuspiciousSemicolonInspection                   | Suspicious semicolon                                      | n/a |
| Probable bugs        | InconsistentQueryBuildInspection                | Inconsistent 'http_build_query(...)' result               | yes       | yes | no   |
| Probable bugs        | SwitchContinuationInLoopInspection              | Continue misbehaviour in switch                           | yes       | yes | no   |
| Probable bugs        | CompactArgumentsInspection                      | 'compact(...)' variables existence                        | n/a |
| Probable bugs        | DispatchingThisIntoClosuresInspection           | Incorrect dispatching $this into closures                 | n/a |
| Probable bugs        | DateIntervalSpecificationInspection             | Date interval specification validity                      | n/a       | yes | n/a  |
| Probable bugs        | UsingInclusionOnceReturnValueInspection         | Suspicious usage of include_once/require_once return value| yes       | yes | no   |
| Probable bugs        | ClassReImplementsParentInterfaceInspection      | Class implements interfaces multiple times                | yes       | yes | no   |

Inspections Lists (Security)
---
| Group                | Short Name                                      | Full Name                                                 | Quick fix | UTs | QFTs |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --------: | --: | ---: |
| Security             | NonSecureUniqidUsageInspection                  | Insecure 'uniqid()' usage                                 | yes       | yes | no   |
| Security             | NonSecureExtractUsageInspection                 | Insecure 'extract(...)' usage                             | n/a |
| Security             | NonSecureParseStrUsageInspection                | Insecure 'parse_str(...)' usage                           | n/a |
| Security             | UntrustedInclusionInspection                    | Untrusted files inclusion                                 | n/a |
| Security             | SecurityAdvisoriesInspection                    | Security advisories for composer packages                 | no        | yes | no   |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                                                                   | Quick fix | UTs | QFTs |
| :------------------- | :---------------------------------------------- | :------------------------------------------------------------------------------------------ | --------: | --: | ---: |
| Performance          | AmbiguousMethodsCallsInArrayMappingInspection   | Non-optimized arrays mapping                                                                | n/a |
| Performance          | StrlenInEmptyStringCheckContextInspection       | 'strlen(...)' should not be used to check if string is empty                                | n/a |
| Performance          | ArrayCastingEquivalentInspection                | Could be replaced with '(array) ...'                                                        | n/a |
| Performance          | CountOnPropelCollectionInspection               | 'count(...)' calls on Propel collection                                                     | n/a |
| Performance          | CallableInLoopTerminationConditionInspection    | Callable calls in loops termination condition                                               | n/a |
| Performance          | SlowArrayOperationsInLoopInspection             | Slow array function used in loop                                                            | n/a |
| Performance          | StrStrUsedAsStrPosInspection                    | 'strstr(...)/stristr(...)' could be replaced with 'strpos(...)/stripos(...)'                | no        | yes | no  | 
| Performance          | StrNcmpUsedAsStrPosInspection                   | 'strncmp(...)/strncasecmp(...)' could be replaced with 'strpos(...)/stripos(...)'           | no  |
| Performance          | SubStrUsedAsStrPosInspection                    | 'substr(...)' could be replaced with 'strpos(...)'                                          | no  |
| Performance          | StrTrUsageAsStrReplaceInspection                | 'strtr(...)' could be replaced with 'str_replace(...)'                                      | no  |
| Performance          | AlterInForeachInspection                        | Slow alter in foreach                                                                       | n/a       | yes | n/a |
| Performance          | ForeachOnArrayComponentsInspection              | 'array_keys(...)/array_values(...)' used as foreach array                                   | n/a |
| Performance          | LowPerformanceArrayUniqueUsageInspection        | 'array_unique()' low performing usage                                                       | n/a |
| Performance          | ArrayPushMissUseInspection                      | 'array_push(...)' misused"                                                                  | yes       | yes | no  |
| Performance          | FileFunctionMissUseInspection                   | 'file(...)' misused                                                                         | yes       | yes | no  |
| Performance          | NotOptimalRegularExpressionsInspection          | Non-optimal regular expression                                                              | n/a |
| Performance          | VariableFunctionsUsageInspection                | Variable functions usage                                                                    | no  |
| Performance          | SubStrShortHandUsageInspection                  | 'substr(...)' short-hand usage                                                              | yes |
| Performance          | InArrayMissUseInspection                        | 'in_array(...)' misused                                                                     | n/a |
| Performance          | CaseInsensitiveStringFunctionsMissUseInspection | 'stristr(...)/stripos()/strripos()' could be replaced with 'strstr(...)/strpos()/strrpos()' | yes |
| Performance          | SubStrUsedAsArrayAccessInspection               | 'substr(...)' used as index-based access                                                    | yes |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                                                          | n/a       | yes | n/a |
| Performance          | SequentialUnSetCallsInspection                  | 'unset(...)' calls could be merged                                                          | n/a |
| Performance          | StrtotimeUsageInspection                        | 'strtotime(...)' misused                                                                    | yes       | yes | no  |

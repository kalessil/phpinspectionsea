# Rules (Inspections) list

Abbreviations meaning:

- QF   - Quick-Fix;
- UTs  - Inspection has tests;
- QFTs - Inspection has QF tests;
- Doc  - documentation at GitJub (IDE also has shorter version of inspections descriptions, not reflected in the list);

Inspections Lists (Type compatibility)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Type compatibility   | IsEmptyFunctionUsageInspection                  | 'empty(...)' usage                                  | yes | yes | yes  | yes |
| Type compatibility   | TypeUnsafeComparisonInspection                  | Type unsafe comparison                              | yes | yes | yes  | no  |
| Type compatibility   | TypeUnsafeArraySearchInspection                 | 'in_array(...)', 'array_search()' type unsafe usage | yes | yes | yes  | no  |
| Type compatibility   | ForeachSourceInspection                         | Foreach source to iterate over                      | n/a | yes | n/a  | wip |
| Type compatibility   | GenericObjectTypeUsageInspection                | Usage of object type in PhpDoc                      | n/a | yes | n/a  | no  |
| Type compatibility   | ArrayTypeOfParameterByDefaultValueInspection    | Parameter could be declared as array                | yes | yes | yes  | no  |

Inspections Lists (Control flow)
---
| Group                | Short Name                                      | Full Name                                                                         | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------------------------------------- | --: | --: | ---: | --: |
| Control flow         | UnSafeIsSetOverArrayInspection                  | 'isset(...)' usage                                                                | no  | yes | no   | no  |
| Control flow         | TernaryOperatorSimplifyInspection               | Ternary operator could be simplified                                              | yes | yes | no   | no  |
| Control flow         | IfReturnReturnSimplificationInspection          | If-return-return could be simplified                                              | yes | yes | no   | no  |
| Control flow         | DefaultValueInElseBranchInspection              | Default value is hidden in else branch                                            | n/a | yes | n/a  | no  |
| Control flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                                                         | n/a |
| Control flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                                                          | n/a | yes | n/a  | no  |
| Control flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                                                    | yes | yes | yes  | no  |
| Control flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach                                         | n/a | yes | n/a  | no  |
| Control flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                                                | yes |
| Control flow         | DegradedSwitchInspection                        | Switch-case could be simplified                                                   | n/a |
| Control flow         | ForeachInvariantsInspection                     | Foreach usage possible                                                            | no  | yes | no   | no  |
| Control flow         | PdoApiUsageInspection                           | PDO api usage                                                                     | yes | yes | yes  | no  |
| Control flow         | OneTimeUseVariablesInspection                   | One-time use variables                                                            | yes | yes | yes  | no  |
| Control flow         | MultiAssignmentUsageInspection                  | 'list(...) = ' usage possible                                                     | n/a | yes | n/a  | no  |
| Control flow         | GetTypeMissUseInspection                        | 'gettype(...)' could be replaced with 'is_*(...)'                                 | yes | yes | no   | no  |
| Control flow         | ArraySearchUsedAsInArrayInspection              | 'array_search(...)' could be replaced by 'in_array(...)'                          | yes | yes | no   | no  |
| Control flow         | SequentialUnSetCallsInspection                  | 'unset(...)' calls could be merged                                                | yes | yes | no   | no  |
| Control flow         | StrTrUsageAsStrReplaceInspection                | 'strtr(...)' could be replaced with 'str_replace(...)'                            | no  | yes | no   | no  |
| Control flow         | SubStrUsedAsStrPosInspection                    | 'substr(...)' could be replaced with 'strpos(...)'                                | no  | yes | no   | no  |
| Control flow         | StringNormalizationInspection                   | Strings normalization                                                             | yes | yes | no   | no  |
| Control flow         | RedundantElseClauseInspection                   | Redundant 'else' keyword                                                          | yes | yes | no   | no  |
| --                   | ArrayFunctionsInvariantsInspection              |                                                                                   |     |     |      |     |

Inspections Lists (PhpUnit)
---
| Group                | Short Name                                      | Full Name                                           | QF  | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: |
| PhpUnit              | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | yes | no  |

Inspections Lists (Unused)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Unused               | OnlyWritesOnParameterInspection                 | Parameter/variable is not used                      | n/a | yes | n/a  | no  |
| Unused               | UselessUnsetInspection                          | Useless unset                                       | no  | yes | no   | no  |
| Unused               | PropertyInitializationFlawsInspection           | Class property initialization flaws                 | yes | yes | no   | no  |
| Unused               | UnusedConstructorDependenciesInspection         | Unused constructor dependencies                     | n/a | yes | n/a  | no  |
| Unused               | SenselessProxyMethodInspection                  | Senseless proxy function                            | yes | yes | no   | no  |
| Unused               | SenselessMethodDuplicationInspection            | Child method is exactly the same                    | yes | yes | yes  | no  |
| Unused               | UnusedGotoLabelInspection                       | Unused goto labels                                  | yes | yes | no   | no  |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | n/a |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes | yes | no   | no  |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime()'/'mktime()' usage                       | yes | yes | no   | no  |
| Compatibility        | FopenBinaryUnsafeUsageInspection                | Binary-unsafe fopen usage                           | yes | yes | no   | no  |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Confusing constructs | SenselessTernaryOperatorInspection              | Suspicious ternary operator                         | n/a | yes | n/a  | no  |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | n/a | yes | n/a  | no  |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | no  | yes | no   | no  |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | n/a | yes | n/a  | no  |
| Confusing constructs | ParameterByRefWithDefaultInspection             | Parameter passed by reference has default value     | n/a | yes | n/a  | no  |
| Confusing constructs | ReferencingObjectsInspection                    | Referencing objects                                 | yes | yes | no   | no  |

Inspections Lists (Code style)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Code style           | UnNecessaryDoubleQuotesInspection               | Unnecessary double quotes                           | yes | yes | no   | no  |
| Code style           | MissingOrEmptyGroupStatementInspection          | Missing or empty group statement                    | no  | yes | no   | no  |
| Code style           | SenselessCommaInArrayDefinitionInspection       | Unnecessary last comma in array definition          | no  | yes | no   | no  |
| Code style           | AccessModifierPresentedInspection               | Access modifiers shall be defined                   | yes | yes | no   | no  |
| Code style           | CallableReferenceNameMismatchInspection         | Callable name case mismatched in a call             | yes | yes | no   | no  |
| Code style           | NestedPositiveIfStatementsInspection            | Nested positive ifs                                 | no  |
| Code style           | StaticInvocationViaThisInspection               | Static methods invocation via '->'                  | yes | yes | no   | no  |
| Code style           | UnnecessaryParenthesesInspection                | Unnecessary parentheses                             | yes | yes | no   | no  |
| Code style           | DynamicInvocationViaScopeResolutionInspection   | Dynamic methods invocation via '::'                 | yes | yes | no   | no  |
| Code style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | yes | yes | no   | no  |
| Code style           | UnnecessaryUseAliasInspection                   | Unnecessary aliases                                 | yes | yes | yes  | no  |
| Code style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | yes | yes | no   | no  |
| Code style           | PrefixedIncDecrementEquivalentInspection        | Prefixed increment/decrement equivalent             | yes | yes | no   | no  |
| Code style           | OpAssignShortSyntaxInspection                   | Short syntax for applied operation                  | yes | yes | no   | no  |
| Code style           | UnnecessaryFinalModifierInspection              | Unnecessary final modifiers                         | yes | yes | no   | no  |
| Code style           | UsingInclusionReturnValueInspection             | Usage of inclusion return value                     | n/a | yes | n/a  | no  |
| Code style           | ShortOpenTagUsageInspection                     | PHP short open tag usage                            | yes | yes | no   | no  |
| Code style           | ComparisonOperandsOrderInspection               | Yoda/regular conditions style usage                 | yes | yes | no   | no  |
| Code style           | UnknownInspectionInspection                     | Unknown inspection suppression                      | n/a | yes | n/a  | no  |
| Code style           | ParameterDefaultValueIsNotNullInspection        | Non-null parameters default value                   | n/a | yes | n/a  | yes |

Inspections Lists (Language level migration)
---
| Group                    | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :----------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Language level migration | ElvisOperatorCanBeUsedInspection                | Elvis operator can be used                          | yes | yes | no   | no  |
| Language level migration | NullCoalescingOperatorCanBeUsedInspection       | Null coalescing operator can be used                | yes | yes | no   | no  |
| Language level migration | TypesCastingWithFunctionsInspection             | Type casting can be used                            | yes | yes | no   | no  |
| Language level migration | dirnameCallOnFileConstantInspection             | __DIR__ constant can be used                        | yes | yes | no   | no  |
| Language level migration | IsNullFunctionUsageInspection                   | 'null === ...' can be used                          | yes | yes | yes  | no  |
| Language level migration | ShortListSyntaxCanBeUsedInspection              | Short list syntax can be used                       | yes | yes | no   | no  |
| Language level migration | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | yes | yes | no   | no  |
| Language level migration | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes | yes | no   | no  |
| Language level migration | ClassConstantCanBeUsedInspection                | ::class can be used                                 | yes | yes | yes  | no  |
| Language level migration | CascadingDirnameCallsInspection                 | Cascading dirname() calls                           | yes | yes | no   | no  |
| Language level migration | PowerOperatorCanBeUsedInspection                | Power operator can be used                          | yes | yes | no   | no  |
| Language level migration | ConstantCanBeUsedInspection                     | A constant can be used                              | yes | yes | yes  | no  |
| Language level migration | ArgumentUnpackingCanBeUsedInspection            | Argument unpacking can be used                      | yes | yes | no   | no  |

Inspections Lists (Architecture)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Architecture         | BadExceptionsProcessingInspection               | Badly organized exception handling                  | n/a | yes | n/a  | no  |
| Architecture         | MoreThanThreeArgumentsInspection                | Too many parameters in a callable                   | n/a | yes | n/a  | no  |
| Architecture         | CallableParameterUseCaseInTypeContextInspection | Callable parameter usage violates definition        | n/a |
| Architecture         | ClassOverridesFieldOfSuperClassInspection       | Class overrides a field of a parent class           | n/a | yes | n/a  | yes |
| Architecture         | SingletonFactoryPatternViolationInspection      | Class violates singleton/factory pattern definition | n/a |
| Architecture         | PrivateConstructorSemanticsInspection           | Private constructor semantics                       | n/a | yes | n/a  | no  |
| Architecture         | LongInheritanceChainInspection                  | Long inheritance chain                              | n/a | yes | n/a  | yes |
| Architecture         | PropertyCanBeStaticInspection                   | Property could be static                            | n/a | yes | n/a  | no  |
| Architecture         | EmptyClassInspection                            | Empty class                                         | n/a | yes | n/a  | no  |
| Architecture         | OverridingDeprecatedMethodInspection            | Overriding deprecated methods                       | n/a | yes | n/a  | yes |

Inspections Lists (Probable bugs)
---
| Group                | Short Name                                      | Full Name                                                 | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --: | --: | ---: | --: |
| Probable bugs        | ForgottenDebugOutputInspection                  | Forgotten debug statements                                | n/a | yes | n/a  | yes |
| Probable bugs        | AdditionOperationOnArraysInspection             | Addition operator used on arrays                          | n/a | yes | n/a  | yes |
| Probable bugs        | MagicMethodsValidityInspection                  | Magic methods validity                                    | n/a |
| Probable bugs        | SuspiciousLoopInspection                        | Suspicious loop                                           | n/a | yes | n/a  | no  |
| Probable bugs        | ReferenceMismatchInspection                     | Reference mismatch                                        | n/a |
| Probable bugs        | ImplicitMagicMethodCallInspection               | Implicit magic method calls                               | yes | yes |  no  | no  |
| Probable bugs        | PrintfScanfArgumentsInspection                  | -printf/-scanf arguments count mismatches                 | n/a | yes | n/a  | no  |
| Probable bugs        | RealpathInSteamContextInspection                | Phar-incompatible 'realpath()' usage                      | yes | yes | yes  | yes |
| Probable bugs        | OffsetOperationsInspection                      | Array and string offset validity                          | n/a | yes | n/a  | no  |
| Probable bugs        | SummerTimeUnsafeTimeManipulationInspection      | Summer-time unsafe date manipulations                     | n/a | yes | n/a  | no  |
| Probable bugs        | MkdirRaceConditionInspection                    | 'mkdir(...)' race condition                               | n/a | yes | n/a  | no  |
| Probable bugs        | IncorrectRandomRangeInspection                  | Incorrect random generation range                         | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousSemicolonInspection                   | Suspicious semicolon                                      | n/a | yes | n/a  | no  |
| Probable bugs        | InconsistentQueryBuildInspection                | Inconsistent 'http_build_query(...)' result               | yes | yes | no   | no  |
| Probable bugs        | SwitchContinuationInLoopInspection              | Continue misbehaviour in switch                           | yes | yes | no   | no  |
| Probable bugs        | CompactArgumentsInspection                      | 'compact(...)' variables existence                        | n/a | yes | n/a  | yes |
| Probable bugs        | DateIntervalSpecificationInspection             | Date interval specification validity                      | n/a | yes | n/a  | no  |
| Probable bugs        | UsingInclusionOnceReturnValueInspection         | Suspicious usage of include_once/require_once return value| yes | yes | no   | no  |
| Probable bugs        | ClassReImplementsParentInterfaceInspection      | Class implements interfaces multiple times                | yes | yes | no   | no  |
| Probable bugs        | PregQuoteUsageInspection                        | Proper preg_quote() usage                                 | n/a | yes | n/a  | yes |
| Probable bugs        | SuspiciousAssignmentsInspection                 | Suspicious assignments                                    | n/a | yes | n/a  | no  |
| Probable bugs        | DateTimeConstantsUsageInspection                | DateTime constants usage validity                         | yes | yes | no   | no  |
| Probable bugs        | SuspiciousReturnInspection                      | Suspicious returns                                        | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousBinaryOperationInspection             | Suspicious binary operations                              | n/a | yes | n/a  | no  |
| Probable bugs        | IncompleteThrowStatementsInspection             | Incomplete throw statements                               | yes | yes | no   | no  |
| Probable bugs        | AutoloadingIssuesInspection                     | Class autoloading correctness                             | n/a | yes | n/a  | no  |
| Probable bugs        | NullCoalescingArgumentExistenceInspection       | Null coalescing operator variable existence               | n/a | yes | n/a  | no  |

Inspections Lists (Security)
---
| Group                | Short Name                                         | Full Name                                                 | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------       | :--------------------------------------------------       | --: | --: | ---: | --: |
| Security             | NonSecureUniqidUsageInspection                     | Insecure 'uniqid()' usage                                 | yes | yes | yes  | no  |
| Security             | NonSecureExtractUsageInspection                    | Insecure 'extract(...)' usage                             | n/a | yes | n/a  | yes |
| Security             | NonSecureParseStrUsageInspection                   | Insecure 'parse_str(...)' usage                           | n/a | yes | n/a  | yes |
| Security             | UntrustedInclusionInspection                       | Untrusted files inclusion                                 | n/a | yes | n/a  | no  |
| Security             | SecurityAdvisoriesInspection                       | Security advisories for composer packages                 | no  | yes | no   | yes |
| Security             | CurlSslServerSpoofingInspection                    | CURL: SSL server spoofing                                 | n/a | yes | n/a  | yes |
| Security             | EncryptionInitializationVectorRandomnessInspection | Encryption initialization vector randomness               | n/a | yes | n/a  | no  |
| Security             | UnserializeExploitsInspection                      | Exploiting unserialize                                    | n/a | yes | n/a  | yes |
| Security             | PotentialMalwareInspection                         | Basic malware patterns                                    | n/a | yes | n/a  | no  |
| Security             | CryptographicallySecureRandomnessInspection        | Cryptographically secure randomness                       | n/a | yes | n/a  | yes |
| Security             | NonSecureCryptUsageInspection                      | Insecure 'crypt(...)' usage                               | n/a | yes | n/a  | no  |
| Security             | CryptographicallySecureAlgorithms                  | Cryptographically secure algorithms                       | n/a |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                                                                   | QF  | UTs | QFTs | Doc |
| :------------------- | :---------------------------------------------- | :------------------------------------------------------------------------------------------ | --: | --: | ---: | --: |
| Performance          | AmbiguousMethodsCallsInArrayMappingInspection   | Non-optimized arrays mapping                                                                | n/a | yes | n/a  | no  |
| Performance          | StrlenInEmptyStringCheckContextInspection       | '(mb_)strlen(...)' misused                                                                  | n/a |
| Performance          | ArrayCastingEquivalentInspection                | Could be replaced with '(array) ...'                                                        | n/a |
| Performance          | CountOnPropelCollectionInspection               | 'count(...)' calls on Propel collection                                                     | n/a |
| Performance          | CallableInLoopTerminationConditionInspection    | Callable calls in loops termination condition                                               | n/a | yes | n/a | no  |
| Performance          | SlowArrayOperationsInLoopInspection             | Slow array function used in loop                                                            | n/a |
| Performance          | StrStrUsedAsStrPosInspection                    | 'str(i)str(...)' could be replaced with 'str(i)pos(...)'                                    | yes | yes | no  | no  |
| Performance          | AlterInForeachInspection                        | Slow alter in foreach                                                                       | n/a | yes | n/a | no  |
| Performance          | LowPerformanceArrayUniqueUsageInspection        | 'array_unique()' low performing usage                                                       | n/a |
| Performance          | ArrayPushMissUseInspection                      | 'array_push(...)' misused"                                                                  | yes | yes | no  | no  |
| Performance          | FileFunctionMissUseInspection                   | 'file(...)' misused                                                                         | yes | yes | no  | no  |
| Performance          | NotOptimalRegularExpressionsInspection          | Non-optimal regular expression                                                              | n/a |
| Performance          | VariableFunctionsUsageInspection                | Variable functions usage                                                                    | yes | yes | yes | no  |
| Performance          | SubStrShortHandUsageInspection                  | 'substr(...)' short-hand usage                                                              | yes | yes | no  | no  |
| Performance          | InArrayMissUseInspection                        | 'in_array(...)' misused                                                                     | yes | yes | no  | no  |
| Performance          | CaseInsensitiveStringFunctionsMissUseInspection | 'stristr(...)/stripos()/strripos()' could be replaced with 'strstr(...)/strpos()/strrpos()' | yes | yes | no  | no  |
| Performance          | SubStrUsedAsArrayAccessInspection               | 'substr(...)' used as index-based access                                                    | yes | yes | yes | no  |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                                                          | no  | yes | no  | no  |
| Performance          | StrtotimeUsageInspection                        | 'strtotime(...)' misused                                                                    | yes | yes | no  | no  |
| Performance          | FilePutContentsMissUseInspection                | 'file_put_contents(...)' misused                                                            | yes | yes | no  | no  |
| Performance          | PackedHashtableOptimizationInspection           | Packed hashtable optimizations                                                              | n/a | yes | n/a | yes |
| Performance          | StaticLocalVariablesUsageInspection             | Static local variables usage                                                                | n/a | yes | n/a | yes |
| Performance          | UnqualifiedReferenceInspection                  | Unqualified function/constant reference                                                     | yes | yes | yes | no  |
| Performance          | ExplodeMissUseInspection                        | 'explode()' misused                                                                         | yes | yes | yes | no  |

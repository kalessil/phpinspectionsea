# Rules (Inspections) list

Abbreviations meaning:

- QF   - Quick-Fix;
- UTs  - Inspection has tests;
- QFTs - Inspection has QF tests;
- Doc  - documentation at GitJub (IDE also has shorter version of inspections descriptions, not reflected in the list);

Inspections Lists (Type compatibility)
---
| Group                | Short Name                                      | Full Name                                              | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :----------------------------------------------------- | --: | --: | ---: | --: |
| Type compatibility   | IsEmptyFunctionUsageInspection                  | 'empty(...)' usage                                     | yes | yes | yes  | yes |
| Type compatibility   | TypeUnsafeComparisonInspection                  | Type unsafe comparison                                 | yes | yes | yes  | yes |
| Type compatibility   | TypeUnsafeArraySearchInspection                 | 'in_array(...)', 'array_search(...)' type unsafe usage | yes | yes | yes  | yes |
| Type compatibility   | ForeachSourceInspection                         | Foreach source to iterate over                         | n/a | yes | n/a  | yes |
| Type compatibility   | ArrayTypeOfParameterByDefaultValueInspection    | Parameter could be declared as array                   | yes | yes | yes  | no  |

Inspections Lists (Control flow)
---
| Group                | Short Name                                      | Full Name                                                                         | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------------------------------------- | --: | --: | ---: | --: |
| Control Flow         | UnSafeIsSetOverArrayInspection                  | 'isset(...)' usage                                                                | yes | yes | yes  | no  |
| Control Flow         | TernaryOperatorSimplifyInspection               | Ternary operator could be simplified                                              | yes | yes | yes  | no  |
| Control Flow         | IfReturnReturnSimplificationInspection          | If-return-return could be simplified                                              | yes | yes | yes  | no  |
| Control Flow         | NotOptimalIfConditionsInspection                | Non-optimal if conditions                                                         | no  | yes | no   | yes |
| Control Flow         | LoopWhichDoesNotLoopInspection                  | Loop which does not loop                                                          | n/a | yes | n/a  | no  |
| Control Flow         | ThrowRawExceptionInspection                     | General '\Exception' is thrown                                                    | yes | yes | yes  | yes |
| Control Flow         | DisconnectedForeachInstructionInspection        | Statement could be decoupled from foreach                                         | n/a | yes | n/a  | no  |
| Control Flow         | ExceptionsAnnotatingAndHandlingInspection       | Exceptions handling and annotating                                                | no  | yes | no   | no  |
| Control Flow         | DegradedSwitchInspection                        | Switch-case could be simplified                                                   | no  | yes | no   | no  |
| Control Flow         | ForeachInvariantsInspection                     | Foreach usage possible                                                            | yes | yes | yes  | no  |
| Control Flow         | PdoApiUsageInspection                           | PDO API usage                                                                     | yes | yes | yes  | no  |
| Control Flow         | OneTimeUseVariablesInspection                   | One-time use variables                                                            | yes | yes | yes  | no  |
| Control Flow         | MultiAssignmentUsageInspection                  | 'list(...) = ' usage possible                                                     | n/a | yes | n/a  | no  |
| Control Flow         | GetTypeMissUseInspection                        | 'gettype(...)' could be replaced with 'is_*(...)'                                 | yes | yes | yes  | no  |
| Control Flow         | ArraySearchUsedAsInArrayInspection              | 'array_search(...)' could be replaced by 'in_array(...)'                          | yes | yes | yes  | no  |
| Control Flow         | UnsetConstructsCanBeMergedInspection            | 'unset(...)' constructs can be merged                                             | yes | yes | yes  | no  |
| Control Flow         | StrTrUsageAsStrReplaceInspection                | 'strtr(...)' could be replaced with 'str_replace(...)'                            | yes | yes | yes  | no  |
| Control Flow         | SubStrUsedAsStrPosInspection                    | 'substr(...)' could be replaced with 'strpos(...)'                                | yes | yes | yes  | no  |
| Control Flow         | StringNormalizationInspection                   | Strings normalization                                                             | yes | yes | yes  | no  |
| Control Flow         | RedundantElseClauseInspection                   | Redundant 'else' keyword                                                          | yes | yes | no   | no  |
| Control Flow         | InvertedIfElseConstructsInspection              | Inverted 'if-else' constructs                                                     | yes | yes | yes  | no  |
| Control Flow         | StringCaseManipulationInspection                | Unnecessary string case manipulation                                              | yes | yes | yes  | no  |
| Control Flow         | ArrayUniqueCanBeUsedInspection                  | 'array_unique(...)' can be used                                                   | yes | yes | yes  | no  |
| Control Flow         | CompactCanBeUsedInspection                      | 'compact(...)' can be used                                                        | yes | yes | yes  | no  |
| Control Flow         | UnnecessaryVariableOverridesInspection          | Unnecessary variable overrides                                                    | yes | yes | yes  | no  |
| Control Flow         | UnnecessaryContinueInspection                   | Unnecessary continue statements                                                   | yes | yes | yes  | no  |
| Control Flow         | UnnecessaryEmptinessCheckInspection             | Unnecessary emptiness check                                                       | n/a | yes | n/a  | no  |
| Control Flow         | IllusionOfChoiceInspection                      | Illusion of choice                                                                | yes | yes | yes  | no  |
| Control Flow         | IssetConstructsCanBeMergedInspection            | 'isset(...)' constructs can be merged                                             | yes | yes | yes  | no  |

Inspections Lists (PhpUnit)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | --:  | --: |
| PhpUnit              | PhpUnitTestsInspection                          | PhpUnit: bugs and best practices                    | yes | yes | yes  | no  |
| PhpUnit              | UnnecessaryAssertionInspection                  | PhpUnit: unnecessary assertion                      | no  | yes | no   | no  |
| PhpUnit              | MockingMethodsCorrectnessInspection             | PhpUnit: methods mocking issues                     | yes | yes | yes  | no  |

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
| Unused               | UnusedGotoLabelInspection                       | Unused goto labels                                  | yes | yes | yes  | no  |
| Unused               | ArgumentEqualsDefaultValueInspection            | Unnecessary call arguments specification            | yes | yes | yes  | no  |
| Unused               | UnnecessaryIssetArgumentsInspection             | Unnecessary isset arguments specification           | yes | yes | yes  | no  |
| Unused               | DateUsageInspection                             | Unnecessary 'date(...)' arguments specification     | yes | yes | yes  | no  |

Inspections Lists (Compatibility)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Compatibility        | DeprecatedIniOptionsInspection                  | Deprecated configuration options                    | n/a | yes | n/a  | no  |
| Compatibility        | RandomApiMigrationInspection                    | Random API migration                                | yes | yes | yes  | no  |
| Compatibility        | MktimeUsageInspection                           | 'gmmktime(...)'/'mktime(...)' usage                 | yes | yes | yes  | no  |
| Compatibility        | FopenBinaryUnsafeUsageInspection                | Binary-unsafe 'fopen(...)' usage                    | yes | yes | yes  | no  |

Inspections Lists (Confusing constructs)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Confusing constructs | ClassMethodNameMatchesFieldNameInspection       | Method name matches existing field name             | n/a | yes | n/a  | no  |
| Confusing constructs | NestedTernaryOperatorInspection                 | Nested ternary operator                             | no  | yes | no   | no  |
| Confusing constructs | UselessReturnInspection                         | Useless return                                      | n/a | yes | n/a  | no  |
| Confusing constructs | ReferencingObjectsInspection                    | Referencing objects                                 | yes | yes | yes  | no  |

Inspections Lists (Code style)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Code Style           | UnNecessaryDoubleQuotesInspection               | Unnecessary double quotes                           | yes | yes | yes  | no  |
| Code Style           | MissingOrEmptyGroupStatementInspection          | Missing or empty conditionals group statement       | yes | yes | yes  | no  |
| Code Style           | SenselessCommaInArrayDefinitionInspection       | Unnecessary last comma in array definition          | yes | yes | yes  | no  |
| Code Style           | AccessModifierPresentedInspection               | Access modifiers shall be defined                   | yes | yes | yes  | yes |
| Code Style           | CallableReferenceNameMismatchInspection         | Callable name case mismatched in a call             | yes | yes | yes  | no  |
| Code Style           | NestedPositiveIfStatementsInspection            | Nested positive ifs                                 | yes | yes | yes  | no  |
| Code Style           | StaticInvocationViaThisInspection               | Static methods invocation via '->'                  | yes | yes | yes  | no  |
| Code Style           | UnnecessaryParenthesesInspection                | Unnecessary parentheses                             | yes | yes | yes  | no  |
| Code Style           | DynamicInvocationViaScopeResolutionInspection   | Dynamic methods invocation via '::'                 | yes | yes | no   | no  |
| Code Style           | UnnecessarySemicolonInspection                  | Unnecessary semicolon                               | yes | yes | no   | no  |
| Code Style           | UnnecessaryUseAliasInspection                   | Unnecessary use aliases                             | yes | yes | yes  | no  |
| Code Style           | NestedNotOperatorsInspection                    | Nested not operators usage                          | yes | yes | no   | no  |
| Code Style           | IncrementDecrementOperationEquivalentInspection | Increment/decrement operation equivalent            | yes | yes | no   | no  |
| Code Style           | OpAssignShortSyntaxInspection                   | Short syntax for applied operation                  | yes | yes | yes  | no  |
| Code Style           | UnnecessaryFinalModifierInspection              | Unnecessary final modifiers                         | yes | yes | yes  | no  |
| Code Style           | UsingInclusionReturnValueInspection             | Usage of inclusion return value                     | n/a | yes | n/a  | no  |
| Code Style           | ShortOpenTagUsageInspection                     | PHP short open tag usage                            | yes | yes | no   | no  |
| Code Style           | ComparisonOperandsOrderInspection               | Yoda/regular conditions style usage                 | yes | yes | no   | no  |
| Code Style           | UnknownInspectionInspection                     | Unknown inspection suppression                      | n/a | yes | n/a  | no  |
| Code Style           | ParameterDefaultValueIsNotNullInspection        | Non-null parameters default value                   | n/a | yes | n/a  | yes |
| Code Style           | MisorderedModifiersInspection                   | PSR-compliant modifiers order                       | yes | yes | yes  | no  |
| Code Style           | DisallowWritingIntoStaticPropertiesInspection   | Disallow writing into static properties             | n/a | yes | n/a  | no  |
| Code Style           | UnnecessaryCastingInspection                    | Unnecessary type casting                            | yes | yes | yes  | no  |
| Code Style           | SelfClassReferencingInspection                  | Self class referencing                              | yes | yes | yes  | no  |
| Code Style           | ImplicitMagicMethodCallInspection               | Implicit magic method calls                         | yes | yes |  no  | no  |
| Code Style           | UnnecessaryClosureInspection                    | Unnecessary closures                                | yes | yes | yes  | no  |

Inspections Lists (Language level migration)
---
| Group                    | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :----------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Language level migration | ElvisOperatorCanBeUsedInspection                | Elvis operator can be used                          | yes | yes | no   | no  |
| Language level migration | NullCoalescingOperatorCanBeUsedInspection       | Null coalescing operator can be used                | yes | yes | no   | no  |
| Language level migration | TypesCastingCanBeUsedInspection                 | Type casting can be used                            | yes | yes | yes  | no  |
| Language level migration | DirectoryConstantCanBeUsedInspection            | __DIR__ constant can be used                        | yes | yes | yes  | no  |
| Language level migration | IsNullFunctionUsageInspection                   | 'null === ...' can be used                          | yes | yes | yes  | no  |
| Language level migration | ShortListSyntaxCanBeUsedInspection              | Short list syntax can be used                       | yes | yes | no   | no  |
| Language level migration | DeprecatedConstructorStyleInspection            | Deprecated constructor style                        | yes | yes | no   | no  |
| Language level migration | AliasFunctionsUsageInspection                   | Alias functions usage                               | yes | yes | yes  | yes |
| Language level migration | ClassConstantCanBeUsedInspection                | ::class can be used                                 | yes | yes | yes  | no  |
| Language level migration | CascadingDirnameCallsInspection                 | Cascading dirname(...) calls                        | yes | yes | no   | no  |
| Language level migration | PowerOperatorCanBeUsedInspection                | Power operator can be used                          | yes | yes | no   | no  |
| Language level migration | ConstantCanBeUsedInspection                     | A constant can be used                              | yes | yes | yes  | no  |
| Language level migration | ArgumentUnpackingCanBeUsedInspection            | Argument unpacking can be used                      | yes | yes | yes  | no  |
| Language level migration | ReturnTypeCanBeDeclaredInspection               | Return type hint can be used                        | yes | yes | yes  | no  |
| Language level migration | GetClassUsageInspection                         | 'get_class(...)' usage correctness                  | n/a | yes | n/a  | no  |
| Language level migration | UnsupportedStringOffsetOperationsInspection     | Unsupported string offset operations                | n/a | yes | n/a  | no  |
| Language level migration | InstanceofCanBeUsedInspection                   | Instanceof can be used                              | yes | yes | yes  | no  |
| Language level migration | DynamicCallsToScopeIntrospectionInspection      | Deprecated dynamic calls to scope introspection     | n/a | yes | n/a  | no  |
| Language level migration | UnsupportedEmptyListAssignmentsInspection       | Unsupported empty list assignments                  | n/a | yes | n/a  | no  |

Inspections Lists (Architecture)
---
| Group                | Short Name                                      | Full Name                                           | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :-------------------------------------------------- | --: | --: | ---: | --: |
| Architecture         | BadExceptionsProcessingInspection               | Badly organized exception handling                  | n/a | yes | n/a  | no  |
| Architecture         | MoreThanThreeArgumentsInspection                | Too many parameters in a callable                   | n/a | yes | n/a  | no  |
| Architecture         | CallableParameterUseCaseInTypeContextInspection | Callable parameter usage violates definition        | n/a | yes | n/a  | no  |
| Architecture         | ClassOverridesFieldOfSuperClassInspection       | Class overrides a field of a parent class           | n/a | yes | n/a  | yes |
| Architecture         | LongInheritanceChainInspection                  | Long inheritance chain                              | n/a | yes | n/a  | yes |
| Architecture         | PropertyCanBeStaticInspection                   | Property could be static                            | n/a | yes | n/a  | no  |
| Architecture         | EmptyClassInspection                            | Empty class                                         | n/a | yes | n/a  | no  |
| Architecture         | OverridingDeprecatedMethodInspection            | Overriding deprecated methods                       | n/a | yes | n/a  | yes |
| Architecture         | LowerAccessLevelInspection                      | Declaration access can be weaker                    | yes | yes | yes  | no  |
| Architecture         | ClassReImplementsParentInterfaceInspection      | Class implements interfaces multiple times          | yes | yes | yes  | no  |
| Architecture         | MultipleReturnStatementsInspection              | Multiple return statements usage                    | n/a | yes | n/a  | yes |
| Architecture         | EfferentObjectCouplingInspection                | Efferent coupling between objects                   | n/a | yes | n/a  | no  |
| Architecture         | TransitiveDependenciesUsageInspection           | Transitive dependencies usage                       | n/a | yes | n/a  | yes |
| Architecture         | CompositionAndInheritanceInspection             | Composition and inheritance                         | n/a | yes | n/a  | no  |

Inspections Lists (Probable bugs)
---
| Group                | Short Name                                      | Full Name                                                 | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------    | :--------------------------------------------------       | --: | --: | ---: | --: |
| Probable bugs        | ForgottenDebugOutputInspection                  | Forgotten debug statements                                | n/a | yes | n/a  | yes |
| Probable bugs        | AdditionOperationOnArraysInspection             | Addition operator used on arrays                          | n/a | yes | n/a  | yes |
| Probable bugs        | MagicMethodsValidityInspection                  | Magic methods validity                                    | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousLoopInspection                        | Suspicious loop                                           | n/a | yes | n/a  | no  |
| Probable bugs        | ReferenceMismatchInspection                     | Reference mismatch                                        | n/a | yes | n/a  | no  |
| Probable bugs        | PrintfScanfArgumentsInspection                  | *printf/*scanf arguments count mismatches                 | n/a | yes | n/a  | no  |
| Probable bugs        | RealpathInSteamContextInspection                | Phar-incompatible 'realpath(...)' usage                   | yes | yes | yes  | yes |
| Probable bugs        | OffsetOperationsInspection                      | Array and string offset validity                          | n/a | yes | n/a  | no  |
| Probable bugs        | SummerTimeUnsafeTimeManipulationInspection      | Summer-time unsafe date manipulations                     | n/a | yes | n/a  | no  |
| Probable bugs        | MkdirRaceConditionInspection                    | 'mkdir(...)' race condition                               | n/a | yes | n/a  | yes |
| Probable bugs        | IncorrectRandomRangeInspection                  | Incorrect random generation range                         | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousSemicolonInspection                   | Suspicious semicolon                                      | n/a | yes | n/a  | no  |
| Probable bugs        | InconsistentQueryBuildInspection                | Inconsistent 'http_build_query(...)' result               | yes | yes | no   | no  |
| Probable bugs        | SwitchContinuationInLoopInspection              | Continue misbehaviour in switch                           | yes | yes | no   | yes |
| Probable bugs        | CompactArgumentsInspection                      | 'compact(...)' variables existence                        | n/a | yes | n/a  | yes |
| Probable bugs        | DateIntervalSpecificationInspection             | Date interval specification validity                      | n/a | yes | n/a  | no  |
| Probable bugs        | UsingInclusionOnceReturnValueInspection         | Suspicious usage of include_once/require_once return value| yes | yes | no   | no  |
| Probable bugs        | PregQuoteUsageInspection                        | Proper preg_quote(...) usage                              | n/a | yes | n/a  | yes |
| Probable bugs        | SuspiciousAssignmentsInspection                 | Suspicious assignments                                    | n/a | yes | n/a  | no  |
| Probable bugs        | DateTimeConstantsUsageInspection                | DateTime constants usage validity                         | yes | yes | yes  | no  |
| Probable bugs        | SuspiciousReturnInspection                      | Suspicious returns                                        | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousBinaryOperationInspection             | Suspicious binary operations                              | n/a | yes | n/a  | no  |
| Probable bugs        | IncompleteThrowStatementsInspection             | Incomplete throw statements                               | yes | yes | no   | no  |
| Probable bugs        | AutoloadingIssuesInspection                     | Class autoloading correctness                             | n/a | yes | n/a  | yes |
| Probable bugs        | IssetArgumentExistenceInspection                | Isset operations variables existence                      | n/a | yes | n/a  | no  |
| Probable bugs        | TraitsPropertiesConflictsInspection             | Traits properties conflicts resolution                    | n/a | yes | n/a  | no  |
| Probable bugs        | NullPointerExceptionInspection                  | Null reference                                            | n/a | yes | n/a  | yes |
| Probable bugs        | MissingIssetImplementationInspection            | Empty/isset results correctness                           | n/a | yes | n/a  | yes |
| Probable bugs        | CallableMethodValidityInspection                | Callable methods validity                                 | n/a | yes | n/a  | no  |
| Probable bugs        | PassingByReferenceCorrectnessInspection         | Passing arguments by reference correctness                | n/a | yes | n/a  | no  |
| Probable bugs        | MockingFinalClassesInspection                   | Mocking correctness                                       | n/a | yes | n/a  | no  |
| Probable bugs        | ClassConstantUsageCorrectnessInspection         | ::class constant usage correctness                        | n/a | yes | n/a  | yes |
| Probable bugs        | DeclareDirectiveCorrectnessInspection           | Declare directive correctness                             | n/a | yes | n/a  | no  |
| Probable bugs        | InfinityLoopInspection                          | Infinity loop detection                                   | n/a | yes | n/a  | no  |
| Probable bugs        | StringsFirstCharactersCompareInspection         | Strings N-character comparison flaws                      | yes | yes | yes  | no  |
| Probable bugs        | SimpleXmlLoadFileUsageInspection                | 'simplexml_load_file(...)' usage correctness              | yes | yes | yes  | no  |
| Probable bugs        | DateTimeSetTimeUsageInspection                  | 'DateTime::setTime(...)' usage correctness                | n/a | yes | n/a  | no  |
| Probable bugs        | FilePutContentsRaceConditionInspection          | 'file_put_contents(...)' race condition                   | yes | yes | yes  | yes |
| Probable bugs        | CoreInterfacesUsageInspection                   | Core interfaces usage correctness                         | n/a | yes | n/a  | no  |
| Probable bugs        | SubstringCompareInspection                      | Substring comparison flaws                                | yes | yes | yes  | no  |
| Probable bugs        | ClassExistenceCheckInspection                   | Class existence and relation check correctness            | n/a | yes | n/a  | no  |
| Probable bugs        | MissingElseKeywordInspection                    | Missing else keyword                                      | n/a | yes | n/a  | no  |
| Probable bugs        | UnsupportedSerializeTypesInspection             | Unsupported serialize types                               | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousArrayElementInspection                | Suspicious array element                                  | yes | yes | yes  | no  |
| Probable bugs        | KeysFragmentationWithArrayFunctionsInspection   | Array keys set fragmentation                              | n/a | yes | n/a  | no  |
| Probable bugs        | SuspiciousTernaryOperatorInspection             | Suspicious ternary operator                               | n/a | yes | n/a  | no  |
| Probable bugs        | MissingDirectorySeparatorInspection             | Missing directory separators                              | n/a | yes | n/a  | no  |

Inspections Lists (Security)
---
| Group                | Short Name                                         | Full Name                                                              | QF  | UTs | QFTs | Doc |
| :------------------- | :-------------------------------------------       | :--------------------------------------------------------------------- | --: | --: | ---: | --: |
| Security             | NonSecureUniqidUsageInspection                     | Insecure 'uniqid(...)' usage (Insufficient Entropy Vulnerability)      | yes | yes | yes  | no  |
| Security             | NonSecureExtractUsageInspection                    | Insecure 'extract(...)' usage (Variable extract Vulnerability)         | n/a | yes | n/a  | yes |
| Security             | NonSecureParseStrUsageInspection                   | Insecure 'parse_str(...)' usage (Variable extract Vulnerability)       | n/a | yes | n/a  | yes |
| Security             | UntrustedInclusionInspection                       | Untrusted files inclusion                                              | n/a | yes | n/a  | yes |
| Security             | SecurityAdvisoriesInspection                       | Security advisories for Composer packages                              | yes | yes | yes  | yes |
| Security             | CurlSslServerSpoofingInspection                    | CURL: SSL server spoofing (SSL MITM and Spoofing Attacks)              | n/a | yes | n/a  | yes |
| Security             | EncryptionInitializationVectorRandomnessInspection | Encryption initialization vector randomness                            | n/a | yes | n/a  | n/a |
| Security             | UnserializeExploitsInspection                      | Exploiting unserialize (PHP Object Injection Vulnerability)            | n/a | yes | n/a  | yes |
| Security             | PotentialMalwareInspection                         | Basic malware patterns                                                 | n/a | yes | n/a  | yes |
| Security             | CryptographicallySecureRandomnessInspection        | Cryptographically secure randomness                                    | n/a | yes | n/a  | yes |
| Security             | NonSecureCryptUsageInspection                      | Insecure 'crypt(...)' usage (Cryptographically weak algorithms)        | n/a | yes | n/a  | n/a |
| Security             | CryptographicallySecureAlgorithmsInspection        | Cryptographically secure algorithms                                    | n/a | yes | n/a  | yes |
| Security             | HostnameSubstitutionInspection                     | Hostname substitution                                                  | n/a | yes | n/a  | no  |
| Security             | RsaOraclePaddingAttacksInspection                  | RSA oracle padding vulnerability                                       | n/a | yes | n/a  | yes |
| Security             | NonSecureArrayRandUsageInspection                  | Insecure 'array_rand(...)' usage (Insufficient Entropy Vulnerability)  | n/a | yes | n/a  | no  |
| Security             | NonSecureStrShuffleUsageInspection                 | Insecure 'str_shuffle(...)' usage (Insufficient Entropy Vulnerability) | n/a | yes | n/a  | no  |
| Security             | NonSecureHtmlentitiesUsageInspection               | Insecure 'htmlentities(...)' usage (XSS attacks)                       | yes | yes | yes  | no  |
| Security             | NonSecureHtmlspecialcharsUsageInspection           | Insecure 'htmlspecialchars(...)' usage (XSS attacks)                   | yes | yes | yes  | no  |
| Security             | NonSecureShuffleUsageInspection                    | Insecure 'shuffle(...)' usage (Insufficient Entropy Vulnerability)     | n/a | yes | n/a  | no  |
| Security             | BacktickOperatorUsageInspection                    | Backtick operator usage                                                | yes | yes | yes  | n/a |
| Security             | SecureCookiesTransferInspection                    | Secure cookies transfer                                                | yes | yes | yes  | no  |
| Security             | NonSecureOpensslVerifyUsageInspection              | Insecure 'openssl_verify(...)' usage                                   | n/a | yes | n/a  | no  |
| Security             | CommandExecutionAsSuperUserInspection              | Command execution as super user                                        | n/a | yes | n/a  | no  |
| Security             | HardcodedCredentialsInspection                     | Hardcoded credentials                                                  | n/a | yes | n/a  | no  |

Inspections Lists (Performance)
---
| Group                | Short Name                                      | Full Name                                                                                         | QF  | UTs | QFTs | Doc |
| :------------------- | :---------------------------------------------- | :------------------------------------------------------------------------------------------------ | --: | --: | ---: | --: |
| Performance          | AmbiguousMethodsCallsInArrayMappingInspection   | Non-optimized arrays mapping                                                                      | n/a | yes | n/a  | yes |
| Performance          | StrlenInEmptyStringCheckContextInspection       | '(mb_)strlen(...)' misused                                                                        | yes | yes | yes  | no  |
| Performance          | ArrayCastingEquivalentInspection                | Could be replaced with '(array) ...'                                                              | yes | yes | yes  | no  |
| Performance          | CallableInLoopTerminationConditionInspection    | Callable calls in loops termination condition                                                     | n/a | yes | n/a  | no  |
| Performance          | SlowArrayOperationsInLoopInspection             | Slow array function used in loop                                                                  | n/a | yes | n/a  | yes |
| Performance          | StrStrUsedAsStrPosInspection                    | 'str(i)str(...)' could be replaced with 'str(i)pos(...)'                                          | yes | yes | no   | no  |
| Performance          | AlterInForeachInspection                        | Foreach variables reference usage correctness                                                     | n/a | yes | n/a  | yes |
| Performance          | ArrayPushMissUseInspection                      | 'array_push(...)' misused                                                                         | yes | yes | no   | no  |
| Performance          | FileFunctionMissUseInspection                   | 'file(...)' misused                                                                               | yes | yes | yes  | no  |
| Performance          | NotOptimalRegularExpressionsInspection          | Non-optimal regular expression                                                                    | no  | yes | no   | yes | 
| Performance          | VariableFunctionsUsageInspection                | Variable functions usage                                                                          | yes | yes | yes  | no  |
| Performance          | SubStrShortHandUsageInspection                  | 'substr(...)' short-hand usage                                                                    | yes | yes | yes  | no  |
| Performance          | InArrayMissUseInspection                        | 'in_array(...)' misused                                                                           | yes | yes | yes  | no  |
| Performance          | CaseInsensitiveStringFunctionsMissUseInspection | 'stristr(...)/stripos(...)/strripos(...)' could be replaced with 'strstr(...)/strpos()/strrpos()' | yes | yes | yes  | no  |
| Performance          | SubStrUsedAsArrayAccessInspection               | 'substr(...)' used as index-based access                                                          | yes | yes | yes  | no  |
| Performance          | CascadeStringReplacementInspection              | Cascading 'str_replace(...)' calls                                                                | yes | yes | yes  | no  |
| Performance          | StrtotimeUsageInspection                        | 'strtotime(...)' misused                                                                          | yes | yes | yes  | no  |
| Performance          | FilePutContentsMissUseInspection                | 'file_put_contents(...)' misused                                                                  | yes | yes | yes  | no  |
| Performance          | PackedHashtableOptimizationInspection           | Packed hashtable optimizations                                                                    | n/a | yes | n/a  | yes |
| Performance          | UnqualifiedReferenceInspection                  | Unqualified function/constant reference                                                           | yes | yes | yes  | no  |
| Performance          | ExplodeMissUseInspection                        | 'explode(...)' misused                                                                            | yes | yes | yes  | no  |
| Performance          | ScandirUsageInspection                          | 'scandir(...)' low performing usage                                                               | yes | yes | yes  | no  |
| Performance          | FixedTimeStartWithInspection                    | Fixed-time string starts with checks                                                              | yes | yes | yes  | no  |
| Performance          | ArrayKeysMissUseInspection                      | 'array_keys(...)' misused                                                                         | yes | yes | yes  | no  |
| Performance          | RepetitiveMethodCallsInspection                 | Repetitive method calls                                                                           | n/a | yes | n/a  | no  |
| Performance          | ArrayValuesMissUseInspection                    | 'array_values(...)' misused                                                                       | yes | yes | yes  | no  |

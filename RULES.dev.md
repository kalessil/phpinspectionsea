# Inspections list

> **Attention:** this inspection list is in development.
> For stable list, check [RULES.md](https://github.com/kalessil/phpinspectionsea/blob/master/RULES.md) file. If you wants help with this list, read the [PR #267](https://github.com/kalessil/phpinspectionsea/pull/267).

**Abbreviations meaning:**

* **UTs**: unit tests for the inspection;
* **QF**: quick-fix (`*` *mean not-tested yet*);
* **Doc**: fully documented at [documentation page](docs/inspections.md);

**Values meaning:**

* `yes`: fully implemented;
* `yes*`: fully implemented but not tested programatically (*unit tests*);
* `no`: yet not implemented, but possible;
* `-`: impossible to implements;

---

## Type Compatibility

| Class                                       | Short description                                   | UTs | QFs  | Doc |
| :------------------------------------------ | :-------------------------------------------------- | :-: | :--: | :-: |
| IsEmptyFunctionUsageInspector               | 'empty(...)' usage                                  |     |      |     |
| TypeUnsafeComparisonInspector               | Type unsafe comparison                              |     |      |     |
| TypeUnsafeArraySearchInspector              | 'in_array(...)', 'array_search()' type unsafe usage |     |      |     |
| ForeachSourceInspector                      | Foreach source to iterate over                      |     |      |     |
| GenericObjectTypeUsageInspector             | Usage of object type in PhpDoc                      |     |      |     |
| ArrayTypeOfParameterByDefaultValueInspector | Parameter could be declared as array                |     |      |     |

## Control Flow

| Class                                    | Short description                                        | UTs | QFs  | Doc |
| :--------------------------------------- | :------------------------------------------------------- | :-: | :--: | :-: |
| UnSafeIsSetOverArrayInspector            | 'isset(...)' usage                                       |     |      |     |
| TernaryOperatorSimplifyInspector         | Ternary operator could be simplified                     |     |      |     |
| IfReturnReturnSimplificationInspector    | If-return-return could be simplified                     |     |      |     |
| DefaultValueInElseBranchInspector        | Default value is hidden in else branch                   |     |      |     |
| NotOptimalIfConditionsInspector          | Non-optimal if conditions                                |     |      |     |
| LoopWhichDoesNotLoopInspector            | Loop which does not loop                                 |     |      |     |
| ThrowRawExceptionInspector               | General '\Exception' is thrown                           |     |      |     |
| DisconnectedForeachInstructionInspector  | Statement could be decoupled from foreach                |     |      |     |
| ExceptionsAnnotatingAndHandlingInspector | Exceptions handling and annotating                       |     |      |     |
| DegradedSwitchInspector                  | Switch-case could be simplified                          |     |      |     |
| ForeachInvariantsInspector               | Foreach usage possible                                   |     |      |     |
| PdoApiUsageInspector                     | PDO api usage                                            |     |      |     |
| OneTimeUseVariablesInspector             | One-time use variables                                   |     |      |     |
| MultiAssignmentUsageInspector            | 'list(...) = ' usage possible                            |     |      |     |
| GetTypeMissUseInspector                  | 'gettype(...)' could be replaced with 'is_*(...)'        |     |      |     |
| ArraySearchUsedAsInArrayInspector        | 'array_search(...)' could be replaced by 'in_array(...)' |     |      |     |
| SequentialUnSetCallsInspector            | 'unset(...)' calls could be merged                       |     |      |     |
| StrTrUsageAsStrReplaceInspector          | 'strtr(...)' could be replaced with 'str_replace(...)'   |     |      |     |
| SubStrUsedAsStrPosInspector              | 'substr(...)' could be replaced with 'strpos(...)'       |     |      |     |
| StringNormalizationInspector             | Strings normalization                                    |     |      |     |
| RedundantElseClauseInspector             | Redundant 'else' keyword                                 |     |      |     |
| ArrayFunctionsInvariantsInspector        |                                                          |     |      |     |

## PhpUnit

| Class                 | Short description                | UTs | QFs  | Doc |
| :-------------------- | :------------------------------- | :-: | :--: | :-: |
| PhpUnitTestsInspector | PhpUnit: bugs and best practices |     |      |     |

## Unused

| Class                                  | Short description                   | UTs | QFs  | Doc |
| :------------------------------------- | :---------------------------------- | :-: | :--: | :-: |
| OnlyWritesOnParameterInspector         | Parameter/variable is not used      |     |      |     |
| UselessUnsetInspector                  | Useless unset                       |     |      |     |
| PropertyInitializationFlawsInspector   | Class property initialization flaws |     |      |     |
| UnusedConstructorDependenciesInspector | Unused constructor dependencies     |     |      |     |
| SenselessProxyMethodInspector          | Senseless proxy function            |     |      |     |
| SenselessMethodDuplicationInspector    | Child method is exactly the same    |     |      |     |
| UnusedGotoLabelInspector               | Unused goto labels                  |     |      |     |

## Compatibility

| Class                           | Short description                | UTs | QFs  | Doc |
| :------------------------------ | :------------------------------- | :-: | :--: | :-: |
| DeprecatedIniOptionsInspector   | Deprecated configuration options |     |      |     |
| RandomApiMigrationInspector     | Random API migration             |     |      |     |
| MktimeUsageInspector            | 'gmmktime()'/'mktime()' usage    |     |      |     |
| FopenBinaryUnsafeUsageInspector | Binary-unsafe fopen usage        |     |      |     |

## Confusing Constructs

| Class                                    | Short description                               | UTs | QFs  | Doc |
| :--------------------------------------- | :---------------------------------------------- | :-: | :--: | :-: |
| SenselessTernaryOperatorInspector        | Suspicious ternary operator                     |     |      |     |
| ClassMethodNameMatchesFieldNameInspector | Method name matches existing field name         |     |      |     |
| NestedTernaryOperatorInspector           | Nested ternary operator                         |     |      |     |
| UselessReturnInspector                   | Useless return                                  |     |      |     |
| ParameterByRefWithDefaultInspector       | Parameter passed by reference has default value |     |      |     |
| ReferencingObjectsInspector              | Referencing objects                             |     |      |     |

## Code Style

| Class                                        | Short description                          | UTs | QFs  | Doc |
| :------------------------------------------- | :----------------------------------------- | :-: | :--: | :-: |
| UnNecessaryDoubleQuotesInspector             | Unnecessary double quotes                  |     |      |     |
| MissingOrEmptyGroupStatementInspector        | Missing or empty group statement           |     |      |     |
| SenselessCommaInArrayDefinitionInspector     | Unnecessary last comma in array definition |     |      |     |
| AccessModifierPresentedInspector             | Access modifiers shall be defined          |     |      |     |
| CallableReferenceNameMismatchInspector       | Callable name case mismatched in a call    |     |      |     |
| NestedPositiveIfStatementsInspector          | Nested positive ifs                        |     |      |     |
| StaticInvocationViaThisInspector             | Static methods invocation via '->'         |     |      |     |
| UnnecessaryParenthesesInspector              | Unnecessary parentheses                    |     |      |     |
| DynamicInvocationViaScopeResolutionInspector | Dynamic methods invocation via '::'        |     |      |     |
| UnnecessarySemicolonInspector                | Unnecessary semicolon                      |     |      |     |
| UnnecessaryUseAliasInspector                 | Unnecessary aliases                        |     |      |     |
| NestedNotOperatorsInspector                  | Nested not operators usage                 |     |      |     |
| PrefixedIncDecrementEquivalentInspector      | Prefixed increment/decrement equivalent    |     |      |     |
| OpAssignShortSyntaxInspector                 | Short syntax for applied operation         |     |      |     |
| UnnecessaryFinalModifierInspector            | Unnecessary final modifiers                |     |      |     |
| UsingInclusionReturnValueInspector           | Usage of inclusion return value            |     |      |     |
| ShortOpenTagUsageInspector                   | PHP short open tag usage                   |     |      |     |
| ComparisonOperandsOrderInspector             | Yoda/regular conditions style usage        |     |      |     |
| UnknownInspectionInspector                   | Unknown inspection suppression             |     |      |     |
| ParameterDefaultValueIsNotNullInspector      | Non-null parameters default value          |     |      |     |

## Language Level Migration

| Class                                    | Short description                    | UTs | QFs  | Doc |
| :--------------------------------------- | :----------------------------------- | :-: | :--: | :-: |
| ElvisOperatorCanBeUsedInspector          | Elvis operator can be used           |     |      |     |
| NullCoalescingOperatorCanBeUsedInspector | Null coalescing operator can be used |     |      |     |
| TypesCastingWithFunctionsInspector       | Type casting can be used             |     |      |     |
| dirnameCallOnFileConstantInspector       | __DIR__ constant can be used         |     |      |     |
| IsNullFunctionUsageInspector             | 'null === ...' can be used           |     |      |     |
| ShortListSyntaxCanBeUsedInspector        | Short list syntax can be used        |     |      |     |
| DeprecatedConstructorStyleInspector      | Deprecated constructor style         |     |      |     |
| AliasFunctionsUsageInspector             | Alias functions usage                |     |      |     |
| ClassConstantCanBeUsedInspector          | ::class can be used                  |     |      |     |
| CascadingDirnameCallsInspector           | Cascading dirname() calls            |     |      |     |
| PowerOperatorCanBeUsedInspector          | Power operator can be used           |     |      |     |
| ConstantCanBeUsedInspector               | A constant can be used               |     |      |     |
| ArgumentUnpackingCanBeUsedInspector      | Argument unpacking can be used       |     |      |     |

## Architecture

| Class                                          | Short description                                   | UTs | QFs  | Doc |
| :--------------------------------------------- | :-------------------------------------------------- | :-: | :--: | :-: |
| BadExceptionsProcessingInspector               | Badly organized exception handling                  |     |      |     |
| MoreThanThreeArgumentsInspector                | Too many parameters in a callable                   |     |      |     |
| CallableParameterUseCaseInTypeContextInspector | Callable parameter usage violates definition        |     |      |     |
| ClassOverridesFieldOfSuperClassInspector       | Class overrides a field of a parent class           |     |      |     |
| SingletonFactoryPatternViolationInspector      | Class violates singleton/factory pattern definition |     |      |     |
| PrivateConstructorSemanticsInspector           | Private constructor semantics                       |     |      |     |
| LongInheritanceChainInspector                  | Long inheritance chain                              |     |      |     |
| PropertyCanBeStaticInspector                   | Property could be static                            |     |      |     |
| EmptyClassInspector                            | Empty class                                         |     |      |     |
| OverridingDeprecatedMethodInspector            | Overriding deprecated methods                       |     |      |     |

## Probable Bugs

| Class                                     | Short description                                          | UTs | QFs  | Doc |
| :---------------------------------------- | :--------------------------------------------------------- | :-: | :--: | :-: |
| ForgottenDebugOutputInspector             | Forgotten debug statements                                 |     |      |     |
| AdditionOperationOnArraysInspector        | Addition operator used on arrays                           |     |      |     |
| MagicMethodsValidityInspector             | Magic methods validity                                     |     |      |     |
| SuspiciousLoopInspector                   | Suspicious loop                                            |     |      |     |
| ReferenceMismatchInspector                | Reference mismatch                                         |     |      |     |
| ImplicitMagicMethodCallInspector          | Implicit magic method calls                                |     |      |     |
| PrintfScanfArgumentsInspector             | -printf/-scanf arguments count mismatches                  |     |      |     |
| RealpathInSteamContextInspector           | Phar-incompatible 'realpath()' usage                       |     |      |     |
| OffsetOperationsInspector                 | Array and string offset validity                           |     |      |     |
| SummerTimeUnsafeTimeManipulationInspector | Summer-time unsafe date manipulations                      |     |      |     |
| MkdirRaceConditionInspector               | 'mkdir(...)' race condition                                |     |      |     |
| IncorrectRandomRangeInspector             | Incorrect random generation range                          |     |      |     |
| SuspiciousSemicolonInspector              | Suspicious semicolon                                       |     |      |     |
| InconsistentQueryBuildInspector           | Inconsistent 'http_build_query(...)' result                |     |      |     |
| SwitchContinuationInLoopInspector         | Continue misbehaviour in switch                            |     |      |     |
| CompactArgumentsInspector                 | 'compact(...)' variables existence                         |     |      |     |
| DateIntervalSpecificationInspector        | Date interval specification validity                       |     |      |     |
| UsingInclusionOnceReturnValueInspector    | Suspicious usage of include_once/require_once return value |     |      |     |
| ClassReImplementsParentInterfaceInspector | Class implements interfaces multiple times                 |     |      |     |
| PregQuoteUsageInspector                   | Proper preg_quote() usage                                  |     |      |     |
| SuspiciousAssignmentsInspector            | Suspicious assignments                                     |     |      |     |
| DateTimeConstantsUsageInspector           | DateTime constants usage validity                          |     |      |     |
| SuspiciousReturnInspector                 | Suspicious returns                                         |     |      |     |
| SuspiciousBinaryOperationInspector        | Suspicious binary operations                               |     |      |     |
| IncompleteThrowStatementsInspector        | Incomplete throw statements                                |     |      |     |
| AutoloadingIssuesInspector                | Class autoloading correctness                              |     |      |     |

## Security

| Class                                             | Short description                           | UTs | QFs  | Doc |
| :------------------------------------------------ | :------------------------------------------ | :-: | :--: | :-: |
| NonSecureUniqidUsageInspector                     | Insecure 'uniqid()' usage                   |     |      |     |
| NonSecureExtractUsageInspector                    | Insecure 'extract(...)' usage               |     |      |     |
| NonSecureParseStrUsageInspector                   | Insecure 'parse_str(...)' usage             |     |      |     |
| UntrustedInclusionInspector                       | Untrusted files inclusion                   |     |      |     |
| SecurityAdvisoriesInspector                       | Security advisories for composer packages   |     |      |     |
| CurlSslServerSpoofingInspector                    | CURL: SSL server spoofing                   |     |      |     |
| EncryptionInitializationVectorRandomnessInspector | Encryption initialization vector randomness |     |      |     |
| UnserializeExploitsInspector                      | Exploiting unserialize                      |     |      |     |
| PotentialMalwareInspector                         | Basic malware patterns                      |     |      |     |
| CryptographicallySecureRandomnessInspector        | Cryptographically secure randomness         |     |      |     |
| NonSecureCryptUsageInspector                      | Insecure 'crypt(...)' usage                 |     |      |     |
| CryptographicallySecureAlgoritor                  | Cryptographically secure algorithms         |     |      |     |

## Performance

| Class                                          | Short description                                                                           | UTs | QFs  | Doc |
| :--------------------------------------------- | :------------------------------------------------------------------------------------------ | :-: | :--: | :-: |
| AmbiguousMethodsCallsInArrayMappingInspector   | Non-optimized arrays mapping                                                                |     |      |     |
| StrlenInEmptyStringCheckContextInspector       | '(mb_)strlen(...)' misused                                                                  |     |      |     |
| ArrayCastingEquivalentInspector                | Could be replaced with '(array) ...'                                                        |     |      |     |
| CountOnPropelCollectionInspector               | 'count(...)' calls on Propel collection                                                     |     |      |     |
| CallableInLoopTerminationConditionInspector    | Callable calls in loops termination condition                                               |     |      |     |
| SlowArrayOperationsInLoopInspector             | Slow array function used in loop                                                            |     |      |     |
| StrStrUsedAsStrPosInspector                    | 'str(i)str(...)' could be replaced with 'str(i)pos(...)'                                    |     |      |     |
| AlterInForeachInspector                        | Slow alter in foreach                                                                       |     |      |     |
| LowPerformanceArrayUniqueUsageInspector        | 'array_unique()' low performing usage                                                       |     |      |     |
| ArrayPushMissUseInspector                      | 'array_push(...)' misused"                                                                  |     |      |     |
| FileFunctionMissUseInspector                   | 'file(...)' misused                                                                         |     |      |     |
| NotOptimalRegularExpressionsInspector          | Non-optimal regular expression                                                              |     |      |     |
| VariableFunctionsUsageInspector                | Variable functions usage                                                                    |     |      |     |
| SubStrShortHandUsageInspector                  | 'substr(...)' short-hand usage                                                              |     |      |     |
| InArrayMissUseInspector                        | 'in_array(...)' misused                                                                     |     |      |     |
| CaseInsensitiveStringFunctionsMissUseInspector | 'stristr(...)/stripos()/strripos()' could be replaced with 'strstr(...)/strpos()/strrpos()' |     |      |     |
| SubStrUsedAsArrayAccessInspector               | 'substr(...)' used as index-based access                                                    |     |      |     |
| CascadeStringReplacementInspector              | Cascading 'str_replace(...)' calls                                                          |     |      |     |
| StrtotimeUsageInspector                        | 'strtotime(...)' misused                                                                    |     |      |     |
| FilePutContentsMissUseInspector                | 'file_put_contents(...)' misused                                                            |     |      |     |
| PackedHashtableOptimizationInspector           | Packed hashtable optimizations                                                              |     |      |     |
| StaticLocalVariablesUsageInspector             | Static local variables usage                                                                |     |      |     |
| UnqualifiedReferenceInspector                  | Unqualified function/constant reference                                                     |     |      |     |
| ExplodeMissUseInspector                        | 'explode()' misused                                                                         |     |      |     |

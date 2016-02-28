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


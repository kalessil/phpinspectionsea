Loops:
    'array_merge()/array_fill()/array_combine()': loop + pushing to array
    'array_chunk()':                              loop + array_slice()
    'array_column()':                             loop + index access
    'array_filter()':                             loop + if + pushing to array, loop + if + unset (in some cases even without closure)
    'in_array()/array_search()':                  loop + comparison + break

Amount of traits
Unused function result, but for generators

PHP v8: str_contains, str_starts_with, str_ends_with (bulk-checks on origin/target constructs?)
    - startsWith: 'substr($haystack, 0, strlen($needle)) === (string) $needle' | 'strpos((string) $needle, $haystack) === 0'
    - endsWith:   'substr($haystack, -strlen($needle)) === (string) $needle'
    - contains:   'mb_strpos($haystack, $needle) !== false'

Extended:
- [EA] prefix for QF titles
- PhpUnit -> PHPUnit

java.lang.IllegalArgumentException: Argument for @NotNull parameter 'level' of com/kalessil/phpStorm/phpInspectionsEA/inspectors/phpUnit/strategy/AssertInternalTypeStrategy.apply must not be null
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy.AssertInternalTypeStrategy.apply(AssertInternalTypeStrategy.java)
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitTestsInspector$1.lambda$visitPhpMethodReference$5(PhpUnitTestsInspector.java:240)
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitTestsInspector$1.visitPhpMethodReference(PhpUnitTestsInspector.java:251)
	at com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl.accept(MethodReferenceImpl.java:31)
	at com.intellij.codeInspection.InspectionEngine.acceptElements(InspectionEngine.java:69)
	at com.intellij.codeInsight.daemon.impl.LocalInspectionsPass.lambda$null$6(LocalInspectionsPass.java:320)
	at com.intellij.util.AstLoadingFilter.lambda$toComputable$2(AstLoadingFilter.java:168)
	at com.intellij.util.AstLoadingFilter.disallowTreeLoading(AstLoadingFilter.java:126)
	at com.intellij.util.AstLoadingFilter.disallowTreeLoading(AstLoadingFilter.java:115)
	at com.intellij.util.AstLoadingFilter.disallowTreeLoading(AstLoadingFilter.java:110)
	at com.intellij.codeInsight.daemon.impl.LocalInspectionsPass.lambda$visitRestElementsAndCleanup$7(LocalInspectionsPass.java:320)
	at com.intellij.concurrency.ApplierCompleter.execAndForkSubTasks(ApplierCompleter.java:149)
	at com.intellij.concurrency.ApplierCompleter.execAndForkSubTasks(ApplierCompleter.java:162)
	at com.intellij.concurrency.ApplierCompleter.execAndForkSubTasks(ApplierCompleter.java:162)
	at com.intellij.openapi.application.impl.ApplicationImpl.tryRunReadAction(ApplicationImpl.java:1101)
	at com.intellij.concurrency.ApplierCompleter.lambda$wrapInReadActionAndIndicator$1(ApplierCompleter.java:105)
	at com.intellij.openapi.progress.impl.CoreProgressManager.registerIndicatorAndRun(CoreProgressManager.java:625)
	at com.intellij.openapi.progress.impl.CoreProgressManager.executeProcessUnderProgress(CoreProgressManager.java:570)
	at com.intellij.openapi.progress.impl.ProgressManagerImpl.executeProcessUnderProgress(ProgressManagerImpl.java:61)
	at com.intellij.concurrency.ApplierCompleter.wrapInReadActionAndIndicator(ApplierCompleter.java:117)
	at com.intellij.concurrency.ApplierCompleter.lambda$compute$0(ApplierCompleter.java:96)
	at com.intellij.openapi.application.impl.ReadMostlyRWLock.executeByImpatientReader(ReadMostlyRWLock.java:168)
	at com.intellij.openapi.application.impl.ApplicationImpl.executeByImpatientReader(ApplicationImpl.java:168)
	at com.intellij.concurrency.ApplierCompleter.compute(ApplierCompleter.java:96)
	at java.base/java.util.concurrent.CountedCompleter.exec(CountedCompleter.java:746)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:290)
	at java.base/java.util.concurrent.ForkJoinPool.awaitJoin(ForkJoinPool.java:1708)
	at java.base/java.util.concurrent.ForkJoinTask.get(ForkJoinTask.java:1039)
	at com.intellij.concurrency.JobLauncherImpl.invokeConcurrentlyUnderProgress(JobLauncherImpl.java:65)
	at com.intellij.concurrency.JobLauncher.invokeConcurrentlyUnderProgress(JobLauncher.java:49)
	at com.intellij.codeInsight.daemon.impl.LocalInspectionsPass.visitRestElementsAndCleanup(LocalInspectionsPass.java:331)
	at com.intellij.codeInsight.daemon.impl.LocalInspectionsPass.inspect(LocalInspectionsPass.java:197)
	at com.intellij.codeInsight.daemon.impl.LocalInspectionsPass.collectInformationWithProgress(LocalInspectionsPass.java:115)
	at com.intellij.codeInsight.daemon.impl.ProgressableTextEditorHighlightingPass.doCollectInformation(ProgressableTextEditorHighlightingPass.java:84)
	at com.intellij.codeHighlighting.TextEditorHighlightingPass.collectInformation(TextEditorHighlightingPass.java:52)
	at com.intellij.codeInsight.daemon.impl.PassExecutorService$ScheduledPass.lambda$null$1(PassExecutorService.java:434)
	at com.intellij.openapi.application.impl.ApplicationImpl.tryRunReadAction(ApplicationImpl.java:1106)
	at com.intellij.codeInsight.daemon.impl.PassExecutorService$ScheduledPass.lambda$doRun$2(PassExecutorService.java:427)
	at com.intellij.openapi.progress.impl.CoreProgressManager.registerIndicatorAndRun(CoreProgressManager.java:625)
	at com.intellij.openapi.progress.impl.CoreProgressManager.executeProcessUnderProgress(CoreProgressManager.java:570)
	at com.intellij.openapi.progress.impl.ProgressManagerImpl.executeProcessUnderProgress(ProgressManagerImpl.java:61)
	at com.intellij.codeInsight.daemon.impl.PassExecutorService$ScheduledPass.doRun(PassExecutorService.java:426)
	at com.intellij.codeInsight.daemon.impl.PassExecutorService$ScheduledPass.lambda$run$0(PassExecutorService.java:402)
	at com.intellij.openapi.application.impl.ReadMostlyRWLock.executeByImpatientReader(ReadMostlyRWLock.java:168)
	at com.intellij.openapi.application.impl.ApplicationImpl.executeByImpatientReader(ApplicationImpl.java:168)
	at com.intellij.codeInsight.daemon.impl.PassExecutorService$ScheduledPass.run(PassExecutorService.java:400)
	at com.intellij.concurrency.JobLauncherImpl$VoidForkJoinTask$1.exec(JobLauncherImpl.java:171)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:290)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1020)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1656)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1594)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:177)

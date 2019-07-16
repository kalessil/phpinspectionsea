<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java';

    $foundViolations = [];
    /** @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $matched = preg_match_all('/\w+.getProject\(\)/', file_get_contents($file->getPathname()), $candidates) > 0;
            if ($matched) {
                $violations = array_filter(
                    $candidates[0],
                    static function ($call) { return $call !== 'holder.getProject()'; }
                );
                if ($violations !== []) {
                    $foundViolations[] = $file->getFilename() . ': ' . implode(', ', $violations);
                }
            }
        }
    }

    if ($foundViolations !== []) {
        echo 'Please avoid non-holder based getProject() calls (psi up-scans): ' . PHP_EOL . implode(',' . PHP_EOL, $foundViolations) . PHP_EOL;
        exit(1);
    }
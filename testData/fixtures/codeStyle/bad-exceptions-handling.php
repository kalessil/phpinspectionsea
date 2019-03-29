<?php

    try {
        echo 1;
    } catch (RuntimeException <weak_warning descr="The exception being ignored, please don't fail silently and at least log it.">$failSilently</weak_warning>) {
        /* fail silently */
    } catch (Exception <weak_warning descr="The exception being ignored, please log it or use chained exceptions.">$chainedCalls</weak_warning>) {
        throw new RuntimeException('...');
    } catch (Throwable <weak_warning descr="The exception being re-throws without any processing.">$rethrown</weak_warning>) {
        throw $rethrown;
    }

    <weak_warning descr="Consider moving non-related statements (4 in total) outside the try-block or refactoring the try-body into a function/method.">try</weak_warning> {
        echo 1;
        echo 2;
        echo 3;
        echo 4;
    } catch (Throwable $exception) {
        throw new RuntimeException('', null, $exception);
    }
<?php

    try {
        echo 1;
        echo 2;
        echo 3;
    } catch (Exception $failSilently) {
    }

    <weak_warning descr="Consider moving non-related statements (4 in total) outside the try-block or refactoring the try-body into a function/method.">try</weak_warning> {
        echo 1;
        echo 2;
        echo 3;
        echo 4;
    } catch (Exception $failSilently) {
    }
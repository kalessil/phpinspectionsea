<?php

    try {
        echo 1;
        echo 2;
        echo 3;
    } catch (RuntimeException <weak_warning descr="[EA] The exception being ignored, please don't fail silently and at least log it.">$failSilently</weak_warning>) {
        /* fail silently */
    } catch (Exception <weak_warning descr="[EA] The exception being ignored, please log it or use chained exceptions.">$chainedCalls</weak_warning>) {
        throw new RuntimeException('...');
    }

    <weak_warning descr="[EA] It is possible that some of the statements contained in the try block can be extracted into their own methods or functions (we recommend that you do not include more than three statements per try block).">try</weak_warning> {
        echo 1;
        echo 2;
        echo 3;
        echo 4;
    } catch (Exception $exception) {
        throw new RuntimeException('', null, $exception);
    }
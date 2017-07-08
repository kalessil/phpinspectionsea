<?php

class ReferenceMismatchForeach
{
    public function method(& $parameterByReference) {
        foreach ([] as <error descr="Probable bug: variable should be renamed to prevent writing into already existing reference.">$parameterByReference</error>)               {}
        foreach ([] as <error descr="Probable bug: variable should be renamed to prevent writing into already existing reference.">$parameterByReference</error> => $whatever)  {}

        foreach ([] as &$referenceTwo) {}
        foreach ([] as <error descr="Probable bug: variable should be renamed to prevent writing into already existing reference.">$referenceTwo</error>)               {}
        foreach ([] as <error descr="Probable bug: variable should be renamed to prevent writing into already existing reference.">$referenceTwo</error> => $whatever)  {}

        /* false-positive: unset after foreach */
        foreach ([] as &$referenceOne) {}
        unset($referenceOne);
        foreach ([] as $referenceOne)  {}
    }
}
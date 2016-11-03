<?php

/* core com.kalessil.phpStorm.phpInspectionsEA.classes with .compare_objects support */
class myDateTime extends DateTime {
}
function TUCObjectCompare() {
    return new myDateTime('now') == new DateTime('+1 seconds');
}

/* class comparison with string: needs to have __toString */
class myClassHasToString {
    public function __toString()
    {
        return '';
    }
}
class myMiddlewareClass extends myClassHasToString {
}
function TUCStringContext() {
    return '' == new myMiddlewareClass();
}
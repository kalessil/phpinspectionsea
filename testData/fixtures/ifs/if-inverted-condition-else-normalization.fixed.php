<?php

function returnsBool(): bool { return true; }
function returnsNullableBool(): ?bool { return true; }

if (returnsBool()) { c(); d(); }
else { a(); b(); }

if (returnsBool()) { c(); d(); }
else { a(); b(); }

if (!true)
{ a(); b(); }
elseif (false) { e(); f(); }
else { c(); d(); }

if (!true)
{ a(); b(); }
else if (false) { e(); f(); }
else { c(); d(); }

if (true === true) { c(); d(); }
else { a(); b(); }

if (false !== returnsBool()) { c(); d(); }
else { a(); b(); }

/* not supported: must follow PSR and use `{}` */
if (!true){a();b();}else c();
if (!true)a();else{b();c();}
if (!true) a();else{b();c();}
if (!true)a();else b();

/* false-positives */
if (!true) {}

if (!true && !false) {}
else {}

if (!empty([])) {}
else {}

if (false === returnsNullableBool())
{ a(); b(); }
else
{ c(); d(); }
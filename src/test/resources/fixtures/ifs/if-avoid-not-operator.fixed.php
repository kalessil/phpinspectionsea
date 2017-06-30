<?php

if (expr()) {
    c();
    d();
}
else {
    a();
    b();
}

if ( expr() ) {
    c();
    d();
}
else {
    a();
    b();
}

if (!true)
{ a(); b(); }
elseif (false) {
    e();
    f();
}
else {
    c();
    d();
}

if (!true)
{ a(); b(); }
else if (false) {
    e();
    f();
}
else {
    c();
    d();
}

if ((true === true)) {
    c();
    d();
}
else {
    a();
    b();
}

// Complex cases.
if (true) {
    c();
    d();
} else {
    a();
    b();
}

if (true) {
    c();
} else {
    a();
    b();
}

if (true) {
    b();
    c();
} else {
    a();
}

if (true) {
    b();
    c();
} else {
    a();
}

if (true) {
    b();
} else {
    a();
}

// False-positives.
if (!true)
{ }

if (!true && !false)
{ }
else
{ }

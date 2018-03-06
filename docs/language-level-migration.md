# Language level migration

## Alias functions usage

In PHP [some functions](http://php.net/manual/en/aliases.php) don't have own implementation and referencing to 
another functions. That's what called "alias functions". Alias functions are kept for maintaining backward compatibility.

In order to avoid surprises (e.g. mysqli_* aliases were dropped in PHP 5.4 or magic_quotes_runtime was dropped in PHP 7.0),
we encouraging to not rely on alias functions at all.

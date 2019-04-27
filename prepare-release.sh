#!/usr/bin/env bash
echo "<html><ul>" > change-notes.html
git log `git describe --tags --abbrev=0`..HEAD --no-merges --oneline --pretty=format:"<li>%h %s (%an)</li>" >> change-notes.html
echo "</ul></html>" >> change-notes.html

cp change-notes.html src/main/resources/META-INF/

rm change-notes.html

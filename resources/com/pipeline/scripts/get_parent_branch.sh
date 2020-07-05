#!/usr/bin/env bash
echo "Determining parent branch ..."
git show-branch -a \
| sed "s/].*//" \
| grep "\*" \
| grep -v "$(git rev-parse --abbrev-ref HEAD)" \
| head -n1 \
| sed "s/^.*\[//" \
| sed "s/\^.*//"
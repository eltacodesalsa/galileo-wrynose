#!/bin/sh
# Instala la compuerta de pre-push en este clon. Un clon nuevo NO la trae:
# .git/hooks no se versiona, por eso hay que ejecutar esto una vez.
set -eu
repo_root=$(git rev-parse --show-toplevel)
cp "$repo_root/scripts/hooks/pre-push" "$repo_root/.git/hooks/pre-push"
chmod +x "$repo_root/.git/hooks/pre-push"
echo "hook instalado en .git/hooks/pre-push"

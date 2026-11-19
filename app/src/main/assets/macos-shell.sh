# macOS-style Termux prompt and OSC window title.
if [ -n "$ZSH_VERSION" ]; then
    PROMPT='@efxd29 %~ % '
    autoload -Uz add-zsh-hook
    function _termux_macos_title() { printf '\033]0;zsh|%s\007' "$PWD" }
    add-zsh-hook precmd _termux_macos_title
elif [ -n "$BASH_VERSION" ] && [[ $- == *i* ]]; then
    if [ -x "$PREFIX/bin/zsh" ]; then exec "$PREFIX/bin/zsh" -l; fi
    PS1='@efxd29 \w % '
    PROMPT_COMMAND='printf "\033]0;bash|%s\007" "$PWD"'
fi

# macOS-style Termux prompt and OSC window title.
if [ -n "$ZSH_VERSION" ]; then
    PROMPT='@efxd29 %~ % '
    autoload -Uz add-zsh-hook
    function _termux_macos_title() {
        local label=zsh
        [ -n "$TMUX" ] && label=zsh
        local tmux=""
        [ -n "$TMUX" ] && tmux=tmux
        printf '\033]0;%s|%s|%s\007' "$label" "$PWD" "$tmux"
    }
    function _termux_macos_preexec() {
        local label="${1%% *}"
        label="${label##*/}"
        [ -n "$TMUX" ] && label="${label}"
        local tmux=""
        [ -n "$TMUX" ] && tmux=tmux
        printf '\033]0;%s|%s|%s\007' "$label" "$PWD" "$tmux"
    }
    add-zsh-hook precmd _termux_macos_title
    add-zsh-hook preexec _termux_macos_preexec
elif [ -n "$BASH_VERSION" ] && [[ $- == *i* ]]; then
    if [ -x "$PREFIX/bin/zsh" ]; then exec "$PREFIX/bin/zsh" -l; fi
    PS1='@efxd29 \w % '
    PROMPT_COMMAND='printf "\033]0;bash|%s|%s\007" "$PWD" "${TMUX:+tmux}"'
fi

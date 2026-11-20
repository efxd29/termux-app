# App-managed macOS-style Last login notice.
# This file is installed by TermuxActivity; no manual shell configuration is needed.
case "$-" in
    *i*) ;;
    *) return ;;
esac

# Avoid duplicate output if both login and interactive startup files source this hook.
[ "${TERMUX_APP_LAST_LOGIN_SHOWN:-0}" = "1" ] && return
TERMUX_APP_LAST_LOGIN_SHOWN=1
export TERMUX_APP_LAST_LOGIN_SHOWN

_termux_last_login_file="$HOME/.termux/last-login"
_termux_last_login_now="$(LC_ALL=C date '+%a %b %e %H:%M:%S' 2>/dev/null)"
_termux_last_login_tty="$(tty 2>/dev/null | sed 's#^/dev/##')"

if [ -r "$_termux_last_login_file" ] && [ ! -e "$HOME/.hushlogin" ]; then
    IFS='|' read -r _termux_previous_time _termux_previous_tty < "$_termux_last_login_file"
    if [ -n "$_termux_previous_time" ] && [ -n "$_termux_previous_tty" ]; then
        printf 'Last login: %s on %s\n' "$_termux_previous_time" "$_termux_previous_tty"
    fi
fi

# First startup has no saved record, so it prints nothing and only records this login.
if [ -n "$_termux_last_login_now" ] && [ -n "$_termux_last_login_tty" ]; then
    (umask 077; printf '%s|%s\n' "$_termux_last_login_now" "$_termux_last_login_tty" > "$_termux_last_login_file.tmp" &&
        mv -f "$_termux_last_login_file.tmp" "$_termux_last_login_file")
fi
unset _termux_last_login_file _termux_last_login_now _termux_last_login_tty
unset _termux_previous_time _termux_previous_tty

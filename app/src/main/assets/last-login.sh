# App-managed macOS-style Last login notice.
case "$-" in
    *i*) ;;
    *) return ;;
esac

[ "\${TERMUX_APP_LAST_LOGIN_SHOWN:-0}" = "1" ] && return
TERMUX_APP_LAST_LOGIN_SHOWN=1
export TERMUX_APP_LAST_LOGIN_SHOWN

_termux_last_login_file="$HOME/.termux/last-login"
_termux_last_login_now="$(LC_ALL=C date '+%a %b %e %H:%M:%S' 2>/dev/null)"
_termux_last_login_tty="$(readlink "/proc/$$/fd/0" 2>/dev/null)"

case "$_termux_last_login_tty" in
    /dev/pts/[0-9]*) _termux_last_login_tty="\${_termux_last_login_tty#/dev/}" ;;
    *) _termux_last_login_tty="$(tty 2>/dev/null | sed -n 's#^/dev/\(pts/[0-9][0-9]*\)$#\1#p')" ;;
esac

if [ -r "$_termux_last_login_file" ] && [ ! -e "$HOME/.hushlogin" ]; then
    IFS='|' read -r _termux_previous_time _termux_previous_tty < "$_termux_last_login_file"
    if [ -n "$_termux_previous_time" ] && [ -n "$_termux_previous_tty" ]; then
        printf 'Last login: %s on %s\n' "$_termux_previous_time" "$_termux_previous_tty"
    fi
fi

if [ -n "$_termux_last_login_now" ] && [ -n "$_termux_last_login_tty" ]; then
    (umask 077; printf '%s|%s\n' "$_termux_last_login_now" "$_termux_last_login_tty" > "$_termux_last_login_file.tmp" &&
        mv -f "$_termux_last_login_file.tmp" "$_termux_last_login_file")
fi
unset _termux_last_login_file _termux_last_login_now _termux_last_login_tty
unset _termux_previous_time _termux_previous_tty

# macOS-style Termux profile

These are user configuration examples for the customized app. The app does not overwrite files in a user's `$HOME` during installation.

## zsh prompt

Install zsh with `pkg install zsh`, then place this in `~/.zshrc`:

```zsh
NAMA="${NAMA:-efxd29}"
PROMPT="@${NAMA} ~ % "
```

The prompt intentionally uses `~` as requested. To display the current directory in the prompt as well, replace it with `PROMPT="@${NAMA} %~ % "`.

## Terminal colors

Create `~/.termux/colors.properties`:

```properties
background=#1e1e1e
foreground=#ffffff
```

Reload the style from Termux or restart the app.

## tmux true color and title bar controls

Add to `~/.tmux.conf`:

```tmux
set -g default-terminal "tmux-256color"
set -ag terminal-overrides ",*:RGB"
```

The three title bar buttons send tmux's default Ctrl-B prefix plus `x`, `d`, or `z`: kill-pane (tmux asks for confirmation), detach-client, and resize-pane -Z. They require a running tmux client using the default prefix key. Nerd Font glyphs require a font containing U+F10B5; put the font at `~/.termux/font.ttf`, then restart Termux.

## Optional custom folder-home glyph

To make a custom icon, convert the supplied folder-home SVG to a monochrome vector outline, import it into FontForge or Fontello, assign it to a private-use code point, export a TTF, and install it as `~/.termux/font.ttf`. If you use U+F10B5, the app title bar already requests that code point. The glyph design will follow the font's metrics and rendering; this does not embed the provided PNG as a bitmap.

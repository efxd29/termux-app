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

The three title bar buttons send tmux's default Ctrl-B prefix plus `x`, `d`, or `z`: kill-pane (tmux asks for confirmation), detach-client, and resize-pane -Z. They require a running tmux client using the default prefix key. The title bar folder icon uses the supplied PNG image asset.

## Optional custom font glyph

For other terminal glyphs, install a Nerd Font at `~/.termux/font.ttf` and restart Termux. The title bar folder icon is the supplied image asset and does not depend on a Nerd Font.

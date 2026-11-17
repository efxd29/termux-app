# Termux Claude Code Desktop-Inspired UI

## Product goal

Keep Termux as the real Termux terminal and Linux environment. Redesign the Android app shell so Claude Code's macOS terminal presentation is the visual reference. This is a native Android UI project, not a web mockup and not a replacement terminal.

## Non-negotiable compatibility

- Preserve the existing Termux package identity: `com.termux`.
- Preserve the upstream terminal emulator, shell/session lifecycle, PTY, input pipeline, process execution, package manager, storage permissions, intents, plugins, and terminal escape-sequence behavior.
- Do not replace the terminal renderer with a WebView or fake terminal.
- Do not change the user's `$HOME`, `$PREFIX`, installed packages, shell startup files, `termux.properties`, extra-key configuration, or keyboard layout.
- Do not uninstall or wipe app data.
- Keep upstream Termux features available; visual redesign must not silently remove functionality.
- Keep the existing GitHub debug signing setup for test builds. Never claim compatibility with an installed APK until its signing certificate is verified.

## Visual direction

Reference: Claude Code running inside macOS Terminal, as shown in the user-provided screenshots.

### Main terminal screen

- Full-screen terminal workspace with a dark graphite/near-black canvas.
- Compact macOS-like title bar at the top: three traffic-light window controls (decorative on Android), folder/workspace icon, current session or working directory, and app/session title.
- Claude-like bordered welcome panel rendered as a native terminal/UI overlay, not by changing Claude Code output.
- Welcome panel has a left identity/status area and a right tips/recent-activity area when there is enough width. On narrow screens it must reflow vertically without clipping.
- Terminal transcript remains the real Termux terminal output and keeps its original scrollback and selection behavior.
- Input row stays visually distinct with a thin separator and a clear prompt/cursor.
- Existing extra-keys toolbar remains accessible and configurable. It may be visually restyled, but its actions and custom layout must remain intact.
- Keep Android system navigation and status bars behaving correctly; no fake macOS system chrome.

### Themes

- Dark theme: near-black background, charcoal surfaces, warm orange accent, readable off-white text, muted grey secondary text.
- Light theme: warm off-white background, white/very-light panels, dark text, restrained orange accent.
- Theme selection must be available in app settings and persist across restarts.
- Respect Android system theme as an optional follow-system mode.
- Maintain accessible contrast and avoid hard-coded colors in terminal output.

### Responsive behavior

- Phone portrait: compact title/header, single-column welcome content, terminal receives maximum vertical space, extra keys remain usable.
- Phone landscape/tablet: two-column welcome panel and wider desktop-like header.
- Handle keyboard open/closed states, display cutouts, edge-to-edge insets, font scaling, and rotation without losing sessions.

## Claude Code compatibility

The UI is only a visual shell. Claude Code must continue to run as an ordinary process inside the real Termux session.

- Do not parse, intercept, rewrite, or special-case Claude Code commands.
- Do not change Claude Code model selection, modes, slash commands, keybindings, permissions, MCP, hooks, skills, tools, billing, or output.
- Do not require Claude Code to be installed for Termux itself to work.
- The welcome panel may show generic session metadata, but must not fabricate Claude account, model, billing, or usage information.
- All keyboard shortcuts and terminal input must pass through unchanged.

## Implementation sequence

1. Establish a reproducible GitHub Actions debug build for this branch.
2. Implement the native app chrome and responsive welcome panel without touching terminal execution internals.
3. Add persistent dark/light/system theme setting.
4. Restyle existing session and extra-key surfaces without changing their behavior.
5. Add regression checks for session persistence, keyboard/input, selection/copy, storage, plugins, and orientation.
6. Build and inspect an ARM64 APK; verify its package ID and signing certificate before attempting an update install.

## Acceptance checklist

- [ ] Actual Termux terminal still runs arbitrary shell commands unchanged.
- [ ] Claude Code launches and behaves normally; UI does not intercept its commands.
- [ ] Dark, light, and follow-system modes persist.
- [ ] Welcome panel matches the supplied macOS Claude Code reference in layout and visual hierarchy.
- [ ] Narrow screens reflow without hiding terminal input or extra keys.
- [ ] Existing extra-key configuration and keyboard layout remain unchanged.
- [ ] Existing sessions, home directory, packages, and configuration survive app update.
- [ ] Copy, selection, paste, scrollback, and context menu still work.
- [ ] ARM64 APK builds in GitHub Actions.
- [ ] APK signature is verified against the installed Termux before update installation.

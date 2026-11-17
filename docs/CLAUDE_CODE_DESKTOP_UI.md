# Termux plus Claude Code companion app

## Scope

This repository builds the Termux Android terminal application. Termux remains a general-purpose terminal; it is not turned into a Claude-only application and its existing terminal UI is not replaced with a macOS imitation.

A separate Android Claude Code companion app will be developed later. Its visual reference is the user-provided Claude Code macOS terminal welcome screen, adapted to portrait mobile. That companion app must use the real Claude Code CLI installed in the Termux environment.

## Termux requirements

- Preserve upstream Termux terminal rendering, PTY/session lifecycle, shell execution, package management, storage permissions, session management, terminal settings, plugins integration points, and arbitrary command behavior.
- Preserve every existing extra-key in the user's configured keyboard layout and append a dedicated action row only.
- Do not overwrite `~/.termux/termux.properties`, shell startup files, `$HOME`, `$PREFIX`, installed packages, or existing keyboard settings.
- Add these extra-key actions:
  - `LAST RUN`: select the most recent command execution (command plus terminal output, up to the next prompt).
  - `COPY RUN`: copy that execution to the Android clipboard.
  - `SELECT ALL`: select all terminal scrollback and visible terminal text.
  - `COPY ALL`: copy the terminal transcript.
- If command-boundary detection cannot confidently identify a prompt, show a clear message rather than selecting an arbitrary part of the transcript.
- Keep all normal Termux actions and keys unchanged.

## Claude Code companion app requirements

- Claude Code CLI is installed and updated inside Termux, not bundled or modified by this app.
- The companion app must not patch, fork, reimplement, or vendor Claude Code's CLI engine.
- The app communicates with a Termux-owned local service through explicit Android IPC.
- All shell execution, tool execution, session state, project files, model configuration, and ZRO router processes remain owned by the Termux environment.
- The UI is a native Android presentation layer inspired by Claude Code on macOS: dark/light themes, a compact title/header, welcome panel, model/context/account/project information, tips, recent activity, message timeline, and bottom composer.
- Any status or account detail shown must come from real CLI/service state. Never fabricate model, billing, context, or session information.
- Claude Code features (commands, modes, permissions, hooks, skills, MCP, tools, and sessions) must remain those of the installed CLI.

## Build and signing notes

- This branch currently uses upstream Termux's original application ID and GitHub debug build signing configuration. A GitHub-signed APK is not an in-place update for the user's F-Droid-signed installation.
- A separately installable clone requires a unique application ID and a matching custom bootstrap/package build. Upstream package binaries cannot simply be reused with a different package prefix.
- Do not claim the APK is ready for side-by-side installation until its package ID, bootstrap prefix, ABI, and signature have been validated.

## Acceptance checklist

- [ ] Termux still behaves as a general-purpose terminal.
- [ ] Existing extra-key buttons remain in the same order and keep their original behavior.
- [ ] The four new actions work and do not overwrite the user's keyboard config.
- [ ] Last-execution selection is bounded to one prompt-to-prompt command run.
- [ ] Existing scrollback, copy, selection, paste, and session behavior continue to work.
- [ ] Claude Code CLI is not modified.
- [ ] Companion app uses a Termux-owned IPC service and the same files/processes.
- [ ] Dark/light modes persist in the companion app.
- [ ] Install/update safety is verified before distributing an APK.

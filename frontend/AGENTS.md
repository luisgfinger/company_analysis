# AGENTS.md

# Agent Instructions

## Notification behavior

This rule is conditional and applies only when the current user request includes the keyword `afk`.
Treat `afk` as an explicit opt-in flag for the notification script.

If the current user request does not include `afk`, do not execute the notification script at any point.

If the current user request includes `afk` and the agent is about to ask the user to authorize an escalated command, execute the notification script SYNCHRONOUSLY immediately before sending the authorization request.
Only notify once per distinct authorization wait.
Do not repeatedly execute the script while waiting on the same pending approval.

If the current user request includes `afk`, execute the notification script SYNCHRONOUSLY again before sending any final response after completing the requested work.
When `afk` is present, the task is not complete until the completion notification script execution has finished.

An authorization-wait notification does not replace the completion notification.
If both events happen, the script should run once before the authorization request and once before the final response.

If the script cannot be executed when `afk` is present, explicitly report that failure in the authorization request or final response, as applicable.

Use PowerShell in the current session so the sound plays reliably.
Do not use `Start-Process` for this notification.
Wait for it to finish before sending the authorization request or final response when `afk` is present.
Do not replace this with another notification mechanism.
The required command uses a path relative to the parent directory that contains the `agent-tools` folder.
Do not hardcode or document any user-specific absolute path in this file.
If the current working directory is already the `agent-tools` directory, change only the working directory context to its parent and then run the command exactly as written.
When `afk` is present, always use this command:

```powershell
& .\agent-tools\notify-done.ps1
```

Examples:

- `say hello world` -> do not execute the script.
- `say hello world afk` -> execute the script before the final response.
- `afk say hello world` -> execute the script before the final response.
- `afk: say hello world` -> execute the script before the final response.
- `afk run tests that require approval` -> execute the script before the authorization request, and execute it again before the final response if the task is later completed.

## Project architecture

In any code change, refactor, bug fix, or new feature, follow the existing project architecture before implementing anything.

Inspect the relevant structure, module boundaries, naming conventions, and responsibility split of the codebase before making changes.

Keep each change aligned with the established architecture for the project or subproject being modified.
Do not introduce code in arbitrary folders, bypass existing layers, or mix concerns that are currently separated.

Place new functionality in the appropriate architectural layer or module, reusing existing patterns and shared abstractions when they already exist.
Prefer extending the current design over creating parallel structures that duplicate responsibilities.

When a requested change appears to conflict with the current architecture, do not silently improvise a new structure.
Instead, either adapt the solution to the existing architecture or explicitly surface the architectural conflict and ask for direction if needed.

If implementing a feature requires creating a new architectural pattern, module type, or cross-cutting abstraction, do so only when the existing structure clearly cannot support the change cleanly.
In that case, keep the design minimal, consistent with the rest of the repository, and explain the rationale in the response.


## CSS and styling changes

In any frontend or UI-related change, do not create new CSS classes and do not alter existing CSS classes without prior user authorization.

Before introducing any new class or changing the definition of an existing one, explicitly ask for approval.

When styling changes are needed, prefer reusing the existing CSS classes and styling patterns already present in the project whenever possible.

Also prefer existing project classes over applying direct Tailwind utility classes for colors, spacing, typography, or similar visual decisions.

Use direct Tailwind styling only when it is already the established local pattern and existing classes cannot support the change cleanly without introducing unnecessary duplication.
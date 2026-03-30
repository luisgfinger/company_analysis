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

param(
    [int]$TotalDurationSeconds = 10
)

if ($TotalDurationSeconds -le 0) {
    Write-Host "The duration must be greater than zero."
    exit 1
}

$melody = @(
    @{ F = 440; D = 500 }
    @{ F = 440; D = 500 }
    @{ F = 440; D = 500 }
    @{ F = 349; D = 350 }
    @{ F = 523; D = 150 }
    @{ F = 440; D = 500 }
    @{ F = 349; D = 350 }
    @{ F = 523; D = 150 }
    @{ F = 440; D = 700 }
    @{ F = 659; D = 500 }
    @{ F = 659; D = 500 }
    @{ F = 659; D = 500 }
    @{ F = 698; D = 350 }
    @{ F = 523; D = 150 }
    @{ F = 415; D = 500 }
    @{ F = 349; D = 350 }
    @{ F = 523; D = 150 }
    @{ F = 440; D = 700 }
)

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$totalMilliseconds = $TotalDurationSeconds * 1000

try {
    while ($stopwatch.ElapsedMilliseconds -lt $totalMilliseconds) {
        foreach ($note in $melody) {
            $remaining = $totalMilliseconds - $stopwatch.ElapsedMilliseconds
            if ($remaining -le 0) {
                break
            }

            $duration = [Math]::Min($note.D, $remaining)

            if ($note.F -gt 37 -and $note.F -lt 32767) {
                [Console]::Beep($note.F, [int]$duration)
            } else {
                Start-Sleep -Milliseconds $duration
            }
        }
    }
}
catch {
    Write-Host "Unable to play beeps in this environment."
}

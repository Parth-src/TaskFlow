$cp = Get-Content (Join-Path $PSScriptRoot "..\cp.txt") -Raw
$fullCp = "target\classes;" + $cp.Trim()
java -cp $fullCp com.project.taskflow.worker.server.JavaWorkerServer

$JavaVersion = "21"
$CommonDeps  = "validation,actuator,lombok,testcontainers"

function New-NextbankService {
    param(
        [string]$Artifact,
        [string]$Package,
        [string]$Deps
    )
    Write-Host "Generando $Artifact..."

    $params = [ordered]@{
        type         = "maven-project"
        language     = "java"
        javaVersion  = $JavaVersion
        groupId      = "com.nextbank"
        artifactId   = $Artifact
        packageName  = $Package
        dependencies = $Deps
    }

    $query = ($params.GetEnumerator() | ForEach-Object {
        "$($_.Key)=$([uri]::EscapeDataString($_.Value))"
    }) -join "&"

    $uri = "https://start.spring.io/starter.zip?$query"
    $zipPath = "$Artifact.zip"

    try {
        Invoke-WebRequest -Uri $uri -OutFile $zipPath -UseBasicParsing -ErrorAction Stop
    } catch {
        Write-Host "  ERROR descargando ${Artifact}: $($_.Exception.Message)" -ForegroundColor Red
        return
    }

    $fileInfo = Get-Item $zipPath
    if ($fileInfo.Length -lt 1000) {
        Write-Host "  El archivo descargado para $Artifact no parece un zip valido (solo $($fileInfo.Length) bytes)." -ForegroundColor Red
        Write-Host "  Contenido recibido:" -ForegroundColor Yellow
        Get-Content $zipPath | Select-Object -First 10
        Remove-Item $zipPath
        return
    }

    Expand-Archive -Path $zipPath -DestinationPath $Artifact -Force
    Remove-Item $zipPath
    Remove-Item "$Artifact\.gitignore" -ErrorAction SilentlyContinue
    Write-Host "  $Artifact generado correctamente." -ForegroundColor Green
}

New-NextbankService -Artifact "identity-auth-service" -Package "com.nextbank.identityauth" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,$CommonDeps"
New-NextbankService -Artifact "account-service" -Package "com.nextbank.account" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,kafka,$CommonDeps"
New-NextbankService -Artifact "transfers-service" -Package "com.nextbank.transfers" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,kafka,$CommonDeps"
New-NextbankService -Artifact "notifications-service" -Package "com.nextbank.notifications" -Deps "web,data-jpa,postgresql,kafka,$CommonDeps"

Write-Host "Proceso terminado."

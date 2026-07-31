$BootVersion = "3.3.0"
$JavaVersion = "21"
$CommonDeps  = "validation,actuator,lombok,testcontainers"

function New-NextbankService {
    param(
        [string]$Artifact,
        [string]$Package,
        [string]$Deps
    )
    Write-Host "Generando $Artifact..."

    $curlArgs = @(
        "-s",
        "https://start.spring.io/starter.zip",
        "-d", "type=maven-project",
        "-d", "language=java",
        "-d", "bootVersion=$BootVersion",
        "-d", "javaVersion=$JavaVersion",
        "-d", "groupId=com.nextbank",
        "-d", "artifactId=$Artifact",
        "-d", "packageName=$Package",
        "-d", "dependencies=$Deps",
        "-o", "$Artifact.zip"
    )

    & curl.exe @curlArgs

    Expand-Archive -Path "$Artifact.zip" -DestinationPath $Artifact -Force
    Remove-Item "$Artifact.zip"
    Remove-Item "$Artifact\.gitignore" -ErrorAction SilentlyContinue
}

New-NextbankService -Artifact "identity-auth-service" -Package "com.nextbank.identityauth" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,$CommonDeps"
New-NextbankService -Artifact "account-service" -Package "com.nextbank.account" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,kafka,$CommonDeps"
New-NextbankService -Artifact "transfers-service" -Package "com.nextbank.transfers" -Deps "web,data-jpa,postgresql,security,oauth2-resource-server,kafka,resilience4j,$CommonDeps"
New-NextbankService -Artifact "notifications-service" -Package "com.nextbank.notifications" -Deps "web,data-jpa,postgresql,kafka,$CommonDeps"

Write-Host "Listo. Los 4 servicios estan generados."

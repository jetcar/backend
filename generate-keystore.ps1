# PowerShell script to generate a self-signed SSL certificate for Spring Boot
$keyStoreFile = "keystore.p12"
$keyStorePassword = "changeit"
$keyAlias = "springboot"

$keytool = "keytool"

& $keytool -genkeypair -alias $keyAlias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore $keyStoreFile -validity 365 -storepass $keyStorePassword -dname "CN=localhost, OU=Dev, O=Company, L=City, S=State, C=US"
Write-Host "Keystore generated: $keyStoreFile"
Write-Host "Password: $keyStorePassword"

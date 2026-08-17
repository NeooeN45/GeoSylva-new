plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Module Kotlin pur (JVM), sans dépendance Android/Compose/ViewModel/Room —
// voir PROMPT_03 (pack de transmission 2026-07-19). Vérifie hors ligne le
// contrat de capsule territoriale produit par Quintessences/gsie_execution_kit
// (Python). Testable directement sur JVM via `./gradlew :capsule-verifier:test`,
// sans émulateur Android.

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Ed25519 : le JDK natif (java.security) ne le supporte qu'à partir
    // d'Android API 33+, incompatible avec le minSdk 26 de l'app — Bouncy
    // Castle donne un comportement identique sur toutes les API cibles.
    implementation(libs.bouncycastle.provider)
    // Lecture ZIP bas niveau (bits de drapeau général, tailles déclarées) —
    // nécessaire pour rejeter les membres chiffrés et calculer le ratio de
    // compression comme le fait `zipfile` côté Python ; `java.util.zip` seul
    // n'expose pas les bits de drapeau général.
    implementation(libs.commons.compress)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

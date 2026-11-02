plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "com.rowell.shareme"
    compileSdk = 36
    buildToolsVersion = "35.0.0"
    defaultConfig { applicationId = "com.rowell.shareme"; minSdk = 29; targetSdk = 36; versionCode = 6; versionName = "1.1.0" }
    signingConfigs {
        create("rowell") {
            storeFile = rootDir.resolve("../.local/signing/shareme.jks")
            storePassword = rootDir.resolve("../.local/signing/password.txt").takeIf { it.exists() }?.readText()?.trim()
            keyAlias = "shareme"; keyPassword = storePassword
        }
    }
    buildTypes { release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"); signingConfig = signingConfigs.getByName("rowell") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
}
tasks.withType<Test>().configureEach {
    systemProperty("sharemode.screenshots",rootDir.resolve("../.local/ui-1.1.0").absolutePath)
    systemProperty("maven.repo.local",rootDir.resolve("../.local/robolectric-maven").absolutePath)
    systemProperty("robolectric.dependency.repo.url","https://repo.maven.apache.org/maven2")
}

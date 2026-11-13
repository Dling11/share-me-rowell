import java.awt.image.BufferedImage

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "com.rowell.shareme"
    compileSdk = 36
    buildToolsVersion = "35.0.0"
    defaultConfig { applicationId = "com.rowell.shareme"; minSdk = 29; targetSdk = 36; versionCode = 15; versionName = "1.2.7" }
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
    systemProperty("sharemode.screenshots",rootDir.resolve("../.local/ui-1.2.7").absolutePath)
    systemProperty("maven.repo.local",rootDir.resolve("../.local/robolectric-maven").absolutePath)
    systemProperty("robolectric.dependency.repo.url","https://repo.maven.apache.org/maven2")
}

// Compile the generated art at its in-game pixel resolution. The high-resolution source stays
// outside res/, so it cannot bloat the APK; the authored artwork itself is preserved unchanged.
val compilePixelArmory by tasks.registering {
    val source=rootDir.resolve("art/pixel-armory-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_armory.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height && image.width>=256 && image.colorModel.hasAlpha()){"Invalid pixel armor source"}
        val pixels=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        var transparent=0
        for(y in 0 until 256)for(x in 0 until 256){
            val colour=image.getRGB(((x+.5)*image.width/256).toInt().coerceAtMost(image.width-1),((y+.5)*image.height/256).toInt().coerceAtMost(image.height-1))
            val compiled=if((colour ushr 24)<128){transparent++;0}else colour or (0xff shl 24)
            pixels.setRGB(x,y,compiled)
        }
        check(transparent>256*256/3){"Pixel armor source must have a transparent background"}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
android.sourceSets.named("main"){res.srcDir(layout.buildDirectory.dir("generated/pixel-art"))}
tasks.named("preBuild"){dependsOn(compilePixelArmory)}

val compilePixelHeads by tasks.registering {
    val source=rootDir.resolve("art/pixel-heads-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_heads.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height&&image.width>=192&&image.colorModel.hasAlpha()){"Invalid pixel head source"}
        val pixels=BufferedImage(192,192,BufferedImage.TYPE_INT_ARGB)
        var transparent=0
        for(y in 0 until 192)for(x in 0 until 192){
            val colour=image.getRGB(((x+.5)*image.width/192).toInt().coerceAtMost(image.width-1),((y+.5)*image.height/192).toInt().coerceAtMost(image.height-1))
            val compiled=if((colour ushr 24)<128){transparent++;0}else colour or (0xff shl 24)
            pixels.setRGB(x,y,compiled)
        }
        check(transparent>192*192/3){"Pixel head source must have a transparent background"}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelHeads)}

val compilePixelProgression by tasks.registering {
    val source=rootDir.resolve("art/pixel-progression-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_progression.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height&&image.width>=256&&image.colorModel.hasAlpha()){"Invalid progression atlas"}
        val pixels=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        var transparent=0
        for(y in 0 until 256)for(x in 0 until 256){
            val colour=image.getRGB(((x+.5)*image.width/256).toInt().coerceAtMost(image.width-1),((y+.5)*image.height/256).toInt().coerceAtMost(image.height-1))
            val compiled=if((colour ushr 24)<128){transparent++;0}else colour or (0xff shl 24)
            pixels.setRGB(x,y,compiled)
        }
        check(transparent>256*256/3){"Progression atlas requires transparency"}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelProgression)}

val compilePixelAnime by tasks.registering {
    val source=rootDir.resolve("art/pixel-anime-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_anime.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height&&image.width>=256&&image.colorModel.hasAlpha()){"Invalid anime equipment source"}
        val sampled=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        for(y in 0 until 256)for(x in 0 until 256){val c=image.getRGB(((x+.5)*image.width/256).toInt(),((y+.5)*image.height/256).toInt());sampled.setRGB(x,y,if(c ushr 24<128)0 else c or (0xff shl 24))}
        val pixels=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        for(slot in 0 until 16){
            val ox=slot%4*64;val oy=slot/4*64
            val top=(8..48).first{y->(25..39).any{x->sampled.getRGB(ox+x,oy+y) ushr 24>=128}}
            val baseline=(63 downTo 45).first{y->(23..41).any{x->sampled.getRGB(ox+x,oy+y) ushr 24>=128}}
            // Fit the authored body to the existing collar/boot anchors; don't crop off shoulders.
            for(y in 0 until 64)for(x in 0 until 64){
                val sy=Math.round(top+(y-35)*(baseline-top)/26.0).toInt()
                val c=if(sy in 0..63&&!(x in 18..46&&y<35))sampled.getRGB(ox+x,oy+sy)else 0
                pixels.setRGB(ox+x,oy+y,c)
            }
        }
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelAnime)}


val compilePixelFocus by tasks.registering {
    val source=rootDir.resolve("art/pixel-focus-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_focus.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height&&image.width>=96&&image.colorModel.hasAlpha()){"Invalid focused face source"}
        val pixels=BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB)
        for(y in 0 until 96)for(x in 0 until 96){val c=image.getRGB(((x+.5)*image.width/96).toInt(),((y+.5)*image.height/96).toInt());pixels.setRGB(x,y,if(c ushr 24<128)0 else c or (0xff shl 24))}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelFocus)}

val compilePixelSpecial by tasks.registering {
    val source=rootDir.resolve("art/pixel-special-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_special.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width*3==image.height*2&&image.width>=256&&image.colorModel.hasAlpha()){"Invalid complete-character sheet"}
        val sampled=BufferedImage(256,384,BufferedImage.TYPE_INT_ARGB)
        for(y in 0 until 384)for(x in 0 until 256){val c=image.getRGB(((x+.5)*image.width/256).toInt(),((y+.5)*image.height/384).toInt());sampled.setRGB(x,y,if(c ushr 24<128)0 else c or (0xff shl 24))}
        val pixels=BufferedImage(256,384,BufferedImage.TYPE_INT_ARGB)
        var transparent=0
        for(row in 0 until 6)for(cell in 0 until 4){
            val ox=cell*64;val oy=row*64
            val baseline=(63 downTo 40).first{y->(18..46).any{x->sampled.getRGB(ox+x,oy+y) ushr 24>=128}}
            val shift=(61-baseline).coerceIn(-8,8)
            for(y in 0 until 64)for(x in 0 until 64){val sy=y-shift;val c=if(sy in 0..63)sampled.getRGB(ox+x,oy+sy)else 0;pixels.setRGB(ox+x,oy+y,c);if(c ushr 24==0)transparent++}
        }
        check(transparent>256*384/3){"Complete characters must have transparent gutters"}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelSpecial)}

val compilePixelYugi by tasks.registering {
    val source=rootDir.resolve("art/pixel-yugi-source.png")
    val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/pixel_yugi.png")
    inputs.file(source);outputs.file(output)
    doLast {
        val image=checkNotNull(javax.imageio.ImageIO.read(source))
        check(image.width==image.height&&image.width>=256&&image.colorModel.hasAlpha()){"Invalid duelist sprite sheet"}
        val sampled=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        for(y in 0 until 256)for(x in 0 until 256){val c=image.getRGB(((x+.5)*image.width/256).toInt(),((y+.5)*image.height/256).toInt());sampled.setRGB(x,y,if(c ushr 24<128)0 else c or (0xff shl 24))}
        val pixels=BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB)
        var transparent=0
        for(row in 0 until 4)for(cell in 0 until 4){
            val ox=cell*64;val oy=row*64
            val baseline=(63 downTo 40).first{y->(18..46).any{x->sampled.getRGB(ox+x,oy+y) ushr 24>=128}}
            val shift=(61-baseline).coerceIn(-8,8)
            for(y in 0 until 64)for(x in 0 until 64){val sy=y-shift;val c=if(sy in 0..63)sampled.getRGB(ox+x,oy+sy)else 0;pixels.setRGB(ox+x,oy+y,c);if(c ushr 24==0)transparent++}
        }
        check(transparent>256*256/3){"Duelist sprite sheet must have transparent gutters"}
        output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
    }
}
tasks.named("preBuild"){dependsOn(compilePixelYugi)}

// One complete character per source, twelve cels for each of two earned outfits.
listOf("luffy","zoro","boa","shanks","eren","levi","mikasa","sukuna","itachi","rock-lee","might-guy","madara").forEach { slug ->
    val compileSolo=tasks.register("compilePixel"+slug.split('-').joinToString(""){it.replaceFirstChar{c->c.uppercaseChar()}}) {
        val source=rootDir.resolve("art/pixel-$slug-source.png")
        val resource="pixel_"+slug.replace('-','_')
        val output=layout.buildDirectory.file("generated/pixel-art/drawable-nodpi/$resource.png")
        inputs.file(source);outputs.file(output)
        doLast {
            val image=checkNotNull(javax.imageio.ImageIO.read(source))
            check(image.width*3==image.height*2&&image.width>=256&&image.colorModel.hasAlpha()){"Invalid solo character sheet: $slug"}
            val sampled=BufferedImage(256,384,BufferedImage.TYPE_INT_ARGB)
            for(y in 0 until 384)for(x in 0 until 256){val c=image.getRGB(((x+.5)*image.width/256).toInt(),((y+.5)*image.height/384).toInt());sampled.setRGB(x,y,if(c ushr 24<128)0 else c or (0xff shl 24))}
            val pixels=BufferedImage(256,384,BufferedImage.TYPE_INT_ARGB)
            var transparent=0
            for(row in 0 until 6)for(cell in 0 until 4){
                val ox=cell*64;val oy=row*64
                val baseline=(63 downTo 40).first{y->(18..46).any{x->sampled.getRGB(ox+x,oy+y) ushr 24>=128}}
                val shift=(61-baseline).coerceIn(-8,8)
                var visible=0
                for(y in 0 until 64)for(x in 0 until 64){val sy=y-shift;val c=if(sy in 0..63)sampled.getRGB(ox+x,oy+sy)else 0;pixels.setRGB(ox+x,oy+y,c);if(c ushr 24==0)transparent++ else visible++}
                check(visible>150){"Missing solo cel in $slug at $row/$cell"}
            }
            check(transparent>256*384/3){"Solo characters require transparent gutters: $slug"}
            output.get().asFile.apply{parentFile.mkdirs();check(javax.imageio.ImageIO.write(pixels,"png",this))}
        }
    }
    tasks.named("preBuild"){dependsOn(compileSolo)}
}

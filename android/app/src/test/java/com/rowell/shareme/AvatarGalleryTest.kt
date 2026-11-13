package com.rowell.shareme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.compose.setContent
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w600dp-h1600dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvatarGalleryTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun renderEveryCharacterInBothLooksWithoutChangingRealProgress(){
        compose.activity.setContent{ShareTheme{Surface(color=Bg){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            Text("Share Mode · Pixel adventurers",style=MaterialTheme.typography.headlineMedium)
            Text("Personal Boy/Girl looks · Signature starter/mastery outfits",color=Muted)
            Adventure.roles.indices.sortedBy{Adventure.roles[it].level}.chunked(3).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                row.forEach{i->Surface(color=Panel,shape=MaterialTheme.shapes.medium,modifier=Modifier.weight(1f)){Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Row{for(variant in 0..1){val special=Adventure.isSpecial(i);val label=if(special)if(variant==0)"Starter"else "Mastery"else if(variant==0)"Boy"else "Girl"
                        ChibiAvatar(AvatarStyle(hair=if(variant==0)1 else 3,look=variant,hairColour=5,role=i),Modifier.size(68.dp),"$label ${Adventure.roles[i].name}",if(special&&variant==1)100 else Adventure.roles[i].level)
                    }}
                    Text(Adventure.roles[i].name,style=MaterialTheme.typography.labelMedium)
                    Text("Lv. ${Adventure.roles[i].level} · ${if(Adventure.isSpecial(i))"Fixed"else "Customizable"}",color=Muted,style=MaterialTheme.typography.labelSmall)
                }}};if(row.size<3)repeat(3-row.size){Spacer(Modifier.weight(1f))}
            }}
            Text("Cosmetic unlocks. Sharing stays fast at every level.",color=Muted)
        }}}}
        Adventure.roles.forEachIndexed{i,role->for(label in if(Adventure.isSpecial(i))listOf("Starter","Mastery")else listOf("Boy","Girl"))compose.onNodeWithContentDescription("$label ${role.name}").assertIsDisplayed()}
        compose.runOnIdle{
            val global=ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"),"getInstance")
            val views=ReflectionHelpers.getField<List<View>>(global,"mViews");val view=views.last{it.width>0&&it.height>0}
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap))
            val target=File(checkNotNull(System.getProperty("sharemode.screenshots")),"adventurer-gallery.png");target.parentFile!!.mkdirs();target.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
}

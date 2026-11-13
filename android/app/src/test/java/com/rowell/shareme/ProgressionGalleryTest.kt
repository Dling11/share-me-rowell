package com.rowell.shareme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w640dp-h1260dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProgressionGalleryTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun renderTheCompleteNoviceToLevel100Roadmap(){
        compose.activity.setContent{ShareTheme{Surface(color=Bg){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            Text("Share Mode · Level 1 → 100",style=MaterialTheme.typography.headlineMedium)
            Text("Simple beginnings. Rare armour, wings and ascension later.",color=Muted)
            Adventure.gearMilestones.chunked(2).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){
                row.forEach{step->Surface(color=Panel,shape=MaterialTheme.shapes.medium,modifier=Modifier.weight(1f)){Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    val gear=Adventure.gear(step.role,step.level)
                    Row{for(look in 0..1)ChibiAvatar(AvatarStyle(hair=if(look==0)1 else 3,hairColour=5,look=look,role=step.role),Modifier.size(105.dp),"Level ${step.level} ${if(look==0)"Boy" else "Girl"}",step.level)}
                    Text("Lv. ${step.level} · ${gear.title}",style=MaterialTheme.typography.titleSmall)
                    Text("${java.text.NumberFormat.getIntegerInstance().format(Adventure.threshold(step.level))} total XP",color=Muted,style=MaterialTheme.typography.bodySmall)
                }}};if(row.size==1)Spacer(Modifier.weight(1f))
            }}
            Text("Hair and colours stay free. Gear evolves with your earned level.",color=Muted)
            Text("Example previews · no player XP or unlocks changed",color=Muted,style=MaterialTheme.typography.bodySmall)
        }}}}
        compose.onNodeWithContentDescription("Level 1 Boy").assertIsDisplayed()
        compose.onNodeWithContentDescription("Level 100 Girl").assertIsDisplayed()
        compose.runOnIdle{
            val global=ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"),"getInstance")
            val views=ReflectionHelpers.getField<List<View>>(global,"mViews");val view=views.last{it.width>0&&it.height>0}
            val image=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(image))
            val file=File(checkNotNull(System.getProperty("sharemode.screenshots")),"gear-progression.png");file.parentFile!!.mkdirs();file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
}

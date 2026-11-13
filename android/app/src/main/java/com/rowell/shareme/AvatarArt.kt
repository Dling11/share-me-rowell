package com.rowell.shareme

import android.animation.ValueAnimator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.*

internal val skinTones=listOf(0xFFFFD9BC,0xFFEFB58D,0xFFD59467,0xFFAB704C,0xFF74472F).map{Color(it)}
internal val hairTones=listOf(0xFF302536,0xFF79492E,0xFFE7B95B,0xFFB073AD,0xFF4B8095,0xFFD8DDED).map{Color(it)}
internal val shirtTones=listOf(0xFF58D6B0,0xFF9775E8,0xFFE06185,0xFFE9BE59,0xFF69A9E2).map{Color(it)}
internal val elementTones=listOf(0xFF70E2C2,0xFFFFA071,0xFF85C9FF,0xFFA6E48B,0xFFBFA3FF,0xFFFFDA89).map{Color(it)}

/** Complete approved portraits throughout the app; motion never changes equipment or XP. */
@Composable fun ChibiAvatar(style:AvatarStyle,modifier:Modifier=Modifier.size(64.dp),description:String="RPG avatar",level:Int=1,animated:Boolean=false,interactive:Boolean=false,locked:Boolean=false,gearLevel:Int=level,skillRequest:Int=0,autoSkill:Boolean=false) {
    val context=LocalContext.current;val preferences by Model.state.collectAsState();val pixel=preferences.avatarPixels
    val role=CharacterRoster.current(style.role);val current=style.copy(role=role)
    val moving=animated&&ValueAnimator.areAnimatorsEnabled()
    val phase=if(moving)rememberInfiniteTransition(label="Character idle").animateFloat(0f,1f,infiniteRepeatable(tween(9600,easing=LinearEasing)),label="Idle cycle")else remember{mutableFloatStateOf(0f)}
    val pose by remember(phase){derivedStateOf{PixelIdle.pose(phase.value)}}
    val cheer=remember{Animatable(0f)};val scope=rememberCoroutineScope()
    val skill=remember(role,gearLevel){Animatable(-1f)}
    val elapsed by remember(skill){derivedStateOf{if(skill.value<0)-1L else skill.value.toLong()/33*33}}
    var job by remember{mutableStateOf<Job?>(null)}
    val hop by remember{derivedStateOf{(cheer.value*4).roundToInt()}}
    val skillAvailable=CharacterSkills.available(role,gearLevel)
    suspend fun play(){if(!moving||!skillAvailable)return;try{skill.snapTo(0f);skill.animateTo(CharacterSkills.duration(role).toFloat(),tween(CharacterSkills.duration(role).toInt(),easing=LinearEasing))}finally{skill.snapTo(-1f)}}
    LaunchedEffect(moving,role,gearLevel){job?.cancel();skill.snapTo(-1f);if(!moving){cheer.stop();cheer.snapTo(0f)}}
    LaunchedEffect(skillRequest){if(skillRequest>0&&moving&&skillAvailable){job?.cancel();job=scope.launch{play()}}}
    LaunchedEffect(role,gearLevel,autoSkill){if(autoSkill&&moving&&skillAvailable&&level>=CharacterSkills.unlock(role)){job?.cancel();job=scope.launch{play()}}}
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    val tap=if(interactive)Modifier.clickable(onClickLabel=if(skillAvailable)"Use skill" else "Cheer"){
        if(moving){if(elapsed>=0){job?.cancel();scope.launch{skill.snapTo(-1f)}}else if(skillAvailable){job?.cancel();job=scope.launch{play()}}else scope.launch{cheer.snapTo(1f);cheer.animateTo(0f,spring(dampingRatio=.4f,stiffness=180f))}}
    }else Modifier
    val image=remember(current,gearLevel,pose,hop,pixel){PortraitArt.portrait(context,current,gearLevel,pose,hop,pixel)}
    Canvas(modifier.then(tap).semantics{contentDescription=description;stateDescription=if(elapsed>=0)"Skill playing"else"Idle"}){
        if(elapsed>=0){drawIntoCanvas{CharacterSkills.draw(it.nativeCanvas,context,role,elapsed,android.graphics.RectF(0f,0f,size.width,size.height),pixel)}}else{
            val side=min(size.width,size.height);val extent=if(pixel&&side>=128)(side/128).toInt()*128f else side
            val lift=if(role==0)((if(pose and 1!=0)1 else 0)+hop)*side/256 else hop*side/96
            drawImage(image,IntOffset.Zero,IntSize(image.width,image.height),IntOffset(((size.width-extent)/2).roundToInt(),((size.height-extent)/2-lift).roundToInt()),IntSize(extent.roundToInt(),extent.roundToInt()),filterQuality=if(pixel)FilterQuality.None else FilterQuality.High)
        }
    }
}

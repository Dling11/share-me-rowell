package com.rowell.shareme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val skinTones=listOf(0xFFFFD9BC,0xFFEFB58D,0xFFD59467,0xFFAB704C,0xFF74472F).map{Color(it)}
private val hairTones=listOf(0xFF302536,0xFF79492E,0xFFE7B95B,0xFFB073AD,0xFF4B8095).map{Color(it)}
private val shirtTones=listOf(0xFF70E2C2,0xFFAFA5FF,0xFFFFA7B8,0xFFFFD875,0xFF85C9FF).map{Color(it)}

// A few drawing primitives replace an image pack, remote images and an avatar SDK.
@Composable fun ChibiAvatar(style:AvatarStyle,modifier:Modifier=Modifier.size(64.dp),description:String="Cute avatar") {
    Canvas(modifier.semantics{contentDescription=description}) {
        val unit=minOf(size.width,size.height)/100f
        scale(unit,unit,pivot=Offset.Zero) {
            val skin=skinTones[style.skin];val hair=hairTones[style.hairColour];val shirt=shirtTones[style.shirt]
            drawCircle(shirt.copy(alpha=.13f),48f,Offset(50f,50f))
            if(style.hair==3)drawRoundRect(hair,Offset(22f,20f),Size(56f,65f),androidx.compose.ui.geometry.CornerRadius(24f))
            if(style.hair==4){drawCircle(hair,13f,Offset(24f,30f));drawCircle(hair,13f,Offset(76f,30f))}
            drawOval(shirt,Offset(29f,70f),Size(42f,26f))
            drawLine(shirt,Offset(32f,77f),Offset(22f,83f),9f,StrokeCap.Round)
            drawLine(shirt,Offset(68f,77f),Offset(78f,67f),9f,StrokeCap.Round)
            drawCircle(skin,5f,Offset(80f,64f))
            drawCircle(skin,6f,Offset(23f,49f));drawCircle(skin,6f,Offset(77f,49f))
            drawOval(skin,Offset(23f,23f),Size(54f,51f))
            if(style.hair!=5){
                drawArc(hair,180f,180f,true,Offset(22f,14f),Size(56f,47f))
                val fringe=Path().apply{moveTo(25f,35f);lineTo(27f,50f);lineTo(37f,33f);lineTo(47f,43f);lineTo(51f,30f);lineTo(62f,40f);lineTo(69f,31f);lineTo(76f,49f);lineTo(76f,29f);close()}
                drawPath(fringe,hair)
                if(style.hair==1)drawLine(hair,Offset(47f,20f),Offset(57f,9f),7f,StrokeCap.Round)
                if(style.hair==2){drawCircle(hair,8f,Offset(33f,22f));drawCircle(hair,8f,Offset(47f,19f));drawCircle(hair,8f,Offset(62f,22f))}
            }
            val ink=Color(0xFF302536)
            if(style.eyes==1){
                drawArc(ink,190f,160f,false,Offset(34f,46f),Size(10f,9f),style=Stroke(2.5f,cap=StrokeCap.Round))
                drawArc(ink,190f,160f,false,Offset(56f,46f),Size(10f,9f),style=Stroke(2.5f,cap=StrokeCap.Round))
            }else{
                drawOval(ink,Offset(36f,46f),Size(5f,8f));drawCircle(Color.White,1.2f,Offset(38f,48f))
                if(style.eyes==2)drawLine(ink,Offset(58f,50f),Offset(64f,50f),2.5f,StrokeCap.Round)
                else {drawOval(ink,Offset(59f,46f),Size(5f,8f));drawCircle(Color.White,1.2f,Offset(61f,48f))}
            }
            drawOval(Color(0xFFE98F98).copy(alpha=.6f),Offset(29f,54f),Size(11f,5f))
            drawOval(Color(0xFFE98F98).copy(alpha=.6f),Offset(61f,54f),Size(11f,5f))
            drawArc(ink,0f,180f,false,Offset(45f,55f),Size(10f,8f),style=Stroke(2.2f,cap=StrokeCap.Round))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable fun ProfileSheet(profile:ShareProfile,onDismiss:()->Unit,onSave:(ShareProfile)->Unit) {
    var name by remember{mutableStateOf(profile.name)}
    var avatar by remember{mutableStateOf(profile.avatar)}
    ModalBottomSheet(onDismissRequest=onDismiss,containerColor=Bg,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(horizontal=24.dp).padding(bottom=28.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Your nearby look",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            ChibiAvatar(avatar,Modifier.size(112.dp).align(Alignment.CenterHorizontally))
            OutlinedTextField(name,{name=it.take(24)},label={Text("Your name")},singleLine=true,modifier=Modifier.fillMaxWidth(),supportingText={Text("Seen by people on the same network while Receive is open.")})
            TextButton(onClick={avatar=AvatarStyle.random()},modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Surprise me")}
            Text("Hair",fontWeight=FontWeight.SemiBold)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("Soft","Tuft","Fluffy","Long","Buns","None").forEachIndexed{index,label->FilterChip(selected=avatar.hair==index,onClick={avatar=avatar.copy(hair=index)},label={Text(label)})}
            }
            StyleColours("Skin",skinTones,avatar.skin){avatar=avatar.copy(skin=it)}
            StyleColours("Hair colour",hairTones,avatar.hairColour){avatar=avatar.copy(hairColour=it)}
            StyleColours("Outfit",shirtTones,avatar.shirt){avatar=avatar.copy(shirt=it)}
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("Bright","Happy","Wink").forEachIndexed{index,label->FilterChip(selected=avatar.eyes==index,onClick={avatar=avatar.copy(eyes=index)},label={Text(label)})}
            }
            Button(onClick={onSave(profile.copy(name=profileName(name),avatar=avatar))},modifier=Modifier.fillMaxWidth().heightIn(min=50.dp)){Text("Save my look")}
        }
    }
}
@Composable private fun StyleColours(label:String,colours:List<Color>,selected:Int,onSelect:(Int)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(label,fontWeight=FontWeight.SemiBold)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            colours.forEachIndexed{index,colour->
                OutlinedIconButton(onClick={onSelect(index)},colors=IconButtonDefaults.outlinedIconButtonColors(containerColor=if(index==selected)Mint.copy(alpha=.2f) else Color.Transparent),modifier=Modifier.size(46.dp)) {
                    Canvas(Modifier.size(24.dp).semantics{contentDescription="$label ${index+1}${if(index==selected)", selected" else ""}"}){drawCircle(colour)}
                }
            }
        }
    }
}

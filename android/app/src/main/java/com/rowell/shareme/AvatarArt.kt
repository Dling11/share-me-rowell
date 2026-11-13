package com.rowell.shareme

import android.animation.ValueAnimator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import kotlin.math.*

internal val skinTones=listOf(0xFFFFD9BC,0xFFEFB58D,0xFFD59467,0xFFAB704C,0xFF74472F).map{Color(it)}
internal val hairTones=listOf(0xFF302536,0xFF79492E,0xFFE7B95B,0xFFB073AD,0xFF4B8095,0xFFD8DDED).map{Color(it)}
internal val shirtTones=listOf(0xFF58D6B0,0xFF9775E8,0xFFE06185,0xFFE9BE59,0xFF69A9E2).map{Color(it)}
internal val elementTones=listOf(0xFF70E2C2,0xFFFFA071,0xFF85C9FF,0xFFA6E48B,0xFFBFA3FF,0xFFFFDA89).map{Color(it)}
private val pixelInk=Color(0xFF151321)

/** Generated pixel equipment + layered pixel head. No sprite/video SDK or new background work. */
@Composable fun ChibiAvatar(style:AvatarStyle,modifier:Modifier=Modifier.size(64.dp),description:String="Pixel RPG avatar",level:Int=1,animated:Boolean=false,interactive:Boolean=false,locked:Boolean=false,gearLevel:Int=level) {
    val context=LocalContext.current
    val gear=Adventure.gear(style.role,gearLevel)
    val fixedCharacter=Adventure.isSpecial(style.role)&&Adventure.canEquip(style.role,gearLevel)
    val fixedFrames=remember(fixedCharacter,style.role,gear.id){if(fixedCharacter)SpecialSprites.frames(context,style.role,gearLevel)else null}
    val headStyle=if(gearLevel<Adventure.roles[style.role].level)style.copy(role=0)else style
    val equipment=remember(style.look,style.shirt,style.element,style.skin,gear.id,fixedCharacter){if(fixedCharacter)null else PixelArmory.sprite(context,style,gearLevel)}
    val face=remember(style.skin,style.eyes,style.element){PixelHeads.face(context,style.eyes,style.skin,style.element)}
    val closed=remember(style.skin,style.element,style.eyes){PixelHeads.face(context,if(style.eyes==3)5 else 4,style.skin,style.element)}
    val leftFace=remember(style.skin,style.element,style.eyes){if(style.eyes==3)PixelHeads.face(context,6,style.skin,style.element)else face}
    val rightFace=remember(style.skin,style.element,style.eyes){if(style.eyes==3)PixelHeads.face(context,7,style.skin,style.element)else face}
    val fringe=remember(style.hair,style.hairColour){PixelHeads.hair(context,style)}
    val moving=animated && ValueAnimator.areAnimatorsEnabled() && !locked
    val phase=if(moving)rememberInfiniteTransition(label="Pixel idle").animateFloat(0f,1f,infiniteRepeatable(tween(9600,easing=LinearEasing)),label="Idle cycle")else remember{mutableFloatStateOf(0f)}
    val pose by remember(phase,gear.ornate){derivedStateOf{PixelIdle.pose(phase.value).let{if(gear.ornate)it else it and (7 shl 2).inv()}}}
    val cheer=remember{Animatable(0f)};val scope=rememberCoroutineScope()
    val hop by remember{derivedStateOf{(cheer.value*4).roundToInt()}}
    LaunchedEffect(moving){if(!moving){cheer.stop();cheer.snapTo(0f)}}
    val tap=if(interactive)Modifier.clickable(onClickLabel="Cheer"){if(moving)scope.launch{cheer.snapTo(1f);cheer.animateTo(0f,spring(dampingRatio=.4f,stiffness=180f))}}else Modifier
    Canvas(modifier.then(tap).semantics{contentDescription=description}){
        val available=min(size.width,size.height)/80f
        val unit=available
        translate((size.width-80*unit)/2,(size.height-80*unit)/2){scale(unit,unit,pivot=Offset.Zero){
            pixelFrame(level,locked)
            px(Color.Black.copy(alpha=.28f),26+hop,69,28-hop*2,2);px(Color.Black.copy(alpha=.14f),22,68,36,1)
            val bob=if(style.role<12&&moving&&pose and 1!=0)-1 else 0
            val sway=PixelIdle.direction(pose,5);val turn=PixelIdle.direction(pose,7);val hairLag=(pose shr 9) and 1
            val leftArm=if(hop>0 || (pose shr 10) and 1!=0)-1 else 0
            val rightArm=if(hop>0 || (pose shr 11) and 1!=0)-1 else 0
            translate(8f,8f-hop){
                if(fixedCharacter){
                    val frames=checkNotNull(fixedFrames)
                    val filter=if(locked)ColorFilter.tint(Color(0xFF2B3A50),BlendMode.SrcIn)else null
                    drawImage(frames[SpecialSprites.poseIndex(pose,style.role,hop)],IntOffset.Zero,IntSize(64,54),IntOffset(0,bob),IntSize(64,54-bob),colorFilter=filter,filterQuality=FilterQuality.None)
                    drawImage(frames[0],IntOffset(0,54),IntSize(64,10),IntOffset(0,54),IntSize(64,10),colorFilter=filter,filterQuality=FilterQuality.None)
                    return@translate
                }
                val body=checkNotNull(equipment)
                translate((sway+turn-hairLag).toFloat(),2f){drawBackHair(style,locked,bob)}
                val filter=if(locked)ColorFilter.tint(Color(0xFF2B3A50),BlendMode.SrcIn)else null
                // Shared hip/neck anchors keep the rig connected as arms, chest and head move.
                drawImage(body,IntOffset(0,54),IntSize(64,10),IntOffset(0,54),IntSize(64,10),colorFilter=filter,filterQuality=FilterQuality.None)
                drawImage(body,IntOffset(23,0),IntSize(18,54),IntOffset(23+sway,bob),IntSize(18,54-bob),colorFilter=filter,filterQuality=FilterQuality.None)
                drawImage(body,IntOffset.Zero,IntSize(25,54),IntOffset(sway,bob+leftArm),IntSize(25,54-bob-leftArm),colorFilter=filter,filterQuality=FilterQuality.None)
                drawImage(body,IntOffset(40,0),IntSize(24,54),IntOffset(40+sway,bob+rightArm),IntSize(24,54-bob-rightArm),colorFilter=filter,filterQuality=FilterQuality.None)
                val skin=if(locked)Color(0xFF2B3A50)else skinTones[style.skin]
                px(skin.shade(.76f),30+sway,35+bob,4,3);px(skin,31+sway,36+bob,2,2)
                val expression=if(moving&&pose and 2!=0&&style.eyes!=1)closed else if(turn<0)leftFace else if(turn>0)rightFace else face
                translate((sway+turn).toFloat(),(bob+2).toFloat()){drawPixelHead(headStyle,locked,expression,fringe,gear.ornate,hairLag)}
                if(!locked&&moving){
                    val sparkle=(pose shr 2) and 7;val colour=elementTones[style.element]
                    if(gear.ornate&&sparkle in 1..4)pixelStar(colour.copy(alpha=if(sparkle%2==0).9f else .45f),if(sparkle%2==0)53 else 9,if(sparkle%3==0)30 else 46,1)
                    if(gear.ornate&&hop>0){pixelStar(colour,15,16,2);pixelStar(colour,50,11,1)}
                }
            }
        }}
    }
}

private fun DrawScope.drawBackHair(style:AvatarStyle,locked:Boolean,bob:Int){
    val hair=if(locked)Color(0xFF2B3A50)else hairTones[style.hairColour]
    if(style.hair==3){
        pixelPoly(pixelInk,18,14+bob,46,14+bob,49,43+bob,45,48+bob,40,46+bob,23,49+bob,16,44+bob)
        pixelPoly(hair.shade(.65f),20,16+bob,44,16+bob,47,43+bob,43,45+bob,40,43+bob,23,46+bob,18,43+bob)
        px(hair.shade(.9f),20,23+bob,2,18);px(hair.shade(1.2f),44,26+bob,1,14)
    }
}
private fun DrawScope.drawPixelHead(style:AvatarStyle,locked:Boolean,face:ImageBitmap,fringe:PixelHair?,ornate:Boolean,hairLag:Int=0){
    val accent=if(locked)Color(0xFF34455D)else elementTones[style.element]
    val metal=if(locked)Color(0xFF34455D)else Color(0xFFDAE5EF)
    val cloth=if(locked)Color(0xFF2B3A50)else shirtTones[style.shirt]
    if(style.role in listOf(2,3)){
        pixelPoly(pixelInk,14,22,17,12,24,5,32,2,40,5,47,12,50,22,46,33,18,33)
        pixelPoly(cloth.shade(.55f),16,22,19,13,25,7,32,4,39,7,45,13,48,22,44,31,20,31)
    }
    val filter=if(locked)ColorFilter.tint(Color(0xFF2B3A50),BlendMode.SrcIn)else null
    drawImage(face,IntOffset.Zero,IntSize(face.width,face.height),IntOffset(18,8),IntSize(28,28),colorFilter=filter,filterQuality=FilterQuality.None)
    fringe?.let{drawImage(it.image,IntOffset.Zero,IntSize(it.image.width,it.image.height),it.offset+IntOffset(-hairLag,hairLag),IntSize(it.image.width,it.image.height),colorFilter=filter,filterQuality=FilterQuality.None)}
    when(style.role){
        0->if(ornate){px(pixelInk,42,18,5,4);px(metal,43,19,3,2);px(accent,44,19)}
        1->{
            pixelPoly(pixelInk,13,15,23,12,29,0,35,4,39,12,51,15,49,18,15,18)
            pixelPoly(cloth.shade(.72f),17,15,24,13,30,2,34,5,38,14,47,15,47,17,17,17)
            if(ornate){px(metal,21,15,23,1);pixelStar(accent,32,10,2)}
        }
        2,3->{
            pixelPoly(pixelInk,15,18,18,10,25,4,32,1,40,5,47,12,49,19,45,16,39,9,32,6,25,9,20,14)
            pixelPoly(cloth.shade(if(style.role==3).58f else .76f),17,17,20,11,26,6,32,3,39,7,45,12,47,17,44,14,38,8,32,5,25,8,21,13)
            if(ornate){px(metal.shade(.65f),19,12,1,5);px(metal.shade(.65f),44,13,1,5);pixelStar(accent,32,8,1)}
        }
        4->if(ornate){
            pixelPoly(pixelInk,19,14,15,9,15,1,20,6,22,12,42,12,44,6,49,1,49,9,45,14,43,17,21,17)
            pixelPoly(Color(0xFFC59A65),19,12,17,8,17,4,20,8,23,14,41,14,44,8,47,4,47,8,45,12,42,16,22,16)
            px(metal,22,15,20,1);pixelGem(accent,32,13,3)
        }
        5->if(ornate){
            val gold=if(locked)metal else Color(0xFFFFD37A)
            px(gold,23,1,18,1);px(gold,19,2,4,1);px(gold,41,2,4,1);px(gold,17,3,2,2);px(gold,45,3,2,2);px(gold,19,5,4,1);px(gold,41,5,4,1);px(gold,23,6,18,1);pixelStar(accent,32,1,1)
        }
        6->if(ornate){
            pixelPoly(pixelInk,19,4,26,8,32,0,38,8,45,4,44,15,20,15)
            pixelPoly(if(locked)metal else Color(0xFFE9BC65),21,7,27,10,32,3,37,10,43,7,42,14,22,14)
            px(metal,22,13,20,1);pixelGem(accent,32,10,2)
        }
        7->{px(pixelInk,19,13,27,3);px(cloth,20,14,25,1)}
        8->if(ornate){pixelPoly(pixelInk,17,18,25,19,38,18,47,16,46,24,38,26,24,26,18,24);px(cloth.shade(.35f),20,20,24,3)}
        10->{px(pixelInk,17,13,30,6);px(Color(0xFF414F69),18,14,28,4);px(metal,27,14,12,4);px(pixelInk,31,15,4,1);px(pixelInk,32,16);val marks=if(locked)Color(0xFF2B3A50)else skinTones[style.skin].shade(.5f);for(y in listOf(26,29)){px(marks,21,y,3);px(marks,40,y,3)}}
    }
    if(style.look==1){pixelPoly(pixelInk,44,18,41,16,41,23,45,21,49,23,49,16);pixelPoly(cloth,43,18,42,18,42,21,45,20,48,21,48,18,46,19);px(metal,45,19)}
}

private fun DrawScope.pixelFrame(level:Int,locked:Boolean){
    val colour=Color(Adventure.rank(level).colour).copy(alpha=if(locked).24f else .75f)
    pixelBorder(colour,3,3,74,74,8)
    px(colour.copy(alpha=.12f),12,4,56,2);px(colour.copy(alpha=.08f),4,12,2,56)
    if(level>=25){pixelBorder(colour.shade(1.2f),6,6,68,68,6)}
    if(level>=45){for(x in listOf(10,69))for(y in listOf(10,69))pixelGem(colour,x,y,2)}
    if(level>=60){pixelGem(colour,40,4,3);pixelGem(colour,40,75,2)}
    if(level>=60)for(sign in listOf(-1,1)){
        pixelPoly(colour,40+sign*30,61,40+sign*38,53,40+sign*36,65,40+sign*25,74)
        px(Bg,if(sign<0)5 else 71,62,4,2)
    }
    if(level>=75){pixelPoly(colour,8,10,2,1,3,12,10,17);pixelPoly(colour,72,10,78,1,77,12,70,17)}
    if(level>=85){pixelStar(colour,40,3,3);for(y in listOf(23,34,45)){px(colour,1,y,2,5);px(colour,77,y,2,5)}}
    if(level>=100){pixelStar(Color(0xFFF6E6FF),40,3,2);pixelStar(colour,4,40,3);pixelStar(colour,75,40,3)}
}
private fun Color.shade(amount:Float):Color=if(amount<=1)lerp(Color.Black,this,amount)else lerp(this,Color.White,(amount-1).coerceAtMost(1f))
private fun DrawScope.px(colour:Color,x:Int,y:Int,w:Int=1,h:Int=1){drawRect(colour,Offset(x.toFloat(),y.toFloat()),Size(w.toFloat(),h.toFloat()))}
private fun DrawScope.pixelBox(colour:Color,x:Int,y:Int,w:Int,h:Int,cut:Int){px(colour,x+cut,y,w-cut*2,h);px(colour,x,y+cut,w,h-cut*2)}
private fun DrawScope.pixelStar(colour:Color,x:Int,y:Int,r:Int){px(colour,x-r,y,r*2+1);px(colour,x,y-r,1,r*2+1)}
private fun DrawScope.pixelGem(colour:Color,x:Int,y:Int,r:Int){for(dy in -r..r){val span=r-abs(dy);px(colour,x-span,y+dy,span*2+1)};px(colour.shade(1.3f),x,y-r)}
private fun DrawScope.pixelBorder(colour:Color,x:Int,y:Int,w:Int,h:Int,cut:Int){
    px(colour,x+cut,y,w-cut*2);px(colour,x+cut,y+h-1,w-cut*2);px(colour,x,y+cut,1,h-cut*2);px(colour,x+w-1,y+cut,1,h-cut*2)
    for(i in 0 until cut step 2){for((sx,sy)in listOf(1 to 1,-1 to 1,1 to -1,-1 to -1)){
        val left=if(sx==1)x else x+w-2;val top=if(sy==1)y else y+h-2
        px(colour,left+sx*i,top+sy*(cut-i-2),2,2)
    }}
}
/** Integer scanlines avoid antialiased vector edges at the logical sprite resolution. */
private fun DrawScope.pixelPoly(colour:Color,vararg xy:Int){
    val count=xy.size/2;val top=(0 until count).minOf{xy[it*2+1]};val bottom=(0 until count).maxOf{xy[it*2+1]}
    for(y in top..bottom){val crossings=ArrayList<Float>(8);val py=y+.5f
        for(i in 0 until count){val j=(i+1)%count;val y1=xy[i*2+1];val y2=xy[j*2+1]
            if((y1<=py&&y2>py)||(y2<=py&&y1>py))crossings.add(xy[i*2]+(py-y1)*(xy[j*2]-xy[i*2]).toFloat()/(y2-y1))
        }
        crossings.sort();for(i in 0 until crossings.size-1 step 2){val left=ceil(crossings[i]-.5f).toInt();val right=ceil(crossings[i+1]-.5f).toInt();if(right>left)px(colour,left,y,right-left)}
    }
}

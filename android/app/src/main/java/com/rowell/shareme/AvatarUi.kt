package com.rowell.shareme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.text.NumberFormat

private fun points(value:Long)=NumberFormat.getIntegerInstance().format(value)
@Composable fun JourneyProgress(journey:AdventurerJourney) {
    val colour=Color(journey.rank.colour)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
        Text("Lv. ${journey.level} · ${journey.rank.name}",color=colour,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.labelLarge)
        Text(if(journey.level==100)"MAX" else "${points(journey.levelXp)} / ${points(journey.neededXp)} XP",color=Muted,style=MaterialTheme.typography.labelSmall)
    }
    Canvas(Modifier.fillMaxWidth().height(10.dp).semantics{progressBarRangeInfo=ProgressBarRangeInfo(journey.fraction,0f..1f)}){
        drawRect(colour.copy(alpha=.25f))
        val inset=2.dp.toPx();val gap=1.dp.toPx();val width=(size.width-inset*2-gap*19)/20
        for(i in 0 until 20){val x=inset+i*(width+gap);drawRect(Bg,androidx.compose.ui.geometry.Offset(x,inset),androidx.compose.ui.geometry.Size(width,size.height-inset*2));val fill=(journey.fraction*20-i).coerceIn(0f,1f);if(fill>0)drawRect(colour,androidx.compose.ui.geometry.Offset(x,inset),androidx.compose.ui.geometry.Size(width*fill,size.height-inset*2))}
    }
}
@Composable fun JourneyCard(state:ScreenState,onOpen:()->Unit) {
    Surface(color=Panel,shape=MaterialTheme.shapes.medium,modifier=Modifier.fillMaxWidth().clickable(onClick=onOpen)){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                ChibiAvatar(state.profile.avatar,Modifier.size(52.dp),level=state.journey.level)
                Column(Modifier.weight(1f)){
                    Text("Your adventurer",fontWeight=FontWeight.SemiBold)
                    Text(Adventure.nextRole(state.journey.level)?.let{"Next: ${it.name} · Lv. ${it.level}"}?:"Your character collection is complete",style=MaterialTheme.typography.bodySmall,color=Muted)
                }
            }
            JourneyProgress(state.journey)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable fun ProfileSheet(profile:ShareProfile,onDismiss:()->Unit,onSave:(ShareProfile)->Unit,journey:AdventurerJourney=AdventurerJourney(),animations:Boolean=true,onAnimations:(Boolean)->Unit={}) {
    var name by remember{mutableStateOf(profile.name)}
    var avatar by remember{mutableStateOf(profile.avatar)}
    var tab by remember{mutableIntStateOf(0)}
    var preview by remember{mutableIntStateOf(profile.avatar.role)}
    fun save(style:AvatarStyle=avatar,newName:String=name){avatar=style;name=newName;onSave(profile.copy(name=newName,avatar=style))}
    fun close(){onSave(profile.copy(name=name,avatar=avatar));onDismiss()}
    fun previewStyle(role:Int)=avatar.copy(role=role)
    val keyboard=WindowInsets.ime.getBottom(LocalDensity.current)>0
    val compact=LocalConfiguration.current.screenHeightDp<720
    ModalBottomSheet(onDismissRequest={close()},containerColor=Bg,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().heightIn(max=(LocalConfiguration.current.screenHeightDp*.88f).dp).imePadding().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Text("Your adventurer",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
                Text("Saved automatically",color=Mint,style=MaterialTheme.typography.labelSmall)
            }
            if(!keyboard)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                ChibiAvatar(if(tab==1)previewStyle(preview)else avatar,Modifier.size(if(compact)100.dp else 128.dp),"Your adventurer preview. Tap to cheer.",journey.level,animated=animations,interactive=true,gearLevel=if(tab==1)maxOf(journey.level,Adventure.roles[preview].level)else journey.level)
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    val role=Adventure.roles[if(tab==1)preview else avatar.role]
                    Text(Adventure.gear(if(tab==1)preview else avatar.role,if(tab==1)maxOf(journey.level,role.level)else journey.level).title,fontWeight=FontWeight.SemiBold,fontFamily=FontFamily.Monospace)
                    if(tab==1&&preview!=avatar.role){
                        if(Adventure.canEquip(preview,journey.level))TextButton(onClick={save(avatar.copy(role=preview))}){Text("Equip character")}
                        else Text("Unlocks at Lv. ${role.level}",color=Color(0xFFFFDA89),style=MaterialTheme.typography.bodySmall)
                    }
                    JourneyProgress(journey)
                    if(animations)Text("Tap avatar to cheer",color=Muted,style=MaterialTheme.typography.labelSmall)
                }
            }
            TabRow(selectedTabIndex=tab,containerColor=Bg){listOf("Style","Characters","Ranks").forEachIndexed{i,label->Tab(selected=tab==i,onClick={tab=i;preview=avatar.role},content={Text(label,modifier=Modifier.padding(vertical=14.dp),style=MaterialTheme.typography.labelLarge,maxLines=1,softWrap=false)})}}
            key(tab){Column(Modifier.weight(1f,false).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
                when(tab){
                    0->{
                        OutlinedTextField(name,{save(newName=it.take(24))},label={Text("Your nearby name")},singleLine=true,modifier=Modifier.fillMaxWidth())
                        if(Adventure.isSpecial(avatar.role)){
                            Surface(color=Panel,shape=MaterialTheme.shapes.medium){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                                Text("Signature character · fixed design",fontWeight=FontWeight.SemiBold)
                                Text("${Adventure.roles[avatar.role].name} has its own head, hair, outfit and colours. Your personal avatar style stays saved.",color=Muted,style=MaterialTheme.typography.bodySmall)
                                TextButton(onClick={tab=1;preview=0}){Text("Choose a customizable character")}
                            }}
                        }else{
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Boy","Girl").forEachIndexed{i,label->FilterChip(selected=avatar.look==i,onClick={if(avatar.look!=i)save(avatar.copy(look=i,hair=if(i==1)3 else 0))},label={Text(label)})}}
                            TextButton(onClick={save(AvatarStyle.random(avatar.look).copy(role=avatar.role,element=avatar.element))}){Text("Surprise me")}
                        }
                        Text("Hair",fontWeight=FontWeight.SemiBold)
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Soft","Tuft","Fluffy","Long","Buns","None").forEachIndexed{i,label->FilterChip(selected=avatar.hair==i,onClick={save(avatar.copy(hair=i))},label={Text(label)})}}
                        StyleColours("Skin",skinTones,avatar.skin){save(avatar.copy(skin=it))}
                        StyleColours("Hair colour",hairTones,avatar.hairColour){save(avatar.copy(hairColour=it))}
                        StyleColours("Outfit",shirtTones,avatar.shirt){save(avatar.copy(shirt=it))}
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Bright","Happy","Wink","Focused").forEachIndexed{i,label->FilterChip(selected=avatar.eyes==i,onClick={save(avatar.copy(eyes=i))},label={Text(label)})}}
                        Text("Element colour",fontWeight=FontWeight.SemiBold)
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){Adventure.elements.forEachIndexed{i,label->FilterChip(selected=avatar.element==i,onClick={save(avatar.copy(element=i))},label={Text(label)})}}
                        }
                        Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Idle animation",fontWeight=FontWeight.SemiBold);Text("Pauses during transfers and in the background. Respects system motion settings.",color=Muted,style=MaterialTheme.typography.bodySmall)};Switch(checked=animations,onCheckedChange=onAnimations,modifier=Modifier.semantics{contentDescription="Idle animation"})}
                    }
                    1->{
                        val next=Adventure.nextRole(journey.level)
                        Text(next?.let{"Next unlock: ${it.name} at Lv. ${it.level}"}?:"All characters unlocked",color=Color(0xFFFFDA89),fontWeight=FontWeight.SemiBold)
                        if(next!=null)Text("${points(Adventure.threshold(next.level)-journey.xp)} XP to go. Tap a character to preview.",color=Muted,style=MaterialTheme.typography.bodySmall)
                        Adventure.roles.indices.sortedBy{Adventure.roles[it].level}.chunked(2).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                            row.forEach{i->val role=Adventure.roles[i];val unlocked=Adventure.canEquip(i,journey.level)
                                Surface(color=if(preview==i)Color(0xFF26344B)else Panel,shape=MaterialTheme.shapes.medium,modifier=Modifier.weight(1f).clickable(onClickLabel="Preview ${role.name}"){preview=i}){
                                    Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
                                        ChibiAvatar(previewStyle(i),Modifier.size(86.dp),"${role.name}${if(unlocked)"" else ", locked"}",journey.level,locked=!unlocked,gearLevel=maxOf(journey.level,role.level))
                                        Text(role.name,fontWeight=FontWeight.SemiBold,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.labelLarge)
                                        Text(if(Adventure.isSpecial(i))"Fixed signature"else "Customizable",color=Muted,style=MaterialTheme.typography.labelSmall)
                                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){
                                            if(!unlocked)Icon(Icons.Rounded.Lock,null,Modifier.size(12.dp),tint=Muted)
                                            Text(if(i==avatar.role)"Equipped" else if(unlocked)"Unlocked" else "Lv. ${role.level}",color=if(i==avatar.role)Mint else Muted,style=MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            };if(row.size==1)Spacer(Modifier.weight(1f))
                        }}
                        Text(Adventure.roles[preview].story,color=Muted,style=MaterialTheme.typography.bodySmall)
                    }
                    2->{
                        Text("Gear progression",fontWeight=FontWeight.SemiBold)
                        Text("Start with simple clothes. Armour, auras and wings come later. Colours stay freely selectable.",color=Muted,style=MaterialTheme.typography.bodySmall)
                        Adventure.gearMilestones.forEach{step->Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                            val gear=Adventure.gear(step.role,step.level)
                            ChibiAvatar(avatar.copy(role=step.role),Modifier.size(64.dp),"${gear.title} at level ${step.level}",step.level)
                            Column(Modifier.weight(1f)){Text(gear.title,fontWeight=FontWeight.SemiBold);Text("Level ${step.level} · ${points(Adventure.threshold(step.level))} total XP",color=Muted,style=MaterialTheme.typography.bodySmall)}
                            if(journey.level<step.level)Icon(Icons.Rounded.Lock,null,Modifier.size(16.dp),tint=Muted)
                        }}
                        HorizontalDivider()
                        Text("Rank frames",fontWeight=FontWeight.SemiBold)
                        Adventure.ranks.forEach{rank->Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                            ChibiAvatar(avatar,Modifier.size(54.dp),"${rank.name} frame",rank.level)
                            Column(Modifier.weight(1f)){Text(rank.name,color=Color(rank.colour),fontWeight=FontWeight.SemiBold);Text("Level ${rank.level} · ${points(Adventure.threshold(rank.level))} total XP",color=Muted,style=MaterialTheme.typography.bodySmall)}
                            if(journey.level>=rank.level)Text("✓",color=Mint)
                        }}
                        HorizontalDivider()
                        Text("How XP works",fontWeight=FontWeight.SemiBold)
                        Text("New verified content earns 1 XP per complete 256 KB, up to 50 XP per file. Smaller files share normally but earn no XP. Repeats, retries and previews earn nothing. Existing XP and character unlocks stay saved.",color=Muted,style=MaterialTheme.typography.bodySmall)
                        Text("Levels get progressively harder. Characters and frames are cosmetic; sharing stays equally fast at every level. Progress stays on this device, without an account.",color=Muted,style=MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }}
            Button(shape=PixelCorners,onClick={close()},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text("Done")}
            Spacer(Modifier.height(12.dp))
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun StyleColours(label:String,colours:List<Color>,selected:Int,onSelect:(Int)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(label,fontWeight=FontWeight.SemiBold)
        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            colours.forEachIndexed{index,colour->OutlinedIconButton(onClick={onSelect(index)},colors=IconButtonDefaults.outlinedIconButtonColors(containerColor=if(index==selected)Mint.copy(alpha=.2f) else Color.Transparent),modifier=Modifier.size(46.dp)){
                Canvas(Modifier.size(24.dp).semantics{contentDescription="$label ${index+1}${if(index==selected)", selected" else ""}"}){drawCircle(colour)}
            }}
        }
    }
}

package com.rowell.shareme

import android.content.Context
import android.graphics.*
import kotlin.math.*

data class SkillMoment(val body:Int=0,val effect:Int=-1,val eye:Int=-1,val aura:Float=0f,val closeup:Float=0f,val scale:Float=.54f,val caption:String="",val complete:Boolean=false)
object CharacterSkills {
    fun has(role:Int)=role in listOf(13,19,23)
    fun duration(role:Int)=when(role){13->2800L;19->4800L;else->5200L}
    fun title(role:Int)=when(role){13->"Three-Sword Tempest";19->"Malevolent Shrine";else->"Crimson Awakening"}
    fun unlock(role:Int)=if(role==13)82 else Adventure.roles[role].level
    fun available(role:Int,outfit:Int)=has(role)&&(role!=13||outfit>=82)
    private fun p(t:Long,start:Int,length:Int)=((t-start)/length.toFloat()).coerceIn(0f,1f)
    fun at(role:Int,millis:Long):SkillMoment {
        val t=millis.coerceAtLeast(0);if(role==13)return when {
            t<650->SkillMoment(body=(t*24/650).toInt().coerceAtMost(23),caption="Mastery · Emerald eye")
            t<1450->SkillMoment(body=24+(p(t,650,800)*24).toInt().coerceAtMost(23),effect=(p(t,650,800)*16).toInt().coerceAtMost(15),aura=1f,caption="Three-Sword Style · Crosscut")
            t<2600->SkillMoment(body=48+(p(t,1450,1150)*24).toInt().coerceAtMost(23),effect=if(t<1900)12+((t-1450)*4/450).toInt().coerceAtMost(3)else 16+((t-1900)*8/700).toInt().coerceAtMost(7),aura=if(t<1900).65f else ((2600-t)/240f).coerceAtMost(1f),caption=if(t<1900)"Mastery · Final draw" else "Emerald Tempest · Sever")
            else->SkillMoment(body=71,caption=title(role),complete=t>=duration(role))
        }
        if(role==19)return when {
            t<180->SkillMoment(caption=title(role))
            t<1200->SkillMoment(body=(p(t,180,1020)*12).toInt().coerceAtMost(11),caption="Domain hand sign")
            t<1850->SkillMoment(body=12+(p(t,1200,650)*4).toInt().coerceAtMost(3),eye=(p(t,1200,650)*16).toInt().coerceAtMost(15),closeup=min(p(t,1200,100),1-p(t,1730,120)),caption="Domain Expansion")
            t<3000->SkillMoment(body=16+(p(t,1850,1150)*4).toInt().coerceAtMost(3),effect=(p(t,1850,1150)*16).toInt().coerceAtMost(15),aura=p(t,1850,1150),scale=.54f-.14f*p(t,1850,1150),caption="Malevolent Shrine · Manifest")
            t<4200->SkillMoment(body=19,effect=16+(p(t,3000,1200)*8).toInt().coerceAtMost(7),aura=1f,scale=.4f,caption="Malevolent Shrine · Unleashed")
            t<4800->SkillMoment(body=20+(p(t,4200,600)*4).toInt().coerceAtMost(3),effect=23-(p(t,4200,600)*24).toInt().coerceAtMost(23),aura=1-p(t,4200,600),scale=.4f+.14f*p(t,4200,600),caption="Domain dissolves")
            else->SkillMoment(body=23,caption=title(role),complete=true)
        }
        val time=if(t>=5200)9400 else t*9400/5200
        return when {
            time<600->SkillMoment(caption=title(role))
            time<2800->SkillMoment(body=(p(time,600,2200)*12).toInt().coerceAtMost(11),caption="The hand sign")
            time<4000->SkillMoment(body=12+(p(time,2800,1200)*4).toInt().coerceAtMost(3),eye=(p(time,2800,1200)*16).toInt().coerceAtMost(15),closeup=min(p(time,2800,160),1-p(time,3820,180)),caption="Eyes of the Uchiha")
            time<5600->SkillMoment(body=16+(p(time,4000,1600)*4).toInt().coerceAtMost(3),effect=(p(time,4000,1600)*12).toInt().coerceAtMost(11),aura=p(time,4000,1600),scale=.54f-.14f*p(time,4000,1600),caption="Susanoo emerges")
            time<7400->SkillMoment(body=19,effect=12+(p(time,5600,1800)*12).toInt().coerceAtMost(11),aura=1f,scale=.4f,caption="Susanoo · Awakened")
            time<8600->SkillMoment(body=20+(p(time,7400,1200)*4).toInt().coerceAtMost(3),effect=23-(p(time,7400,1200)*24).toInt().coerceAtMost(23),aura=1-p(time,7400,1200),scale=.4f+.14f*p(time,7400,1200),caption="Power, controlled")
            else->SkillMoment(body=23,caption=title(role),complete=t>=duration(role))
        }
    }
    private fun pulse(t:Long,start:Int,decay:Int):Float {val e=t-start;return if(e<0)0f else if(e<20)e/20f else (1-(e-20)/decay.toFloat()).coerceAtLeast(0f)}
    fun impact(t:Long)=max(max(pulse(t,850,100),pulse(t,1180,100)),pulse(t,1900,380))
    fun draw(canvas:Canvas,context:Context,role:Int,time:Long,bounds:RectF,pixel:Boolean){
        val saved=canvas.save();canvas.clipRect(bounds)
        try{
            val m=at(role,time);val extent=min(bounds.width(),bounds.height());val stage=RectF(bounds.centerX()-extent/2,bounds.centerY()-extent/2,bounds.centerX()+extent/2,bounds.centerY()+extent/2)
            val paint=Paint().apply{isAntiAlias=!pixel;isFilterBitmap=!pixel};val energy=when(role){13->Color.rgb(115,244,160);19->Color.rgb(234,78,91);else->Color.rgb(178,126,250)}
            fun colour(c:Int,opacity:Float=1f){paint.shader=null;paint.color=c;paint.alpha=(opacity.coerceIn(0f,1f)*255).toInt();paint.style=Paint.Style.FILL}
            fun cel(name:String,index:Int,box:RectF,opacity:Float=1f){if(index<0||opacity<=0)return;val image=PortraitArt.cel(context,name,index,pixel);colour(Color.WHITE,opacity);canvas.drawBitmap(image,null,box,paint)}
            fun core(){
                colour(Color.rgb(25,29,40));canvas.drawRect(bounds,paint)
                paint.shader=LinearGradient(stage.left,stage.top,stage.left,stage.bottom,when(role){13->Color.rgb(20,38,30);19->Color.rgb(45,22,29);else->Color.rgb(26,22,45)},Color.rgb(25,29,40),Shader.TileMode.CLAMP);canvas.drawRect(stage,paint);paint.shader=null
                if(role==13){
                    val bank=when(m.body/24){0->"zoro_ready";1->"zoro_combo";else->"zoro_finish"};cel(bank,m.body%24,stage)
                    if(m.effect>=0)cel("zoro_slashes",m.effect,stage,m.aura)
                    if(time in 50..260){val glow=sin((time-50)/210f*PI).toFloat().coerceAtLeast(0f);colour(energy,glow);paint.strokeWidth=max(1f,extent/128);val x=stage.left+extent*.43f;val y=stage.top+extent*(.42f+.045f*time/300);canvas.drawLine(x-extent*.1f*glow,y,x+extent*.025f*glow,y,paint)}
                    return
                }
                val prefix=if(role==19)"sukuna"else"madara";val bottom=stage.bottom-extent*.035f
                if(m.aura>0){
                    colour(energy,m.aura*.4f);paint.style=Paint.Style.STROKE;paint.strokeWidth=max(1f,extent/128);canvas.drawOval(RectF(stage.centerX()-extent*.35f,bottom-extent*.13f,stage.centerX()+extent*.35f,bottom),paint)
                    colour(energy,m.aura*.5f);for(i in 0..17){val phase=time*.0004+i*2.399;val x=stage.centerX()+cos(phase).toFloat()*extent*(.22f+i%3*.04f);val y=bottom-((time*.035+i*29)%(extent*.8)).toFloat();canvas.drawRect(x,y,x+extent*.006f,y+extent*.016f,paint)}
                    cel(prefix+if(role==19)"_shrine"else"_guardian",m.effect,RectF(stage.left,bottom-extent,stage.right,bottom),m.aura)
                }
                val body=extent*(.48f+(m.scale-.4f)/.14f*.46f);cel(prefix+"_cast",m.body,RectF(stage.centerX()-body/2,bottom-body,stage.centerX()+body/2,bottom))
                if(m.closeup>0){colour(Color.rgb(25,29,40),m.closeup*.88f);canvas.drawRect(stage,paint);val eye=extent*.86f*(.72f+.28f*m.closeup);cel(prefix+"_eye",m.eye,RectF(stage.centerX()-eye/2,stage.centerY()-eye/2,stage.centerX()+eye/2,stage.centerY()+eye/2),m.closeup)}
            }
            val pulse=if(role==13)impact(time)else 0f
            if(pulse<=0)core()else{
                colour(Color.rgb(7,16,12));canvas.drawRect(bounds,paint);val reverse=time in 1180..1899;val shift=(if(time>=1900)22 else 7)*pulse*extent/256
                for(half in 0..1){val s=canvas.save();val cut=Path();if(reverse){if(half==0){cut.moveTo(stage.left,stage.top);cut.lineTo(stage.right,stage.top);cut.lineTo(stage.left,stage.bottom)}else{cut.moveTo(stage.right,stage.top);cut.lineTo(stage.right,stage.bottom);cut.lineTo(stage.left,stage.bottom)}}else{if(half==0){cut.moveTo(stage.left,stage.top);cut.lineTo(stage.right,stage.top);cut.lineTo(stage.right,stage.bottom)}else{cut.moveTo(stage.left,stage.top);cut.lineTo(stage.right,stage.bottom);cut.lineTo(stage.left,stage.bottom)}};cut.close();canvas.clipPath(cut);val direction=if(half==0)1 else -1;canvas.translate(direction*shift,(if(reverse)direction else -direction)*shift);core();canvas.restoreToCount(s)}
                colour(energy,pulse*.85f);paint.strokeWidth=max(1f,extent*.025f*pulse);canvas.drawLine(if(reverse)stage.right else stage.left,stage.top,if(reverse)stage.left else stage.right,stage.bottom,paint)
                colour(Color.rgb(225,255,235),pulse*.95f);paint.strokeWidth=max(1f,extent*.007f*pulse);canvas.drawLine(if(reverse)stage.right else stage.left,stage.top,if(reverse)stage.left else stage.right,stage.bottom,paint)
            }
        }finally{canvas.restoreToCount(saved)}
    }
}

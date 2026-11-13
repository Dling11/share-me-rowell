package com.rowell.shareme

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class AdventurerJourney(val xp:Long=0,val files:Long=0,val bytes:Long=0) {
    val level get()=Adventure.level(xp)
    val rank get()=Adventure.rank(level)
    val levelXp get()=xp-Adventure.threshold(level)
    val neededXp get()=if(level==100)0L else Adventure.threshold(level+1)-Adventure.threshold(level)
    val fraction get()=if(level==100)1f else (levelXp.toDouble()/neededXp).toFloat().coerceIn(0f,1f)
}
data class HeroRole(val name:String,val level:Int,val story:String)
data class HeroRank(val name:String,val level:Int,val colour:Long)
data class GearLook(val id:Int,val source:Int,val pair:Int,val title:String,val level:Int,val ornate:Boolean=false)
data class GearMilestone(val level:Int,val role:Int)

object Adventure {
    val ranks=listOf(
        HeroRank("Novice",1,0xFFB5C4D1),HeroRank("Adventurer",8,0xFFE0AC76),
        HeroRank("Vanguard",18,0xFF85D7EA),HeroRank("Elemental",30,0xFF70E2C2),
        HeroRank("Shadow",45,0xFFBFA3FF),HeroRank("Demonic",60,0xFFFF857F),
        HeroRank("Heavenly",80,0xFFFFDA89),HeroRank("God-tier",100,0xFFE6D2FF)
    )
    val roles=listOf(
        HeroRole("Courier",1,"Every great journey begins with a small delivery."),
        HeroRole("Rune Mage",8,"A little magic. A thousand stories carried safely."),
        HeroRole("Forest Ranger",18,"Finds a path between distant companions."),
        HeroRole("Shadow Reaper",35,"A quiet guardian with a crescent blade."),
        HeroRole("Demon Warden",55,"Forged in ember, sworn to protect the journey."),
        HeroRole("Heavenly Seraph",75,"Golden wings for a well-travelled messenger."),
        HeroRole("Astral Sovereign",95,"A crown beneath a sky of unfinished adventures."),
        HeroRole("Silent Ronin",12,"A serious swordsman. Quiet focus, earned mastery."),
        HeroRole("Gojo-inspired",40,"A cool silver-haired teacher with a limitless sense of style."),
        HeroRole("Saitama-inspired",65,"An ordinary training suit. An extraordinary hero."),
        HeroRole("Naruto-inspired",85,"A determined orange-clad ninja chasing the next horizon."),
        HeroRole("Yugi-inspired",50,"A thoughtful card duelist with a bold heart and a golden puzzle."),
        HeroRole("Luffy-inspired",20,"A straw-hat captain with a boundless spirit of adventure."),
        HeroRole("Zoro-inspired",28,"Three blades, quiet focus and an unwavering promise."),
        HeroRole("Boa Hancock-inspired",32,"A graceful pirate empress with a fearless heart."),
        HeroRole("Shanks-inspired",48,"A calm red-haired captain whose presence speaks for itself."),
        HeroRole("Eren-inspired",36,"A determined scout looking beyond the walls."),
        HeroRole("Levi-inspired",58,"A disciplined captain, precise and unshakable."),
        HeroRole("Mikasa-inspired",46,"A steadfast protector with a familiar red scarf."),
        HeroRole("Sukuna-inspired",88,"A confident cursed sovereign with a dangerous smile."),
        HeroRole("Itachi-inspired",68,"A quiet shinobi beneath a cloak of crimson clouds."),
        HeroRole("Rock Lee-inspired",24,"Hard work, bright eyes and a spirit that never gives up."),
        HeroRole("Might Guy-inspired",78,"A proud green-clad mentor with a radiant fighting spirit."),
        HeroRole("Madara-inspired",96,"A legendary warrior clad in deep crimson armour.")
    )
    val favouriteTitles=listOf("Straw Hat Captain","Three-Sword Wanderer","Pirate Empress","Red-Haired Captain","Beyond the Walls","Scout Captain","Crimson Scarf","Cursed Sovereign","Crimson Crow","Youthful Fighter","Radiant Mentor","Crimson Legend")
    val favouriteMasteries=listOf(80,82,84,90,86,92,88,99,94,76,98,100)
    val favouriteMasterTitles=listOf("Liberated Captain","Emerald Swordsman","Serpent Empress","Emperor of the Sea","Freedom Seeker","Humanity's Strongest","Unbroken Protector","King of Curses","Scarlet Illusionist","Lotus Champion","Night Mentor","Six-Paths Sovereign")
    val elements=listOf("Aether","Fire","Water","Wind","Shadow","Light")
    fun threshold(level:Int):Long {val n=level.coerceIn(1,100).toLong()-1;return 40*n*n+3*n*n*n}
    fun level(xp:Long):Int=(1..100).last{xp.coerceAtLeast(0)>=threshold(it)}
    fun rank(level:Int)=ranks.last{level.coerceIn(1,100)>=it.level}
    fun canEquip(role:Int,level:Int)=role in roles.indices && level>=roles[role].level
    fun isSpecial(role:Int)=role in 8 until roles.size
    fun specialSheet(role:Int):Int {require(isSpecial(role));return when(role){in 8..10->0;11->1;else->role-10}}
    fun specialCels(role:Int)=when{role>=12->12;role==11->8;else->4}
    fun specialRow(role:Int,level:Int):Int {require(isSpecial(role));return when(role){11->if(level>=95)2 else 0;in 8..10->(role-8)*2+if(level>=listOf(80,90,100)[role-8])1 else 0;else->if(level>=favouriteMasteries[role-12])3 else 0}}
    fun nextRole(level:Int)=CharacterRoster.roles.map{roles[it]}.filter{it.level>level}.minByOrNull{it.level}
    fun signature(style:AvatarStyle,role:Int):AvatarStyle=when(role){
        7->style.copy(role=role,hair=0,hairColour=0,eyes=3,shirt=1)
        8->style.copy(role=role,hair=2,hairColour=5,eyes=3,element=2)
        9->style.copy(role=role,hair=5,eyes=3,shirt=3,element=5)
        10->style.copy(role=role,hair=2,hairColour=2,eyes=3,shirt=3,element=3)
        else->style.copy(role=role)
    }
    const val XP_BYTES=256L*1024
    const val MAX_FILE_XP=50
    // Existing XP/level thresholds stay intact; this slower rate applies only to future files.
    fun fileXp(bytes:Long):Int=(bytes.coerceAtLeast(0)/XP_BYTES).coerceAtMost(MAX_FILE_XP.toLong()).toInt()
    val gearMilestones=listOf(GearMilestone(1,0),GearMilestone(10,0),GearMilestone(25,0),GearMilestone(45,0),GearMilestone(60,3),GearMilestone(75,4),GearMilestone(85,5),GearMilestone(95,6),GearMilestone(100,6))
    fun gear(role:Int,level:Int):GearLook {
        require(role in roles.indices)
        val at=level.coerceIn(1,100)
        if(role!=0&&at<roles[role].level)return gear(0,at)
        return when(role){
            0->when{
                at<10->GearLook(0,1,0,"Novice Courier",1)
                at<25->GearLook(1,1,1,"Courier Trainee",10)
                at<45->GearLook(2,1,2,"Iron Courier",25)
                else->GearLook(3,0,0,"Courier Knight",45,true)
            }
            1->if(at<45)GearLook(4,1,3,"Apprentice Mage",8)else GearLook(5,0,1,"Rune Mage",45,true)
            2->if(at<45)GearLook(6,1,4,"Forest Scout",18)else GearLook(7,0,2,"Forest Ranger",45,true)
            3->if(at<60)GearLook(8,1,5,"Shadow Initiate",35)else GearLook(9,0,3,"Shadow Reaper",60,true)
            4->if(at<75)GearLook(10,1,6,"Warden Recruit",55)else GearLook(11,0,4,"Demon Warden",75,true)
            5->if(at<85)GearLook(12,1,2,"Seraph Aspirant",75)else GearLook(13,0,5,"Heavenly Seraph",85,true)
            6->if(at<100)GearLook(14,0,6,"Astral Herald",95)else GearLook(15,1,7,"Astral Sovereign",100,true)
            7->if(at<55)GearLook(16,2,0,"Ronin Trainee",12)else GearLook(17,2,1,"Silent Ronin",55,true)
            8->if(at<80)GearLook(18,2,2,"Silver Academy",40)else GearLook(19,2,3,"Limitless Teacher",80,true)
            9->if(at<90)GearLook(20,2,4,"Everyday Training",65)else GearLook(21,2,5,"One-Punch Hero",90,true)
            10->if(at<100)GearLook(22,2,6,"Leaf Ninja",85)else GearLook(23,2,7,"Sage of Dawn",100,true)
            11->if(at<95)GearLook(24,3,0,"Card Duelist",50)else GearLook(25,3,1,"Pharaoh Duelist",95,true)
            else->{val index=role-12;val master=at>=favouriteMasteries[index];GearLook(26+index*2+if(master)1 else 0,4+index,if(master)1 else 0,if(master)favouriteMasterTitles[index]else favouriteTitles[index],if(master)favouriteMasteries[index]else roles[role].level,master)}
        }
    }
}

/** An offline, cosmetic journey. Only a digest/size receipt is retained, never filenames or peers. */
object JourneyStore {
    fun load(context:Context):AdventurerJourney=runCatching{Ledger(context).use{it.read()}}.getOrDefault(AdventurerJourney())
    @Synchronized fun award(context:Context,file:FileMeta):Int {
        if(file.size<=0 || !file.sha.matches(Regex("[a-f0-9]{64}")))return 0
        // Cosmetic persistence must never cause a verified file transfer to fail.
        return runCatching{
            val result=Ledger(context).use{it.credit(file)}
            Model.set{it.copy(journey=result.first,earnedXp=(it.earnedXp+result.second).coerceAtMost(MAX_BATCH_XP))}
            result.second
        }.getOrDefault(0)
    }
    private const val MAX_BATCH_XP=Adventure.MAX_FILE_XP*16
    private class Ledger(context:Context):SQLiteOpenHelper(context.applicationContext,"adventurer.db",null,1) {
        override fun onCreate(db:SQLiteDatabase){
            db.execSQL("CREATE TABLE journey (id INTEGER PRIMARY KEY, xp INTEGER NOT NULL, files INTEGER NOT NULL, bytes INTEGER NOT NULL)")
            db.execSQL("INSERT INTO journey VALUES (1,0,0,0)")
            db.execSQL("CREATE TABLE credited (digest TEXT NOT NULL, size INTEGER NOT NULL, PRIMARY KEY(digest,size)) WITHOUT ROWID")
        }
        override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){}
        fun read():AdventurerJourney=read(readableDatabase)
        private fun read(db:SQLiteDatabase)=db.rawQuery("SELECT xp,files,bytes FROM journey WHERE id=1",null).use{
            check(it.moveToFirst());AdventurerJourney(it.getLong(0).coerceIn(0,Adventure.threshold(100)),it.getLong(1).coerceAtLeast(0),it.getLong(2).coerceAtLeast(0))
        }
        fun credit(file:FileMeta):Pair<AdventurerJourney,Int> {
            val db=writableDatabase;db.beginTransaction()
            try{
                val added=db.compileStatement("INSERT OR IGNORE INTO credited(digest,size) VALUES (?,?)").use{it.bindString(1,file.sha);it.bindLong(2,file.size);it.executeUpdateDelete()}
                val previous=read(db);val points=if(added==1)Adventure.fileXp(file.size)else 0
                val current=if(added==1)AdventurerJourney((previous.xp+points).coerceAtMost(Adventure.threshold(100)),previous.files+1,previous.bytes+(file.size.coerceAtMost(Long.MAX_VALUE-previous.bytes)))else previous
                if(added==1)db.execSQL("UPDATE journey SET xp=?,files=?,bytes=? WHERE id=1",arrayOf(current.xp,current.files,current.bytes))
                db.setTransactionSuccessful();return current to (current.xp-previous.xp).toInt()
            }finally{db.endTransaction()}
        }
    }
}

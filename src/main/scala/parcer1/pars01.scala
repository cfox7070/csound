package parser1

import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable

import java.io._

abstract class ScoreElem {
    def scoStr():String
}

class Comments (str:String) extends ScoreElem(){
    
    override def scoStr():String= s";$str"
//  ^[\s]*(\/\/.+)$
}
object Comments {
    def apply(str:String) : Comments=new Comments(str)
}

class Native1(code:String) extends ScoreElem(){
    override def scoStr():String= code
}

object Native1 {
   def apply(code:String)=new Native1(code)
}

class Native (code:String,cmt1:String,cmt2:String) extends ScoreElem(){
/*
< //this is native csound code
f1 0 16384 10 1                                          ; Sine
f2 0 16384 10 1 0.5 0.3 0.25 0.2 0.167 0.14 0.125 .111   ; Sawtooth
>
*/
    
    override def scoStr():String= s";$cmt1\n"+code+s";$cmt2\n"

}
object Native{

    def strOrCms(s:String) = if(s!=null) s else ""
    
    def apply(str:String, iter:Iterator[String]) : Native ={
        val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments   use function with substrings in trait
        val enative="^>(.*)".r
        val rcom(_:String,co1)=str
        var co2=""
        var cd:String=""
        var continue=true
        while(iter.hasNext && continue ){
            val s=iter.next.trim
            s match {
               case enative(es) =>{ val rcom(_:String,co)=es;co2=co;continue=false}
               case cds => cd+=cds+"\n"
            }           
        }
      new Native(cd,strOrCms(co1),strOrCms(co2))  
        
    }
}


class Measure (str:String) extends ScoreElem(){ 
//  m4 1 // comment
//  (^[m]([0-9]+))[\t ]+([0-9])+([\t ]*\/\/.*)*$    
    override def scoStr():String= s";$str" 
}

object Measure {
   def apply(str : String):Measure = new Measure(str)
}

object Tempo extends ScoreElem(){ 
   var vals  = List[(Double,Double)]()
   override def scoStr():String= "t " + vals.flatten{case (a,b) => List(a,b)}.mkString(" ") 
   def set(k:Double,v:Double) { vals = vals :+ (k -> v)}
}

class Note (var name:String ="",var instr:String ="",
						var start:Double =0.0, var dur:Double =0.0,var vol : Double = 0.3,
							var pitch:String ="", var params:Seq[String] = null) extends ScoreElem(){
/*
([rcdefgah]([0-9]*)(#[+-]?|##[+-]?|b[+-]?|bb[+-]?)*)([\t ]+([a-z]+)([0-9]*))*([\t ]*\/\/.*)*$
*/
    def scoStr():String =if(name.startsWith("r"))
							s";$name"
						else
							s"$instr \t%.4f\t%.4f\t%.4f\t $pitch \t  ${params.mkString("\t")} \t;$name".
																formatLocal(java.util.Locale.US,start,dur,vol)
	override def toString() = s"$name $pitch"
}

object Note {   
    val lpch=Map("c" ->"00",
				 "c#"->"01",
				 "db"->"01",
				 "d" ->"02",
				 "d#"->"03",
				 "eb"->"03",
				 "e" ->"04",
				 "e#"->"05",
				 "fb"->"04",
				 "f" ->"05",
				 "f#"->"06",
				 "gb"->"06",
				 "g" ->"07",
				 "g#"->"08",
				 "ab"->"08",
				 "a" ->"09",
				 "a#"->"10",
				 "bb"->"10",
				 "b" ->"11",
				 "b#"->"00",
				 "r" ->"")

    val durVal = Map( "1" ->4.0, 
					  "2" ->2.0,
					  "4" ->1.0,
					  "4." ->1.5,
					  "8" ->0.5,
					  "8." ->0.75,
					  "8_3" -> 1.0/3.0,
					  "16"->0.25,
					  "16_3" -> 0.5/3.0,
					  "16_6" -> 1.0/6.0,
					  "32"->0.125,
					  "32_3"-> 0.25/3,
					  "64"->1.0/16.0)
					  
	class VCur (var curOkt:Int=0,var curDur : Double=0.0,var curTime : Double=0.0,
                        var curInstr:String="i1",var curParams : Array[String] = Array[String]())
	
	val vcur = new ArrayBuffer[VCur]()
 
   def apply(str : String, sco:ArrayBuffer[ScoreElem]):Unit ={
//	val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments
	val pitchex ="^((?:[cdefgar#b]+[0-9]*[|]?)+)".r
	val pitchex1 ="^([cdefgar#b]+)([0-9]*)".r
	val inum="^i.+".r
//	val dur="^([0-9._+]+)".r
	val vols="^v([0-9.]+)".r
	val tmp="^t([0-9.]+)".r
//	val st="^st([0-9.]+)".r
	val notes = new ArrayBuffer[Note]()
    //println("string: "+str)
	val voices=str.split("[|]{2}")
	for (i <- 0 until voices.size if(score.vprod == -1 || score.vprod == i)){
	    while(vcur.size < i+1){ 
            vcur += new VCur()
            if(i!=0 && score.vprod == -1) {
                vcur(i).curTime = vcur(i-1).curTime
                vcur(i).curInstr = vcur(i-1).curInstr
            }
        }
		val vdt=voices(i).split("[\\s]+")
		for (j <- 0 until vdt.size if j!=1){
			 vdt(j) match{
				case pitchex(nt) =>{ 
									vcur(i).curTime=vcur(i).curTime+vcur(i).curDur
				                    if(vdt.size > 1)
                                        if (durVal contains vdt(1)) 
                                            vcur(i).curDur = durVal(vdt(1))
                                        else if(vdt(1)!=".")
                                            vcur(i).curDur=vdt(1).toDouble
                                            
									for(m <- nt.split("[\\s]*[|][\\s]*")){
										val pitchex1(p,o) = m
										if(o!="") vcur(i).curOkt=o.toInt
										notes += new Note(pitch = s"${vcur(i).curOkt}.${lpch(p)}",
															name = p+vcur(i).curOkt)                                            
									}
								}
				case tmp(tval) => Tempo.set(vcur(i).curTime,tval.toDouble * score.tplus)
                                   // println(s"$i ${vcur(i).curTime} $tval ${score.tplus} ${tval.toDouble + score.tplus}")
				case inum(ins) => vcur(i).curInstr=ins
				case s:String => { while (vcur(i).curParams.size < (j+1))  
                                        vcur(i).curParams = vcur(i).curParams :+ " "
                                   if(s!=".") vcur(i).curParams(j) = s
                                }
			 }				
		}
		notes.foreach((n:Note) => {n.instr=vcur(i).curInstr 
                                   n.start = vcur(i).curTime
                                   n.dur= vcur(i).curDur 
                                   n.params =vcur(i).curParams.clone})
	//	vcur(i).curParams=""
		score.mSco ++=notes
		notes.clear
	}
  }
}

object score extends App{

      val mSco=ArrayBuffer[ScoreElem]()
      var vprod = -1
      var tplus =1.0
      var nname=""

      
      val varg="^-v([0-9.]+)".r
      val targ="^-t([0-9.-]+)".r
      val farg="^-f([0-9.-]+)".r
      
      if(args.length==0 || args(args.size - 1).startsWith("-")){
            println("No file specified")
      }else{ 
//        println(args mkString "||")
        for(a <- args)
            a match {
                case varg(v) => vprod = v.toInt
                case targ(v) => tplus = v.toDouble
                case farg(v) => nname = v + ".sco"
                case _ => 
            }
    //    println(s"vprod=$vprod")
    //    println(s"tplus=$tplus")
        val fname=args(args.size - 1)
        if(nname == "") {
            val dotind=fname.lastIndexOf(".")
            nname=fname.substring(0,if (dotind> -1) dotind else fname.length )+".sco"
        }
        try {
            val src=Source.fromFile(fname)
            readFile(src.getLines)
            src.close
            
            val writer = new PrintWriter(new File(nname ))
            writer.write(Tempo.scoStr + " \n")
            mSco.foreach(x => writer.write(x.scoStr+"\n"))
            
            /*if(mSco!=null){
                for ( x <- mSco ) {
                    writer.write(x.scoStr+"\n")
                }
            }*/
            writer.close()
        } catch {
            case e: FileNotFoundException => println("Couldn't find that file.")
            case e: IOException => println("Got an IOException!")
        }     
    }
   
   def readFile(iter:Iterator[String]){
 //      val note="^[cdefgabr].*".r
      val measure="^[m](.+)".r
 //     val measure="""^[m]([0-9]+)[\t ]+([0-9])+(?:[\t ]*\/\/(.*))?$""".r
      val comments="^;(.*)".r
      val native="^<.*".r
      val native1 = "^>>(.*)".r
      while(iter.hasNext){
        val tstr=iter.next.trim
        tstr match {
            case ""=>
            case measure(m) => mSco += Measure(m)    
            case comments(cmt) =>  mSco+=Comments(cmt)       // delete
            case native1(cd) => mSco+=Native1(cd)       // delete.length>0
            case _ => Note(tstr,mSco)
        }
      }
   }   
}

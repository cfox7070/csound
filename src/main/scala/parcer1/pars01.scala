package parser1

import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer

import java.io._

abstract class ScoreElem {
    def scoStr():String
}

trait StrFns {
    def strOrCms(s:String) = if(s!=null) s else ""
}

class Comments (str:String) extends ScoreElem(){
    
    override def scoStr():String= s";$str"
//  ^[\s]*(\/\/.+)$
}
object Comments {
    def apply(str:String) : Comments=new Comments(str)
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
object Native extends StrFns{
    
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


class Measure (str:String) extends ScoreElem(){ //comments in string, errors
//  m4 1 // comment
//  (^[m]([0-9]+))[\t ]+([0-9])+([\t ]*\/\/.*)*$    
    override def scoStr():String= s";$str" 
}

object Measure {
   def apply(str : String):Measure ={   //delete this     
 //       val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments
 //       val rcom(vl,co)=str
 //       val rval="""m([0-9]+)\s+([0-9]+)""".r
 //       val rval(bits,num)=vl
 //       Note.curMStart=bits.toInt*(num.toInt-1)
 //       Note.curStart=0
        new Measure(str)
   }
 /*  def apply(bits:Int,num:Int,cmts:String):Measure ={        
 //       Note.curMStart=bits*(num-1)
 //       Note.curStart=0
        new Measure(bits,num,cmts)
   }*/
}

class Note (var name:String ="",var instr:String ="",
						var start:Double =0.0, var dur:Double =0.0,
							var pitch:String ="", var params:String ="") extends ScoreElem(){
/*
([rcdefgah]([0-9]*)(#[+-]?|##[+-]?|b[+-]?|bb[+-]?)*)([\t ]+([a-z]+)([0-9]*))*([\t ]*\/\/.*)*$
*/
    def scoStr():String =if(name.startsWith("r"))
							s";name"
						else
							s"$instr \t%.4f\t%.4f\t 0.5\t $pitch \t  $params ;$name".
																formatLocal(java.util.Locale.US,start,dur)
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
				 
//	def durVal(dv:Float)= 4/dv
 	val durVal = Map( "1" ->4.0, 
					  "2" ->2.0,
					  "4" ->1.0,
					  "4." ->1.5,
					  "8" ->0.5,
					  "8_3" -> 1.0/3.0,
					  "16"->0.25,
					  "16_3" -> 0.5/3.0,
					  "16_6" -> 1.0/6.0,
					  "32"->0.125,
					  "64"->1.0/16.0)
					  
	class VCur (var curOkt:Int=0,var curDur : Double=0.0,var curTime : Double=0.0, var curInstr:String="i1",
							var curParams : String = "")
	val vcur = new ArrayBuffer[VCur]()
 
   def apply(str : String, sco:ArrayBuffer[ScoreElem]):Unit ={
//	val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments
	val pitchex ="^((?:[cdefgar#b]+[0-9]*[|]?)+)".r
	val pitchex1 ="^([cdefgar#b]+)([0-9]*)".r
	val inum="^i.+".r
	val dur="^([0-9._+]+)".r
//	val st="^st([0-9.]+)".r
	val notes = new ArrayBuffer[Note]()
    //println("string: "+str)
	val voices=str.split("[|]{2}")
	for (i <- 0 until voices.size){
	    if(vcur.size < i+1){ vcur += new VCur()
							 if(i!=0) {
								vcur(i).curTime = vcur(i-1).curTime
								vcur(i).curInstr = vcur(i-1).curInstr
							 }
						   }
		val vdt=voices(i).split("[\\s]+")
		for (j <- 0 until vdt.size){
			 vdt(j) match{
				case pitchex(nt) =>{ 
									vcur(i).curTime=vcur(i).curTime+vcur(i).curDur
				                    if(vdt.size > 1 && durVal.contains(vdt(1))) vcur(i).curDur = durVal(vdt(1))
									for(m <- nt.split("[\\s]*[|][\\s]*")){
										val pitchex1(p,o) = m
										if(o!="") vcur(i).curOkt=o.toInt
										notes += new Note(pitch = s"${vcur(i).curOkt}.${lpch(p)}",
															name = p+vcur(i).curOkt, start = vcur(i).curTime,
															  dur= vcur(i).curDur )
									}
								}
				case dur(d) => if(j!=1){vcur(i).curParams += " "+d; println("d="+d+" v="+voices(i))}
				case inum(ins) => vcur(i).curInstr=ins
				case s:String => vcur(i).curParams += " "+s
			 }				
		}
		notes.foreach((n:Note) => {n.instr=vcur(i).curInstr; n.params =vcur(i).curParams})
		vcur(i).curParams=""
		score.mSco ++=notes
		notes.clear
	}
  }
}

object score extends App with StrFns{

      val mSco=ArrayBuffer[ScoreElem]()
      
      if(args.length==0){
            println("No file specified")
      }else{     
        val fname=args(0)
        try {
            val src=Source.fromFile(fname)
            readFile(src.getLines)
            src.close
            val dotind=fname.lastIndexOf(".")
            val nname=fname.substring(0,if (dotind> -1) dotind else fname.length )+".sco"
            
            val writer = new PrintWriter(new File(nname ))
            
            if(mSco!=null){
                for ( x <- mSco ) {
                    writer.write(x.scoStr+"\n")
                }
            }
            writer.close()
        } catch {
            case e: FileNotFoundException => println("Couldn't find that file.")
            case e: IOException => println("Got an IOException!")
        }     
    }
   
   def readFile(iter:Iterator[String]){
 //      val note="^[cdefgabr].*".r
      val measure="^([m].+)".r
 //     val measure="""^[m]([0-9]+)[\t ]+([0-9])+(?:[\t ]*\/\/(.*))?$""".r
 //     val comments="^//(.*)".r
      val native="^<.*".r
      while(iter.hasNext){
        val tstr=iter.next.trim
        tstr match {
            case ""=>
//            case note(_*) => println("note");Note(tstr,mSco)      // delete
            case measure(m) =>{mSco += Measure(m)}    
//            case comments(cmt) => {println(s";$cmt");mSco+=Comments(strOrCms(cmt)) }      // delete
            case native(_*) => {println(";native");mSco+=Native(tstr,iter) }       // delete.length>0
            case _ => Note(tstr,mSco)
        }
      }
   }   
}

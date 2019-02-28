package parser1

import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer

import java.io._

abstract class ScoreElem {
    def outStr():String
}

trait StrFns {
    def strOrCms(s:String) = if(s!=null) s else ""
}

class Comments (str:String) extends ScoreElem(){
    
    override def outStr():String= s";$str"
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
    
    override def outStr():String= s";$cmt1\n"+code+s";$cmt2\n"

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


class Measure (bits:Int,num:Int,cmts:String) extends ScoreElem(){ //comments in string, errors

//  m4 1 // comment
//  (^[m]([0-9]+))[\t ]+([0-9])+([\t ]*\/\/.*)*$
    
    override def outStr():String= s";Measure$bits $num    "+ cmts 

}

object Measure {
   def apply(str : String):Measure ={   //delete this     
        val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments
        val rcom(vl,co)=str
        val rval="""m([0-9]+)\s+([0-9]+)""".r
        val rval(bits,num)=vl
        Note.curMStart=bits.toInt*(num.toInt-1)
        Note.curStart=0
        new Measure(bits.toInt,num.toInt,co)
   }
   def apply(bits:Int,num:Int,cmts:String):Measure ={        
        Note.curMStart=bits*(num-1)
        Note.curStart=0
        new Measure(bits,num,cmts)
   }
}

class Note (st:String, ninst:Int,start:Float,dur:Float,pitch:String,cmts:String) extends ScoreElem(){
/*
([rcdefgah]([0-9]*)(#[+-]?|##[+-]?|b[+-]?|bb[+-]?)*)([\t ]+([a-z]+)([0-9]*))*([\t ]*\/\/.*)*$
*/
    override def outStr():String= {
		val cmt= if((cmts!=null) && (cmts!="")) s"\t;$st\t$cmts" else s"\t;$st"
		s"i $ninst \t%.4f\t%.4f\t 0.5 \t $pitch \t  $cmt".formatLocal(java.util.Locale.US,start,dur)
	}

}

object Note {
   var curNinst:Int=1
   var curStart:Float=0f
   var curMStart:Float=0f
   var curDur:Float=0f
   var curOkt:Int=8
   
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
				 
	def durVal(dv:Float)= 4/dv
/* 	val durVal = Map( "1" ->4.0f, 
					  "2" ->2f,
					  "4" ->1f,
					  "8" ->0.5f,
					  "16"->0.25f,
					  "32"->0.125f,
					  "64"->(1.0/16.0).toFloat)
 */
   def apply(str : String, sco:ArrayBuffer[ScoreElem]):Unit ={
	val rcom="""^([^\/]*)(?:\/\/(.*))?$""".r //value and comments
	val pitchex="^([cdefgar#b]+)([0-9]*)".r
	val inum="^i([0-9]+)".r
	val dur="^dur([0-9.]+)".r
	val st="^st([0-9.]+)".r
	
  	var rcom(vl,co)=str
	if(co!=null && co!="") co="//"+co
    val vls=vl.split("[ \t]+")
	var nts=new ArrayBuffer[(Int,String,String)]()
	for (s <- vls){
		s match {
			case pitchex(p,o) => { if (o!="")curOkt=o.toInt; nts+= ((curOkt,lpch(p),s))}
			case inum(in) => curNinst=in.toInt
			case dur(nm) => curDur=durVal(nm.toFloat)
			case st(nm) => curStart=nm.toFloat
			case _ =>
		}
	}
	//println(nts)
	for ((o,p,st) <- nts){
		if(p!="") sco+=new Note(st,curNinst,curMStart+curStart,curDur,s"$o.$p",co)
	}
	curStart+=curDur
   }
}


object score extends App with StrFns{

      val mSco=ArrayBuffer[ScoreElem]()
      
      if(args.length==0){
            println("No file specified")
      }else{     
        val fname=args(0)
        println(fname)      // delete
        try {
            val src=Source.fromFile(fname)
            readFile(src.getLines)
            src.close
            val dotind=fname.lastIndexOf(".")
            val nname=fname.substring(0,if (dotind> -1) dotind else fname.length )+".sco"
            
            val writer = new PrintWriter(new File(nname ))
            
            if(mSco!=null){
                for ( x <- mSco ) {
                    writer.write(x.outStr+"\n")
                }
            }
            writer.close()
        } catch {
            case e: FileNotFoundException => println("Couldn't find that file.")
            case e: IOException => println("Got an IOException!")
        }     
    }
   
   def readFile(iter:Iterator[String]){
       val note="^[cdefgabr].*".r
 //     val measure="^m.*".r
      val measure="""^[m]([0-9]+)[\t ]+([0-9])+(?:[\t ]*\/\/(.*))?$""".r
      val comments="^//(.*)".r
      val native="^<.*".r
      while(iter.hasNext){
        val tstr=iter.next.trim
        tstr match {
            case ""=>
            case note(_*) => println("note");Note(tstr,mSco)      // delete
            case measure(bits,num,cmt) =>{println(s"$bits $num $cmt");mSco += Measure(bits.toInt,num.toInt,strOrCms(cmt)) }    
            case comments(cmt) => {println(s";$cmt");mSco+=Comments(strOrCms(cmt)) }      // delete
            case native(_*) => {println(";native");mSco+=Native(tstr,iter) }       // delete.length>0
            case _ =>
        }
      }
   }   
}

package jegatheeswaran.spark.core
case class trans(tid:Long,dt:String,cid:Long,amt:Double,cat:String,prod:String,city:String,State:String,transtype:String)
class AllMethods  { //reusable framework
    def mintrans(a:Double,b:Double):Double=
  {
    if (a < b) a else b    
  }
 
  def rddminmaxsum(a:org.apache.spark.rdd.RDD[Double]):(Double,Double,Double)=
  {
    if (!a.isEmpty())
    {
    return (a.min(),a.max(),a.sum())
    }
    else
    return (0.0,1.0,0.0)
  }
  
  def cleanupperlocal(a:String):String=
  {
    return(a.trim().toUpperCase())
  }
}
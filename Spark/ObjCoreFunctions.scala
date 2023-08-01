
import jegatheeswaran.spark.core._
import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.storage.StorageLevel._
//import org.apache.spark.SparkContext
//import org.apache.spark.rdd._
//import org.apache.spark.
////import org.apache.spark.SparkConf
//import org.apache.spark.SparkContext
//import org.apache.spark.storage.StorageLevel.DISK_ONLY_2
//import org.apache.spark.rdd.RDD.doubleRDDToDoubleRDDFunctions
//import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object ObjCoreFunctions extends AllMethods // once for all instantiated class ObjCoreFunctions with AllMethods(Single Inheritance) - singleton object
{
 
 /* def mintrans(a:Double,b:Double):Double=
  {
    if (a < b) a else b    
  }
 
  def rddminmaxsum(a:org.apache.spark.rdd.RDD[Double]):(Double,Double,Double)=
  {return (a.min(),a.max(),a.sum())}
  
  def cleanupperlocal(a:String):String=
  {
    return(a.trim().toUpperCase())
  }*/
  
  def main(args:Array[String])
  {
   // Creating the Spark context to access the spark cluster in a traditional way
   //define spark configuration object
    val conf = new SparkConf().setAppName("Local-sparkcore").setMaster("local[2]")
   //define spark context object
    val sc = new SparkContext(conf) //primary constructor
   // val sc1 = new SparkContext() //auxilary constructor
    //val sqlctx=new org.apache.spark.sql.SQLContext(sc)
    //Set the logger level to error
    sc.setLogLevel("ERROR")


    
  // I have 1 driver(AM) and 2 executors (containers), what will happen? 
   
    val scalalocalval=Array(100,200); 
    //kept in driver - 6 bytes of data (totally 6 bytes)

    val broadcastval=sc.broadcast(scalalocalval) 
    // going to placed in all worker nodes where executors are started - 
    // each executor with 6 bytes of data (totally 12 bytes)
   
    val rddprogramatically=sc.parallelize(scalalocalval) 
   // resilient dist dataset - each executor partition with 3 bytes of data (totally 6 bytes)
    
   val scalabacktolocal=rddprogramatically.map(x=>x+10).collect 
   // how many jobs? 1, how many stages? 1, how many partitions? 2,how many tasks? 2 
   //collecting back data in the driver - 6 bytes of data with value of Array(110,210)
   
    // Create a file rdd with 4 partitions
   val rdd = sc.textFile("file:///home/hduser/hive/data/txns",4) //resilient dist dataset
   // create a file rdd with 4 partitions 
   
   // create the below dir in hadoop and place the file txns in the below dir
   //hadoop fs -mkdir -p hive/data/
   //hadoop fs -put -f ~/hive/data/txns hive/data/
   
   val hadooprdd = sc.textFile("hdfs://127.0.0.1:54310/user/hduser/hive/data/txns",4)
   
   println("Number of partition of the base file is " + rdd.getNumPartitions);
   println("total number of lines in hadoop - " + hadooprdd.count);
   println("print a sample of 20 rows with seed of 110 for dynamic data")
   rdd.takeSample(true, 10, 110).foreach(println); // random sampling
   //Array(1,2,3,4,5,6) -> take(3)-1,2,3 , takeSample(true,3,10) - 2,5,3 takeSample(true,3,101) - 1,5,6
   
   println("print only the first line of the dataset " );
   println(rdd.first());
   val x=rdd.first()
   // RDD(Array(Create, a, splitted, rdd, to, split, the), 
   // Array(line, of, string, to, line, of, array))
   //if (!rdd.isEmpty()) // if(rdd.first().length>0)
   
   val rddsplit=rdd.map(x=>x.split(","));

////////////////////////////////////////////////////////////////////////////////////////////////////   
   // filter only the category contains exercise or product starts with jumping.
   
   val rddexerjump = rddsplit.filter(x => x(4).toUpperCase.contains("EXERCISE") || x(5).toUpperCase.startsWith("JUMP"))
   val valexerjumpcnt= rddexerjump.count()
   // above value created in driver
   println("The count of category contains exercise or product starts with jumping " + valexerjumpcnt)

   
   println("Driver is Dynamically increase or decrease the number of partitions based on the volume of filtered data")
  println("Partition handling in Spark")
  
  //Try to convert this as a method
 // volume of data varies on a daily basis, how do u improve the performance 
  
  //val rddcoleased=rddexerjump.coalesce(1);
  //Performance Tuning 
  var rddexerjumpnew=sc.emptyRDD[Array[String]]; //global scope accross everywhere
  println(rddexerjumpnew.isEmpty())
  
/*  var abcd=0 //scope is everywhere
if (true) {
abcd=100; 
println(abcd) // i can access everywhere
} 
println(abcd) // i can access everywhere
*/  
  if (valexerjumpcnt > 0 && valexerjumpcnt <= 10000)  
  {
    //val rddcoalesed=rddexerjump.coalesce(2); //local scope within the if condition
    
    rddexerjumpnew=rddexerjump.coalesce(2);
    println(rddexerjumpnew.isEmpty())
    println(s"Number of partition of the filtered data with $valexerjumpcnt count " + rddexerjumpnew.getNumPartitions);    
  }   else if (valexerjumpcnt > 10001 && valexerjumpcnt < 50000)
  {
    rddexerjumpnew=rddexerjump.coalesce(3);
    println(s"Number of partition of the filtered data with $valexerjumpcnt count " + rddexerjumpnew.getNumPartitions); 
  }   else 
  {
    rddexerjumpnew=rddexerjump.repartition(6);
    println(s"Number of partition of the filtered/union data with $valexerjumpcnt count " + rddexerjumpnew.getNumPartitions); 
  }
  
   //println(rddcoalesed.count) // cant be printed as the scope is within the if condition
  
   val rddexerjumpcnt1=rddexerjumpnew.count();
   println("Count the filtered rdd with new partition " + rddexerjumpcnt1)
   println("Display only first row ")
   rddexerjumpnew.take(1)
   println(" Check whether the newrdd is empty true/false " + rddexerjumpnew.isEmpty())
   println(" New RDD partition count " + rddexerjumpnew.getNumPartitions)
   println("No of lines are " + rddexerjumpcnt1 + " with exercise or jumping");
   println(s"No of lines are $rddexerjumpcnt1 with exercise or jumping");
//////////////////////////////////////////////////////////////////////////////////////////////   
   
 // Identify the non credit transactions alone
   //00000859,07-29-2011,4001263,074.35,Outdoor Play credit,Sandboxes,Sunnyvale,California,cash - false,false->true
   //00000860,07-29-2011,4001263,074.35,Outdoor Play Equipment,Sandboxes,Sunnyvale,California,cash - true, false->true
   //00000861,07-29-2011,4001263,074.35,Outdoor Play Equipment,Sandboxes,Sunnyvale,California,credit - true, true->false
   val rddcreditstrsearch = rdd.filter(x => !x.contains("cred")) // can search for a pattern accross the string of fields
   val rddcredit = rddsplit.filter(x => !x.contains("credit")) // can search for a literal accross the fields in array
   val rddcreditexact = rddsplit.filter(x => !x(8).contains("red")) // can search for a pattern wildcard/like operation on a given field x(8)
   val rddcreditexactcredit = rddsplit.filter(x => x(8) != "credit")// true, true, false
   val rddcreditexactcash = rddsplit.filter(x => x(8) != "cash") 
// use this != operator the direct value will be compared
   val rddcreditexactcash1 = rddsplit.filter(x => !x.equals("cash")) 
// use this ! operator for negation in the case of functions we use result will be same as above, but return of the equals function will be negated
   
  val cnt = rddcreditexact.count()
  println(s"No of lines that does not contain Credit: $cnt" )

//Ways to access RDD elements -> base rdd -> split to access using index eg. rddsplit -> map it into tuples to access using position
  // or apply case class to access by name (best one to use)

  
//Requirement identify only California cash transactions and show me a report of sales - total, sum, min, max, average 
  println(" Requirement identify only California cash transactions and show me a report of sales - total, sum, min, max, average ")
  
  println(" Transform the RDD with only California data who did cash transactions ")   
  
  // Convert into tuples and do the filter by position // optimal case scenario
  
  val rdd2tuples=rddsplit.map(x=>(x(0).toLong,x(1),x(2).toLong,x(3).toDouble,x(4),x(5),x(6),x(7),x(8))) //converting from Array-index to tuple-position
  val rdd2filter=rdd2tuples.filter(x=>x._8== "California" && x._9 == "cash") //filter by position
  val rdd3amt = rdd2filter.map(x => x._4) //select by position
  
  val rdd4amt=rddsplit.map(x=>x(3)) //select by index

  //Convert into Schemaed RDD applying Case class and do the filter by name // best case scenario
  
  //case class shouldn't be created inside the main method
  //case class trans(tid:Long,dt:String,cid:Long,amt:Double,cat:String,prod:String,city:String,State:String,transtype:String)
 // rdd string-> rdd splitted Array[String]-> tuple rdd Array[Tuples()]
 //                                        -> schemaed Rdd Array[case class type]
  val rdda = sc.textFile("file:///home/hduser/hive/data/txns",4)
  val rddsplita=rdda.map(_.split(","))
  //case class trans(tid:Long,dt:String,cid:Long,amt:Double,cat:String,prod:String,city:String,State:String,transtype:String)
  val rdd2schemardd=rddsplita.map(x=>trans(x(0).toLong,x(1),x(2).toLong,x(3).toDouble,x(4),x(5),x(6),x(7),x(8))) 
  val rdd2filterschema=rdd2schemardd.filter(x=>x.State== "California" && x.transtype == "cash")  
  val rdd3amtschema = rdd2filterschema.map(x => x.amt) //select by name
  //val rdd3amtschema = rdd2filterschema.map(x => x._4)
  
  // Use the splitted rdd of type RDD[Array[String]] then filter by index // worst case scenario
  
  val rdd2 = rddsplit.filter(x => x(7) == "California" && x(8) == "cash")
  
   println(" Transform the RDD by taking only the amount column using index")
   val rdd3 = rdd2.map(x => x(3).toDouble)
   //rdd3=Array(32.65,10.01)

   //or
   
   //Ways to access RDD elements -> base rdd -> split to access using index eg. rddsplit -> map it into tuples to access using position
   val rddtuples=rdd2.map(x=>(x(0).toLong,x(1),x(2).toLong,x(3).toDouble))  
   
   println(" Transform the RDD by taking only amount column using position")
   val rdd3byposition = rddtuples.map(x => x._4)
   
   println(" Caching/Persist of RDDs ")
     rdd3.cache()
     //rdd3.unpersist()
        
     //rdd3.persist(DISK_ONLY_2)
     
     val sumofsales = rdd3.sum()
     println("Sum of Sales in california with cash: " + sumofsales)
     //48723.02999999999
     val sumofsalesreduce= rdd3.reduce((x,y)=>x+y);
     //48723.02999999999
     println("Sum of total Sales in california with cash using reduce : " + sumofsalesreduce)
     
     val sumofsalesreduce20dollar= rdd3.reduce((x,y)=>  if (y > 20.0) x+y else x );
     val sumofsalesreduce30dollar= rdd3.reduce((x,y)=>  if (y > 30.0) x+y else x );
     println("Sum of Sales where trans amount is > 20 dollar: " + sumofsalesreduce20dollar)
     println("Sum of Sales where trans amount is > 30 dollar: " + sumofsalesreduce30dollar)
     
     println(" Lets try to achieve the same above functionality using filter")

     val rddfilter3 = rdd3.filter(x=>x>20.0)
     val rddfilter4 = rdd3.filter(x=>x>30.0)
     //val rddfilter31 = rdd2.filter(x=>x(3).toDouble>30.0).map(x => x(3).toDouble)
     val sumofsalesfilterreduce= rddfilter3.sum;
     val sumofsalesfilterreduce1= rddfilter4.sum;
     
     println("Sum of sales (using filter) where minimum trans amount is > 20 dollar: " + sumofsalesfilterreduce)
     println("Sum of sales (using filter) where minimum trans amount is > 20 dollar: " + sumofsalesfilterreduce1)

     
     val maxofsales = rdd3.max()
     println("Max sales value : " + maxofsales)
     
     val maxofsalesusingreduce= rdd3.reduce((x,y)=>  if (x>y) x else y  );
     
     val totalsales = rdd3.count     
     println("Total no fo sales: " + totalsales)
       
     val minofsales1 = rdd3.min

     // Using Custom Function based approach 
     // Can I call a scala method by passing rdd as an argument?
     
    //import org.inceptez.spark.core._;
    //val obj1=new AllMethods;
     
     val minmaxsumofsales = rddminmaxsum(rdd3);

     // Using Built in Function based approach     
     val minmaxsumofsalesdirectway=(rdd3.min,rdd3.max,rdd3.sum);
     
     println(" sales value computed using rddmin method which takes rdd as input and returns the min sales: " + minmaxsumofsales._1)
     println("sales value computed using rddmin method which takes rdd as input and returns the max sales: " + minmaxsumofsales._2)
     println("sales value computed using rddmin method which takes rdd as input and returns the sum sales: " + minmaxsumofsales._3)
     
     val avgofsales = minmaxsumofsales._3/rdd3.count()
     println("Avg sales value : " + avgofsales)
        
     rdd3.unpersist(); // if you dont unpersist, spark fw will unpersist when ever he needs this memory for other rdds or when the sc stopped when application is finished
     
 /////////////////////////////////////////////////////////////////////////////
// Apply a function to every element of a row of the RDD
//import org.inceptez.spark.reusableutils.Cleanup;

   
     val rddtrimupperlocal=rddsplit.map(x=>(x(0),x(1),x(2),x(3),cleanupperlocal(x(4)))) //call ur own user def method
     //val objcleanup=new org.inceptez.spark.sql.reusablefw.cleanup;
     val rddtrimupper=rddsplit.map(x=>(x(0),x(1),x(2),x(3),(x(4))))//call the user def common method
     val rddtrimupper1=rddsplit.map(x=>(x(0),x(1),x(2),x(3),x(4).trim().toUpperCase())) //dont use user def method rather use predefined
   
   println(" Printing the sample data of the rddtrimupper applied trim and upper case on category using trimupcase udf")
   val take10=rddtrimupper.take(10)
   take10.foreach(println)
   
   println("Using set opertor Print only the fields of (txnno,date,custno,category,state) of non exercise and non jumping data set from the whole data")

   //val rddexerjumpnew = rddsplit.filter(x => x(4).toUpperCase.contains("EXERCISE") || x(5).toUpperCase.startsWith("JUMP"))   
   
   // we can achieve the same result using set operator
   //val rddexerjump = rddsplit.filter(x => !x(4).toUpperCase.contains("EXERCISE") && !x(5).toUpperCase.startsWith("JUMP"))   
   
   val rddsplittuple = rddsplit.map(x=>(x(0).toLong,x(1),x(2).toInt,x(4),x(6))) //whole data 95k
   val rddexerjumpingtuple=rddexerjumpnew.map(x=>(x(0).toLong,x(1),x(2).toInt,x(4),x(6))) // exer and jumping data 15k
   println(rddsplittuple.count)
   println(rddexerjumpingtuple.count)
   // we can achieve the same result using set operator (usually used in different source data, not the same dataset)
   val rddsubtract=rddsplittuple.subtract(rddexerjumpingtuple) // 80k
   println(rddsubtract.count)
   rddsubtract.take(10).foreach(println)
   
   // Which one is better - Since we have to do the filter again, we can use subract rather with the existing rdds.
   // if we don't have already the filetered RDD, then apply negated filter is better.
   
   //val rddunion2cols = rddunion.map(x=>(x._3,x._4))
   val rddsubtractdistinct = rddsubtract.distinct
     
  println("Deduplicated count of subtracted rdd contains non exercise and jumping data  " + rddsubtractdistinct.count);
       
  println(" Paired RDD Operations ")   
  println("City wise count : ")
  import org.apache.spark.storage.StorageLevel._
  val rddkvpair=rddsplit.map(x=>((x(6)),(x(3).toDouble))) 
  rddkvpair.persist(DISK_ONLY);
  rddkvpair.countByKey().take(10).foreach(println)
  
  println("City wise sum of amount : ")
  
  rddkvpair.reduceByKey((x,y)=>x+y).take(10).foreach(println)
  
  println("City wise maximum amount of sales: ")
  
  rddkvpair.reduceByKey((a,b)=> (if (a > b) a else b)).take(10).foreach(println)
  
  val rddcity=rddkvpair.reduceByKey(_+_)
  //phidelphia,(10.1,10,20),baltimore,(10,20,30) -> (10.1,10,20).reduceByKey(x,y=>x+y) -> (phidelphia,40.1)
  //phidelphia -> 
  val driverdata=rddcity.take(5)
  driverdata.foreach(println)
  
  println("Transction wise count : ")
  val rddcntbyval=rddsplit.map(x=>(x(8))) 
  val driverdata1=rddcntbyval.countByValue
  driverdata1.take(1).foreach(println)
  
  println("sum of transactions")
  val rddcntbyval1=rddsplit.map(x=>(x(3).toDouble)) 
  println(rddcntbyval1.reduce(_+_))
  
///////////////////////////////////////////////////////////////////////////////////////////
  
  println("Brodcast real example")
//driver data going to be copied into all workers nodes where tasks are going to run
   println("Create a broadcast variable manually ")

   
     println("""Pass the key of the trans data spentby to the local variable in the driver and get the 
              respective value and sum with amount to get the actual amount I collected from the customer""") 

              //driver/AM (client/any one of the node in the cluster where appmaster is going to run)
   val kvpair: Map[String,Short] = Map[String,Short]("credit" -> 2, "cash" -> 0)

   // kvpair - > ganaa.com (US)
   // broadcastkvpair -> fm (AIR)
// 00000867,08-21-2011,4001286,146.49,Water Sports,Water Polo,Fremont,California,credit
   // for every row the kvpair in driver will be called
   // Reduce the amount to the customers based on the spendby dynamically
   val rddwithlessperformance=rddsplit.map(x=>(x(0),x(1),x(3).toDouble-kvpair(x(8))))
   
   // workers where executors/containers runs
   val broadcastkvpair=sc.broadcast(kvpair)
   
   println("""Pass the key of the trans data spentby to the broadcast variable and get the 
              respective value and sum with amount """)  
              
   //worker (better way to achieve)
// if spendby=credit then 040.33 - 2 else 040.33 - 0     -> 38.33        
   val broadcastrdd=rddsplit.map(x=>(x(0),x(1),x(3).toDouble-broadcastkvpair.value(x(8))))
   
   broadcastrdd.take(4).foreach(println);
   
  // Try to get the txnno,spendby from transaction data and customer profession from customer data
 //  Question? can we Broadcast an RDD
 //  java.lang.IllegalArgumentException: requirement failed: Can not directly broadcast RDDs; 
 //  instead, call collect() and broadcast the result.
  
   val custrdd=sc.textFile("file:/home/hduser/hive/data/custs");
   custrdd.cache;
   //val broadcastrdd=sc.broadcast(custrdd)
   // if the number of fields in every row is not same, then filter only fixed number of fields rows 
   val drivercustkvpair = custrdd.map(x => x.split(",")).filter(x=>x.length==5).map(x => (x(0),x(4))).collectAsMap
   
   //drivers hughes telematics driverid, name,age,address,mobile,ssn file-driverinfo.csv
   // driver riding the vehicles -> 1 is overspeeding 
   //capture driverkvpair:Map[Int,Long]=Map(1->9840800131,2->3423423424,3->34342143453)
   println("""Load the custid,profession in a broadcast var and join with the transaction rdd to add the 
            profession in the output""")     
   val broadcastcustkvpair=sc.broadcast(drivercustkvpair)
   //10000 kv pairs -> broadcasted to the workers where exe are running
   
  //txnno(txns),spendby(txns),profession(custs)
  //00000000,credit,Actor            
   val broadcastcustrdd=rddsplit.map(x=>(x(0),x(8),broadcastcustkvpair.value(x(2))))
   //broadcastcustkvpair.destroy()
   broadcastcustrdd.take(4).foreach(println);
  
   println("Save the output to hadoop ")
  
  val fs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:54310"),sc.hadoopConfiguration)
  fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/broadcastrdd"),true)

  //.set("spark.hadoop.validateOutputSpecs","false")

  broadcastcustrdd.saveAsTextFile("hdfs://localhost:54310/user/hduser/broadcastrdd")
  
  /// Join Scenario which is not supposed to be done in the Spark Core:
  
  println("Join Scenario (join is not advisable in spark core)")
  println("Create paired rdd to perform join from trans and cust data ") 
  val transkvpair = rddsplit.map(x => (x(2),(x(0),x(1),x(3),x(5))))
  transkvpair.cache;
// select t.custno,t.txnno,t.dt,t.amt,t.state,c.custno,c.lastname,c.age from trans t join cust c on(t.custno=c.cusno)   
  val custrddsplit=custrdd.map(x => x.split(","))
  val custrddkvpair = custrddsplit.map(x => (x(0),(x(2),x(3))))
  println("Perform inner join ")    
  val custtransjoin = transkvpair.join(custrddkvpair)
  //val custtransjoin: RDD[(String, ((String, String, String, String), (String, String)))]
  println("Map the required fields after joining ")    
  val finaljoinrdd=custtransjoin.map(x=>(x._1,x._2._1._3))  
  println("Print the required fields after joining ")
  finaljoinrdd.take(10).foreach(println)
  println("Writing the output to hdfs with the ~ delimiter using productiterator and mkstring")

  fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/joinedout/"),true)  
  custtransjoin.map(x=>Array(x._1,x._2._1._3).mkString("~")).saveAsTextFile("hdfs://localhost:54310/user/hduser/joinedout/")
//custtransjoin.toDF().write.option("delimiter","~").csv("location")
  }  
}



















package jegatheeswaran.spark.core

import org.apache.spark.{SparkContext,SparkConf};
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SparkSession


object SingleObjSample1 {
  case class data1(amt:String,name:String)

  def main(args:Array[String])
  {
    println("I am a scala program")
    //execute a program - alt+shift+x  s

//traditional way - upto version 2.x of Spark
    val conf=new SparkConf().setMaster("local[1]").setAppName("wd26 spark core app") 
    val sc=new SparkContext(conf) // spark core functions - RDDs
    
    val sqlc=new org.apache.spark.sql.SQLContext(sc) // spark sql functionalities - DF, DS, TViews 
    val hqlc=new org.apache.spark.sql.hive.HiveContext(sc) // spark hive functionalities - HQL on Hive tables
    
    
    println("Spark context object is created, I am a spark core program now")

    sc.setLogLevel("error")
    println("only error will be shown going forward")
    val rdd1=sc.textFile("hdfs://localhost:54310/user/hduser/custout2/*")
    
    println("spark context in traditional way " + sc)
    println("rdd created using spark context in traditional way " + rdd1.getNumPartitions)
    println("sql context in traditional way " + sqlc)
    
    val rdd2=rdd1.map(x=>x.toUpperCase) // transformation
   // println(rdd2.count) //action
    println(rdd2.getNumPartitions)
    
    println("Spark SQL context object is created, I am a spark SQL program now")
    
    val df1=sqlc.read.csv("hdfs://localhost:54310/user/hduser/custout2/part-00000")
    //df1.select("_c0").show(10,false) //dsl
    
    df1.createOrReplaceTempView("df1view")
   // sqlc.sql("select * from df1view").show(10,false) //sql
 
    sc.stop();
    
    //modern way - after version 2.x of Spark    
    //The builder pattern is a design pattern designed to provide a flexible solution to various object creation problems in object-oriented programming.
    val spark = org.apache.spark.sql.SparkSession.builder().appName("WD26 spark sess prog").master("local[2]").getOrCreate()
    //.appName("WD26 spark sess prog").master("local[2]").getOrCreate()
    
    //val sparkContext=new org.apache.spark.SparkContext(conf) // if no sc is already available in this app then create will be called in getOrCreate
    //val sparkContext=sc // if sc is already available in this app then get will be called in getOrCreate to make use of existing sparkcontext obj.
        
    val rddusingsparksess=spark.sparkContext.textFile("hdfs://localhost:54310/user/hduser/custout2/part-00000") // engine // notepad
    val rdd3=rddusingsparksess.map(_.split(",")).map(x=>data1(x(0),x(1))).filter(x=>x.name.startsWith("irfa"))
    rdd3.take(5).foreach(println)
    
    //import spark.sqlContext.implicits._
    //val df11=rdd3.toDF()
    
    val df11=spark.sqlContext.createDataFrame(rdd3);
    
    println("spark context in modern way " + spark.sparkContext);
    println("rdd created using spark sess.spark context in modern way " + rddusingsparksess.getNumPartitions);
    println("sql context in modern way " + spark.sqlContext);
    
    val df1usingsparksess=spark.sqlContext.read.csv("hdfs://localhost:54310/user/hduser/custout2/part-00000") // driver seat of a vehicle
    df1usingsparksess.select("*").where("_c1 like 'irfa%'").show(5,true) //dsl //excel
    
    df1usingsparksess.createOrReplaceTempView("view1")   // sql // table
    spark.sql("select * from view1 where _c1 like 'irfa%'").show(5,true)
    
    
    
  }
}
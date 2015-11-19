/* SparkRF.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils


object SparkRF {
    def main(args: Array[String]) {
        val conf = new SparkConf().setAppName("SparkRF")
        val sc = new SparkContext(conf)
        val data = MLUtils.loadLibSVMFile(sc, "/tmp/LIBSVM-USDJPY-2015-08.txt", true, 4)
        val splits = data.randomSplit(Array(0.8, 0.2))
        val (trainingData, testData) = (splits(0), splits(1))
        val numClasses = 3 //rise, drop or remain unchanged
        val categoricalFeaturesInfo = Map[Int, Int]()
        val numTrees = 10 //can change in the future
        val featureSubsetStrategy = "auto" // Let the algorithm choose.
        val impurity = "gini"
        val maxDepth = 30 //max depth currently supported
        val maxBins = 32

        val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
        val labelAndPreds = testData.map { point =>
            val prediction = model.predict(point.features)
            (point.label, prediction)

        }

        val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
        println("Test Error = " + testErr)
        println("Learned classification forest model:\n" + model.toDebugString)

        model.save(sc, "/tmp/rfmodel-30-10")
      
  }
}

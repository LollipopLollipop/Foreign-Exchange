/* SparkRF.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionModel}


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
        val PredsAndLabels = testData.map { point =>
            val prediction = model.predict(point.features)
            (prediction,point.label)

        } 
        
        val mmetrics = new MulticlassMetrics(PredsAndLabels)
        val confusionMatrix = mmetrics.confusionMatrix
        val testErr = PredsAndLabels.filter(r => r._1 != r._2).count.toDouble / testData.count()
        println("Test Error = " + testErr)
        
        
        val treeModel = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  impurity, maxDepth, maxBins)
        val treePredsAndLabels = testData.map { point =>
            val treePrediction = treeModel.predict(point.features)
            (treePrediction,point.label)

        }
        
        val treeMetrics = new MulticlassMetrics(treePredsAndLabels)
        val treeConfusionMatrix = treeMetrics.confusionMatrix
        val treeTestErr = treePredsAndLabels.filter(r => r._1 != r._2).count.toDouble / testData.count()
        println("Test Error = " + treeTestErr)
        
        
        
        // Run training algorithm to build the model
        val training = trainingData.cache()
        val LRModel = new LogisticRegressionWithLBFGS()
            .setNumClasses(3)
            .run(training)
        
        // Compute raw scores on the test set.
        val LRPredsAndLabels = testData.map { point =>
          val LRPred = LRModel.predict(point.features)
          (LRPred, point.label)
        }

        val LRMetrics = new MulticlassMetrics(LRPredsAndLabels)
        val LRConfusionMatrix = LRMetrics.confusionMatrix
        val LRTestErr = LRPredsAndLabels.filter(r => r._1 != r._2).count.toDouble / testData.count()
        println("Test Error = " + LRTestErr)

      
  }
}

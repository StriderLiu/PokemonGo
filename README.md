# PokemonGo

Brand New Version of PikaPika

Pokemon Go Analysis-Poke Monsters Rarity Spawn Prediction

Big Data Systems Engineer using Scala

Professor Robin Hillyard (Github @rchillyard/Scalaprof)

By: Wenbo Liu, Shuxian Wu
     
You can find our presentation on Prezi.com at the link:

http://prezi.com/rzgxbe7i7xoh/?utm_campaign=share&utm_medium=copy&rc=ex0share

# Summary

     The Dataset we worked on is very new, which is only around 2 months old. There are few reference we can use. We would like to be one of a few pioneers to explore Pokemon data. And hople our project can be a good resource for others. 
     
     To speed up the application, we save the ML models in the /resources/models directory and each time a user requests for a prediction, the saved model will be applied.


# Algorithms and Accuracy

Logistic Regression, Neural Network and Decision Tree

  (1) With 3 rarity (common, rare and very rare), accuracy is 89.27% (We are using this option for final delivery.)
     
  (2) With 15 types, accuracy is around 40%
     
  (3) With 151 poke monsters, the highest accuracy we got is 22%

# Brief Instruction

To get started, you need Play activator UI and run it and least once. 

Pre-cleaned dataset and pre-loaded models are saved under resources folder.

Folder visualization contains images we screenshot from Zeppelin and Tableau.

Folder exploratory keeps are the works we did before this final delivery, including iPython file from anaconda and application from older version named PikaPika. We have not included Zeppelin notebooks yet

# Data Source

Predict'em All from Kaggle: https://www.kaggle.com/semioniy/predictemall

PokemonGO from Kaggle: https://www.kaggle.com/abcsds/pokemongo

Google Map API for geocoding: https://developers.google.com/maps/documentation/geocoding/intro

Weather API Powered by Dark Sky: https://darksky.net/poweredby/

Data Science Toolkits: http://www.datasciencetoolkit.org/developerdocs#coordinates2statistics

# Toolkit

Scala 2.11.8

Spark & Spark Mllib & Spark SQL 2.0.1

Play 2.5.10

Zeppelin 0.6.2

Python 3.5     

# Challenges:

We spend a lot time on data mining to understand how each variable works toward poke monster spawning. Challenges we find are: 
     1. The dataset are generated from dump files which are created from PokemonGo players' reports. It is clean but not "clean" to use. At least half of 208 variables(not include mergerd data but original from Kaggle) are vague on usages. 
     2. With 151 classifiers(151 monsters), it is really hard to build a high accurate classification model (given only 300k instances).
     3. Only few of our variables generate from APIs; most of them were generated on user bases so that they might not be accurate.
     On technology level, we agreed to use Scala, Spark and Play Framework. However, we were still new in Scala. Problems we faced: 
     1. In MLlib, Neural Network is implemented by MultilayerPerceptronClassifier in org.apache.spark.ml package which is different from the way other ML algorithms are implemented (in org.apache.spark.mllib package). Therefore, it is a little bit tricky to provide a uniform service APIs for these algorithms. We provide a way to achieve this (see /services/Predictor.scala and /models/NeuralNetworkGen.scala).
     

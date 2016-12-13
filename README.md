# Pokémon Go Analysis

Brand New Version of PikaPika

Pokemon Go Analysis-Poke Monsters Rarity Spawn Prediction

Big Data Systems Engineer using Scala

Professor Robin Hillyard (Github @rchillyard/Scalaprof)

By: Wenbo Liu, Shuxian Wu
     
You can find our final presentation here: [Final Presentation](http://prezi.com/rzgxbe7i7xoh/?utm_campaign=share&utm_medium=copy&rc=ex0share)

Also the planning presentation is here: [Planning Presentation](http://prezi.com/4bapseyrrq5c/?utm_campaign=share&utm_medium=copy&rc=ex0share)

## Summary

The Dataset we worked on is very new, which is only around 2 months old. There are few references we can use. We would like to be one of a few pioneers to explore Pokémon data. And hope our project can be a good resource for others. 
     
To speed up the application, we save the ML models in the /resources/models directory and each time a user requests for a prediction, the stored model will be applied.


## Algorithms and Accuracy

Logistic Regression, Neural Network and Decision Tree

- With 3 rarity (common, rare and very rare), accuracy is 89.27% (We are using this option for final delivery.)
     
- With 15 poke monsters' types, accuracy is around 40%
     
- With 151 poke monsters, the highest accuracy we got is 22%

## Brief Instruction

1. To get started, you need Play activator UI and run it and least once. 
2. Pre-cleaned dataset and pre-loaded models are saved under resources folder. If you run to train your models by your own, simply delete model files and run our demo gain.
3. Folder visualization contains images we screenshot from Zeppelin and Tableau.
4. Folder exploratory keeps are the works we did before this final delivery, including python file from anaconda and application from older version named PikaPika. We have not included Zeppelin notebooks yet

## Data Source

- [Predict'em All](https://www.kaggle.com/semioniy/predictemall) from Kaggle
- [PokémonGO](https://www.kaggle.com/abcsds/pokemongo) from Kaggle
- Google Map API for [geocoding](https://developers.google.com/maps/documentation/geocoding/intro) 
- Weather API Powered by [Dark Sky](https://darksky.net/poweredby/)
- Data Science Toolkits [Coordinates to Statistics](http://www.datasciencetoolkit.org/developerdocs#coordinates2statistics)

## Toolkit

- Scala 2.11.8
- Spark & Spark MLlib & Spark SQL 2.0.1
- Play 2.5.10
- Zeppelin 0.6.2
- Python 3.5     

## Challenges:

Due to the complex structure of the dataset, we spent a lot time on data mining to understand how each variable works toward poké monster spawning. Spark and RDDs do run very fast. Since we have learned a lot about how to gain a better pre-insight of the dataset, we try to extend the full potential of Spark. Challenges we find are: 
- The dataset is generated from dump files which are created from Pokémon Go players' reports. It is clean but not "clean" to use. At least half of 208 variables (not include merged data but original from Kaggle) are vague on usages. 
- With 151 classifiers (151 monsters), it is hard to build a high accurate classification model (given only 300k instances).
- Only few of our variables were generated from APIs; most of them were generated on players' personal opnions so that data might not be accurate.

On technology level, we agreed to use Scala, Spark and Play Framework. Problems we faced: 
- In MLlib, Neural Network is implemented by MultilayerPerceptronClassifier in org.apache.spark.ml package which is different from the way other ML algorithms are implemented (in org.apache.spark.mllib package). Therefore, it is a little bit tricky to provide a uniform service APIs for these algorithms. We provide a way to achieve this (see /services/Predictor.scala and /models/NeuralNetworkGen.scala).
- We tried to deploy our application on AWS but we met some configuration isses. Boxfuse cannot start the EC2 instance because a java.lang.ClassCastException (org.slf4j.impl.Log4jLoggerFactory cannot be cast to ch.qos.logback.classic.LoggerContext). And exporting war file  is not supported by play 2.x so we cannot upload the application to AWS as .war file.

-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_verbalization
-- ------------------------------------------------------
-- Server version	8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `verbal`
--

DROP TABLE IF EXISTS `verbal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verbal` (
  `id` int NOT NULL AUTO_INCREMENT,
  `verbalDate` datetime NOT NULL,
  `course` int NOT NULL,
  `dateOfExam` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `existsDate_idx` (`dateOfExam`),
  KEY `existsExam_idx` (`course`,`dateOfExam`),
  CONSTRAINT `existsCourse` FOREIGN KEY (`course`) REFERENCES `course` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `existsDate` FOREIGN KEY (`dateOfExam`) REFERENCES `exam` (`examDate`) ON UPDATE CASCADE,
  CONSTRAINT `existsExam` FOREIGN KEY (`course`, `dateOfExam`) REFERENCES `exam` (`courseId`, `examDate`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verbal`
--

LOCK TABLES `verbal` WRITE;
/*!40000 ALTER TABLE `verbal` DISABLE KEYS */;
INSERT INTO `verbal` VALUES (1,'2023-05-08 19:21:31',1,'2022-10-10'),(2,'2023-05-09 06:59:11',1,'2022-10-10'),(3,'2023-05-11 08:51:29',3,'2023-09-07'),(4,'2023-05-29 09:01:59',3,'2023-09-07'),(5,'2023-05-29 10:50:47',1,'2023-02-03'),(6,'2023-05-29 10:52:27',1,'2023-02-03'),(7,'2023-05-29 20:09:56',1,'2023-02-03'),(8,'2023-05-31 13:57:44',1,'2023-02-03');
/*!40000 ALTER TABLE `verbal` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-06-01 12:30:15

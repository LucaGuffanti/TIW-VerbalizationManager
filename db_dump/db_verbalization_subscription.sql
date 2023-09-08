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
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription` (
  `studentId` int NOT NULL,
  `courseId` int NOT NULL,
  `examDate` date NOT NULL,
  `grade` enum('ASSENTE','RIMANDATO','RIPROVATO','18','19','20','21','22','23','24','25','26','27','28','29','30','30 E LODE') DEFAULT NULL,
  `gradeStatus` enum('NON INSERITO','INSERITO','PUBBLICATO','RIFIUTATO','VERBALIZZATO') DEFAULT NULL,
  `wasRejected` tinyint DEFAULT NULL,
  `verbal` int DEFAULT NULL,
  PRIMARY KEY (`studentId`,`courseId`,`examDate`),
  KEY `existing exam_idx` (`courseId`,`examDate`),
  KEY `existing verbal_idx` (`verbal`),
  CONSTRAINT `existing exam` FOREIGN KEY (`courseId`, `examDate`) REFERENCES `exam` (`courseId`, `examDate`),
  CONSTRAINT `existing student` FOREIGN KEY (`studentId`) REFERENCES `user` (`userID`),
  CONSTRAINT `existing verbal` FOREIGN KEY (`verbal`) REFERENCES `verbal` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
INSERT INTO `subscription` VALUES (1,1,'2022-10-10','RIMANDATO','VERBALIZZATO',0,1),(1,1,'2023-02-03','25','VERBALIZZATO',0,5),(1,3,'2023-09-07','RIMANDATO','VERBALIZZATO',1,4),(1,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(3,1,'2022-10-10','25','VERBALIZZATO',0,2),(3,1,'2023-02-03','28','VERBALIZZATO',0,8),(3,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(4,1,'2022-10-10','24','VERBALIZZATO',0,2),(4,1,'2023-02-03','30 E LODE','VERBALIZZATO',0,8),(4,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(5,1,'2022-10-10','30 E LODE','VERBALIZZATO',0,1),(5,1,'2023-02-03','21','VERBALIZZATO',0,7),(5,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(6,1,'2023-02-03','30 E LODE','VERBALIZZATO',0,6),(6,3,'2023-09-07','30 E LODE','VERBALIZZATO',0,3),(6,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(7,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL),(9,6,'2023-07-08',NULL,'NON INSERITO',NULL,NULL);
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-06-01 12:30:16

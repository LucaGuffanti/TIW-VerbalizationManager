<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/homepage.css">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/global.css">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<title>Esito</title>
</head>
<body>
	<div class="container-fluid">
		<div>
			<a class="btn btn-outline-danger" href="Logout">Logout</a>
		</div>
		<div class="text-center">
			<h2>
				Appello di
				<c:out value="${courseName} del giorno ${examDate }">Not Found</c:out>
				- ESITO
			</h2>
			<div class="container-fluid">
				<div class="row justify-content-md-center">
					<c:if test='${!(status.equals("NON INSERITO") || status.equals("INSERITO"))}'>
						<div class="col col-lg">
							<label for="courseDataTable">Informazioni sul corso</label>
							<table id="courseDataTable"
								class="table table-bordered table-hover">
								<thead>
									<tr>
										<th>ID</th>
										<th>Nome</th>
										<th>Docente</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td><c:out value="${courseId}"></c:out></td>
										<td><c:out value="${courseName}"></c:out></td>
										<td><c:out value="${teacher.name} ${teacher.surname}"></c:out></td>
									</tr>
								</tbody>
							</table>
						</div>
						<div class="col col-lg">
							<label for="studentDataTable">Informazioni sullo studente</label>
							<table id="studentDataTable"
								class="table table-bordered table-hover">
								<thead>
									<tr>
										<th>Matricola</th>
										<th>Nome</th>
										<th>Cognome</th>
										<th>E-mail</th>
										<th>Corso di Laurea</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td><c:out value="${student.id}"></c:out></td>
										<td><c:out value="${student.name}"></c:out></td>
										<td><c:out value="${student.surname}"></c:out></td>
										<td><c:out value="${student.email}"></c:out></td>
										<td><c:out value="${student.degree}"></c:out></td>
									</tr>
								</tbody>
							</table>
						</div>
					</c:if>
					<c:choose>
						<c:when
							test='${status.equals("NON INSERITO") || status.equals("INSERITO")}'>
							<div class="alert alert-primary" role="alert">Voto non
								ancora definito</div>
						</c:when>
						<c:otherwise>
							<div class="w-25">
								<label for="gradeTable">Esito</label>
								<table id="gradeTable" class="table table-bordered table-hover">
									<tr>
										<th>Voto</th>
									</tr>
									<tr>
										<td><c:out value="${grade}"></c:out></td>
									</tr>
								</table>
							</div>
						</c:otherwise>
					</c:choose>
					<c:if test='${!status.equals("NON INSERITO")}'>
						<c:if test="${alreadyVerbalized.equals(true)}">
							<div class="alert alert-warning" role="alert">Non puoi
								rifiutare un voto già verbalizzato.</div>
						</c:if>
						<c:choose>
							<c:when
								test='${status.equals("PUBBLICATO") && !(
				grade.equals("ASSENTE")||grade.equals("RIMANDATO")||grade.equals("RIPROVATO"))}'>
								<form action="RejectResult" method="POST">
									<button type="submit">RIFIUTA IL VOTO</button>
									<input type="hidden" name="courseId" value="${courseId}">
									<input type="hidden" name="examDate" value="${examDate}">
								</form><br>
								<div class="alert alert-primary" role="alert">Il voto è
									stato pubblicato.</div>
							</c:when>
							<c:when test='${status.equals("RIFIUTATO")}'>
								<div class="alert alert-danger" role="alert">Il voto è
									stato rifiutato.</div>
							</c:when>
							<c:when test='${status.equals("VERBALIZZATO")}'>
								<div class="alert alert-success" role="alert">Il voto è
									stato verbalizzato.</div>
							</c:when>
							<c:when test='${status.equals("PUBBLICATO")}'>
								<div class="alert alert-primary" role="alert">Il voto è
									stato pubblicato.</div>
							</c:when>
						</c:choose>
					</c:if>
				</div>
			</div>
		</div>
		<div>
			<a href="GoToStudentHome?chosenCourse=${courseId}">Alla pagina
				precedente</a>
		</div>
	</div>
</body>
</html>
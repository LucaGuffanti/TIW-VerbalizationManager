<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/homepage.css">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/global.css">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<title>Home Page - Studente</title>
</head>
<body>
	<div class="container-fluid">
		<div>
			<a class="btn btn-outline-danger" href="Logout">Logout</a>
		</div>
		<div class="text-center">
			<h2>
				Bentornato,
				<c:out
					value="${sessionScope.user.name} ${sessionScope.user.surname}">Not found</c:out>
			</h2>
			<c:choose>
				<c:when test="${courses.size()>0}">
					<p>Ecco una lista di corsi di cui ti sei iscritto agli appelli.
						Seleziona un corso per vedere gli appelli a cui sei iscritto.</p>
					<div class="container-fluid">
						<div class="row justify-content-md-center">
							<div class="col col-lg">
								<table class="table table-bordered table-hover">
									<thead class="thead-dark">
										<tr>
											<th>ID</th>
											<th>Nome</th>
											<th>Professore</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="course" items="${courses}">
											<tr>
												<c:if test="${course.id==chosenCourse.id}">
													<td class="selectedinTable"><c:out
															value="${course.id}"></c:out></td>
													<td class="selectedinTable"><a
														href="GoToStudentHome?chosenCourse=${course.id}"><c:out
																value="${course.name}"></c:out></a></td>
													<td class="selectedinTable"><c:out
															value="${course.teacherName} ${course.teacherSurname}"></c:out></td>

												</c:if>
												<c:if test="${course.id!=chosenCourse.id}">
													<td><c:out value="${course.id}"></c:out></td>
													<td><a
														href="GoToStudentHome?chosenCourse=${course.id}"><c:out
																value="${course.name}"></c:out></a></td>
													<td><c:out
															value="${course.teacherName} ${course.teacherSurname}"></c:out></td>
												</c:if>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
						<div class="col col-lg">
							<table class="table table-bordered table-hover">
								<thead>
									<tr>
										<th><c:out value="Appelli di ${chosenCourse.name}"></c:out></th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="exam" items="${exams}">
										<tr>
											<td><a
												href="GoToExamResults?examDate=${exam.date}&courseId=${chosenCourse.id}"><c:out
														value="${exam.date}"></c:out></a></td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div class="alert alert-warning" role="alert">Sembra che non
						ti sia iscritto a nessun corso.</div>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</body>
</html>
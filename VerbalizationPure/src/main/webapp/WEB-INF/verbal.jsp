<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<title>Verbale</title>
</head>
<body>
	<div class="container-fluid">
		<div>
			<a class="btn btn-outline-danger" href="Logout">Logout</a>
		</div>
		<div class="text-center">

			<c:choose>
				<c:when test="${verbalized==1}">
					<h3>
						Verbale dell'esame di
						<c:out value="${verbal.courseName}">NULL</c:out>
						del
						<c:out value="${verbal.examDate}">NULL</c:out>
					</h3>
					<div class="container-fluid">
						<div class="row justify-content-md-center">
							<h4>Dati del verbale</h4>
							<div class="col col-md">
								<table class="table table-bordered table-hover">
									<thead>
										<tr>
											<th>ID Verbale</th>
											<th>Data e ora di creazione</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><c:out value="${verbal.id}">NULL</c:out></td>
											<td><c:out value="${verbal.date}">NULL</c:out></td>
										</tr>
									</tbody>
								</table>
							</div>
							<h4>Risultati Verbalizzati</h4>
							<div class="col col-md">
								<table class="table table-bordered table-hover">
									<thead>
										<tr>
											<th>Matricola studente</th>
											<th>Nome</th>
											<th>Cognome</th>
											<th>Voto</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="sub" items="${subs}">
											<tr>
												<td><c:out value="${sub.id}">NOT FOUND</c:out></td>
												<td><c:out value="${sub.name}">NOT FOUND</c:out></td>
												<td><c:out value="${sub.surname}">NOT FOUND</c:out></td>
												<td><c:out value="${sub.grade}">NOT FOUND</c:out></td>
											</tr>
										</c:forEach>
								</table>
								</tbody>
							</div>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div class="alert alert-warning" role="alert">
						<h2>Nessun voto è stato verbalizzato.</h2>
					</div>
				</c:otherwise>
			</c:choose>
		</div>
		<div>
			<a
				href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}">Alla
				pagina precedente</a>
		</div>
	</div>
</body>
</html>
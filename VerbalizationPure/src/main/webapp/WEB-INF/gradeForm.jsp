<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/global.css">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<title>INSERIMENTO DEL VOTO</title>
</head>
<body>
	<div class="container-fluid">
		<div>
			<a class="btn btn-outline-danger" href="Logout">Logout</a>
		</div>
		<div class="text-center">
			<h2>
				Inserisci il voto di
				<c:out
					value="${user.name} ${user.surname}   [Matricola ${user.id}] "></c:out>
			</h2>
			<div class="container-fluid">
				<div class="row justify-content-md-center">
					<div class="col col-lg">
						<table class="table table-bordered table-hover">
							<tr>
								<th>INFORMAZIONI STUDENTE
								<th>
							</tr>
							<tr>
								<td><b>Matricola</b></td>
								<td><c:out value="${user.id}"></c:out></td>
							</tr>
							<tr>
								<td><b>Nome</b></td>
								<td><c:out value="${user.name}"></c:out></td>
							</tr>
							<tr>
								<td><b>Cognome</b></td>
								<td><c:out value="${user.surname}"></c:out></td>
							</tr>
							<tr>
								<td><b>E-mail</b></td>
								<td><c:out value="${user.email}"></c:out></td>
							</tr>
							<tr>
								<td><b>Corso di Laurea</b></td>
								<td><c:out value="${user.degree}"></c:out></td>
							</tr>
							<c:if test="${!user.gradeStatus.equals('NON INSERITO')}">
								<tr>
									<td><b>Voto precedente</b></td>
									<td><c:out value="${user.grade}"></c:out></td>
								</tr>
							</c:if>
						</table>
						<div class="w-25 p-3">
						<form method="post"
							action="UpdateGrade?examDate=${examDate}&courseId=${courseId}&studentId=${user.id}">
							<label for="grade"><b>Inserisci il voto</b></label> <select
								class="form-select form-select-sm" name="grade" required>
								<option selected>Seleziona</option>
								<option value="assente">ASSENTE</option>
								<option value="rimandato">RIMANDATO</option>
								<option value="riprovato">RIPROVATO</option>
								<option value="18">18</option>
								<option value="19">19</option>
								<option value="20">20</option>
								<option value="21">21</option>
								<option value="22">22</option>
								<option value="23">23</option>
								<option value="24">24</option>
								<option value="25">25</option>
								<option value="26">26</option>
								<option value="27">27</option>
								<option value="28">28</option>
								<option value="29">29</option>
								<option value="30">30</option>
								<option value="lode">30 E LODE</option>
							</select>
							<button type="submit" class="btn btn-outline-primary">Salva</button>
						</form>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="backToPrevious">
			<a
				href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}">Alla
				pagina precedente</a>
		</div>
	</div>
</body>
</html>
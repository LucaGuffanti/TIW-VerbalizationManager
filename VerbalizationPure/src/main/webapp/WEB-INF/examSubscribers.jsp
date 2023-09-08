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
<title>Iscritti</title>
</head>
<body>
	<div class="container-fluid">
		<div>
			<a class="btn btn-outline-danger" href="Logout">Logout</a>
		</div>
		<div class="text-center">
			<h2>
				Iscritti all'esame di <b><c:out
						value="${courseName} del giorno ${examDate}">NOT FOUND</c:out></b>
			</h2>
			<c:choose>
				<c:when test="${subs.size()>0}">
					<div class="container-fluid">
						<div class="row justify-content-md-center">
							<div class="col col-lg">
								<table class="table table-bordered table-striped table-hover">
									<thead>
										<tr>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=id&ordering=${idOrd}&emailOrd=${emailOrd}">Matricola</a>
													</th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=name&ordering=${nameOrd}&emailOrd=${emailOrd}">Nome</a>
													</th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=surname&ordering=${surnameOrd}&emailOrd=${emailOrd}">Cognome</a>
													</th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=e-mail&ordering=${emailOrd}&emailOrd=${emailOrd}">E-mail</a>
													</th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=degree&ordering=${degreeOrd}&emailOrd=${emailOrd}">Corso
															di Laurea</a></th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=grade&ordering=${gradeOrd}&emailOrd=${emailOrd}">Voto</a>
													</th>
													<th><a
														href="GoToExamSubscribers?examDate=${examDate}&courseId=${courseId}&idOrd=${idOrd}&nameOrd=${nameOrd}&surnameOrd=${surnameOrd}&degreeOrd=${degreeOrd}&gradeOrd=${gradeOrd}&statusOrd=${statusOrd}&orderParameter=status&ordering=${statusOrd}&emailOrd=${emailOrd}">Stato
															della Valutazione</a></th>
													<th>Azioni disponibili</th>
												</tr>
											</thead>
									<tbody>
										<c:forEach var="sub" items="${subs}">
											<tr>
												<td><c:out value="${sub.id}"></c:out></td>
												<td><c:out value="${sub.name}"></c:out></td>
												<td><c:out value="${sub.surname}"></c:out></td>
												<td><c:out value="${sub.email}"></c:out></td>
												<td><c:out value="${sub.degree}"></c:out></td>
												<td><c:if
														test="${sub.grade != null && sub.grade.length()>0}">
														<c:out value="${sub.grade}"></c:out>
													</c:if></td>
												<td><c:out value="${sub.gradeStatus}"></c:out></td>
												<td>

													<form action="GoToGradeModifyForm" method="POST">
														<c:choose>
															<c:when test='${sub.gradeStatus.equals("NON INSERITO")}'>
																<button type="submit" class="btn btn-outline-dark">INSERISCI
																	IL VOTO</button>
															</c:when>
															<c:when test='${sub.gradeStatus.equals("INSERITO")}'>
																<button type="submit" class="btn btn-outline-dark">MODIFICA
																	IL VOTO</button>
															</c:when>
															<c:otherwise>
												Nessuna azione disponibile
											</c:otherwise>
														</c:choose>
														<input type="hidden" name="examDate" value="${examDate}" />
														<input type="hidden" name="courseId" value="${courseId}" />
														<input type="hidden" name="studentId" value="${sub.id}" />
													</form>
												</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col">
							<form action="PublishResults" method="POST">
								<button type="submit" class="btn btn-outline-primary">
									Pubblica i risultati</button>
								<input type="hidden" name="examDate" value="${examDate}">
								<input type="hidden" name="courseId" value="${courseId}">
							</form>
						</div>
						<div class="col">
							<form action="VerbalizeResults" method="POST">
								<button type="submit" class="btn btn-outline-success">Verbalizza
									i risultati</button>
								<input type="hidden" name="examDate" value="${examDate}">
								<input type="hidden" name="courseId" value="${courseId}">
							</form>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div class="alert alert-warning" role="alert">
						<p>Sembra che nessuno si sia iscitto a questo esame!</p>
					</div>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="backToPrevious">
			<a href="GoToTeacherHome?chosenCourse=${courseId}">Alla pagina
				precedente</a>
		</div>
	</div>
</body>
</html>
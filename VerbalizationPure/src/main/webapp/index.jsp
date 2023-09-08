<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css" rel="stylesheet">
<title>Verbalizzazione di esami</title>
</head>
<body>
	<div class="container">
		<div class="text-center">
			<h2>Per accedere inserisci le tue credenziali</h2>
			<c:url value="/CheckLogin" var="loginUrl" />
			<form method="post" action="${loginUrl}">
				<div class="form-group">
					<label for="id">Inserisci il tuo numero di matricola</label> <input
						id="id" type="number" name="id" placeholder="Matricola" required min="1">
					<br>
				</div>
				<div class="form-group">
					<label for="password">Inserisci la tua password</label> <input
						id="password" type="password" placeholder="Password" name="password" required /><br>
				</div>
				<button type="submit" class="btn btn-outline-primary">Accedi</button>
			</form>
		</div>
	</div>
</body>
</html>